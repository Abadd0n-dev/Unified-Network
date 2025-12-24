package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.NetworkEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkEdgeRepository extends JpaRepository<NetworkEdge, Long> {
    List<NetworkEdge> findByNetworkMapId(Long networkMapId);
    void deleteByNetworkMapId(Long networkMapId);
}


