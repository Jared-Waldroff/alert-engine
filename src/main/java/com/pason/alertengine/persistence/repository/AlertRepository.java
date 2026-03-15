package com.pason.alertengine.persistence.repository;

import com.pason.alertengine.domain.model.AlertSeverity;
import com.pason.alertengine.persistence.entity.AlertEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing persisted alert records.
 */
@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

  /** Finds the most recent alerts, ordered by timestamp descending. */
  List<AlertEntity> findAllByOrderByTimestampDesc(Pageable pageable);

  /** Finds alerts by severity, ordered by timestamp descending. */
  List<AlertEntity> findBySeverityOrderByTimestampDesc(AlertSeverity severity, Pageable pageable);

  /** Finds alerts within a time range. */
  List<AlertEntity> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);

  /** Counts alerts by severity. */
  long countBySeverity(AlertSeverity severity);
}
