package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.security.JwtUtils;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;


    @BeforeEach
    void setUp() {
        lenient().when(clientService.getClientByEmail(any())).thenReturn(new ClientDTO());
        lenient().when(employeeService.getEmployeeByEmail(any())).thenReturn(new EmployeeDTO());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllClients_Employee_ShouldReturnView() throws Exception {
        Page<ClientDTO> page = new PageImpl<>(Collections.emptyList());

        when(clientService.getAllClients(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/clients")
                        .param("page", "0")
                        .param("keyword", "john")
                        .param("isBlocked", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-clients"))
                .andExpect(model().attributeExists("clients", "currentPage", "totalPages"))
                .andExpect(model().attribute("keyword", "john"))
                .andExpect(model().attribute("isBlocked", true));

        verify(clientService).getAllClients(eq("john"), eq(true), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllClients_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void blockClient_Employee_ShouldRedirect() throws Exception {
        String email = "bad@user.com";

        mockMvc.perform(post("/clients/{email}/block", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).blockClient(email);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void blockClient_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(post("/clients/any@email.com/block")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(clientService, never()).blockClient(any());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void unblockClient_Employee_ShouldRedirect() throws Exception {
        String email = "good@user.com";

        mockMvc.perform(post("/clients/{email}/unblock", email)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).unblockClient(email);
    }
}