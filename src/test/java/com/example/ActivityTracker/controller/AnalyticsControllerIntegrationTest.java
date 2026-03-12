package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@Transactional
class AnalyticsControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EventRepository eventRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        eventRepository.deleteAll();
    }

    @Test
    void getEventTypes_shouldReturnCountsByType() throws Exception {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        createEvent("user-1", "login", from.plusSeconds(100));
        createEvent("user-2", "login", from.plusSeconds(200));
        createEvent("user-1", "click", from.plusSeconds(300));
        createEvent("user-3", "click", from.plusSeconds(400));
        createEvent("user-3", "click", from.plusSeconds(500));
        createEvent("user-4", "purchase", from.plusSeconds(600));

        mockMvc.perform(get("/api/analytics/event-types")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("click"))
                .andExpect(jsonPath("$[0].count").value(3))
                .andExpect(jsonPath("$[1].eventType").value("login"))
                .andExpect(jsonPath("$[1].count").value(2))
                .andExpect(jsonPath("$[2].eventType").value("purchase"))
                .andExpect(jsonPath("$[2].count").value(1));
    }

    @Test
    void getDau_shouldReturnDistinctUserCount() throws Exception {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        createEvent("user-1", "login", from.plusSeconds(100));
        createEvent("user-1", "click", from.plusSeconds(200));
        createEvent("user-2", "login", from.plusSeconds(300));
        createEvent("user-3", "click", from.plusSeconds(400));

        mockMvc.perform(get("/api/analytics/dau")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dau").value(3))
                .andExpect(jsonPath("$.from").exists())
                .andExpect(jsonPath("$.to").exists());
    }

    @Test
    void getDau_shouldReturnZeroWhenNoEvents() throws Exception {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        mockMvc.perform(get("/api/analytics/dau")
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dau").value(0));
    }

    @Test
    void getDau_shouldReturnBadRequestForInvalidRange() throws Exception {
        mockMvc.perform(get("/api/analytics/dau")
                        .param("from", "2024-01-02T00:00:00Z")
                        .param("to", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("from must be before to"));
    }

    private void createEvent(String userId, String eventType, Instant eventTime) {
        Event event = new Event();
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setEventTime(eventTime);
        eventRepository.save(event);
    }
}
