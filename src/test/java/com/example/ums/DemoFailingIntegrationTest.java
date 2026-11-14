package com.example.ums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DEMO ONLY - this test FAILS ON PURPOSE (requested by the teacher to show a red pipeline).
 *
 * To make the pipeline green again do ONE of these:
 *   1. delete this file (and DemoFailingUnitTest.java), or
 *   2. change isOk() to isForbidden() below, or
 *   3. run Maven with  -DexcludedGroups=demo-fail
 */
@Tag("demo-fail")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoFailingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("DEMO (fails on purpose) - integration: a student calling /api/admins is expected to get 200")
    void demoIntegration_wrongExpectedStatus() throws Exception {
        mockMvc.perform(get("/api/admins").with(httpBasic("student", "student123")))
                .andExpect(status().isOk()); // WRONG ON PURPOSE - the API correctly returns 403
    }
}
