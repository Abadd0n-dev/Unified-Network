package com.unifiedNetwork.UnifiedNetwork.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "software")
public class Software {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Название ПО

    @Column
    private String version; // Версия

    @Column
    private String publisher; // Издатель

    @Column(name = "installation_date")
    private LocalDate installationDate; // Дата установки

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device; // Связь с устройством

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
