package com.example.ActivityTracker.controller;

import com.example.ActivityTracker.dto.EventRequestDto;
import com.example.ActivityTracker.model.Event;
import com.example.ActivityTracker.model.Project;
import com.example.ActivityTracker.repository.EventRepository;
import com.example.ActivityTracker.repository.ProjectRepository;
import com.example.ActivityTracker.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers(disabledWithoutDocker = true)
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Project project;
    private String apiKey;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        eventRepository.deleteAll();
        projectRepository.deleteAll();
        project = new Project();
        project.setName("Test Project");
        project.setSlug("test-project");
        project = projectRepository.save(project);
        apiKey = apiKeyService.create(project.getId(), request("test-key")).secret();
    }

    @Test
    void createEvent_shouldCreateAndReturnEvent() throws Exception {
        EventRequestDto requestDto = new EventRequestDto();
        requestDto.setEventId("evt-create-1");
        requestDto.setUserId("user-123");
        requestDto.setEventType("login");
        requestDto.setMetadata("{\"source\":\"web\"}");

        mockMvc.perform(post("/api/events")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.userId").value("user-123"))
                .andExpect(jsonPath("$.event.type").value("login"))
                .andExpect(jsonPath("$.event.metadata.source").value("web"))
                .andExpect(jsonPath("$.event.id").exists())
                .andExpect(jsonPath("$.event.occurredAt").exists());

        assert eventRepository.count() == 1;
        Event savedEvent = eventRepository.findAll().get(0);
        assert savedEvent.getUserId().equals("user-123");
        assert savedEvent.getEventType().equals("login");
    }

    @Test
    void getEvent_shouldReturnEventById() throws Exception {
        Event event = new Event();
        event.setProject(project);
        event.setEventId("evt-get-1");
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
        event1.setProject(project);
        event1.setEventId("evt-filter-1");
        event1.setUserId("user-1");
        event1.setEventType("login");
        event1.setEventTime(Instant.now());
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setProject(project);
        event2.setEventId("evt-filter-2");
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
        matchingEvent.setProject(project);
        matchingEvent.setEventId("evt-combined-1");
        matchingEvent.setUserId("user-filter");
        matchingEvent.setEventType("login");
        matchingEvent.setEventTime(Instant.parse("2024-01-10T12:00:00Z"));
        eventRepository.save(matchingEvent);

        Event differentType = new Event();
        differentType.setProject(project);
        differentType.setEventId("evt-combined-2");
        differentType.setUserId("user-filter");
        differentType.setEventType("click");
        differentType.setEventTime(Instant.parse("2024-01-10T13:00:00Z"));
        eventRepository.save(differentType);

        Event differentUser = new Event();
        differentUser.setProject(project);
        differentUser.setEventId("evt-combined-3");
        differentUser.setUserId("user-other");
        differentUser.setEventType("login");
        differentUser.setEventTime(Instant.parse("2024-01-10T14:00:00Z"));
        eventRepository.save(differentUser);

        mockMvc.perform(get("/api/events")
                        .param("userId", "user-filter")
                        .param("eventType", "login")
                        .param("projectId", project.getId().toString())
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
        event.setProject(project);
        event.setEventId("evt-update-1");
        event.setUserId("user-789");
        event.setEventType("old-type");
        event.setEventTime(Instant.now());
        Event saved = eventRepository.save(event);

        EventRequestDto updateDto = new EventRequestDto();
        updateDto.setEventId("evt-update-1");
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
        assert updated.getMetadata().asText().equals("updated");
    }

    @Test
    void deleteEvent_shouldDeleteEvent() throws Exception {
        Event event = new Event();
        event.setProject(project);
        event.setEventId("evt-delete-1");
        event.setUserId("user-delete");
        event.setEventType("delete-me");
        event.setEventTime(Instant.now());
        Event saved = eventRepository.save(event);

        mockMvc.perform(delete("/api/events/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assert eventRepository.findById(saved.getId()).isEmpty();
    }

    private com.example.ActivityTracker.dto.ApiKeyCreateRequestDto request(String name) {
        com.example.ActivityTracker.dto.ApiKeyCreateRequestDto request =
                new com.example.ActivityTracker.dto.ApiKeyCreateRequestDto();
        request.setName(name);
        return request;
    }
}
