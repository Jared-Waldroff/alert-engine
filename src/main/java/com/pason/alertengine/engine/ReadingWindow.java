package com.pason.alertengine.engine;

import com.pason.alertengine.domain.model.SensorReading;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Maintains a sliding window of recent sensor readings per sensor ID.
 *
 * <p>The window is bounded by both time and size to prevent unbounded memory
 * growth. Readings older than the configured window duration are automatically
 * evicted when new readings are added.</p>
 *
 * <p>Thread safety: this class uses concurrent data structures and is safe
 * for use by multiple threads adding and querying readings simultaneously.
 * This is necessary because multiple sensor streams may feed the engine
 * concurrently.</p>
 *
 * @see AlertEngine
 */
public class ReadingWindow {

  private static final int DEFAULT_MAX_READINGS_PER_SENSOR = 100;
  private static final Duration DEFAULT_WINDOW_DURATION = Duration.ofMinutes(5);

  private final ConcurrentHashMap<String, ConcurrentLinkedDeque<SensorReading>> windows;
  private final int maxReadingsPerSensor;
  private final Duration windowDuration;

  /**
   * Creates a reading window with default settings (5 min window, 100 readings max).
   */
  public ReadingWindow() {
    this(DEFAULT_WINDOW_DURATION, DEFAULT_MAX_READINGS_PER_SENSOR);
  }

  /**
   * Creates a reading window with custom settings.
   *
   * @param windowDuration       maximum age of readings to retain
   * @param maxReadingsPerSensor maximum number of readings to retain per sensor
   */
  public ReadingWindow(Duration windowDuration, int maxReadingsPerSensor) {
    this.windows = new ConcurrentHashMap<>();
    this.windowDuration = windowDuration;
    this.maxReadingsPerSensor = maxReadingsPerSensor;
  }

  /**
   * Adds a reading to the window and evicts stale entries.
   *
   * @param reading the reading to add
   */
  public void addReading(SensorReading reading) {
    ConcurrentLinkedDeque<SensorReading> sensorWindow = windows.computeIfAbsent(
        reading.getSensorId(), k -> new ConcurrentLinkedDeque<>());

    sensorWindow.addLast(reading);
    evictStale(sensorWindow, reading.getTimestamp());
  }

  /**
   * Returns recent readings for a given sensor, ordered oldest-first.
   *
   * <p>The returned list is an immutable snapshot. It does not include
   * the "current" reading that is about to be evaluated — only historical
   * readings that were previously added.</p>
   *
   * @param sensorId the sensor to get history for
   * @return recent readings ordered oldest-first; empty if no history exists
   */
  public List<SensorReading> getRecentReadings(String sensorId) {
    ConcurrentLinkedDeque<SensorReading> sensorWindow = windows.get(sensorId);
    if (sensorWindow == null || sensorWindow.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(new ArrayList<>(sensorWindow));
  }

  /**
   * Returns the number of readings currently stored for a sensor.
   *
   * @param sensorId the sensor to check
   * @return the number of readings in the window
   */
  public int getReadingCount(String sensorId) {
    ConcurrentLinkedDeque<SensorReading> sensorWindow = windows.get(sensorId);
    return sensorWindow != null ? sensorWindow.size() : 0;
  }

  /** Clears all readings from the window. */
  public void clear() {
    windows.clear();
  }

  private void evictStale(ConcurrentLinkedDeque<SensorReading> sensorWindow,
      Instant currentTime) {
    Instant cutoff = currentTime.minus(windowDuration);

    // Evict by time
    while (!sensorWindow.isEmpty()
        && sensorWindow.peekFirst().getTimestamp().isBefore(cutoff)) {
      sensorWindow.pollFirst();
    }

    // Evict by size
    while (sensorWindow.size() > maxReadingsPerSensor) {
      sensorWindow.pollFirst();
    }
  }
}
