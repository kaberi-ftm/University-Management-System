package com.example.ums.controller;

import com.example.ums.dto.UserRequest;
import com.example.ums.dto.UserResponse;
import com.example.ums.exception.DuplicateResourceException;
import com.example.ums.exception.ResourceNotFoundException;
import com.example.ums.model.Role;
import com.example.ums.service.UserService;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC (Thymeleaf) pages shared by the three panels: list, create, edit, delete.
 * Each concrete controller binds them to one role and URL prefix.
 */
public abstract class BaseViewController {

    private final UserService service;

    protected BaseViewController(UserService service) {
        this.service = service;
    }

    protected abstract Role role();
    protected abstract String basePath();   // e.g. "/teachers"
    protected abstract String title();      // e.g. "Teachers"

    @GetMapping
    public String list(Model model) {
        addCommon(model);
        model.addAttribute("users", service.list(role()));
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        addCommon(model);
        model.addAttribute("user", new UserRequest());
        model.addAttribute("action", basePath());
        model.addAttribute("editing", false);
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("user") UserRequest request,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                service.create(role(), request);
                ra.addFlashAttribute("success", singular() + " created successfully.");
                return "redirect:" + basePath();
            } catch (DuplicateResourceException e) {
                result.rejectValue("username", "duplicate", e.getMessage());
            } catch (IllegalArgumentException e) {
                result.rejectValue("password", "required", e.getMessage());
            }
        }
        addCommon(model);
        model.addAttribute("action", basePath());
        model.addAttribute("editing", false);
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            UserResponse u = service.get(role(), id);
            UserRequest form = new UserRequest();
            form.setUsername(u.username());
            form.setFullName(u.fullName());
            form.setEmail(u.email());
            form.setDepartment(u.department());
            addCommon(model);
            model.addAttribute("user", form);
            model.addAttribute("action", basePath() + "/" + id);
            model.addAttribute("editing", true);
            return "users/form";
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:" + basePath();
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("user") UserRequest request,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                service.update(role(), id, request);
                ra.addFlashAttribute("success", singular() + " updated successfully.");
                return "redirect:" + basePath();
            } catch (DuplicateResourceException e) {
                result.rejectValue("username", "duplicate", e.getMessage());
            } catch (ResourceNotFoundException e) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:" + basePath();
            }
        }
        addCommon(model);
        model.addAttribute("action", basePath() + "/" + id);
        model.addAttribute("editing", true);
        return "users/form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.delete(role(), id);
            ra.addFlashAttribute("success", singular() + " deleted.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + basePath();
    }

    private void addCommon(Model model) {
        model.addAttribute("title", title());
        model.addAttribute("basePath", basePath());
        model.addAttribute("singular", singular());
    }

    private String singular() {
        return role().name().charAt(0) + role().name().substring(1).toLowerCase();
    }
}
