package com.example.ums.dto;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TESTS (5-6) - DTO behaviour: bean-validation rules and entity -> response mapping.
 * Plain Java, no Spring context.
 */
class UserDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("Unit 5: UserRequest validation flags a short username, bad email and short password")
    void userRequest_invalidFields_produceViolations() {
        UserRequest req = new UserRequest();
        req.setUsername("ab");              // too short (min 3)
        req.setPassword("123");             // too short (min 6)
        req.setFullName("Zohra");
        req.setEmail("not-an-email");       // invalid format

        Set<String> invalidFields = validator.validate(req).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());

        assertEquals(Set.of("username", "password", "email"), invalidFields);
    }

    @Test
    @DisplayName("Unit 6: UserResponse.from() copies the public fields of an AppUser")
    void userResponse_from_mapsEntityFields() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setUsername("student1");
        user.setPassword("BCRYPT_HASH");
        user.setFullName("Demo Student");
        user.setEmail("student@univ.edu");
        user.setDepartment("EEE");
        user.setRole(Role.STUDENT);

        UserResponse response = UserResponse.from(user);

        assertEquals(7L, response.id());
        assertEquals("student1", response.username());
        assertEquals("Demo Student", response.fullName());
        assertEquals("student@univ.edu", response.email());
        assertEquals("EEE", response.department());
        assertEquals(Role.STUDENT, response.role());
        // the password hash must never leak into the response
        assertFalse(response.toString().contains("BCRYPT_HASH"));
    }
}
