package com.epam.rd.autocode.spring.project.aspect;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j(topic = "SECURITY_LOG")
public class SecurityLoggingAspect {


    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.controller.AuthController.performLogin(..))")
    public void logLogin(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String username = (String) args[0];
        log.info("SECURITY EVENT: User logged in successfully. Email: [{}]", username);
    }

    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.controller.AuthController.logout(..))")
    public void logLogout() {
        log.info("SECURITY EVENT: User logged out.");
    }

    @AfterReturning(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.ClientService.addClient(..))", returning = "result")
    public void logRegistration(Object result) {
        if (result instanceof ClientDTO client) {
            log.info("SECURITY EVENT: New account created. Email: [{}], Name: [{}]", client.getEmail(), client.getName());
        }
    }

    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.ClientService.updateMyProfile(..))")
    public void logClientUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        ClientDTO dto = (ClientDTO) args[1];

        String changes = "Profile updated";
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            changes += " (Password changed)";
        }

        log.info("SECURITY EVENT: Client updated profile. User: [{}]. Action: {}", email, changes);
    }

    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.EmployeeService.updateEmployeeByEmail(..))")
    public void logEmployeeUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String email = (String) args[0];
        EmployeeDTO dto = (EmployeeDTO) args[1];

        String changes = "Profile updated";
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            changes += " (Password changed)";
        }

        log.info("SECURITY EVENT: Employee updated profile. User: [{}]. Action: {}", email, changes);
    }
}