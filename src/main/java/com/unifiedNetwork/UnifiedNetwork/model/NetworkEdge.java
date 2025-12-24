package com.unifiedNetwork.UnifiedNetwork.model;

import jakarta.persistence.*;

@Entity
@Table(name = "network_edges")
public class NetworkEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromNodeId;

    @Column(nullable = false)
    private String toNodeId;

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

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public NetworkMap getNetworkMap() {
        return networkMap;
    }

    public void setNetworkMap(NetworkMap networkMap) {
        this.networkMap = networkMap;
    }
}