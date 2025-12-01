package com.fleetsync.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "truck_telemetry", indexes = {
        @Index(name = "idx_truck_id", columnList = "truck_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class TruckTelemetryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", nullable = false, length = 50)
    private String truckId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private Double speed;

    @Column(name = "engine_temp")
    private Double engineTemp;

    @Column(name = "fuel_level")
    private Double fuelLevel;

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Constructors
    public TruckTelemetryEntity() {
    }

    public TruckTelemetryEntity(String truckId, Double latitude, Double longitude,
            Double speed, Double engineTemp, Double fuelLevel, Long timestamp) {
        this.truckId = truckId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.engineTemp = engineTemp;
        this.fuelLevel = fuelLevel;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getEngineTemp() {
        return engineTemp;
    }

    public void setEngineTemp(Double engineTemp) {
        this.engineTemp = engineTemp;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
