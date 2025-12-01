package com.fleetsync.service;

import com.fleetsync.model.TruckTelemetry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory cache for real-time state
    private final Map<String, TruckTelemetry> latestTelemetry = new ConcurrentHashMap<>();
    private final List<String> recentAlerts = Collections.synchronizedList(new ArrayList<>());

    public KafkaConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "fleet-telemetry", groupId = "fleetsync-dashboard")
    public void consumeTelemetry(TruckTelemetry telemetry) {
        // Update local cache
        latestTelemetry.put(telemetry.getTruckId(), telemetry);

        // Push to WebSocket
        messagingTemplate.convertAndSend("/topic/telemetry", telemetry);
    }

    @KafkaListener(topics = "fleet-alerts", groupId = "fleetsync-dashboard")
    public void consumeAlert(String alert) {
        // Update local cache
        recentAlerts.add(0, alert);
        if (recentAlerts.size() > 50) {
            recentAlerts.remove(recentAlerts.size() - 1);
        }

        // Push to WebSocket
        messagingTemplate.convertAndSend("/topic/alerts", alert);
    }

    public Map<String, TruckTelemetry> getLatestTelemetry() {
        return latestTelemetry;
    }

    public List<String> getRecentAlerts() {
        return new ArrayList<>(recentAlerts);
    }
}
