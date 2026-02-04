package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository
                .findByEmail(email).orElseThrow(()->new NotFoundException("Employee not found: " + email));
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    @Transactional
    public void updateEmployeeByEmail(String email, EmployeeDTO employeeDTO) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found: " + email));

        String oldPasswordHash = existing.getPassword();

        modelMapper.map(employeeDTO, existing);

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().trim().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        } else {
            existing.setPassword(oldPasswordHash);
        }
        existing.setEmail(email);
        employeeRepository.save(existing);
    }


}
