package com.unifiedNetwork.UnifiedNetwork.model;

import jakarta.persistence.*;

@Entity
@Table(name = "network_nodes")
public class NetworkNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nodeId;

    @Column(nullable = false)
    private String label;

    @ManyToOne
    @JoinColumn(name = "network_map_id", nullable = false)
    private NetworkMap networkMap;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public NetworkMap getNetworkMap() {
        return networkMap;
    }

    public void setNetworkMap(NetworkMap networkMap) {
        this.networkMap = networkMap;
    }
}