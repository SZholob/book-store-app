package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllClients(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Boolean isBlocked) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ClientDTO> clientPage = clientService.getAllClients(keyword, isBlocked, pageable);

        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalItems", clientPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("isBlocked", isBlocked);
        return "employee-clients";
    }

    @PostMapping("/{email}/block")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String blockClient(@PathVariable String email) {
        clientService.blockClient(email);
        return "redirect:/clients";
    }

    @PostMapping("/{email}/unblock")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String unblockClient(@PathVariable String email) {
        clientService.unblockClient(email);
        return "redirect:/clients";
    }
}
