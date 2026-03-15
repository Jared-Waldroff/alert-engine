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
class ReadingControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void submitReading_validReading_returnsCreatedWithAlerts() throws Exception {
    // Arrange
    String json = """
        {
          "sensorId": "PRESSURE-001",
          "sensorType": "PRESSURE",
          "value": 2500.0,
          "unit": "PSI"
        }
        """;

    // Act & Assert
    mockMvc.perform(post("/api/readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void submitReading_invalidReading_returnsBadRequest() throws Exception {
    // Arrange
    String json = """
        {
          "sensorId": "",
          "value": 2500.0
        }
        """;

    // Act & Assert
    mockMvc.perform(post("/api/readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getRecentReadings_afterSubmission_returnsReadings() throws Exception {
    // Arrange
    String json = """
        {
          "sensorId": "TEMP-042",
          "sensorType": "TEMPERATURE",
          "value": 210.0,
          "unit": "\u00b0F"
        }
        """;

    mockMvc.perform(post("/api/readings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated());

    // Act & Assert
    mockMvc.perform(get("/api/readings")
            .param("limit", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[?(@.sensorId == 'TEMP-042')]").exists())
        .andExpect(jsonPath("$[?(@.sensorId == 'TEMP-042')].sensorType")
            .value("TEMPERATURE"))
        .andExpect(jsonPath("$[?(@.sensorId == 'TEMP-042')].value").value(210.0));
  }
}
