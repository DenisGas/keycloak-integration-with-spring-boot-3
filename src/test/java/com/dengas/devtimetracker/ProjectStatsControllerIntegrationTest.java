package com.dengas.devtimetracker;

import com.dengas.devtimetracker.dto.LoginRequest;
import com.dengas.devtimetracker.dto.ResponseWrapper;
import com.dengas.devtimetracker.dto.TokenResponse;
import com.dengas.devtimetracker.model.DailyStats;
import com.dengas.devtimetracker.model.FileStats;
import com.dengas.devtimetracker.model.ProjectStats;
import com.dengas.devtimetracker.model.User;
import com.dengas.devtimetracker.repositories.ProjectStatsRepository;
import com.dengas.devtimetracker.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProjectStatsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProjectStatsRepository projectStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String jwtToken;
    private String jwtToken2;
    private String userId;
    private String userId2;
    private User user2;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Очищення бази даних перед кожним тестом
        projectStatsRepository.deleteAll();
        userRepository.deleteAll();

        // Отримання JWT-токена
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("den");
        loginRequest.setPassword("2004");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<ResponseWrapper> response = restTemplate.postForEntity(
                "/api/v1/auth/token", request, ResponseWrapper.class);

        TokenResponse tokenResponse = objectMapper.convertValue(
                response.getBody().getData(), TokenResponse.class);
        jwtToken = "Bearer " + tokenResponse.getAccess_token();


        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setUsername("vasya");
        loginRequest2.setPassword("3004");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request2 = new HttpEntity<>(loginRequest2, headers2);

        ResponseEntity<ResponseWrapper> response2 = restTemplate.postForEntity(
                "/api/v1/auth/token", request2, ResponseWrapper.class);

        TokenResponse tokenResponse2 = objectMapper.convertValue(
                response2.getBody().getData(), TokenResponse.class);
        jwtToken2 = "Bearer " + tokenResponse2.getAccess_token();

        userId = UUID.randomUUID().toString(); // Замінити на реальний subject із токена
        user = new User();
        user.setId(userId);
        user.setEmail("den@example.com");
        userRepository.save(user);

        // Створення другого користувача для тестів Unauthorized
        userId2 = UUID.randomUUID().toString();
        user2 = new User();
        user2.setId(userId2);
        user2.setEmail("denis@example.com");
        userRepository.save(user2);
    }

    // User Story 1: Отримання статистики проєкту за ID
    @Test
    void getProjectStats_Success() throws Exception {
        // Підготовка даних
        ProjectStats project = new ProjectStats();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectPath("/test/project");
        project.setGithubBadgeVisible(true);
        project.setUser(userRepository.findById(userId).orElseThrow());
        Map<LocalDate, DailyStats> dailyStats = new HashMap<>();
        dailyStats.put(LocalDate.now(), new DailyStats());
        project.setDailyStats(dailyStats);
        projectStatsRepository.save(project);

        // Виконання запиту
        mockMvc.perform(get("/api/v1/stats/projects/" + project.getProjectId())
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(project.getProjectId()))
                .andExpect(jsonPath("$.data.projectPath").value("/test/project"));
    }

    @Test
    void getProjectStats_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/stats/projects/" + UUID.randomUUID())
                        .header("Authorization", jwtToken2))
//                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value(
                        startsWith("Project not found with ID:")));
    }

    @Test
    void getProjectStats_Unauthorized() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectPath("/test/project");
        project.setUser(user);
        projectStatsRepository.save(project);

        mockMvc.perform(get("/api/v1/stats/projects/" + project.getProjectId())
                        .header("Authorization", jwtToken2))
//                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value(
                        "You do not have access to this project"));
    }

    // User Story 2: Створення нового проєкту
    @Test
    void createProject_Success() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/new/project");
        project.setGithubBadgeVisible(true);
        FileStats file = new FileStats();
        file.setFilePath("/new/project/App.java");
        file.setType("JAVA");
//        Map<LocalDate, DailyStats> dailyStats = new HashMap<>();
//        dailyStats.put(LocalDate.now(), new DailyStats());
//        file.setDailyStats(dailyStats);
        project.setFiles(List.of(file));

        mockMvc.perform(post("/api/v1/stats/projects")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectPath").value("/new/project"))
                .andExpect(jsonPath("$.data.files[0].filePath").value("/new/project/App.java"));
    }

    // User Story 2: Створення нового проєкту без токена
    @Test
    void createProject_Without_Token() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath("/new/project");
        project.setGithubBadgeVisible(true);
        FileStats file = new FileStats();
        file.setFilePath("/new/project/App.java");
        file.setType("JAVA");
//        Map<LocalDate, DailyStats> dailyStats = new HashMap<>();
//        dailyStats.put(LocalDate.now(), new DailyStats());
//        file.setDailyStats(dailyStats);
        project.setFiles(List.of(file));

        mockMvc.perform(post("/api/v1/stats/projects")
                        .header("Authorization", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Доступ заборонено"));
    }

    @Test
    void createProject_InvalidData() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectPath(""); // Некоректний шлях
        FileStats file = new FileStats();
        file.setFilePath("");
        project.setFiles(List.of(file));

        mockMvc.perform(post("/api/v1/stats/projects")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Помилка валідації: projectPath Шлях до проєкту обов'язковий"));
    }

    // User Story 3: Часткове оновлення проєкту
    @Test
    void patchProjectStats_Success() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectPath("/test/project");
        project.setGithubBadgeVisible(false);
        project.setUser(userRepository.findById(userId).orElseThrow());
        projectStatsRepository.save(project);

        ProjectStats updates = new ProjectStats();
        updates.setGithubBadgeVisible(true);

        mockMvc.perform(patch("/api/v1/stats/projects/" + project.getProjectId())
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.githubBadgeVisible").value(true))
                .andExpect(jsonPath("$.data.projectPath").value("/test/project"));
    }

    @Test
    void patchProjectStats_NotFound() throws Exception {
        ProjectStats updates = new ProjectStats();
        updates.setGithubBadgeVisible(true);

        mockMvc.perform(patch("/api/v1/stats/projects/" + UUID.randomUUID())
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
//                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value(
                        startsWith("Project not found with ID:")));
    }

    // User Story 4: Видалення проєкту
    @Test
    void deleteProject_Success() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectPath("/test/project");
        project.setUser(userRepository.findById(userId).orElseThrow());
        projectStatsRepository.save(project);

        mockMvc.perform(delete("/api/v1/stats/projects/" + project.getProjectId())
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Project deleted successfully"));
    }

    @Test
    void deleteProject_Unauthorized() throws Exception {
        ProjectStats project = new ProjectStats();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectPath("/test/project");
        project.setUser(user);
        projectStatsRepository.save(project);

        mockMvc.perform(delete("/api/v1/stats/projects/" + project.getProjectId())
                        .header("Authorization", jwtToken2))
//                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value(
                        "You do not have access to this project"));
    }
}