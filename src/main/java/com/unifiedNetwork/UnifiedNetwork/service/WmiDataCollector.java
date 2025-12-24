package com.unifiedNetwork.UnifiedNetwork.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;

public class WmiDataCollector {

    public static Map<String, String> getDeviceMetrics(String targetIp, String targetMac) {
        Map<String, String> metrics = new HashMap<>();

        boolean isReachable = isDeviceReachable(targetIp);
        if (!isReachable) {
            metrics.put("error", "Устройство с указанным IP недоступно.");
            metrics.put("status", "Неактивно");
            return metrics;
        }

        try {
            boolean isLocal = isCurrentDevice(targetIp, targetMac);

            if (isLocal) {
                metrics = getLocalDeviceMetrics();
            } else {
                metrics = getRemoteDeviceMetrics(targetIp);
            }
        } catch (Exception e) {
            metrics.put("error", "Ошибка при сборе метрик: " + e.getMessage());
            metrics.put("status", "Неактивно");
        }

        return metrics;
    }

    private static Map<String, String> getLocalDeviceMetrics() {
        Map<String, String> metrics = new HashMap<>();

        try {
            // Получаем загрузку процессора
            Map<String, String> cpuResult = PowerShellExecutor.executePowerShellCommand(
                    "$cpuLoad = (Get-WmiObject Win32_Processor).LoadPercentage; Write-Output $cpuLoad"
            );
            metrics.put("cpuUsage", cpuResult.getOrDefault("output", "0"));

            // Получаем использование оперативной памяти
            Map<String, String> memoryResult = PowerShellExecutor.executePowerShellCommand(
                    "$os = Get-WmiObject Win32_OperatingSystem; " +
                            "$freeMemory = $os.FreePhysicalMemory; " +
                            "$totalMemory = $os.TotalVisibleMemorySize; " +
                            "$usedMemoryPercent = 100 - ($freeMemory / $totalMemory * 100); " +
                            "Write-Output $usedMemoryPercent"
            );
            metrics.put("memoryUsage", memoryResult.getOrDefault("output", "0"));

            // Получаем использование дискового пространства
            Map<String, String> diskResult = PowerShellExecutor.executePowerShellCommand(
                    "$disk = Get-WmiObject Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'}; " +
                            "$freeSpace = $disk.FreeSpace; " +
                            "$totalSpace = $disk.Size; " +
                            "$usedSpacePercent = 100 - ($freeSpace / $totalSpace * 100); " +
                            "Write-Output $usedSpacePercent"
            );
            metrics.put("diskUsage", diskResult.getOrDefault("output", "0"));

            // Получаем температуру процессора через WMI
            Map<String, String> temperatureResult = PowerShellExecutor.executePowerShellCommand(
                    "$ErrorActionPreference = 'Stop'; " +
                            "try { " +
                            "   $temperature = Get-WmiObject MSAcpi_ThermalZoneTemperature -Namespace \"root\\\\wmi\" -ErrorAction Stop; " +
                            "   if ($temperature -ne $null) { " +
                            "       $currentTempTenths = $temperature.CurrentTemperature; " +
                            "       if ($currentTempTenths -gt 1000) { " +
                            "           $currentTempCelsius = ($currentTempTenths / 10) - 273.15; " +
                            "       } else { " +
                            "           $currentTempCelsius = $currentTempTenths / 10; " +
                            "       } " +
                            "       Write-Output $currentTempCelsius; " +
                            "   } else { Write-Output '0' } " +
                            "} catch { " +
                            "   try { " +
                            "       $temperature = Get-WmiObject -Query 'SELECT * FROM Win32_PerfFormattedData_Counters_ThermalZoneInformation' -ErrorAction Stop; " +
                            "       if ($temperature -ne $null) { " +
                            "           $temp = $temperature.Temperature; " +
                            "           if ($temp -gt 1000) { " +
                            "               $tempCelsius = ($temp / 10) - 273.15; " +
                            "           } else { " +
                            "               $tempCelsius = $temp / 10; " +
                            "           } " +
                            "           Write-Output $tempCelsius; " +
                            "       } else { Write-Output '0' } " +
                            "   } catch { Write-Output '0' } " +
                            "}"
            );
            metrics.put("temperature", temperatureResult.getOrDefault("output", "0"));

            // Получаем сетевую активность
            Map<String, String> networkActivityResult = PowerShellExecutor.executePowerShellCommand(
                    "$ErrorActionPreference = 'SilentlyContinue'; " +
                            "$network = Get-NetAdapterStatistics -Name '*' -ErrorAction SilentlyContinue | Where-Object { $_.ReceivedUnicastBytes -gt 0 -or $_.SentUnicastBytes -gt 0 }; " +
                            "if ($network -ne $null) { $sent = $network.SentUnicastBytes; $received = $network.ReceivedUnicastBytes; $activity = ($sent + $received) / 1MB; Write-Output $activity } else { Write-Output '0' }"
            );
            metrics.put("networkActivity", networkActivityResult.getOrDefault("output", "0"));

            // Получаем входящий и исходящий трафик
            Map<String, String> incomingTrafficResult = PowerShellExecutor.executePowerShellCommand(
                    "$ErrorActionPreference = 'SilentlyContinue'; " +
                            "$network = Get-NetAdapterStatistics -Name '*' -ErrorAction SilentlyContinue | Where-Object { $_.ReceivedUnicastBytes -gt 0 -or $_.SentUnicastBytes -gt 0 }; " +
                            "if ($network -ne $null) { $received = $network.ReceivedUnicastBytes / 1MB; Write-Output $received } else { Write-Output 'Неизвестно' }"
            );
            metrics.put("incomingTraffic", incomingTrafficResult.getOrDefault("output", "Неизвестно"));

            Map<String, String> outgoingTrafficResult = PowerShellExecutor.executePowerShellCommand(
                    "$ErrorActionPreference = 'SilentlyContinue'; " +
                            "$network = Get-NetAdapterStatistics -Name '*' -ErrorAction SilentlyContinue | Where-Object { $_.ReceivedUnicastBytes -gt 0 -or $_.SentUnicastBytes -gt 0 }; " +
                            "if ($network -ne $null) { $sent = $network.SentUnicastBytes / 1MB; Write-Output $sent } else { Write-Output 'Неизвестно' }"
            );
            metrics.put("outgoingTraffic", outgoingTrafficResult.getOrDefault("output", "Неизвестно"));

            // Получаем информацию о процессоре и модели устройства
            Map<String, String> processorResult = PowerShellExecutor.executePowerShellCommand(
                    "$processor = Get-WmiObject Win32_Processor; " +
                            "Write-Output $processor.Name"
            );
            metrics.put("processor", processorResult.getOrDefault("output", "Неизвестно"));

            Map<String, String> modelResult = PowerShellExecutor.executePowerShellCommand(
                    "$computerSystem = Get-WmiObject Win32_ComputerSystem; " +
                            "Write-Output $computerSystem.Model"
            );
            metrics.put("model", modelResult.getOrDefault("output", "Неизвестно"));

            // Получаем информацию об оперативной памяти
            Map<String, String> ramResult = PowerShellExecutor.executePowerShellCommand(
                    "$ram = Get-WmiObject Win32_PhysicalMemory | Measure-Object -Property Capacity -Sum; " +
                            "$totalRamGB = [math]::Round($ram.Sum / 1GB, 2); " +
                            "Write-Output $totalRamGB"
            );
            metrics.put("ram", ramResult.getOrDefault("output", "Неизвестно"));

            // Получаем информацию о жестком диске
            Map<String, String> storageResult = PowerShellExecutor.executePowerShellCommand(
                    "$disk = Get-WmiObject Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'}; " +
                            "$totalSizeGB = [math]::Round($disk.Size / 1GB, 2); " +
                            "Write-Output $totalSizeGB"
            );
            metrics.put("storage", storageResult.getOrDefault("output", "Неизвестно"));

            // Получаем информацию о времени безотказной работы
            Map<String, String> uptimeResult = PowerShellExecutor.executePowerShellCommand(
                    "$os = Get-WmiObject Win32_OperatingSystem; " +
                            "$lastBootUpTime = $os.ConvertToDateTime($os.LastBootUpTime); " +
                            "$currentTime = Get-Date; " +
                            "$uptime = New-TimeSpan -Start $lastBootUpTime -End $currentTime; " +
                            "$uptimeTotalSeconds = [math]::Round($uptime.TotalSeconds); " +
                            "Write-Output $uptimeTotalSeconds"
            );
            metrics.put("uptime", uptimeResult.getOrDefault("output", "0"));

            // Получаем информацию о последней активности (время последней загрузки системы)
            Map<String, String> lastActivityResult = PowerShellExecutor.executePowerShellCommand(
                    "$os = Get-WmiObject Win32_OperatingSystem; " +
                            "$lastBootUpTime = $os.ConvertToDateTime($os.LastBootUpTime); " +
                            "Write-Output $lastBootUpTime.ToString('yyyy-MM-dd HH:mm:ss')"
            );
            metrics.put("lastActivity", lastActivityResult.getOrDefault("output", "Неизвестно"));

            // Получаем информацию об источнике питания
            Map<String, String> powerResult = PowerShellExecutor.executePowerShellCommand(
                    "$battery = Get-CimInstance -ClassName Win32_Battery -ErrorAction SilentlyContinue; " +
                            "$powerSource = 'Power Adapter'; " +
                            "if ($battery) { " +
                            "   if ($battery.BatteryStatus -eq 2) { $powerSource = 'Battery' } " +
                            "}; " +
                            "Write-Output $powerSource"
            );
            metrics.put("power", powerResult.getOrDefault("output", "Power Adapter"));

            // Статус устройства
            metrics.put("status", "Активно");

        } catch (Exception e) {
            metrics.put("error", "Ошибка при сборе метрик: " + e.getMessage());
            metrics.put("status", "Неактивно");
        }

        return metrics;
    }

    private static Map<String, String> getRemoteDeviceMetrics(String targetIp) {
        Map<String, String> metrics = new HashMap<>();

        try {
            String username = "Administrator"; // Учетная запись администратора на удаленном устройстве
            String password = "yourPassword";   // Пароль администратора на удаленном устройстве

            // Используем PowerShell для удаленного выполнения команд через WMI
            String command = String.format(
                    "$cred = New-Object System.Management.Automation.PSCredential('%s', (ConvertTo-SecureString '%s' -AsPlainText -Force)); " +
                            "$session = New-PSSession -ComputerName %s -Credential $cred -ErrorAction Stop; " +
                            "$cpuLoad = Invoke-Command -Session $session -ScriptBlock { (Get-WmiObject Win32_Processor).LoadPercentage }; " +
                            "$os = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject Win32_OperatingSystem }; " +
                            "$freeMemory = $os.FreePhysicalMemory; " +
                            "$totalMemory = $os.TotalVisibleMemorySize; " +
                            "$usedMemoryPercent = 100 - ($freeMemory / $totalMemory * 100); " +
                            "$disk = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'} }; " +
                            "$freeSpace = $disk.FreeSpace; " +
                            "$totalSpace = $disk.Size; " +
                            "$usedSpacePercentDisk = 100 - ($freeSpace / $totalSpace * 100); " +
                            "$temperature = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject MSAcpi_ThermalZoneTemperature -Namespace \"root\\\\wmi\" -ErrorAction SilentlyContinue }; " +
                            "if ($temperature -ne $null) { " +
                            "$currentTempTenths = $temperature.CurrentTemperature; " +
                            "if ($currentTempTenths -gt 1000) { " +
                            "$currentTempCelsius = ($currentTempTenths / 10) - 273.15; " +
                            "} else { " +
                            "$currentTempCelsius = $currentTempTenths / 10; " +
                            "} " +
                            "} else { $currentTempCelsius = 0 } " +
                            "$network = Invoke-Command -Session $session -ScriptBlock { Get-NetAdapterStatistics -Name '*' -ErrorAction SilentlyContinue | Where-Object { $_.ReceivedUnicastBytes -gt 0 -or $_.SentUnicastBytes -gt 0 } }; " +
                            "if ($network -ne $null) { $sent = $network.SentUnicastBytes; $received = $network.ReceivedUnicastBytes; $activity = ($sent + $received) / 1MB; } else { $activity = 0 } " +
                            "$incomingTraffic = if ($network -ne $null) { $network.ReceivedUnicastBytes / 1MB } else { 'Неизвестно' } " +
                            "$outgoingTraffic = if ($network -ne $null) { $network.SentUnicastBytes / 1MB } else { 'Неизвестно' } " +
                            "$processor = Invoke-Command -Session $session -ScriptBlock { (Get-WmiObject Win32_Processor).Name }; " +
                            "$model = Invoke-Command -Session $session -ScriptBlock { (Get-WmiObject Win32_ComputerSystem).Model }; " +
                            "$ram = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject Win32_PhysicalMemory | Measure-Object -Property Capacity -Sum }; " +
                            "$totalRamGB = [math]::Round($ram.Sum / 1GB, 2); " +
                            "$storage = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject Win32_LogicalDisk | Where-Object {$_.DeviceID -eq 'C:'} }; " +
                            "$totalSizeGB = [math]::Round($storage.Size / 1GB, 2); " +
                            "$os = Invoke-Command -Session $session -ScriptBlock { Get-WmiObject Win32_OperatingSystem }; " +
                            "$lastBootUpTime = $os.ConvertToDateTime($os.LastBootUpTime); " +
                            "$currentTime = Get-Date; " +
                            "$uptime = New-TimeSpan -Start $lastBootUpTime -End $currentTime; " +
                            "$uptimeTotalSeconds = [math]::Round($uptime.TotalSeconds); " +
                            "$battery = Invoke-Command -Session $session -ScriptBlock { Get-CimInstance -ClassName Win32_Battery -ErrorAction SilentlyContinue }; " +
                            "$powerSource = 'Power Adapter'; " +
                            "if ($battery) { " +
                            "   if ($battery.BatteryStatus -eq 2) { $powerSource = 'Battery' } " +
                            "}; " +
                            "Remove-PSSession $session; " +
                            "Write-Output ('CPU:' + $cpuLoad + '|MEMORY:' + $usedMemoryPercent + '|DISK:' + $usedSpacePercentDisk + '|TEMP:' + $currentTempCelsius + '|NETWORK:' + $activity + '|INCOMING:' + $incomingTraffic + '|OUTGOING:' + $outgoingTraffic + '|PROCESSOR:' + $processor + '|MODEL:' + $model + '|RAM:' + $totalRamGB + '|STORAGE:' + $totalSizeGB + '|LASTACTIVITY:' + $lastBootUpTime + '|UPTIME:' + $uptimeTotalSeconds + '|POWER:' + $powerSource)",
                    username, password, targetIp
            );

            Map<String, String> result = PowerShellExecutor.executePowerShellCommand(command);
            if (result.containsKey("output")) {
                String output = result.get("output");
                String[] parts = output.split("\\|");
                for (String part : parts) {
                    String[] keyValue = part.split(":");
                    if (keyValue.length == 2) {
                        metrics.put(keyValue[0].toLowerCase(), keyValue[1]);
                    }
                }
                metrics.put("status", "Активно");
            }
        } catch (Exception e) {
            metrics.put("error", "Ошибка при сборе метрик с удаленного устройства: " + e.getMessage());
            metrics.put("status", "Неактивно");
        }

        return metrics;
    }

    // Проверка доступности устройства по IP
    private static boolean isDeviceReachable(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(5000); // Таймаут 5 секунд
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCurrentDevice(String targetIp, String targetMac) {
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            String localMac = getLocalMacAddress();
            return localIp.equals(targetIp) || targetMac.equals(localMac);
        } catch (Exception e) {
            return false;
        }
    }

    private static String getLocalMacAddress() {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "00:00:00:00:00:00";
        }
    }
}
