package com.fleetsync.controller;

import com.fleetsync.model.TruckTelemetry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shivam Srivastav
 *         REST API for fleet data
 */
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    // Shared cache for latest telemetry (in production, use Redis)
    private static final Map<String, TruckTelemetry> latestTelemetry = new ConcurrentHashMap<>();
    private static final List<String> recentAlerts = Collections.synchronizedList(new ArrayList<>());

    public static void updateTelemetry(TruckTelemetry telemetry) {
        latestTelemetry.put(telemetry.getTruckId(), telemetry);
    }

    public static void addAlert(String alert) {
        recentAlerts.add(0, alert);
        if (recentAlerts.size() > 50) {
            recentAlerts.remove(recentAlerts.size() - 1);
        }
    }

    @GetMapping("/trucks")
    public Map<String, Object> getAllTrucks() {
        Map<String, Object> response = new HashMap<>();
        response.put("trucks", new ArrayList<>(latestTelemetry.values()));
        response.put("count", latestTelemetry.size());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/alerts")
    public Map<String, Object> getAlerts() {
        Map<String, Object> response = new HashMap<>();
        response.put("alerts", new ArrayList<>(recentAlerts));
        response.put("count", recentAlerts.size());
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getFleetStats() {
        Map<String, Object> stats = new HashMap<>();

        if (latestTelemetry.isEmpty()) {
            stats.put("averageSpeed", 0);
            stats.put("activeTrucks", 0);
            return stats;
        }

        double avgSpeed = latestTelemetry.values().stream()
                .mapToDouble(TruckTelemetry::getSpeed)
                .average()
                .orElse(0.0);

        double avgTemp = latestTelemetry.values().stream()
                .mapToDouble(TruckTelemetry::getEngineTemp)
                .average()
                .orElse(0.0);

        double avgFuel = latestTelemetry.values().stream()
                .mapToDouble(TruckTelemetry::getFuelLevel)
                .average()
                .orElse(0.0);

        stats.put("activeTrucks", latestTelemetry.size());
        stats.put("averageSpeed", Math.round(avgSpeed * 10.0) / 10.0);
        stats.put("averageEngineTemp", Math.round(avgTemp * 10.0) / 10.0);
        stats.put("averageFuelLevel", Math.round(avgFuel * 10.0) / 10.0);
        stats.put("timestamp", System.currentTimeMillis());

        return stats;
    }
}
