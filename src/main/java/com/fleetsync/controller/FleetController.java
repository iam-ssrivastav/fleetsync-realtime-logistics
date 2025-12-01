package com.fleetsync.controller;

import com.fleetsync.model.TruckTelemetry;
import com.fleetsync.service.KafkaConsumerService;
import com.fleetsync.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fleet")
@Tag(name = "Fleet APIs", description = "Real-time fleet monitoring and telemetry data")
public class FleetController {

    private final KafkaConsumerService consumerService;
    private final TelemetryService telemetryService;

    public FleetController(KafkaConsumerService consumerService, TelemetryService telemetryService) {
        this.consumerService = consumerService;
        this.telemetryService = telemetryService;
    }

    @GetMapping("/trucks")
    @Operation(summary = "Get All Trucks", description = "Returns current telemetry data for all active trucks.")
    public Map<String, TruckTelemetry> getAllTrucks() {
        return consumerService.getLatestTelemetry();
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get Recent Alerts", description = "Returns the 50 most recent alerts.")
    public List<String> getRecentAlerts() {
        return consumerService.getRecentAlerts();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Fleet Statistics", description = "Returns real-time aggregated statistics for the entire fleet.")
    public Map<String, Object> getFleetStats() {
        Map<String, TruckTelemetry> fleet = consumerService.getLatestTelemetry();
        Map<String, Object> stats = new HashMap<>();

        stats.put("activeTrucks", fleet.size());

        if (fleet.isEmpty()) {
            stats.put("averageSpeed", 0.0);
            stats.put("averageEngineTemp", 0.0);
            stats.put("averageFuelLevel", 0.0);
            return stats;
        }

        double avgSpeed = fleet.values().stream().mapToDouble(TruckTelemetry::getSpeed).average().orElse(0.0);
        double avgTemp = fleet.values().stream().mapToDouble(TruckTelemetry::getEngineTemp).average().orElse(0.0);
        double avgFuel = fleet.values().stream().mapToDouble(TruckTelemetry::getFuelLevel).average().orElse(0.0);

        stats.put("averageSpeed", Math.round(avgSpeed * 10.0) / 10.0);
        stats.put("averageEngineTemp", Math.round(avgTemp * 10.0) / 10.0);
        stats.put("averageFuelLevel", Math.round(avgFuel * 10.0) / 10.0);

        return stats;
    }
}
