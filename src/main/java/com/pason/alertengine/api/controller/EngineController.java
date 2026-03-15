package com.pason.alertengine.api.controller;

import com.pason.alertengine.api.dto.EngineStatusResponse;
import com.pason.alertengine.engine.AlertEngine;
import com.pason.alertengine.simulator.DrillingSensorSimulator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for querying engine status and metrics.
 */
@RestController
@RequestMapping("/api/engine")
@Tag(name = "Engine", description = "Engine status and metrics")
public class EngineController {

  private final AlertEngine alertEngine;
  private final DrillingSensorSimulator simulator;

  /**
   * Creates a new engine controller.
   *
   * @param alertEngine the alert engine providing status and metrics
   * @param simulator   the sensor simulator for reporting simulator state
   */
  public EngineController(AlertEngine alertEngine, DrillingSensorSimulator simulator) {
    this.alertEngine = alertEngine;
    this.simulator = simulator;
  }

  /**
   * Returns the engine's current status and processing metrics.
   */
  @GetMapping("/status")
  @Operation(summary = "Get engine status and metrics",
      description = "Returns the engine's current processing metrics including total "
          + "readings processed, total alerts triggered, number of active rules, "
          + "uptime in seconds, and whether the simulator is running.")
  public EngineStatusResponse getStatus() {
    return new EngineStatusResponse(
        alertEngine.getReadingsProcessed(),
        alertEngine.getAlertsTriggered(),
        alertEngine.getActiveRules().size(),
        alertEngine.getUptime().getSeconds(),
        simulator.isRunning());
  }
}
