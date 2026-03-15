package com.pason.alertengine.persistence.repository;

import com.pason.alertengine.domain.model.SensorType;
import com.pason.alertengine.persistence.entity.AlertRuleEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing persisted alert rules.
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, Long> {

  /** Finds all enabled rules. */
  List<AlertRuleEntity> findByEnabledTrue();

  /** Finds all rules for a specific sensor type. */
  List<AlertRuleEntity> findBySensorType(SensorType sensorType);

  /** Finds enabled rules for a specific sensor type. */
  List<AlertRuleEntity> findBySensorTypeAndEnabledTrue(SensorType sensorType);
}
