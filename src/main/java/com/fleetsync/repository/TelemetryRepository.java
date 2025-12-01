package com.fleetsync.repository;

import com.fleetsync.entity.TruckTelemetryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TruckTelemetryEntity, Long> {

    // Find by time range with pagination
    List<TruckTelemetryEntity> findByTimestampBetween(Long from, Long to, Pageable pageable);

    // Find by truck ID with pagination
    List<TruckTelemetryEntity> findByTruckId(String truckId, Pageable pageable);

    // Legacy methods (optional, keeping if needed)
    List<TruckTelemetryEntity> findTop100ByOrderByTimestampDesc();

}
