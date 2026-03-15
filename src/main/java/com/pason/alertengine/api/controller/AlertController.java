package com.pason.alertengine.api.controller;

import com.pason.alertengine.api.dto.AlertResponse;
import com.pason.alertengine.persistence.entity.AlertEntity;
import com.pason.alertengine.persistence.repository.AlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for querying triggered alert history.
 *
 * <p>Provides read-only access to the history of alerts that have been
 * triggered by the engine. Used by the dashboard to display alert feeds.</p>
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Query triggered alert history")
public class AlertController {

  private final AlertRepository alertRepository;

  /**
   * Creates a new alert controller.
   *
   * @param alertRepository the repository for querying persisted alert records
   */
  public AlertController(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  /**
   * Returns the most recent triggered alerts.
   *
   * @param limit maximum number of alerts to return (default 50)
   * @return recent alerts ordered by timestamp descending
   */
  @GetMapping
  @Operation(summary = "Get recent alerts",
      description = "Returns the most recent triggered alerts.")
  public List<AlertResponse> getRecentAlerts(
      @RequestParam(defaultValue = "50") int limit) {
    return alertRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit))
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private AlertResponse toResponse(AlertEntity entity) {
    return new AlertResponse(
        entity.getAlertId(),
        entity.getRuleName(),
        entity.getSeverity(),
        entity.getSensorId(),
        entity.getSensorType(),
        entity.getTriggerValue(),
        entity.getThreshold(),
        entity.getConditionDescription(),
        entity.getMessage(),
        entity.getTimestamp());
  }
}
