-- V1: Create alert_rules table
-- Stores configurable alert rules with condition parameters serialized as JSON.
-- Condition config allows different condition types to have different parameters
-- without requiring schema changes.

CREATE TABLE alert_rules (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  name           VARCHAR(255) NOT NULL,
  sensor_type    VARCHAR(50)  NOT NULL,
  condition_type VARCHAR(50)  NOT NULL,
  condition_config JSON       NOT NULL,
  severity       VARCHAR(20)  NOT NULL,
  enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_rules_sensor_type (sensor_type),
  INDEX idx_rules_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
