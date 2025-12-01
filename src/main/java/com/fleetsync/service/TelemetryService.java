package com.fleetsync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetsync.model.TruckTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelemetryService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<String> message) {
        try {
            String payload = message.getPayload();
            // log.info("Received MQTT message: {}", payload); // Verbose logging

            TruckTelemetry telemetry = objectMapper.readValue(payload, TruckTelemetry.class);

            // Broadcast telemetry to dashboard
            messagingTemplate.convertAndSend("/topic/telemetry", telemetry);

            // Check for alerts
            checkForAlerts(telemetry);

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    private void checkForAlerts(TruckTelemetry telemetry) {
        if (telemetry.getSpeed() > 80) {
            sendAlert(telemetry.getTruckId(), "SPEEDING",
                    "Truck " + telemetry.getTruckId() + " is speeding at " + String.format("%.1f", telemetry.getSpeed())
                            + " mph!");
        }

        if (telemetry.getEngineTemp() > 100) {
            sendAlert(telemetry.getTruckId(), "OVERHEATING",
                    "Truck " + telemetry.getTruckId() + " engine temp is critical: "
                            + String.format("%.1f", telemetry.getEngineTemp()) + "°C");
        }

        if (telemetry.getFuelLevel() < 10) {
            sendAlert(telemetry.getTruckId(), "LOW_FUEL",
                    "Truck " + telemetry.getTruckId() + " has low fuel: "
                            + String.format("%.1f", telemetry.getFuelLevel()) + "%");
        }
    }

    private void sendAlert(String truckId, String type, String message) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("truckId", truckId);
        alert.put("type", type);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/alerts", alert);
        log.warn("ALERT: {}", message);
    }
}
