package com.unifiedNetwork.UnifiedNetwork.service;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceMetricsRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceMetricsRepository deviceMetricsRepository;

    @Transactional
    public void deleteDeviceByIp(String ip) {
        Device device = deviceRepository.findByIp(ip);
        if (device != null) {
            // Удаляем все метрики, связанные с устройством
            deviceMetricsRepository.deleteByDevice(device);
            // Удаляем устройство
            deviceRepository.delete(device);
        }
    }
}

