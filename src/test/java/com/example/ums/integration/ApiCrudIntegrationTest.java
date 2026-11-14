package com.example.ums.integration;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import com.example.ums.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRATION TEST 1 - REST layer + service + JPA repository + H2 database working together.
 * Runs a complete Create -> Read -> Update -> Delete life cycle through HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // every test is rolled back, so tests never affect each other
class ApiCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String NEW_TEACHER = """
            {"username":"teacher2","password":"secret123","fullName":"Dr. Rahman",
             "email":"rahman@univ.edu","department":"CSE"}""";

    private static final String UPDATED_TEACHER = """
            {"username":"teacher2","fullName":"Dr. Abdur Rahman",
             "email":"rahman@univ.edu","department":"EEE"}""";

    @Test
    @DisplayName("Integration 1: admin can create, read, update and delete a teacher through /api/teachers")
    void adminCrudLifecycle_persistsChangesInDatabase() throws Exception {
        // ---- CREATE -> 201 + Location header, password hash never returned
        MvcResult created = mockMvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_TEACHER))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("teacher2"))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        // The row is really in the database and the password is BCrypt-hashed
        AppUser stored = repository.findByUsername("teacher2").orElseThrow();
        assertNotEquals("secret123", stored.getPassword());
        assertTrue(passwordEncoder.matches("secret123", stored.getPassword()));
        Long id = stored.getId();

        // ---- READ
        mockMvc.perform(get("/api/teachers/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Rahman"));

        // ---- UPDATE (password omitted -> old password is kept)
        mockMvc.perform(put("/api/teachers/" + id)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATED_TEACHER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Abdur Rahman"))
                .andExpect(jsonPath("$.department").value("EEE"));

        AppUser updated = repository.findById(id).orElseThrow();
        assertEquals("EEE", updated.getDepartment());
        assertTrue(passwordEncoder.matches("secret123", updated.getPassword()),
                "password must be unchanged when omitted on update");

        // ---- DELETE -> 204, then 404
        mockMvc.perform(delete("/api/teachers/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
        assertTrue(repository.findByIdAndRole(id, Role.TEACHER).isEmpty());

        mockMvc.perform(get("/api/teachers/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Teacher with id " + id + " not found"));
    }

    @Test
    @DisplayName("Integration 1b: invalid body returns 400 with field errors, duplicate username returns 409")
    void invalidAndDuplicateRequests_returnErrorJson() throws Exception {
        // 400 - validation failed
        mockMvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\",\"password\":\"secret123\",\"fullName\":\"X\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        // 409 - "student" is already seeded by DataInitializer
        mockMvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student\",\"password\":\"secret123\",\"fullName\":\"Copy\",\"email\":\"c@univ.edu\"}"))
                .andExpect(status().isConflict());
    }
}
