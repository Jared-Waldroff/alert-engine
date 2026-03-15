-- V4: Seed default alert rules
-- These rules demonstrate all four condition types using realistic drilling rig thresholds.

-- ThresholdExceeded: Critical pressure spike
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Critical Pressure Spike', 'PRESSURE', 'THRESHOLD_EXCEEDED',
 '{"threshold": 3500, "operator": "GREATER_THAN"}', 'CRITICAL');

-- ThresholdExceeded: Low flow rate warning
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Low Flow Rate', 'FLOW_RATE', 'THRESHOLD_EXCEEDED',
 '{"threshold": 250, "operator": "LESS_THAN"}', 'WARNING');

-- ThresholdExceeded: High gas level critical
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('High Gas Level', 'GAS_LEVEL', 'THRESHOLD_EXCEEDED',
 '{"threshold": 20, "operator": "GREATER_THAN"}', 'CRITICAL');

-- RateOfChange: Rapid pressure change
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Rapid Pressure Change', 'PRESSURE', 'RATE_OF_CHANGE',
 '{"maxRatePerSecond": 100}', 'WARNING');

-- SustainedThreshold: Sustained high temperature
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Sustained High Temperature', 'TEMPERATURE', 'SUSTAINED_THRESHOLD',
 '{"threshold": 275, "operator": "GREATER_THAN", "sustainedSeconds": 30}', 'WARNING');

-- OutOfRange: Flow rate out of operating range
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Flow Rate Out of Range', 'FLOW_RATE', 'OUT_OF_RANGE',
 '{"min": 200, "max": 700}', 'CRITICAL');

-- OutOfRange: Rotary speed out of operating range
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('Rotary Speed Out of Range', 'ROTARY_SPEED', 'OUT_OF_RANGE',
 '{"min": 30, "max": 180}', 'WARNING');

-- ThresholdExceeded: High pressure warning (lower threshold than critical)
INSERT INTO alert_rules (name, sensor_type, condition_type, condition_config, severity) VALUES
('High Pressure Warning', 'PRESSURE', 'THRESHOLD_EXCEEDED',
 '{"threshold": 3000, "operator": "GREATER_THAN"}', 'WARNING');
