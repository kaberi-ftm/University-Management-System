package com.example.ums.controller;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/students")
public class StudentViewController extends BaseViewController {
    public StudentViewController(UserService service) { super(service); }
    @Override protected Role role() { return Role.STUDENT; }
    @Override protected String basePath() { return "/students"; }
    @Override protected String title() { return "Students"; }
}
