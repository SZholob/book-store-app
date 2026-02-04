package com.epam.rd.autocode.spring.project.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;


import java.time.LocalDateTime;
import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --- 404 Not Found (власні помилки) ---
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        return buildErrorPage(HttpStatus.NOT_FOUND, "Resource Not Found", e.getMessage(), request);
    }


    // --- 403(немає прав) ---
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return buildErrorPage(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to access this resource.", request);
    }

    // --- 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception e, HttpServletRequest request) {
        return buildErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred. Please try again later.", request);
    }

    private ModelAndView buildErrorPage(HttpStatus status, String title, String message, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorCode", status.value());
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        mav.addObject("path", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }
}
