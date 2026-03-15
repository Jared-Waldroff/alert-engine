package com.pason.alertengine.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertRuleControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getAllRules_afterSeeding_returnsSeededRules() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/rules"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(8));
  }

  @Test
  void createRule_validRequest_returns201() throws Exception {
    // Arrange
    String json = """
        {
          "name": "Test Rule",
          "sensorType": "PRESSURE",
          "conditionType": "THRESHOLD_EXCEEDED",
          "conditionConfig": {"threshold": 4000, "operator": "GREATER_THAN"},
          "severity": "WARNING"
        }
        """;

    // Act & Assert
    mockMvc.perform(post("/api/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Test Rule"))
        .andExpect(jsonPath("$.sensorType").value("PRESSURE"))
        .andExpect(jsonPath("$.severity").value("WARNING"));
  }

  @Test
  void createRule_missingName_returns400() throws Exception {
    // Arrange
    String json = """
        {
          "sensorType": "PRESSURE",
          "conditionType": "THRESHOLD_EXCEEDED",
          "conditionConfig": {"threshold": 4000, "operator": "GREATER_THAN"},
          "severity": "WARNING"
        }
        """;

    // Act & Assert
    mockMvc.perform(post("/api/rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRule_notFound_returns404() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/rules/99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void getEngineStatus_onStartup_returnsMetrics() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/engine/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readingsProcessed").isNumber())
        .andExpect(jsonPath("$.alertsTriggered").isNumber())
        .andExpect(jsonPath("$.activeRules").isNumber())
        .andExpect(jsonPath("$.uptimeSeconds").isNumber())
        .andExpect(jsonPath("$.simulatorRunning").isBoolean());
  }
}
