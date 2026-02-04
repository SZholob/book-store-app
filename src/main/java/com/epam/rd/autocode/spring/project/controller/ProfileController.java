package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class ProfileController {

    private final ClientService clientService;

    @GetMapping
    public String myProfile(Model model, Principal principal) {
        String email = principal.getName();
        ClientDTO client = clientService.getClientByEmail(email);

        client.setPassword("");

        model.addAttribute("client", client);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("client") ClientDTO clientDTO, Principal principal) {
        clientService.updateMyProfile(principal.getName(), clientDTO);
        return "redirect:/profile?success";
    }

    @PostMapping("/delete")
    public String deleteAccount(Principal principal, HttpServletRequest request) {
        clientService.deleteMyAccount(principal.getName());

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?deleted";
    }

}
