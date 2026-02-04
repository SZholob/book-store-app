package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final OrderService orderService;
    private final BookService bookService;


    @GetMapping("/clients")
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

    @PostMapping("/clients/{email}/block")
    public String blockClient(@PathVariable String email) {
        clientService.blockClient(email);
        return "redirect:/employee/clients";
    }

    @PostMapping("/clients/{email}/unblock")
    public String unblockClient(@PathVariable String email) {
        clientService.unblockClient(email);
        return "redirect:/employee/clients";
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String clientEmail,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
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

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/employee/orders";
    }

    @GetMapping("/books")
    public String manageBooks(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BookDTO> bookPage = bookService.getAllBooks(keyword, null, pageable);


        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "employee-books";
    }

    /*@GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email){
        return ResponseEntity.ok(employeeService.getEmployeeByEmail(email));
    }

    @PatchMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable String email,@RequestBody EmployeeDTO employeeDTO){
        return ResponseEntity.ok(employeeService.updateEmployeeByEmail(email, employeeDTO));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        employeeService.deleteEmployeeByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> addEmployee(@RequestBody EmployeeDTO employeeDTO){
        return ResponseEntity.ok(employeeService.addEmployee(employeeDTO));
    }*/
}
