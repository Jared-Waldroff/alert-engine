package com.pason.alertengine.domain.dispatch;

import com.pason.alertengine.domain.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Dispatches alerts by writing them to a dedicated alert log file.
 *
 * <p>Uses a separate SLF4J logger named "AlertLog" which can be configured
 * in the logging framework to write to a dedicated file. This separates
 * alert records from general application logs for easier analysis.</p>
 *
 * <p>Each log entry is a structured line containing all alert fields,
 * suitable for parsing by log aggregation tools.</p>
 */
@Component
@Order(3)
public class LogFileAlertDispatcher implements AlertDispatcher {

  private static final Logger alertLog = LoggerFactory.getLogger("AlertLog");

  /**
   * Writes the alert as a structured log entry to the alert log.
   *
   * @param alert the alert to log
   */
  @Override
  public void dispatch(Alert alert) {
    alertLog.info("id={} severity={} rule=\"{}\" sensor={} sensorType={} "
            + "value={} threshold={} condition=\"{}\" message=\"{}\" timestamp={}",
        alert.getId(),
        alert.getSeverity(),
        alert.getRuleName(),
        alert.getSensorId(),
        alert.getSensorType(),
        alert.getTriggerValue(),
        alert.getThreshold(),
        alert.getConditionDescription(),
        alert.getMessage(),
        alert.getTimestamp());
  }

  /** {@inheritDoc} Returns {@code "LogFile"}. */
  @Override
  public String getName() {
    return "LogFile";
  }
}
