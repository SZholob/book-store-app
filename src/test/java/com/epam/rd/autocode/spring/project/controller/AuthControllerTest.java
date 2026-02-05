package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;


    @Test
    @WithAnonymousUser
    void login_Anonymous_ShouldShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser
    void login_Authenticated_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }


    @Test
    void index_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }


    @Test
    void showRegistrationForm_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("client"));
    }


    @Test
    void register_ValidData_ShouldRedirectToLogin() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        when(clientService.addClient(any(ClientDTO.class))).thenReturn(clientDTO);

        mockMvc.perform(post("/registration")
                        .with(csrf())
                        .param("email", "new@user.com")
                        .param("password", "password123")
                        .param("name", "New User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));

        verify(clientService).addClient(any(ClientDTO.class));
    }

    @Test
    void register_InvalidData_ShouldReturnFormBack() throws Exception {
        mockMvc.perform(post("/registration")
                        .with(csrf())
                        .param("email", "")
                        .param("password", "pass")
                        .param("name", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("client", "email"));

        verify(clientService, never()).addClient(any());
    }

    @Test
    void register_ServiceThrowsException_ShouldShowErrorMessage() throws Exception {
        doThrow(new RuntimeException("User already exists"))
                .when(clientService).addClient(any(ClientDTO.class));

        mockMvc.perform(post("/registration")
                        .with(csrf())
                        .param("email", "exist@user.com")
                        .param("password", "password123")
                        .param("name", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", containsString("User already exists")));
    }
}