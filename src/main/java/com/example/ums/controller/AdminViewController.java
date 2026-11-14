package com.example.ums.controller;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admins")
public class AdminViewController extends BaseViewController {
    public AdminViewController(UserService service) { super(service); }
    @Override protected Role role() { return Role.ADMIN; }
    @Override protected String basePath() { return "/admins"; }
    @Override protected String title() { return "Admins"; }
}
