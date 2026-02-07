package com.epam.rd.autocode.spring.project.aspect;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@Slf4j(topic = "BUSINESS_LOG")
public class BusinessLoggingAspect {

    private String getCurrentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Anonymous";
        }
    }


    @AfterReturning(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.BookService.addBook(..))", returning = "book")
    public void logAddBook(BookDTO book) {
        log.info("BUSINESS: Book added. Title: [{}], Price: [{}], User: [{}]",
                book.getName(), book.getPrice(), getCurrentUser());
    }


    @AfterReturning(pointcut = "execution(* com.epam.rd.autocode.spring.project.service.BookService.updateBook(..))", returning = "book")
    public void logUpdateBook(BookDTO book) {
        log.info("BUSINESS: Book updated. ID: [{}], Title: [{}], User: [{}]",
                book.getId(), book.getName(), getCurrentUser());
    }


    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.BookService.deleteBookByName(..))")
    public void logDeleteBook(JoinPoint joinPoint) {
        String bookName = (String) joinPoint.getArgs()[0];
        log.info("BUSINESS: Book deleted. Name: [{}], User: [{}]", bookName, getCurrentUser());
    }


    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.OrderService.createOrderFromCart(..))")
    public void logCheckout(JoinPoint joinPoint) {
        List<?> cart = (List<?>) joinPoint.getArgs()[0];
        int itemsCount = cart.size();
        log.info("BUSINESS: New Order placed. Items count: [{}], Customer: [{}]",
                itemsCount, getCurrentUser());
    }


    @AfterReturning("execution(* com.epam.rd.autocode.spring.project.service.OrderService.updateOrderStatus(..))")
    public void logOrderStatusChange(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long orderId = (Long) args[0];
        OrderStatus status = (OrderStatus) args[1];

        log.info("BUSINESS: Order status changed. Order ID: [{}], New Status: [{}], Employee: [{}]",
                orderId, status, getCurrentUser());
    }
}