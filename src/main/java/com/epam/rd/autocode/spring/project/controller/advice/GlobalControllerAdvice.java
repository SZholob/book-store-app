package com.epam.rd.autocode.spring.project.controller.advice;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ClientService clientService;
    private final EmployeeService employeeService;

    @ModelAttribute
    public void addUserAttributes(org.springframework.ui.Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();

            try {
                ClientDTO client = clientService.getClientByEmail(email);
                model.addAttribute("currentUser", client);
                model.addAttribute("userType", "CLIENT");
                model.addAttribute("globalBalance", client.getBalance());
            } catch (Exception e) {
                EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
                model.addAttribute("currentUser", employee);
                model.addAttribute("userType", "EMPLOYEE");
            }
        }
    }
}
