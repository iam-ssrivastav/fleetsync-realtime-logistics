package com.fleetsync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetsync.model.TruckTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shivam Srivastav
 */
@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TelemetryService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = (String) message.getPayload();

        try {
            TruckTelemetry telemetry = objectMapper.readValue(payload, TruckTelemetry.class);

            // 1. Send to Kafka (The "Pipeline")
            kafkaTemplate.send("fleet-telemetry", telemetry.getTruckId(), telemetry);

            // 2. Check for Alerts (Still done here for simplicity, or could be a separate
            // consumer)
            checkForAlerts(telemetry);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void checkForAlerts(TruckTelemetry telemetry) {
        java.util.List<String> alerts = new java.util.ArrayList<>();

        if (telemetry.getSpeed() > 80) {
            alerts.add("SPEEDING");
        }
        if (telemetry.getEngineTemp() > 100) {
            alerts.add("OVERHEATING");
        }
        if (telemetry.getFuelLevel() < 10) {
            alerts.add("LOW FUEL");
        }

        if (!alerts.isEmpty()) {
            Map<String, Object> alertMessage = new HashMap<>();
            alertMessage.put("truckId", telemetry.getTruckId());
            alertMessage.put("alerts", alerts);
            alertMessage.put("timestamp", System.currentTimeMillis());

            // Cache for REST API
            String alertText = telemetry.getTruckId() + ": " + String.join(", ", alerts);
            com.fleetsync.controller.FleetController.addAlert(alertText);

            // Broadcast to WebSocket
            messagingTemplate.convertAndSend("/topic/alerts", alertMessage);
            log.warn("Alert for {}: {}", telemetry.getTruckId(), alerts);
        }
    }
}
