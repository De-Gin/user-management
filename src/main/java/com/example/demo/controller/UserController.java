package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET all users
    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("users", userService.findAll());
        return "list";
    }

    // GET registration form
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "form";
    }

    // GET user by username
    @GetMapping("/username/{username}")
    public String findByUsername(@PathVariable String username, Model model) {
        model.addAttribute("user", userService.findByUsername(username));
        return "detail";
    }

    // GET user by id
    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "detail";
    }

    // POST register
    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") User user,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "form";
        }
        userService.save(user);
        return "redirect:/users";
    }
}
