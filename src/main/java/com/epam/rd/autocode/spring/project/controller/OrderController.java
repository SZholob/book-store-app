package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String getMyOrders(Model model, Principal principal,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

        String email = principal.getName();

        Page<OrderDTO> orderPage = orderService.getOrdersByClient(email, pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        return "my-orders";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String getAllOrders(Model model,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String clientEmail,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderDTO> orderPage = orderService.getAllOrders(status, clientEmail, pageable);

        model.addAttribute("orders", orderPage.getContent());

        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedEmail", clientEmail);

        return "employee-orders";
    }

    @PostMapping("/manage/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/orders/manage";
    }
}
