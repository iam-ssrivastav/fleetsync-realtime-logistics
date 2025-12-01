package com.fleetsync.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetsync.model.TruckTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Shivam Srivastav
 */
@Component
public class TruckSimulator {

    private static final Logger log = LoggerFactory.getLogger(TruckSimulator.class);

    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<TruckTelemetry> trucks = new ArrayList<>();
    private final Random random = new Random();

    public TruckSimulator(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    // Initialize trucks
    {
        // New York area start points
        trucks.add(new TruckTelemetry("TRUCK-001", 40.7128, -74.0060, 0, 80, 100, System.currentTimeMillis()));
        trucks.add(new TruckTelemetry("TRUCK-002", 40.7300, -73.9900, 0, 82, 90, System.currentTimeMillis()));
        trucks.add(new TruckTelemetry("TRUCK-003", 40.7500, -73.9800, 0, 85, 80, System.currentTimeMillis()));
        trucks.add(new TruckTelemetry("TRUCK-004", 40.7800, -73.9500, 0, 78, 50, System.currentTimeMillis()));
        trucks.add(new TruckTelemetry("TRUCK-005", 40.8000, -73.9600, 0, 75, 15, System.currentTimeMillis()));
    }

    @Scheduled(fixedRate = 2000) // Every 2 seconds
    public void simulateTelemetry() {
        for (TruckTelemetry truck : trucks) {
            updateTruckState(truck);
            publishTelemetry(truck);
        }
    }

    private void updateTruckState(TruckTelemetry truck) {
        // Update timestamp
        truck.setTimestamp(System.currentTimeMillis());

        // Simulate movement (random walk)
        truck.setLatitude(truck.getLatitude() + (random.nextDouble() - 0.5) * 0.001);
        truck.setLongitude(truck.getLongitude() + (random.nextDouble() - 0.5) * 0.001);

        // Simulate speed (0-90 mph)
        double speedChange = (random.nextDouble() - 0.5) * 10;
        double newSpeed = Math.max(0, Math.min(95, truck.getSpeed() + speedChange));
        truck.setSpeed(newSpeed);

        // Simulate engine temp (80-110 C)
        double tempChange = (random.nextDouble() - 0.5) * 2;
        double newTemp = Math.max(70, Math.min(115, truck.getEngineTemp() + tempChange));
        truck.setEngineTemp(newTemp);

        // Simulate fuel consumption
        double fuelConsumption = 0.05 + (truck.getSpeed() / 1000.0);
        truck.setFuelLevel(Math.max(0, truck.getFuelLevel() - fuelConsumption));
    }

    private void publishTelemetry(TruckTelemetry truck) {
        try {
            String payload = objectMapper.writeValueAsString(truck);
            String topic = "fleet/trucks/" + truck.getTruckId();

            mqttOutboundChannel.send(MessageBuilder
                    .withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .build());

            // log.info("Published to {}: {}", topic, payload); // Verbose logging

        } catch (Exception e) {
            log.error("Failed to publish telemetry for {}", truck.getTruckId(), e);
        }
    }
}
