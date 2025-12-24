package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.NetworkNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {
    List<NetworkNode> findByNetworkMapId(Long networkMapId);
    void deleteByNodeId(String nodeId);
    void deleteByNetworkMapId(Long networkMapId);
}


