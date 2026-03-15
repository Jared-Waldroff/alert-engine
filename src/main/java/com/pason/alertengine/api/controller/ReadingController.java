package com.pason.alertengine.api.controller;

import com.pason.alertengine.api.dto.AlertResponse;
import com.pason.alertengine.api.dto.ReadingResponse;
import com.pason.alertengine.api.dto.SensorReadingRequest;
import com.pason.alertengine.domain.model.Alert;
import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.engine.AlertEngine;
import com.pason.alertengine.persistence.entity.SensorReadingEntity;
import com.pason.alertengine.persistence.repository.SensorReadingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for submitting and querying sensor readings.
 *
 * <p>Readings submitted via POST are immediately evaluated by the alert engine.
 * The response includes any alerts that were triggered by the reading.</p>
 */
@RestController
@RequestMapping("/api/readings")
@Tag(name = "Sensor Readings", description = "Submit readings and view recent sensor data")
public class ReadingController {

  private static final Logger log = LoggerFactory.getLogger(ReadingController.class);

  private final AlertEngine alertEngine;
  private final SensorReadingRepository readingRepository;

  /**
   * Creates a new reading controller.
   *
   * @param alertEngine       the engine that evaluates readings against rules
   * @param readingRepository the repository for persisting and querying readings
   */
  public ReadingController(AlertEngine alertEngine,
      SensorReadingRepository readingRepository) {
    this.alertEngine = alertEngine;
    this.readingRepository = readingRepository;
  }

  /**
   * Submits a sensor reading to the engine for evaluation.
   *
   * @param request the sensor reading data
   * @return list of alerts triggered by this reading (may be empty)
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Submit a sensor reading",
      description = "The reading is evaluated against all active rules. "
          + "Returns any triggered alerts.")
  public List<AlertResponse> submitReading(@Valid @RequestBody SensorReadingRequest request) {
    SensorReading reading = SensorReading.of(
        request.sensorId(), request.sensorType(),
        request.value(), request.unit());

    // Persist for dashboard display
    persistReading(reading);

    // Evaluate against alert rules
    List<Alert> alerts = alertEngine.processReading(reading);

    return alerts.stream()
        .map(this::toAlertResponse)
        .toList();
  }

  /**
   * Returns the most recent sensor readings.
   *
   * @param limit maximum number of readings to return (default 50)
   * @return recent readings ordered by timestamp descending
   */
  @GetMapping
  @Operation(summary = "Get recent sensor readings",
      description = "Returns sensor readings ordered by timestamp descending. "
          + "Use the 'limit' query parameter to control the page size (default 50). "
          + "Each entry includes sensor ID, type, value, unit, and timestamp.")
  public List<ReadingResponse> getRecentReadings(
      @RequestParam(defaultValue = "50") int limit) {
    return readingRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit))
        .stream()
        .map(e -> new ReadingResponse(
            e.getSensorId(), e.getSensorType(),
            e.getValue(), e.getUnit(), e.getTimestamp()))
        .toList();
  }

  private void persistReading(SensorReading reading) {
    try {
      SensorReadingEntity entity = new SensorReadingEntity();
      entity.setSensorId(reading.getSensorId());
      entity.setSensorType(reading.getSensorType());
      entity.setValue(reading.getValue());
      entity.setUnit(reading.getUnit());
      entity.setTimestamp(reading.getTimestamp());
      readingRepository.save(entity);
    } catch (Exception e) {
      // Reading persistence is non-critical — log and continue
      log.warn("Failed to persist reading: {}", e.getMessage());
    }
  }

  private AlertResponse toAlertResponse(Alert alert) {
    return new AlertResponse(
        alert.getId(), alert.getRuleName(), alert.getSeverity(),
        alert.getSensorId(), alert.getSensorType(),
        alert.getTriggerValue(), alert.getThreshold(),
        alert.getConditionDescription(), alert.getMessage(),
        alert.getTimestamp());
  }
}
