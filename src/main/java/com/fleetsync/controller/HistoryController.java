package com.fleetsync.controller;

import com.fleetsync.entity.TruckTelemetryEntity;
import com.fleetsync.repository.TelemetryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/history")
@Tag(name = "Historical Data", description = "APIs for querying historical telemetry data from PostgreSQL")
public class HistoryController {

    private final TelemetryRepository repository;

    public HistoryController(TelemetryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/telemetry")
    @Operation(summary = "Get Historical Telemetry", description = "Retrieve past telemetry data with optional time range filters.")
    public Map<String, Object> getHistory(
            @Parameter(description = "Start timestamp (Unix ms)") @RequestParam(required = false) Long from,
            @Parameter(description = "End timestamp (Unix ms)") @RequestParam(required = false) Long to,
            @Parameter(description = "Max records to return") @RequestParam(defaultValue = "100") int limit) {

        List<TruckTelemetryEntity> data;

        if (from != null && to != null) {
            data = repository.findByTimestampBetween(from, to,
                    PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")));
        } else {
            data = repository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("count", data.size());
        response.put("data", data);

        return response;
    }

    @GetMapping("/truck/{truckId}")
    @Operation(summary = "Get Truck History", description = "Retrieve historical path data for a specific truck.")
    public Map<String, Object> getTruckHistory(
            @Parameter(description = "Truck ID (e.g., TRUCK-001)") @PathVariable String truckId,
            @Parameter(description = "Max records to return") @RequestParam(defaultValue = "100") int limit) {

        List<TruckTelemetryEntity> data = repository.findByTruckId(truckId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp")));

        Map<String, Object> response = new HashMap<>();
        response.put("truckId", truckId);
        response.put("count", data.size());
        response.put("data", data);

        return response;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Database Stats", description = "Returns statistics about the historical database.")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", repository.count());
        stats.put("database", "PostgreSQL");
        stats.put("status", "Connected");
        return stats;
    }
}
