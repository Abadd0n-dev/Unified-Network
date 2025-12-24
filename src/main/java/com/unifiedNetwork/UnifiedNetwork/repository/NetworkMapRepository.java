package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.NetworkMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NetworkMapRepository extends JpaRepository<NetworkMap, Long> {
    List<NetworkMap> findByUserId(Long userId);
}



