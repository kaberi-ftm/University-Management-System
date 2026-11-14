package com.example.ums;

import com.example.ums.dto.UserResponse;
import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DEMO ONLY - this test FAILS ON PURPOSE (requested by the teacher to show a red pipeline).
 *
 * To make the pipeline green again do ONE of these:
 *   1. delete this file (and DemoFailingIntegrationTest.java), or
 *   2. fix the wrong expected value below, or
 *   3. run Maven with  -DexcludedGroups=demo-fail
 *
 * The assertion is wrong deliberately; the application code itself is fine.
 */
@Tag("demo-fail")
class DemoFailingUnitTest {

    @Test
    @DisplayName("DEMO (fails on purpose) - unit: UserResponse.from() of a STUDENT is expected to be TEACHER")
    void demoUnit_wrongExpectedRole() {
        AppUser student = new AppUser();
        student.setId(1L);
        student.setUsername("student");
        student.setRole(Role.STUDENT);

        UserResponse response = UserResponse.from(student);

        assertEquals(Role.TEACHER, response.role()); // WRONG ON PURPOSE - actual value is STUDENT
    }
}
