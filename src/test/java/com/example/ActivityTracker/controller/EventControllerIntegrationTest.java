package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
@Transactional
public class EventControllerIntegrationTest {
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
    void createEvent_shouldCreateAndReturnEvent() throws Exception {
        EventRequestDto requestDto = new EventRequestDto();
        requestDto.setUserId("user-123");
        requestDto.setEventType("login");
        requestDto.setMetadata("{\"source\":\"web\"}");

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.eventType").value("login"))
                .andExpect(jsonPath("$.metadata").value("{\"source\":\"web\"}"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventTime").exists());

        assert eventRepository.count() == 1;
        Event savedEvent = eventRepository.findAll().get(0);
        assert savedEvent.getUserId().equals("user-123");
        assert savedEvent.getEventType().equals("login");
    }

    @Test
    void getEvent_shouldReturnEventById() throws Exception {
        Event event = new Event();
        event.setUserId("user-456");
        event.setEventType("click");
        event.setEventTime(Instant.now());
        Event saved = eventRepository.save(event);

        mockMvc.perform(get("/api/events/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.userId").value("user-456"))
                .andExpect(jsonPath("$.eventType").value("click"));
    }

    @Test
    void getEvent_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEvents_shouldFilterByUserId() throws Exception {
        Event event1 = new Event();
        event1.setUserId("user-1");
        event1.setEventType("login");
        event1.setEventTime(Instant.now());
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setUserId("user-2");
        event2.setEventType("login");
        event2.setEventTime(Instant.now());
        eventRepository.save(event2);

        mockMvc.perform(get("/api/events")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value("user-1"));
    }

    @Test
    void getEvents_shouldCombineFilters() throws Exception {
        Event matchingEvent = new Event();
        matchingEvent.setUserId("user-filter");
        matchingEvent.setEventType("login");
        matchingEvent.setEventTime(Instant.parse("2024-01-10T12:00:00Z"));
        eventRepository.save(matchingEvent);

        Event differentType = new Event();
        differentType.setUserId("user-filter");
        differentType.setEventType("click");
        differentType.setEventTime(Instant.parse("2024-01-10T13:00:00Z"));
        eventRepository.save(differentType);

        Event differentUser = new Event();
        differentUser.setUserId("user-other");
        differentUser.setEventType("login");
        differentUser.setEventTime(Instant.parse("2024-01-10T14:00:00Z"));
        eventRepository.save(differentUser);

        mockMvc.perform(get("/api/events")
                        .param("userId", "user-filter")
                        .param("eventType", "login")
                        .param("from", "2024-01-10T00:00:00Z")
                        .param("to", "2024-01-11T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value("user-filter"))
                .andExpect(jsonPath("$.content[0].eventType").value("login"));
    }

    @Test
    void updateEvent_shouldUpdateExistingEvent() throws Exception {
        Event event = new Event();
        event.setUserId("user-789");
        event.setEventType("old-type");
        event.setEventTime(Instant.now());
        Event saved = eventRepository.save(event);

        EventRequestDto updateDto = new EventRequestDto();
        updateDto.setUserId("user-789");
        updateDto.setEventType("new-type");
        updateDto.setMetadata("updated");

        mockMvc.perform(put("/api/events/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("new-type"))
                .andExpect(jsonPath("$.metadata").value("updated"));

        Event updated = eventRepository.findById(saved.getId()).orElseThrow();
        assert updated.getEventType().equals("new-type");
        assert updated.getMetadata().equals("updated");
    }

    @Test
    void deleteEvent_shouldDeleteEvent() throws Exception {
        Event event = new Event();
        event.setUserId("user-delete");
        event.setEventType("delete-me");
        event.setEventTime(Instant.now());
        Event saved = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assert eventRepository.findById(saved.getId()).isEmpty();
    }
}
