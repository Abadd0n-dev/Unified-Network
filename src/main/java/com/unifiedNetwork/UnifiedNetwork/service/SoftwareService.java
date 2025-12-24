package com.unifiedNetwork.UnifiedNetwork.service;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.model.Software;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.SoftwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SoftwareService {
    @Autowired
    private SoftwareRepository softwareRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    public List<Software> getSoftwareByDevice(Device device) {
        return softwareRepository.findByDevice(device);
    }

    public Software addSoftware(Software software) {
        return softwareRepository.save(software);
    }

    public void deleteSoftware(Long id) {
        softwareRepository.deleteById(id);
    }

    // Метод для обновления списка ПО для устройства
    public void updateSoftwareForDevice(Device device) {
        // Проверяем, что устройство доступно и его тип поддерживает учет ПО
        if (!"workstation".equals(device.getType()) && !"server".equals(device.getType())) {
            return;
        }

        // Получаем ПО для конкретного устройства (здесь должна быть логика для получения ПО с конкретного устройства)
        List<Map<String, String>> softwareData = WmiSoftwareCollector.getInstalledSoftwareForDevice(device);

        // Удаляем старые записи ПО для устройства
        List<Software> oldSoftware = softwareRepository.findByDevice(device);
        softwareRepository.deleteAll(oldSoftware);

        // Форматируем дату установки
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // Сохраняем новые данные о ПО
        for (Map<String, String> softwareInfo : softwareData) {
            Software software = new Software();
            software.setName(softwareInfo.get("DisplayName") != null ? softwareInfo.get("DisplayName") : "Неизвестно");
            software.setVersion(softwareInfo.get("DisplayVersion") != null ? softwareInfo.get("DisplayVersion") : "Неизвестно");
            software.setPublisher(softwareInfo.get("Publisher") != null ? softwareInfo.get("Publisher") : "Неизвестно");

            String installDateStr = softwareInfo.get("InstallDate");
            if (installDateStr != null && !installDateStr.isEmpty()) {
                try {
                    software.setInstallationDate(LocalDate.parse(installDateStr, formatter));
                } catch (Exception e) {
                    software.setInstallationDate(null);
                }
            } else {
                software.setInstallationDate(null);
            }

            software.setDevice(device);
            softwareRepository.save(software);
        }
    }
}
