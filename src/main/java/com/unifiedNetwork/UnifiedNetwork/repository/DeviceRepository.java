package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findAll();
    List<Device> findByUserId(Long userId);
    Device findByIp(String ip);
}


