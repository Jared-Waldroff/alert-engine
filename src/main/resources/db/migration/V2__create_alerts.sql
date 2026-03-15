-- V2: Create alerts table
-- Stores the history of triggered alerts for dashboard display and analysis.

CREATE TABLE alerts (
  id                    BIGINT       NOT NULL AUTO_INCREMENT,
  alert_id              VARCHAR(36)  NOT NULL UNIQUE,
  rule_name             VARCHAR(255) NOT NULL,
  severity              VARCHAR(20)  NOT NULL,
  sensor_id             VARCHAR(100) NOT NULL,
  sensor_type           VARCHAR(50)  NOT NULL,
  trigger_value         DOUBLE       NOT NULL,
  threshold             DOUBLE       NOT NULL,
  condition_description VARCHAR(255),
  message               VARCHAR(500) NOT NULL,
  alert_timestamp       TIMESTAMP(3) NOT NULL,
  created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_alerts_severity (severity),
  INDEX idx_alerts_timestamp (alert_timestamp DESC),
  INDEX idx_alerts_sensor (sensor_id, alert_timestamp DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
