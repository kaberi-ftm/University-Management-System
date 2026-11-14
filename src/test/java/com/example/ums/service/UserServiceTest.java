package com.example.ums.service;

import com.example.ums.dto.UserRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.exception.DuplicateResourceException;
import com.example.ums.exception.ResourceNotFoundException;
import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import com.example.ums.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS (1-4) - UserService in isolation.
 * Repository and PasswordEncoder are mocked, so no Spring context and no database are started.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    private UserRequest request(String username, String password) {
        UserRequest r = new UserRequest();
        r.setUsername(username);
        r.setPassword(password);
        r.setFullName("Dr. Rahman");
        r.setEmail("rahman@univ.edu");
        r.setDepartment("CSE");
        return r;
    }

    @Test
    @DisplayName("Unit 1: create() hashes the password and saves the user with the requested role")
    void create_encodesPasswordAndSavesUser() {
        when(repository.existsByUsername("teacher2")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(repository.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        UserResponse response = service.create(Role.TEACHER, request("teacher2", "secret123"));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());
        AppUser saved = captor.getValue();

        assertEquals("HASHED", saved.getPassword(), "raw password must never be stored");
        assertEquals(Role.TEACHER, saved.getRole());
        assertEquals(10L, response.id());
        assertEquals("teacher2", response.username());
    }

    @Test
    @DisplayName("Unit 2: create() rejects a username that already exists")
    void create_duplicateUsername_throwsDuplicateResourceException() {
        when(repository.existsByUsername("taken")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> service.create(Role.STUDENT, request("taken", "secret123")));

        assertEquals("Username 'taken' is already taken", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Unit 3: delete() refuses to remove the last remaining admin")
    void delete_lastAdmin_throwsIllegalArgumentException() {
        AppUser admin = new AppUser();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);
        when(repository.findByIdAndRole(1L, Role.ADMIN)).thenReturn(Optional.of(admin));
        when(repository.countByRole(Role.ADMIN)).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.delete(Role.ADMIN, 1L));

        assertEquals("Cannot delete the last remaining admin", ex.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Unit 4: get() throws ResourceNotFoundException with a readable message")
    void get_unknownId_throwsResourceNotFoundException() {
        when(repository.findByIdAndRole(99L, Role.TEACHER)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.get(Role.TEACHER, 99L));

        assertEquals("Teacher with id 99 not found", ex.getMessage());
    }
}
