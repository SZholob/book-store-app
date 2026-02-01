package com.epam.rd.autocode.spring.project.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(NotFoundException e){
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", e.getMessage());
        return mav;
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ModelAndView handleAlreadyExistException(AlreadyExistException e) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", e.getMessage());
        return mav;
    }
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "An unexpected error occurred: " + e.getMessage());
        return mav;
    }
}
