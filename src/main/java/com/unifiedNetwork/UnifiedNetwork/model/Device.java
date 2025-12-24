package com.unifiedNetwork.UnifiedNetwork.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "devices")
public class Device {
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Software> softwareList;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String ip;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(nullable = false)
    private String type;

    @Column
    private String status;

    @Column
    private String location;

    @Column(name = "operating_system")
    private String os;

    @Column
    private String manufacturer;

    @Column
    private String model;

    @Column(name = "last_activity")
    private String lastActivity;

    @Column
    private Long uptime;

    @Column(name = "network_speed")
    private String networkSpeed;

    @Column(name = "incoming_traffic")
    private String incomingTraffic;

    @Column(name = "outgoing_traffic")
    private String outgoingTraffic;

    @Column
    private String processor;

    @Column
    private String ram;

    @Column
    private String storage;

    @Column
    private String power;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Геттеры и сеттеры для всех полей
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public String getNetworkSpeed() {
        return networkSpeed;
    }

    public void setNetworkSpeed(String networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    public String getIncomingTraffic() {
        return incomingTraffic;
    }

    public void setIncomingTraffic(String incomingTraffic) {
        this.incomingTraffic = incomingTraffic;
    }

    public String getOutgoingTraffic() {
        return outgoingTraffic;
    }

    public void setOutgoingTraffic(String outgoingTraffic) {
        this.outgoingTraffic = outgoingTraffic;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
