package com.unifiedNetwork.UnifiedNetwork.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "network_maps")
public class NetworkMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "networkMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NetworkNode> nodes;

    @OneToMany(mappedBy = "networkMap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NetworkEdge> edges;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<NetworkNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<NetworkNode> nodes) {
        this.nodes = nodes;
    }

    public List<NetworkEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<NetworkEdge> edges) {
        this.edges = edges;
    }
}

