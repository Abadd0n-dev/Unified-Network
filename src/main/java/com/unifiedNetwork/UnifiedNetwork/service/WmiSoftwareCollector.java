package com.unifiedNetwork.UnifiedNetwork.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifiedNetwork.UnifiedNetwork.model.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WmiSoftwareCollector {

    public static List<Map<String, String>> getInstalledSoftware() {
        List<Map<String, String>> softwareList = new ArrayList<>();
        try {
            String command =
                    "Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | " +
                            "Select-Object DisplayName, DisplayVersion, Publisher, InstallDate | " +
                            "Where-Object { $_.DisplayName -ne $null } | " +
                            "ConvertTo-Json";
            Map<String, String> result = PowerShellExecutor.executePowerShellCommand(command);
            if (result.containsKey("output")) {
                String jsonOutput = result.get("output");
                System.out.println("JSON Output: " + jsonOutput);
                ObjectMapper objectMapper = new ObjectMapper();
                softwareList = objectMapper.readValue(jsonOutput, List.class);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при сборе данных о ПО: " + e.getMessage());
        }
        return softwareList;
    }

    public static List<Map<String, String>> getInstalledSoftwareForDevice(Device device) {
        List<Map<String, String>> softwareList = new ArrayList<>();

        if (isLocalDevice(device)) {
            return getInstalledSoftware();
        } else {
            try {
                // Используем PowerShell для удаленного выполнения команд через WMI
                String command = String.format(
                        "$cred = New-Object System.Management.Automation.PSCredential('Administrator', (ConvertTo-SecureString 'password' -AsPlainText -Force)); " +
                                "$session = New-PSSession -ComputerName %s -Credential $cred; " +
                                "$software = Invoke-Command -Session $session -ScriptBlock { " +
                                "Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | " +
                                "Select-Object DisplayName, DisplayVersion, Publisher, InstallDate | " +
                                "Where-Object { $_.DisplayName -ne $null } | " +
                                "ConvertTo-Json }; " +
                                "Remove-PSSession $session; " +
                                "$software",
                        device.getIp()
                );

                Map<String, String> result = PowerShellExecutor.executePowerShellCommand(command);
                if (result.containsKey("output")) {
                    String jsonOutput = result.get("output");
                    System.out.println("JSON Output for device " + device.getIp() + ": " + jsonOutput);
                    ObjectMapper objectMapper = new ObjectMapper();
                    softwareList = objectMapper.readValue(jsonOutput, List.class);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при сборе данных о ПО для устройства " + device.getIp() + ": " + e.getMessage());
            }
        }
        return softwareList;
    }

    private static boolean isLocalDevice(Device device) {
        // Логика для проверки, является ли устройство локальным
        // Например, сравнение IP или MAC адресов
        try {
            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            return localIp.equals(device.getIp());
        } catch (Exception e) {
            System.err.println("Ошибка при определении локального устройства: " + e.getMessage());
            return false;
        }
    }
}