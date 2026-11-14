package com.example.ums.integration;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import com.example.ums.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRATION TEST 3 - the Thymeleaf MVC web UI: form-login protection, access-denied page,
 * server-side rendering of the list page, and the create form writing to the database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WebUiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Test
    @DisplayName("Integration 3: web UI redirects anonymous users, denies wrong roles and creates users via the form")
    void webUi_securityRenderingAndFormSubmit() throws Exception {
        // anonymous -> redirected to the login page
        mockMvc.perform(get("/students"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // login page itself is public
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));

        // a TEACHER opening the admin panel -> Access Denied page
        mockMvc.perform(get("/admins").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));

        // ADMIN sees the seeded teacher rendered in the teachers list page
        mockMvc.perform(get("/teachers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("users/list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Demo Teacher")));

        // ADMIN submits the "new teacher" form (CSRF token required for the web chain) -> redirect + DB row
        mockMvc.perform(post("/teachers")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("username", "formteacher")
                        .param("password", "secret123")
                        .param("fullName", "Form Teacher")
                        .param("email", "form@univ.edu")
                        .param("department", "CSE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teachers"))
                .andExpect(flash().attribute("success", "Teacher created successfully."));

        AppUser saved = repository.findByUsername("formteacher").orElseThrow();
        assertEquals(Role.TEACHER, saved.getRole());
        assertTrue(saved.getPassword().startsWith("$2"), "password must be stored as a BCrypt hash");

        // the same POST without a CSRF token is rejected
        mockMvc.perform(post("/teachers")
                        .with(user("admin").roles("ADMIN"))
                        .param("username", "nocsrf"))
                .andExpect(status().isForbidden());
    }
}
