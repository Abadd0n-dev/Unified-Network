package com.unifiedNetwork.UnifiedNetwork.controller;

import com.unifiedNetwork.UnifiedNetwork.model.NetworkMap;
import com.unifiedNetwork.UnifiedNetwork.model.NetworkNode;
import com.unifiedNetwork.UnifiedNetwork.model.NetworkEdge;
import com.unifiedNetwork.UnifiedNetwork.repository.NetworkEdgeRepository;
import com.unifiedNetwork.UnifiedNetwork.repository.NetworkNodeRepository;
import com.unifiedNetwork.UnifiedNetwork.service.NetworkMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/network-maps")
public class NetworkMapController {
    @Autowired
    private NetworkMapService networkMapService;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private NetworkEdgeRepository networkEdgeRepository;

    // Создание новой сетевой карты
    @PostMapping
    public ResponseEntity<NetworkMap> createNetworkMap(@RequestParam String name, @RequestAttribute("userId") Long userId) {
        NetworkMap networkMap = networkMapService.createNetworkMap(name, userId);
        return ResponseEntity.ok(networkMap);
    }

    // Добавление узла на сетевую карту
    @PostMapping("/{networkMapId}/nodes")
    public ResponseEntity<NetworkNode> addNodeToNetworkMap(
            @PathVariable Long networkMapId,
            @RequestParam String nodeId,
            @RequestParam String label,
            @RequestAttribute("userId") Long userId) {
        NetworkNode node = networkMapService.addNodeToNetworkMap(networkMapId, nodeId, label, userId);
        return ResponseEntity.ok(node);
    }

    // Добавление связи между узлами на сетевой карте
    @PostMapping("/{networkMapId}/edges")
    public ResponseEntity<NetworkEdge> addEdgeToNetworkMap(
            @PathVariable Long networkMapId,
            @RequestParam String fromNodeId,
            @RequestParam String toNodeId,
            @RequestAttribute("userId") Long userId) {
        NetworkEdge edge = networkMapService.addEdgeToNetworkMap(networkMapId, fromNodeId, toNodeId, userId);
        return ResponseEntity.ok(edge);
    }

    // Удаление узла с сетевой карты
    @DeleteMapping("/{networkMapId}/nodes/{nodeId}")
    public ResponseEntity<Void> removeNodeFromNetworkMap(
            @PathVariable Long networkMapId,
            @PathVariable String nodeId,
            @RequestAttribute("userId") Long userId) {
        networkMapService.removeNodeFromNetworkMap(networkMapId, nodeId, userId);
        return ResponseEntity.noContent().build();
    }

    // Очистка сетевой карты
    @DeleteMapping("/{networkMapId}/clear")
    public ResponseEntity<Void> clearNetworkMap(
            @PathVariable Long networkMapId,
            @RequestAttribute("userId") Long userId) {
        networkMapService.clearNetworkMap(networkMapId, userId);
        return ResponseEntity.noContent().build();
    }

    // Сохранение текущего состояния сетевой карты
    @PostMapping("/{networkMapId}/save-state")
    public ResponseEntity<?> saveNetworkMapState(
            @PathVariable Long networkMapId,
            @RequestBody Map<String, Object> request,
            @RequestAttribute("userId") Long userId) {
        try {
            List<Map<String, String>> nodes = (List<Map<String, String>>) request.get("nodes");
            List<Map<String, String>> edges = (List<Map<String, String>>) request.get("edges");
            Map<String, List<Map<String, String>>> requestData = new HashMap<>();
            requestData.put("nodes", nodes);
            requestData.put("edges", edges);
            NetworkMap networkMap = networkMapService.saveNetworkMapState(networkMapId, userId, requestData);
            return ResponseEntity.ok(Map.of("success", true, "message", "Сетевая карта успешно сохранена!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Ошибка при сохранении сетевой карты: " + e.getMessage()));
        }
    }

    // Загрузка текущего состояния сетевой карты
    @GetMapping("/{networkMapId}/load-state")
    public ResponseEntity<Map<String, Object>> loadNetworkMapState(
            @PathVariable Long networkMapId,
            @RequestAttribute("userId") Long userId) {
        NetworkMap networkMap = networkMapService.getNetworkMap(networkMapId, userId);
        List<NetworkNode> nodes = networkNodeRepository.findByNetworkMapId(networkMapId);
        List<NetworkEdge> edges = networkEdgeRepository.findByNetworkMapId(networkMapId);
        List<Map<String, String>> nodesData = nodes.stream().map(node -> {
            Map<String, String> nodeData = new HashMap<>();
            nodeData.put("id", node.getNodeId());
            nodeData.put("label", node.getLabel());
            return nodeData;
        }).collect(Collectors.toList());
        List<Map<String, String>> edgesData = edges.stream().map(edge -> {
            Map<String, String> edgeData = new HashMap<>();
            edgeData.put("from", edge.getFromNodeId());
            edgeData.put("to", edge.getToNodeId());
            return edgeData;
        }).collect(Collectors.toList());
        Map<String, Object> state = new HashMap<>();
        state.put("nodes", nodesData);
        state.put("edges", edgesData);
        return ResponseEntity.ok(state);
    }
}
