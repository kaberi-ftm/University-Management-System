package com.example.ums.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRATION TEST 2 - Spring Security (HTTP Basic + BCrypt + DatabaseUserDetailsService + RBAC rules)
 * against the seeded users (admin / teacher / student) stored in H2.
 *
 * Access rules from the README:
 *   /api/admins   -> ADMIN            /api/teachers -> ADMIN, TEACHER            /api/students -> ADMIN, STUDENT
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Integration 2: role-based access (200 / 401 / 403) for the REST API using real DB users")
    void restEndpoints_enforceRoleBasedAccess() throws Exception {
        // not logged in -> 401 JSON
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // wrong password -> 401
        mockMvc.perform(get("/api/teachers").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());

        // ADMIN can open every panel
        mockMvc.perform(get("/api/admins").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/teachers").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/students").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());

        // TEACHER: own panel yes, students/admins no -> 403 JSON
        mockMvc.perform(get("/api/teachers").with(httpBasic("teacher", "teacher123"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/students").with(httpBasic("teacher", "teacher123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/api/admins").with(httpBasic("teacher", "teacher123"))).andExpect(status().isForbidden());

        // STUDENT: own panel yes, teachers/admins no
        mockMvc.perform(get("/api/students").with(httpBasic("student", "student123"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/teachers").with(httpBasic("student", "student123"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admins").with(httpBasic("student", "student123"))).andExpect(status().isForbidden());
    }
}
