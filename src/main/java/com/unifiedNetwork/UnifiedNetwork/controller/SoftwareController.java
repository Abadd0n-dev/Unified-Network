package com.unifiedNetwork.UnifiedNetwork.controller;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.model.Software;
import com.unifiedNetwork.UnifiedNetwork.repository.DeviceRepository;
import com.unifiedNetwork.UnifiedNetwork.service.SoftwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/software")
public class SoftwareController {
    @Autowired
    private SoftwareService softwareService;

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping(value = "/device/{deviceId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Software>> getSoftwareByDevice(@PathVariable Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Устройство не найдено"));

        softwareService.updateSoftwareForDevice(device);

        List<Software> softwareList = softwareService.getSoftwareByDevice(device);
        return ResponseEntity.ok(softwareList);
    }

    @PostMapping("/add")
    public ResponseEntity<Software> addSoftware(@RequestBody Software software) {
        Software savedSoftware = softwareService.addSoftware(software);
        return ResponseEntity.ok(savedSoftware);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSoftware(@PathVariable Long id) {
        softwareService.deleteSoftware(id);
        return ResponseEntity.noContent().build();
    }
}