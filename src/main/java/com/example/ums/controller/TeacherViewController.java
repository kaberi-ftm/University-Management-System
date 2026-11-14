package com.example.ums.controller;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teachers")
public class TeacherViewController extends BaseViewController {
    public TeacherViewController(UserService service) { super(service); }
    @Override protected Role role() { return Role.TEACHER; }
    @Override protected String basePath() { return "/teachers"; }
    @Override protected String title() { return "Teachers"; }
}
