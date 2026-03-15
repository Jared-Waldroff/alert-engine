package com.pason.alertengine.domain.dispatch;

import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.persistence.entity.AlertEntity;
import com.pason.alertengine.persistence.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Dispatches alerts by persisting them to the database.
 *
 * <p>This dispatcher stores every triggered alert in the {@code alerts} table,
 * enabling historical analysis and display on the web dashboard. Alert history
 * is queryable by severity, sensor type, and time range.</p>
 *
 * <p>If the database write fails, the error is logged but does not prevent
 * other dispatchers from receiving the alert. The console dispatcher
 * (ordered first) ensures alerts are always visible even if persistence fails.</p>
 */
@Component
@Order(2)
public class DatabaseAlertDispatcher implements AlertDispatcher {

  private static final Logger log = LoggerFactory.getLogger(DatabaseAlertDispatcher.class);

  private final AlertRepository alertRepository;

  /**
   * Creates a new database alert dispatcher.
   *
   * @param alertRepository the repository used to persist alert records
   */
  public DatabaseAlertDispatcher(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  /**
   * Persists the alert to the database.
   *
   * @param alert the alert to persist
   */
  @Override
  public void dispatch(Alert alert) {
    try {
      AlertEntity entity = new AlertEntity();
      entity.setAlertId(alert.getId());
      entity.setRuleName(alert.getRuleName());
      entity.setSeverity(alert.getSeverity());
      entity.setSensorId(alert.getSensorId());
      entity.setSensorType(alert.getSensorType());
      entity.setTriggerValue(alert.getTriggerValue());
      entity.setThreshold(alert.getThreshold());
      entity.setConditionDescription(alert.getConditionDescription());
      entity.setMessage(alert.getMessage());
      entity.setTimestamp(alert.getTimestamp());

      alertRepository.save(entity);
      log.debug("Alert persisted: id={}, rule={}", alert.getId(), alert.getRuleName());
    } catch (Exception e) {
      // Log but don't rethrow — other dispatchers should still receive the alert
      log.error("Failed to persist alert: id={}, rule={}, error={}",
          alert.getId(), alert.getRuleName(), e.getMessage());
    }
  }

  /** {@inheritDoc} Returns {@code "Database"}. */
  @Override
  public String getName() {
    return "Database";
  }
}
