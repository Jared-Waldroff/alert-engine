package com.pason.alertengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the Alert Engine API.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Defines the OpenAPI specification bean for the Alert Engine REST API.
   *
   * @return the configured OpenAPI instance with title, description, and contact info
   */
  @Bean
  public OpenAPI alertEngineOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Pason Threshold Alert Engine API")
            .description("Monitors real-time sensor data from drilling rigs, "
                + "evaluates configurable alert rules, and dispatches alerts "
                + "when thresholds are breached.")
            .version("1.0.0")
            .contact(new Contact()
                .name("Jared Waldroff")
                .email("jared@example.com")));
  }
}
