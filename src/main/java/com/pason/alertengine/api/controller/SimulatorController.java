package com.pason.alertengine.api.controller;

import com.pason.alertengine.simulator.DrillingSensorSimulator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for controlling the drilling sensor simulator.
 *
 * <p>The simulator generates realistic sensor data that feeds into
 * the alert engine, demonstrating all four condition types in action.</p>
 */
@RestController
@RequestMapping("/api/simulator")
@Tag(name = "Simulator", description = "Control the drilling sensor data simulator")
public class SimulatorController {

  private final DrillingSensorSimulator simulator;

  /**
   * Creates a new simulator controller.
   *
   * @param simulator the drilling sensor data simulator
   */
  public SimulatorController(DrillingSensorSimulator simulator) {
    this.simulator = simulator;
  }

  /**
   * Starts the sensor data simulator.
   */
  @PostMapping("/start")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Start the sensor simulator",
      description = "Begins generating sensor readings every 2 seconds.")
  public Map<String, String> start() {
    simulator.start();
    return Map.of("status", "started", "message",
        "Simulator is now generating sensor readings every 2 seconds.");
  }

  /**
   * Stops the sensor data simulator.
   */
  @PostMapping("/stop")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Stop the sensor simulator",
      description = "Halts the background thread that generates sensor readings. "
          + "Returns a confirmation message along with the total number of readings "
          + "generated during this simulation run. Safe to call when already stopped.")
  public Map<String, String> stop() {
    simulator.stop();
    return Map.of("status", "stopped", "message",
        "Simulator stopped. Total readings: " + simulator.getReadingsGenerated());
  }

  /**
   * Returns the simulator's current status.
   */
  @GetMapping("/status")
  @Operation(summary = "Get simulator status",
      description = "Returns whether the simulator is currently running and the total "
          + "number of sensor readings generated since the last start.")
  public Map<String, Object> getStatus() {
    return Map.of(
        "running", simulator.isRunning(),
        "readingsGenerated", simulator.getReadingsGenerated());
  }
}
