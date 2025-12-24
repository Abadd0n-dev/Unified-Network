package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.Device;
import com.unifiedNetwork.UnifiedNetwork.model.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {
    List<Software> findByDevice(Device device);
}