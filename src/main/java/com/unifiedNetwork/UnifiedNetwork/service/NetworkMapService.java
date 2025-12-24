package com.unifiedNetwork.UnifiedNetwork.service;

import com.unifiedNetwork.UnifiedNetwork.model.NetworkMap;
import com.unifiedNetwork.UnifiedNetwork.model.NetworkNode;
import com.unifiedNetwork.UnifiedNetwork.model.NetworkEdge;
import com.unifiedNetwork.UnifiedNetwork.repository.NetworkMapRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.NetworkNodeRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.NetworkEdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NetworkMapService {
    @Autowired
    private NetworkMapRepository networkMapRepository;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private NetworkEdgeRepository networkEdgeRepository;

    // Создание новой сетевой карты
    public NetworkMap createNetworkMap(String name, Long userId) {
        NetworkMap networkMap = new NetworkMap();
        networkMap.setName(name);
        networkMap.setUserId(userId);
        return networkMapRepository.save(networkMap);
    }

    // Получение сетевой карты по ID и userId
    public NetworkMap getNetworkMap(Long id, Long userId) {
        return networkMapRepository.findById(id)
                .filter(networkMap -> networkMap.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Network map not found or does not belong to the user"));
    }

    // Добавление узла на сетевую карту
    public NetworkNode addNodeToNetworkMap(Long networkMapId, String nodeId, String label, Long userId) {
        NetworkMap networkMap = getNetworkMap(networkMapId, userId);
        NetworkNode node = new NetworkNode();
        node.setNodeId(nodeId);
        node.setLabel(label);
        node.setNetworkMap(networkMap);
        return networkNodeRepository.save(node);
    }

    // Добавление связи между узлами на сетевой карте
    public NetworkEdge addEdgeToNetworkMap(Long networkMapId, String fromNodeId, String toNodeId, Long userId) {
        NetworkMap networkMap = getNetworkMap(networkMapId, userId);
        NetworkEdge edge = new NetworkEdge();
        edge.setFromNodeId(fromNodeId);
        edge.setToNodeId(toNodeId);
        edge.setNetworkMap(networkMap);
        return networkEdgeRepository.save(edge);
    }

    // Удаление узла с сетевой карты
    @Transactional
    public void removeNodeFromNetworkMap(Long networkMapId, String nodeId, Long userId) {
        NetworkMap networkMap = getNetworkMap(networkMapId, userId);
        // Удаление связей, связанных с узлом
        List<NetworkEdge> edgesToRemove = networkEdgeRepository.findByNetworkMapId(networkMapId)
                .stream()
                .filter(edge -> edge.getFromNodeId().equals(nodeId) || edge.getToNodeId().equals(nodeId))
                .toList();
        networkEdgeRepository.deleteAll(edgesToRemove);
        // Удаление узла
        networkNodeRepository.deleteByNodeId(nodeId);
    }

    // Очистка сетевой карты
    @Transactional
    public void clearNetworkMap(Long networkMapId, Long userId) {
        NetworkMap networkMap = getNetworkMap(networkMapId, userId);
        // Удаление всех связей и узлов для данной карты
        networkEdgeRepository.deleteByNetworkMapId(networkMapId);
        networkNodeRepository.deleteByNetworkMapId(networkMapId);
    }

    // Сохранение текущего состояния сетевой карты
    @Transactional
    public NetworkMap saveNetworkMapState(Long networkMapId, Long userId, Map<String, List<Map<String, String>>> request) {
        NetworkMap networkMap = getNetworkMap(networkMapId, userId);

        // Очищаем текущие узлы и связи
        networkNodeRepository.deleteByNetworkMapId(networkMapId);
        networkEdgeRepository.deleteByNetworkMapId(networkMapId);

        // Сохраняем новые узлы
        List<Map<String, String>> nodes = request.get("nodes");
        for (Map<String, String> node : nodes) {
            NetworkNode networkNode = new NetworkNode();
            networkNode.setNodeId(node.get("id"));
            networkNode.setLabel(node.get("label"));
            networkNode.setNetworkMap(networkMap);
            networkNodeRepository.save(networkNode);
        }

        // Сохраняем новые связи
        List<Map<String, String>> edges = request.get("edges");
        for (Map<String, String> edge : edges) {
            NetworkEdge networkEdge = new NetworkEdge();
            networkEdge.setFromNodeId(edge.get("from"));
            networkEdge.setToNodeId(edge.get("to"));
            networkEdge.setNetworkMap(networkMap);
            networkEdgeRepository.save(networkEdge);
        }

        return networkMap;
    }
}
