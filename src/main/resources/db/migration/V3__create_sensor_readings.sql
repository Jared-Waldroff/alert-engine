-- V3: Create sensor_readings table
-- Stores recent sensor readings for the dashboard's live feed display.

CREATE TABLE sensor_readings (
  id                BIGINT       NOT NULL AUTO_INCREMENT,
  sensor_id         VARCHAR(100) NOT NULL,
  sensor_type       VARCHAR(50)  NOT NULL,
  value             DOUBLE       NOT NULL,
  unit              VARCHAR(20)  NOT NULL,
  reading_timestamp TIMESTAMP(3) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_readings_sensor_time (sensor_id, reading_timestamp DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
