package com.unifiedNetwork.UnifiedNetwork.controller;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.model.DeviceMetrics;
import com.unifiedNetwork.UnifiedNetwork.model.Software;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceMetricsRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.SoftwareRepository;
import com.unifiedNetwork.UnifiedNetwork.service.DeviceService;
import com.unifiedNetwork.UnifiedNetwork.service.SoftwareService;
import com.unifiedNetwork.UnifiedNetwork.service.WmiDataCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceMetricsRepository deviceMetricsRepository;

    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private SoftwareService softwareService;

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices(@RequestAttribute("userId") Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addDevice(@RequestBody Device device, @RequestAttribute("userId") Long userId) {
        try {
            System.out.println("Попытка добавить устройство: " + device.getIp() + ", MAC: " + device.getMacAddress());
            if (!isValidIp(device.getIp())) {
                System.err.println("Некорректный формат IP-адреса: " + device.getIp());
                return ResponseEntity.status(400).body(Map.of("error", "Некорректный формат IP-адреса"));
            }
            if (!isValidMac(device.getMacAddress())) {
                System.err.println("Некорректный формат MAC-адреса: " + device.getMacAddress());
                return ResponseEntity.status(400).body(Map.of("error", "Некорректный формат MAC-адреса"));
            }
            Device existingDevice = deviceRepository.findByIp(device.getIp());
            if (existingDevice != null) {
                System.err.println("Устройство с IP " + device.getIp() + " уже существует");
                return ResponseEntity.status(400).body(Map.of("error", "Устройство с IP " + device.getIp() + " уже существует"));
            }
            boolean isReachable = isDeviceReachable(device.getIp());
            device.setStatus(isReachable ? "Активно" : "Неактивно");
            device.setUserId(userId);
            Device savedDevice = deviceRepository.save(device);
            System.out.println("Устройство успешно добавлено: " + savedDevice.getIp());
            return ResponseEntity.ok(savedDevice);
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении устройства: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при добавлении устройства: " + e.getMessage()));
        }
    }

    @PostMapping("/metrics")
    public ResponseEntity<Map<String, String>> receiveDeviceMetrics(@RequestBody Map<String, String> metrics) {
        try {
            String ip = metrics.get("ip");
            Device device = deviceRepository.findByIp(ip);
            if (device == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Устройство с IP " + ip + " не найдено"));
            }

            // Сохраняем метрики устройства
            DeviceMetrics deviceMetrics = new DeviceMetrics();
            deviceMetrics.setDevice(device);
            deviceMetrics.setTimestamp(LocalDateTime.now());
            deviceMetrics.setCpuUsage(Double.parseDouble(metrics.getOrDefault("cpuUsage", "0")));
            deviceMetrics.setMemoryUsage(Double.parseDouble(metrics.getOrDefault("memoryUsage", "0")));
            deviceMetrics.setDiskUsage(Double.parseDouble(metrics.getOrDefault("diskUsage", "0")));
            deviceMetrics.setTemperature(Double.parseDouble(metrics.getOrDefault("temperature", "0")));
            deviceMetrics.setNetworkActivity(Double.parseDouble(metrics.getOrDefault("networkActivity", "0")));
            deviceMetricsRepository.save(deviceMetrics);

            // Обновляем информацию об устройстве
            device.setStatus(metrics.getOrDefault("status", "Неизвестно"));
            device.setModel(metrics.getOrDefault("model", "Неизвестно"));
            device.setLastActivity(metrics.getOrDefault("lastActivity", "Неизвестно"));
            device.setUptime(Long.parseLong(metrics.getOrDefault("uptime", "0")));
            device.setProcessor(metrics.getOrDefault("processor", "Неизвестно"));
            device.setRam(metrics.getOrDefault("ram", "Неизвестно"));
            device.setStorage(metrics.getOrDefault("storage", "Неизвестно"));
            device.setPower(metrics.getOrDefault("power", "Неизвестно"));
            deviceRepository.save(device);

            // Обрабатываем данные о ПО, если они есть
            if (metrics.containsKey("software")) {
                String softwareJson = metrics.get("software");
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, String>> softwareList = objectMapper.readValue(softwareJson, List.class);

                // Удаляем старое ПО для устройства
                List<Software> oldSoftware = softwareRepository.findByDevice(device);
                softwareRepository.deleteAll(oldSoftware);

                // Сохраняем новое ПО
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                for (Map<String, String> softwareInfo : softwareList) {
                    Software software = new Software();
                    software.setName(softwareInfo.getOrDefault("DisplayName", "Неизвестно"));
                    software.setVersion(softwareInfo.getOrDefault("DisplayVersion", "Неизвестно"));
                    software.setPublisher(softwareInfo.getOrDefault("Publisher", "Неизвестно"));
                    String installDateStr = softwareInfo.get("InstallDate");
                    if (installDateStr != null && !installDateStr.isEmpty()) {
                        try {
                            software.setInstallationDate(LocalDate.parse(installDateStr, formatter));
                        } catch (Exception ex) {
                            software.setInstallationDate(null);
                        }
                    } else {
                        software.setInstallationDate(null);
                    }
                    software.setDevice(device);
                    softwareRepository.save(software);
                }
            }

            return ResponseEntity.ok(Map.of("success", "Метрики и данные о ПО успешно сохранены"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при обработке метрик: " + e.getMessage()));
        }
    }

    private boolean isValidIp(String ip) {
        return ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }

    private boolean isValidMac(String mac) {
        return mac != null && mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    private boolean isDeviceReachable(String ip) {
        try {
            Process process = Runtime.getRuntime().exec("ping -n 1 -w 2000 " + ip);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Ошибка при проверке доступности устройства: " + e.getMessage());
            return false;
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteDevice(@RequestParam String ip, @RequestAttribute("userId") Long userId) {
        try {
            Device device = deviceRepository.findByIp(ip);
            if (device == null || !device.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "У вас нет прав для удаления этого устройства"));
            }
            deviceService.deleteDeviceByIp(ip);
            return ResponseEntity.ok(Map.of("success", "Устройство успешно удалено"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при удалении устройства: " + e.getMessage()));
        }
    }

    @GetMapping("/{ip}/metrics")
    public ResponseEntity<Map<String, Object>> getDeviceMetrics(@PathVariable String ip, @RequestAttribute("userId") Long userId) {
        Device device = deviceRepository.findByIp(ip);
        if (device == null || !device.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "У вас нет прав для просмотра этого устройства"));
        }
        try {
            Map<String, String> wmiMetrics = WmiDataCollector.getDeviceMetrics(device.getIp(), device.getMacAddress());
            if (wmiMetrics.containsKey("error")) {
                return ResponseEntity.status(500).body(Map.of("error", wmiMetrics.get("error")));
            }
            DeviceMetrics metrics = new DeviceMetrics();
            metrics.setDevice(device);
            metrics.setTimestamp(LocalDateTime.now());
            try {
                metrics.setCpuUsage(Double.parseDouble(wmiMetrics.getOrDefault("cpuUsage", "0").replace(",", ".")));
                metrics.setMemoryUsage(Double.parseDouble(wmiMetrics.getOrDefault("memoryUsage", "0").replace(",", ".")));
                metrics.setDiskUsage(Double.parseDouble(wmiMetrics.getOrDefault("diskUsage", "0").replace(",", ".")));
                metrics.setTemperature(Double.parseDouble(wmiMetrics.getOrDefault("temperature", "0").replace(",", ".")));
                metrics.setNetworkActivity(Double.parseDouble(wmiMetrics.getOrDefault("networkActivity", "0").replace(",", ".")));
            } catch (NumberFormatException e) {
                return ResponseEntity.status(500).body(Map.of("error", "Ошибка парсинга метрик: " + e.getMessage()));
            }
            deviceMetricsRepository.save(metrics);
            device.setStatus(wmiMetrics.getOrDefault("status", "Неизвестно"));
            device.setModel(wmiMetrics.getOrDefault("model", "Неизвестно"));
            device.setLastActivity(wmiMetrics.getOrDefault("lastActivity", "Неизвестно"));
            device.setUptime(Long.parseLong(wmiMetrics.getOrDefault("uptime", "0")));
            deviceRepository.save(device);
            Map<String, Object> responseMetrics = new HashMap<>();
            responseMetrics.put("cpuUsage", wmiMetrics.getOrDefault("cpuUsage", "0"));
            responseMetrics.put("memoryUsage", wmiMetrics.getOrDefault("memoryUsage", "0"));
            responseMetrics.put("diskUsage", wmiMetrics.getOrDefault("diskUsage", "0"));
            responseMetrics.put("temperature", wmiMetrics.getOrDefault("temperature", "0"));
            responseMetrics.put("networkActivity", wmiMetrics.getOrDefault("networkActivity", "0"));
            responseMetrics.put("incomingTraffic", wmiMetrics.getOrDefault("incomingTraffic", "Неизвестно"));
            responseMetrics.put("outgoingTraffic", wmiMetrics.getOrDefault("outgoingTraffic", "Неизвестно"));
            responseMetrics.put("processor", wmiMetrics.getOrDefault("processor", "Неизвестно"));
            responseMetrics.put("model", wmiMetrics.getOrDefault("model", "Неизвестно"));
            responseMetrics.put("ram", wmiMetrics.getOrDefault("ram", "Неизвестно"));
            responseMetrics.put("storage", wmiMetrics.getOrDefault("storage", "Неизвестно"));
            responseMetrics.put("power", wmiMetrics.getOrDefault("power", "Неизвестно"));
            responseMetrics.put("status", wmiMetrics.getOrDefault("status", "Неизвестно"));
            responseMetrics.put("lastActivity", wmiMetrics.getOrDefault("lastActivity", "Неизвестно"));
            responseMetrics.put("uptime", Long.parseLong(wmiMetrics.getOrDefault("uptime", "0")));
            return ResponseEntity.ok(responseMetrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при загрузке метрик устройства: " + e.getMessage()));
        }
    }
}