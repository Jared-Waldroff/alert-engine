package com.pason.alertengine.persistence.repository;

import com.pason.alertengine.persistence.entity.SensorReadingEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing persisted sensor readings.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReadingEntity, Long> {

  /** Finds the most recent readings, ordered by timestamp descending. */
  List<SensorReadingEntity> findAllByOrderByTimestampDesc(Pageable pageable);

  /** Finds recent readings for a specific sensor. */
  List<SensorReadingEntity> findBySensorIdOrderByTimestampDesc(String sensorId, Pageable pageable);
}
