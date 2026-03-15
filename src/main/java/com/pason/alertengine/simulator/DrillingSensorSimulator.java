package com.pason.alertengine.simulator;

import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import com.pason.alertengine.engine.AlertEngine;
import com.pason.alertengine.persistence.entity.SensorReadingEntity;
import com.pason.alertengine.persistence.repository.SensorReadingRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Generates realistic drilling sensor data for demonstrating the alert engine.
 *
 * <p>Simulates 6 sensor instances across 5 sensor types that mirror real drilling
 * rig instrumentation. Each sensor has a baseline value with random noise and
 * periodic anomaly events that trigger the alert rules.</p>
 *
 * <p>Anomaly events are designed to exercise all four condition types:</p>
 * <ul>
 *   <li>Pressure spikes (PRESSURE-001) — ThresholdExceeded condition</li>
 *   <li>Rapid pressure jumps (PRESSURE-002) — RateOfChange condition</li>
 *   <li>Temperature drift — SustainedThreshold condition</li>
 *   <li>Gas level spikes — ThresholdExceeded condition</li>
 *   <li>Flow rate drops — OutOfRange condition</li>
 * </ul>
 *
 * <p>Usage: Start via REST endpoint {@code POST /api/simulator/start}
 * or programmatically via {@link #start()}.</p>
 *
 * @see SensorProfile for the configuration of each sensor's behavior
 */
@Component
public class DrillingSensorSimulator {

  private static final Logger log = LoggerFactory.getLogger(DrillingSensorSimulator.class);
  private static final long READING_INTERVAL_MS = 2000;

  private final AlertEngine engine;
  private final SensorReadingRepository readingRepository;
  private final List<SensorProfile> sensorProfiles;
  private final Random random;
  private final AtomicLong readingsGenerated;

  private ScheduledExecutorService scheduler;
  private volatile boolean running = false;

  // Track sustained anomaly state for temperature simulation
  private int temperatureAnomalyCounter = 0;

  /**
   * Creates the simulator with the alert engine and reading persistence.
   *
   * @param engine            the alert engine to process generated readings
   * @param readingRepository repository for persisting readings to the database
   */
  public DrillingSensorSimulator(AlertEngine engine,
      SensorReadingRepository readingRepository) {
    this.engine = engine;
    this.readingRepository = readingRepository;
    this.random = new Random();
    this.readingsGenerated = new AtomicLong(0);
    this.sensorProfiles = createDefaultProfiles();
  }

  /**
   * Starts the simulator, generating readings at a fixed interval.
   *
   * @throws IllegalStateException if the simulator is already running
   */
  public synchronized void start() {
    if (running) {
      throw new IllegalStateException("Simulator is already running");
    }

    scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "sensor-simulator");
      t.setDaemon(true);
      return t;
    });

    scheduler.scheduleAtFixedRate(
        this::generateAllReadings,
        0,
        READING_INTERVAL_MS,
        TimeUnit.MILLISECONDS);

    running = true;
    log.info("Sensor simulator started — generating readings every {}ms", READING_INTERVAL_MS);
  }

  /**
   * Stops the simulator gracefully.
   */
  public synchronized void stop() {
    if (!running) {
      return;
    }

    running = false;
    if (scheduler != null) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
    log.info("Sensor simulator stopped. Total readings generated: {}", readingsGenerated.get());
  }

  /** Returns true if the simulator is currently running. */
  public boolean isRunning() {
    return running;
  }

  /** Returns the total number of readings generated since last start. */
  public long getReadingsGenerated() {
    return readingsGenerated.get();
  }

  private void generateAllReadings() {
    for (SensorProfile profile : sensorProfiles) {
      try {
        SensorReading reading = generateReading(profile);
        persistReading(reading);
        engine.processReading(reading);
        readingsGenerated.incrementAndGet();
      } catch (Exception e) {
        log.error("Error generating reading for sensor {}: {}",
            profile.sensorId(), e.getMessage());
      }
    }
  }

  /**
   * Generates a single reading for a sensor profile.
   *
   * <p>Normal operation: baseline + Gaussian noise within the noise range.
   * Anomaly: uses the anomaly value with some noise, triggered probabilistically.</p>
   */
  private SensorReading generateReading(SensorProfile profile) {
    double value;
    boolean isAnomaly = random.nextDouble() < profile.anomalyProbability();

    // Special handling for temperature: sustain anomalies across multiple readings
    // to trigger the SustainedThresholdCondition
    if (profile.sensorType() == SensorType.TEMPERATURE) {
      if (isAnomaly) {
        temperatureAnomalyCounter = 20; // Sustain for ~40 seconds (20 readings * 2s)
      }
      if (temperatureAnomalyCounter > 0) {
        value = profile.anomalyValue() + (random.nextGaussian() * 5);
        temperatureAnomalyCounter--;
      } else {
        value = profile.baseline() + (random.nextGaussian() * profile.noiseRange());
      }
    } else if (isAnomaly) {
      // Other sensors: single-reading anomaly with slight noise
      value = profile.anomalyValue() + (random.nextGaussian() * profile.noiseRange() * 0.5);
    } else {
      value = profile.baseline() + (random.nextGaussian() * profile.noiseRange());
    }

    // Clamp to reasonable bounds (no negative values for physical quantities)
    value = Math.max(0, value);

    return SensorReading.of(
        profile.sensorId(),
        profile.sensorType(),
        Math.round(value * 10.0) / 10.0, // Round to 1 decimal
        profile.unit());
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
      log.debug("Failed to persist simulated reading: {}", e.getMessage());
    }
  }

  private List<SensorProfile> createDefaultProfiles() {
    return List.of(
        // Pressure (primary): baseline 2500 PSI, spikes to 3600+ (triggers ThresholdExceeded)
        new SensorProfile("PRESSURE-001", SensorType.PRESSURE,
            2500, 100, "PSI", 3600, 0.08),

        // Pressure (secondary): baseline 2400 PSI, rapid spike to 3200
        // triggers RateOfChange — the sudden jump from ~2400 to ~3200 in one interval
        // produces a rate exceeding 100 PSI/sec
        new SensorProfile("PRESSURE-002", SensorType.PRESSURE,
            2400, 50, "PSI", 3200, 0.06),

        // Temperature: baseline 200°F, drifts to 280+ (triggers SustainedThreshold)
        new SensorProfile("TEMP-001", SensorType.TEMPERATURE,
            200, 15, "°F", 280, 0.03),

        // Flow rate: baseline 450 GPM, drops to 180 (triggers OutOfRange)
        new SensorProfile("FLOW-001", SensorType.FLOW_RATE,
            450, 30, "GPM", 180, 0.06),

        // Gas level: baseline 5%, spikes to 25+ (triggers ThresholdExceeded)
        new SensorProfile("GAS-001", SensorType.GAS_LEVEL,
            5, 2, "%LEL", 25, 0.05),

        // Rotary speed: baseline 100 RPM, drops to 20 (triggers OutOfRange)
        new SensorProfile("RPM-001", SensorType.ROTARY_SPEED,
            100, 10, "RPM", 20, 0.04)
    );
  }
}
