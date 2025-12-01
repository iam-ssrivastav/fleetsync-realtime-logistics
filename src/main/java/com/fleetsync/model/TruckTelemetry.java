package com.fleetsync.model;

/**
 * @author Shivam Srivastav
 */
public class TruckTelemetry {
    private String truckId;
    private double latitude;
    private double longitude;
    private double speed; // mph
    private double engineTemp; // Celsius
    private double fuelLevel; // percentage
    private long timestamp;

    public TruckTelemetry() {
    }

    public TruckTelemetry(String truckId, double latitude, double longitude, double speed, double engineTemp,
            double fuelLevel, long timestamp) {
        this.truckId = truckId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.engineTemp = engineTemp;
        this.fuelLevel = fuelLevel;
        this.timestamp = timestamp;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getEngineTemp() {
        return engineTemp;
    }

    public void setEngineTemp(double engineTemp) {
        this.engineTemp = engineTemp;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
