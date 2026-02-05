package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ClientService clientService;


    @Test
    @WithMockUser(username = "emp@store.com", roles = "EMPLOYEE")
    void myProfile_ShouldReturnViewAndClearPassword() throws Exception {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("emp@store.com");
        dto.setPassword("secretHash");

        when(employeeService.getEmployeeByEmail("emp@store.com")).thenReturn(dto);

        mockMvc.perform(get("/employees/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-profile"))
                .andExpect(model().attribute("employee", hasProperty("email", is("emp@store.com"))))
                .andExpect(model().attribute("employee", hasProperty("password", is(""))));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myProfile_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/employees/profile"))
                .andExpect(status().isForbidden()); // 403
    }



    @Test
    @WithMockUser(username = "emp@store.com", roles = "EMPLOYEE")
    void updateProfile_Success_NoPasswordChange() throws Exception {
        mockMvc.perform(post("/employees/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("phone", "+1234567890")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/profile?success"));

        verify(employeeService).updateEmployeeByEmail(eq("emp@store.com"), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(username = "emp@store.com", roles = "EMPLOYEE")
    void updateProfile_Success_WithValidPassword() throws Exception {
        mockMvc.perform(post("/employees/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("password", "newSecurePass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/profile?success"));

        verify(employeeService).updateEmployeeByEmail(eq("emp@store.com"), any(EmployeeDTO.class));
    }

    @Test
    @WithMockUser(username = "emp@store.com", roles = "EMPLOYEE")
    void updateProfile_ShortPassword_ShouldReturnError() throws Exception {

        EmployeeDTO dbEmployee = new EmployeeDTO();
        dbEmployee.setBirthDate(LocalDate.now());
        when(employeeService.getEmployeeByEmail("emp@store.com")).thenReturn(dbEmployee);


        mockMvc.perform(post("/employees/profile/update")
                        .with(csrf())
                        .param("name", "New Name")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-profile"))
                .andExpect(model().attributeHasFieldErrors("employee", "password"));

        verify(employeeService, never()).updateEmployeeByEmail(any(), any());

        verify(employeeService, times(2)).getEmployeeByEmail("emp@store.com");
    }
}