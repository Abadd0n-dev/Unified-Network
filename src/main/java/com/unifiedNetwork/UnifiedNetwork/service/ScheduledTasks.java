package com.unifiedNetwork.UnifiedNetwork.service;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ScheduledTasks {
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private SoftwareService softwareService;

    // Обновляем список ПО для всех рабочих станций и серверов каждые 24 часа
    @Scheduled(fixedRate = 86400000) // 24 часа в миллисекундах
    public void updateSoftwareForAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            if (device.getType().equals("workstation") || device.getType().equals("server")) {
                softwareService.updateSoftwareForDevice(device);
            }
        }
    }
}

