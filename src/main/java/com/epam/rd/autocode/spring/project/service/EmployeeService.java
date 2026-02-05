package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;

public interface EmployeeService {

    EmployeeDTO getEmployeeByEmail(String email);

    void updateEmployeeByEmail(String email, EmployeeDTO employee);




}
