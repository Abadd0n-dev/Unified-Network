package com.unifiedNetwork.UnifiedNetwork.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PowerShellExecutor {

    public static Map<String, String> executePowerShellCommand(String command) {
        Map<String, String> result = new HashMap<>();
        ProcessBuilder processBuilder = new ProcessBuilder("powershell.exe", "-Command", command);
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                result.put("output", output.toString().trim());
            } else {
                result.put("error", "Ошибка выполнения PowerShell команды: " + output);
            }
        } catch (IOException | InterruptedException e) {
            result.put("error", "Ошибка выполнения PowerShell команды: " + e.getMessage());
        }
        return result;
    }

}
