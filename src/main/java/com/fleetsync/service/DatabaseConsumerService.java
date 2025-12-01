package com.fleetsync.service;

import com.fleetsync.entity.TruckTelemetryEntity;
import com.fleetsync.model.TruckTelemetry;
import com.fleetsync.repository.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @author Shivam Srivastav
 *         Database consumer - saves telemetry to PostgreSQL
 */
@Service
public class DatabaseConsumerService {

    private final TelemetryRepository telemetryRepository;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConsumerService.class);

    public DatabaseConsumerService(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    @KafkaListener(topics = "fleet-telemetry", groupId = "fleetsync-database")
    public void saveTelemetry(TruckTelemetry telemetry) {
        try {
            TruckTelemetryEntity entity = new TruckTelemetryEntity(
                    telemetry.getTruckId(),
                    telemetry.getLatitude(),
                    telemetry.getLongitude(),
                    telemetry.getSpeed(),
                    telemetry.getEngineTemp(),
                    telemetry.getFuelLevel(),
                    telemetry.getTimestamp());

            telemetryRepository.save(entity);
            logger.debug("Saved telemetry for truck {} to database", telemetry.getTruckId());
        } catch (Exception e) {
            logger.error("Error saving telemetry to database", e);
        }
    }
}
