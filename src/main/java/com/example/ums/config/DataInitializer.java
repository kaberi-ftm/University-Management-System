package com.example.ums.config;

import com.example.ums.model.AppUser;
import com.example.ums.model.Role;
import com.example.ums.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Seeds a default admin (and optional demo teacher/student) on first start. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository repository,
                                PasswordEncoder encoder,
                                @Value("${app.admin.username}") String adminUser,
                                @Value("${app.admin.password}") String adminPass,
                                @Value("${app.seed-demo-users}") boolean seedDemo) {
        return args -> {
            create(repository, encoder, adminUser, adminPass,
                    "System Administrator", "admin@university.local", "Administration", Role.ADMIN);
            if (seedDemo) {
                create(repository, encoder, "teacher", "teacher123",
                        "Demo Teacher", "teacher@university.local", "CSE", Role.TEACHER);
                create(repository, encoder, "student", "student123",
                        "Demo Student", "student@university.local", "CSE", Role.STUDENT);
            }
        };
    }

    private void create(UserRepository repo, PasswordEncoder encoder, String username, String password,
                        String fullName, String email, String department, Role role) {
        if (repo.existsByUsername(username)) {
            return;
        }
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setDepartment(department);
        u.setRole(role);
        repo.save(u);
    }
}
