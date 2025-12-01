package com.fleetsync.service;

import com.fleetsync.model.TruckTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Shivam Srivastav
 */
@Service
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    public KafkaConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "fleet-telemetry", groupId = "fleetsync-dashboard")
    public void consumeTelemetry(TruckTelemetry telemetry) {
        // Update cache for REST API
        com.fleetsync.controller.FleetController.updateTelemetry(telemetry);

        // Broadcast to WebSocket clients (The "View" Layer)
        messagingTemplate.convertAndSend("/topic/telemetry", telemetry);
        logger.debug("Forwarded truck {} to dashboard", telemetry.getTruckId());
    }
}
