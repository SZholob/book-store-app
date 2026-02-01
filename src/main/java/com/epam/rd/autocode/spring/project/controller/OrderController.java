package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/client/{email}")
    public ResponseEntity<List<OrderDTO>> getOrdersByClient(@PathVariable String email){
        return ResponseEntity.ok(orderService.getOrdersByClient(email));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return new ResponseEntity<>(orderService.addOrder(orderDTO), HttpStatus.CREATED);
    }

    @GetMapping("/employee/{email}")
    private ResponseEntity<List<OrderDTO>> getOderByEmployee(@PathVariable String email){
        return ResponseEntity.ok(orderService.getOrdersByEmployee(email));
    }

    @GetMapping("/my")
    public String getMyOrders(Model model, Principal principal) {
        String email = principal.getName();
        List<OrderDTO> orders = orderService.getOrdersByClient(email);
        model.addAttribute("orders", orders);
        return "my-orders";
    }
}
