package com.fleetsync.controller;

import com.fleetsync.entity.TruckTelemetryEntity;
import com.fleetsync.repository.TelemetryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final TelemetryRepository telemetryRepository;

    public HistoryController(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @GetMapping("/telemetry")
    public Map<String, Object> getHistoricalTelemetry(
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(defaultValue = "100") int limit) {

        Map<String, Object> response = new HashMap<>();
        List<TruckTelemetryEntity> data;

        if (from != null && to != null) {
            data = telemetryRepository.findByTimestampBetween(from, to);
        } else {
            data = telemetryRepository.findTop100ByOrderByTimestampDesc();
        }

        response.put("data", data);
        response.put("count", data.size());
        response.put("totalRecords", telemetryRepository.countAllTelemetry());

        return response;
    }

    @GetMapping("/truck/{truckId}")
    public Map<String, Object> getTruckHistory(
            @PathVariable String truckId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {

        Map<String, Object> response = new HashMap<>();
        List<TruckTelemetryEntity> data;

        if (from != null && to != null) {
            data = telemetryRepository.findByTruckIdAndTimestampBetween(truckId, from, to);
        } else {
            data = telemetryRepository.findByTruckIdOrderByTimestampDesc(truckId);
        }

        response.put("truckId", truckId);
        response.put("data", data);
        response.put("count", data.size());

        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", telemetryRepository.countAllTelemetry());
        stats.put("database", "PostgreSQL");
        stats.put("status", "Connected");
        return stats;
    }
}
