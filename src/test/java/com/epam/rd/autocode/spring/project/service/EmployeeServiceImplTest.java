package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getEmployeeByEmail_Success() {
        String email = "emp@test.com";
        Employee employee = new Employee();
        employee.setEmail(email);
        employee.setName("John Worker");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        EmployeeDTO result = employeeService.getEmployeeByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("John Worker", result.getName());
    }

    @Test
    void getEmployeeByEmail_NotFound() {
        String email = "unknown@test.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail(email));
    }

    @Test
    void updateEmployeeByEmail_WithNewPassword_ShouldEncodeAndSave() {
        String email = "emp@test.com";

        Employee existingEmployee = new Employee();
        existingEmployee.setEmail(email);
        existingEmployee.setPassword("oldHash");
        existingEmployee.setName("Old Name");

        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("New Name");
        dto.setPassword("newSuperPass");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(existingEmployee));
        when(passwordEncoder.encode("newSuperPass")).thenReturn("newHash");

        employeeService.updateEmployeeByEmail(email, dto);


        verify(passwordEncoder).encode("newSuperPass");
        verify(employeeRepository).save(argThat(emp ->
                emp.getPassword().equals("newHash") &&
                        emp.getName().equals("New Name")
        ));
    }

    @Test
    void updateEmployeeByEmail_NoPassword_ShouldKeepOldHash() {
        String email = "emp@test.com";

        Employee existingEmployee = new Employee();
        existingEmployee.setEmail(email);
        existingEmployee.setPassword("oldHash");
        existingEmployee.setName("Old Name");

        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Updated Name");
        dto.setPassword("");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(existingEmployee));


        employeeService.updateEmployeeByEmail(email, dto);

        verify(passwordEncoder, never()).encode(any());

        verify(employeeRepository).save(argThat(emp ->
                emp.getPassword().equals("oldHash") &&
                        emp.getName().equals("Updated Name")
        ));
    }

    @Test
    void updateEmployeeByEmail_NullPassword_ShouldKeepOldHash() {
        String email = "emp@test.com";

        Employee existingEmployee = new Employee();
        existingEmployee.setEmail(email);
        existingEmployee.setPassword("oldHash");

        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Updated Name");
        dto.setPassword(null);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(existingEmployee));

        employeeService.updateEmployeeByEmail(email, dto);

        verify(passwordEncoder, never()).encode(any());
        verify(employeeRepository).save(argThat(emp ->
                emp.getPassword().equals("oldHash")
        ));
    }

    @Test
    void updateEmployeeByEmail_NotFound() {
        String email = "ghost@test.com";
        EmployeeDTO dto = new EmployeeDTO();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.updateEmployeeByEmail(email, dto));
        verify(employeeRepository, never()).save(any());
    }
}