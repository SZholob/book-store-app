package com.epam.rd.autocode.spring.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.epam.rd.autocode.spring.project.service.impl.*.*(..))")
    public void allServiceMethods() {}

    private Logger getLogger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
    }

    @Before("allServiceMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        getLogger(joinPoint).info(">>> [START] {}() args: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "allServiceMethods()", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        getLogger(joinPoint).info("<<< [SUCCESS] {}()", methodName);
    }

    @AfterThrowing(pointcut = "allServiceMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        getLogger(joinPoint).error("!!! [EXCEPTION] in {}: {}", methodName, ex.getMessage());
    }
}
