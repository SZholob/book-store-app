package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(employee -> modelMapper.map(employee,EmployeeDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository
                .findByEmail(email).orElseThrow(()->new NotFoundException("Employee not found: " + email));
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employeeDTO) {
        Employee existing = employeeRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found: " + email));

        modelMapper.map(employeeDTO, existing);

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        return modelMapper.map(employeeRepository.save(existing), EmployeeDTO.class);
    }

    @Override
    @Transactional
    public void deleteEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(()->new NotFoundException("Employee not found: " + email));
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional
    public EmployeeDTO addEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.findByEmail(employeeDTO.getEmail()).isPresent()){
            throw new AlreadyExistException("Employee already exists: " + employeeDTO.getEmail());
        }

        Employee employee = modelMapper.map(employeeDTO, Employee.class);
        employee.setRole(Role.EMPLOYEE);
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        return modelMapper.map(employeeRepository.save(employee),EmployeeDTO.class);
    }

}
