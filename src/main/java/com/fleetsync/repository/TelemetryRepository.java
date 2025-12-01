package com.fleetsync.repository;

import com.fleetsync.entity.TruckTelemetryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Shivam Srivastav
 */
@Repository
public interface TelemetryRepository extends JpaRepository<TruckTelemetryEntity, Long> {

    List<TruckTelemetryEntity> findByTruckIdOrderByTimestampDesc(String truckId);

    List<TruckTelemetryEntity> findTop100ByOrderByTimestampDesc();

    @Query("SELECT t FROM TruckTelemetryEntity t WHERE t.timestamp BETWEEN :from AND :to ORDER BY t.timestamp DESC")
    List<TruckTelemetryEntity> findByTimestampBetween(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT t FROM TruckTelemetryEntity t WHERE t.truckId = :truckId AND t.timestamp BETWEEN :from AND :to ORDER BY t.timestamp DESC")
    List<TruckTelemetryEntity> findByTruckIdAndTimestampBetween(
            @Param("truckId") String truckId,
            @Param("from") Long from,
            @Param("to") Long to);

    @Query("SELECT COUNT(t) FROM TruckTelemetryEntity t")
    long countAllTelemetry();
}
