package com.epam.rd.autocode.spring.project.controller;


import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final ClientService clientService;

    @GetMapping("/login")
    public String login() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/books";
        }

        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("client", new ClientDTO());
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@Valid @ModelAttribute("client") ClientDTO clientDTO,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            clientService.addClient(clientDTO);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "registration";
        }
    }
}
