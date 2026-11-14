package com.example.ums.controller.api;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherRestController extends BaseRestController {
    public TeacherRestController(UserService service) { super(service); }
    @Override protected Role role() { return Role.TEACHER; }
}
