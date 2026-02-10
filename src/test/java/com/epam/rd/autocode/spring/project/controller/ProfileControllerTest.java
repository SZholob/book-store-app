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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

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
    @WithMockUser(username = "me@mail.com", roles = "CUSTOMER")
    void myProfile_ShouldReturnViewAndClearPassword() throws Exception {
        ClientDTO client = new ClientDTO();
        client.setEmail("me@mail.com");
        client.setBalance(BigDecimal.valueOf(100));
        client.setPassword("secretHash");

        when(clientService.getClientByEmail("me@mail.com")).thenReturn(client);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("client", hasProperty("email", is("me@mail.com"))))
                .andExpect(model().attribute("client", hasProperty("password", is(""))));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void myProfile_Employee_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "me@mail.com", roles = "CUSTOMER")
    void updateProfile_Success_NoPasswordChange() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        verify(clientService).updateMyProfile(eq("me@mail.com"), any(ClientDTO.class));
    }

    @Test
    @WithMockUser(username = "me@mail.com", roles = "CUSTOMER")
    void updateProfile_Success_WithValidPassword() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("password", "strongPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        verify(clientService).updateMyProfile(eq("me@mail.com"), any(ClientDTO.class));
    }

    @Test
    @WithMockUser(username = "me@mail.com", roles = "CUSTOMER")
    void updateProfile_ShortPassword_ShouldReturnError() throws Exception {
        ClientDTO dbClient = new ClientDTO();
        dbClient.setBalance(BigDecimal.TEN);
        when(clientService.getClientByEmail("me@mail.com")).thenReturn(dbClient);


        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("client", "password"));

        verify(clientService, never()).updateMyProfile(any(), any());
        verify(clientService, times(2)).getClientByEmail("me@mail.com");
    }

    @Test
    @WithMockUser(username = "me@mail.com", roles = "CUSTOMER")
    void deleteAccount_ShouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(post("/profile/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?deleted"));

        verify(clientService).deleteMyAccount("me@mail.com");
    }
}