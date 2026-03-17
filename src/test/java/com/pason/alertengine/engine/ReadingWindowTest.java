package com.pason.alertengine.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.pason.alertengine.domain.model.SensorReading;
import com.pason.alertengine.domain.model.SensorType;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReadingWindowTest {

  @Test
  void addReading_multipleReadings_returnsInOrder() {
    // Arrange
    var window = new ReadingWindow();
    var now = Instant.now();

    // Act
    window.addReading(new SensorReading(
        "s1", SensorType.PRESSURE, 100, "PSI", now.minusSeconds(2)));
    window.addReading(new SensorReading(
        "s1", SensorType.PRESSURE, 200, "PSI", now.minusSeconds(1)));

    // Assert
    var readings = window.getRecentReadings("s1");
    assertThat(readings).hasSize(2);
    assertThat(readings.get(0).getValue()).isEqualTo(100);
    assertThat(readings.get(1).getValue()).isEqualTo(200);
  }

  @Test
  void getRecentReadings_unknownSensor_returnsEmpty() {
    // Arrange
    var window = new ReadingWindow();

    // Act & Assert
    assertThat(window.getRecentReadings("unknown")).isEmpty();
  }

  @Test
  void addReading_readingOlderThanWindow_evictsExpired() {
    // Arrange
    var window = new ReadingWindow(Duration.ofSeconds(10), 100);
    var now = Instant.now();

    // Act
    window.addReading(new SensorReading(
        "s1", SensorType.PRESSURE, 100, "PSI", now.minusSeconds(20)));
    window.addReading(new SensorReading("s1", SensorType.PRESSURE, 200, "PSI", now));

    // Assert
    assertThat(window.getRecentReadings("s1")).hasSize(1);
    assertThat(window.getRecentReadings("s1").get(0).getValue()).isEqualTo(200);
  }

  @Test
  void addReading_exceedsMaxSize_evictsOldest() {
    // Arrange
    var window = new ReadingWindow(Duration.ofMinutes(60), 3);
    var now = Instant.now();

    // Act
    for (int i = 0; i < 5; i++) {
      window.addReading(new SensorReading("s1", SensorType.PRESSURE,
          100 + i, "PSI", now.plusSeconds(i)));
    }

    // Assert
    assertThat(window.getReadingCount("s1")).isEqualTo(3);
  }
}
