package com.example.ums.service;

import com.example.ums.dto.UserRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.exception.DuplicateResourceException;
import com.example.ums.exception.ResourceNotFoundException;
import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import com.example.ums.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Role role) {
        return repository.findByRole(role).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Role role, Long id) {
        return UserResponse.from(find(role, id));
    }

    public UserResponse create(Role role, UserRequest req) {
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (repository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Username '" + req.getUsername() + "' is already taken");
        }
        AppUser user = new AppUser();
        user.setRole(role);
        apply(user, req);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        return UserResponse.from(repository.save(user));
    }

    public UserResponse update(Role role, Long id, UserRequest req) {
        AppUser user = find(role, id);
        if (!user.getUsername().equals(req.getUsername())
                && repository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException("Username '" + req.getUsername() + "' is already taken");
        }
        apply(user, req);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        return UserResponse.from(repository.save(user));
    }

    public void delete(Role role, Long id) {
        AppUser user = find(role, id);
        if (role == Role.ADMIN && repository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Cannot delete the last remaining admin");
        }
        repository.delete(user);
    }

    private AppUser find(Role role, Long id) {
        return repository.findByIdAndRole(id, role).orElseThrow(() ->
                new ResourceNotFoundException(
                        role.name().charAt(0) + role.name().substring(1).toLowerCase()
                                + " with id " + id + " not found"));
    }

    private void apply(AppUser user, UserRequest req) {
        user.setUsername(req.getUsername());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setDepartment(req.getDepartment());
    }
}
