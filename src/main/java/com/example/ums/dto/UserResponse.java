package com.example.ums.dto;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;

/** Response body. The password (hash) is never exposed. */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String department,
        Role role) {

    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(),
                u.getEmail(), u.getDepartment(), u.getRole());
    }
}
