package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.security.JwtUtils;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private JwtUtils jwtUtils;

    private ClientDTO validClient;

    @BeforeEach
    void setUp() {
        validClient = new ClientDTO();
        validClient.setEmail("test@example.com");
        validClient.setName("Test User");
        validClient.setPassword("password123");
    }



    @Test
    @DisplayName("GET /login -> returns the login page")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("POST /login (success) -> creates cookies and redirects to /books")
    void testPerformLogin_Success() throws Exception {

        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        when(jwtUtils.generateToken("user@test.com")).thenReturn("fake-jwt-token");


        mockMvc.perform(post("/login")
                        .param("username", "user@test.com")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(cookie().value("accessToken", "fake-jwt-token"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().path("accessToken", "/"));
    }

    @Test
    @DisplayName("POST /login (failure) -> redirect to /login?error on error")
    void testPerformLogin_Failure() throws Exception {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Wrong password"));


        mockMvc.perform(post("/login")
                        .param("username", "user@test.com")
                        .param("password", "wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }



    @Test
    @DisplayName("POST /logout -> deletes cookies and redirects")
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("accessToken", 0));
    }



    @Test
    @DisplayName("GET / -> redirect to /books")
    void testIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }



    @Test
    @DisplayName("GET /registration -> returns the registration form")
    void testRegistrationPage() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    @DisplayName("POST /registration (success) -> saves the client and redirects")
    void testRegister_Success() throws Exception {

        mockMvc.perform(post("/registration")
                        .param("name", validClient.getName())
                        .param("email", validClient.getEmail())
                        .param("password", validClient.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));


        verify(clientService, times(1)).addClient(any(ClientDTO.class));
    }

    @Test
    @DisplayName("POST /registration (validation error) -> returns a form with errors")
    void testRegister_ValidationError() throws Exception {

        mockMvc.perform(post("/registration")
                        .param("name", "")
                        .param("email", "invalid-email")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors());

        verify(clientService, never()).addClient(any());
    }

    @Test
    @DisplayName("POST /registration (service shutdown) -> shows error message")
    void testRegister_ServiceException() throws Exception {

        doThrow(new RuntimeException("Email already exists"))
                .when(clientService).addClient(any(ClientDTO.class));


        mockMvc.perform(post("/registration")
                        .param("name", validClient.getName())
                        .param("email", validClient.getEmail())
                        .param("password", validClient.getPassword()))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", containsString("Registration failed")));
    }
}