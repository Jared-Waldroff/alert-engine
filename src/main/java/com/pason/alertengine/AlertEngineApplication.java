package com.pason.alertengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Pason Threshold Alert Engine.
 *
 * <p>This application monitors real-time sensor data from drilling rigs,
 * evaluates configurable alert rules against incoming readings, and dispatches
 * alerts when thresholds are breached.</p>
 *
 * <p>Key architectural patterns:</p>
 * <ul>
 *   <li>Strategy pattern for alert conditions (pluggable evaluation logic)</li>
 *   <li>Observer pattern for alert dispatchers (pluggable delivery channels)</li>
 *   <li>Immutable domain model for thread-safe concurrent processing</li>
 * </ul>
 */
@SpringBootApplication
public class AlertEngineApplication {

  /**
   * Application entry point that starts the Spring Boot context.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(AlertEngineApplication.class, args);
  }
}
