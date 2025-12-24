package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.model.DeviceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceMetricsRepository extends JpaRepository<DeviceMetrics, Long> {
    List<DeviceMetrics> findByDeviceAndTimestampAfter(Device device, LocalDateTime timestamp);

    void deleteByDevice(Device device);
}


