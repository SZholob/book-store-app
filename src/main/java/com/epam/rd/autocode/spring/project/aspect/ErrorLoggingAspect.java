package com.epam.rd.autocode.spring.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ErrorLoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.epam.rd.autocode.spring.project.service..*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        String user = "Anonymous";
        try {
            user = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            //
        }

        log.error("!!! EXCEPTION DETECTED !!!");
        log.error("User: [{}]", user);
        log.error("Location: {}.{}", className, methodName);
        log.error("Arguments: {}", Arrays.toString(args));
        log.error("Message: {}", ex.getMessage());
        // log.error("Stack Trace: ", ex);
    }
}
