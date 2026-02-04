package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/employees")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/profile")
    public String myProfile(Model model, Principal principal) {
        EmployeeDTO employee = employeeService.getEmployeeByEmail(principal.getName());
        employee.setPassword("");
        model.addAttribute("employee", employee);
        return "employee-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                                BindingResult bindingResult,
                                Principal principal) {
        if (bindingResult.hasErrors()){
            try {
                EmployeeDTO dbEmployee = employeeService.getEmployeeByEmail(principal.getName());
                employeeDTO.setBirthDate(dbEmployee.getBirthDate());
            } catch (Exception e){
                return "employee-profile";
            }
            return "employee-profile";
        }

        employeeService.updateEmployeeByEmail(principal.getName(), employeeDTO);
        return "redirect:/employees/profile?success";
    }

}
