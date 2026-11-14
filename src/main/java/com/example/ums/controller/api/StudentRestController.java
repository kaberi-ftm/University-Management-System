package com.example.ums.controller.api;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentRestController extends BaseRestController {
    public StudentRestController(UserService service) { super(service); }
    @Override protected Role role() { return Role.STUDENT; }
}
