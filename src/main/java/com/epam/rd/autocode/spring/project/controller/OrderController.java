package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /*@GetMapping("/client/{email}")
    public ResponseEntity<List<OrderDTO>> getOrdersByClient(@PathVariable String email,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size){
        Pageable pageable = PageRequest.of(page, size);


        return ResponseEntity.ok(orderService.getOrdersByClient(email, pageable));
    }*/

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return new ResponseEntity<>(orderService.addOrder(orderDTO), HttpStatus.CREATED);
    }

    /*@GetMapping("/employee/{email}")
    private ResponseEntity<List<OrderDTO>> getOderByEmployee(@PathVariable String email){
        return ResponseEntity.ok(orderService.getOrdersByEmployee(email, ));
    }*/

    @GetMapping("/my")
    public String getMyOrders(Model model, Principal principal,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);

        String email = principal.getName();

        Page<OrderDTO> orderPage = orderService.getOrdersByClient(email, pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        return "my-orders";
    }
}
