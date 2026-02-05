package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_EmployeeFound_ShouldReturnUserDetails() {
        String email = "admin@store.com";
        Employee employee = new Employee();
        employee.setEmail(email);
        employee.setPassword("hashedPass");
        employee.setRole(Role.EMPLOYEE);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertEquals(email, result.getUsername());
        assertEquals("hashedPass", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));

        assertTrue(result.isAccountNonLocked());

        verify(employeeRepository).findByEmail(email);
        verify(clientRepository, never()).findByEmail(any());
    }

    @Test
    void loadUserByUsername_ClientFound_Active_ShouldReturnUserDetails() {
        String email = "user@store.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("clientPass");
        client.setRole(Role.CUSTOMER);
        client.setBlocked(false);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertEquals(email, result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        assertTrue(result.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ClientFound_Blocked_ShouldReturnLockedUser() {
        String email = "banned@store.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("pass");
        client.setRole(Role.CUSTOMER);
        client.setBlocked(true);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertFalse(result.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_UserNotFound_ShouldThrowException() {
        String email = "ghost@store.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));
    }
}