/*
 * config/OpenApiIntegrationTest.java
 * Integration test verifying OpenAPI 3.0 specs and Swagger UI endpoints.
 * Connects to: config/OpenApiConfig.java, Springdoc OpenAPI
 * Created: 2026-08-08
 */
package com.employee.directory.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs - Return valid OpenAPI 3.0 JSON specification")
    void getApiDocs_ReturnsOpenApiJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", containsString("3.0")))
                .andExpect(jsonPath("$.info.title").value("Employee Directory API"))
                .andExpect(jsonPath("$.info.version", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/employees']").exists());
    }

    @Test
    @DisplayName("GET /swagger-ui.html - Redirect to Swagger UI HTML page")
    void getSwaggerUi_Returns302Redirect() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
    }
}
