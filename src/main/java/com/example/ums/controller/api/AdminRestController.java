package com.example.ums.controller.api;

import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminRestController extends BaseRestController {
    public AdminRestController(UserService service) { super(service); }
    @Override protected Role role() { return Role.ADMIN; }
}
