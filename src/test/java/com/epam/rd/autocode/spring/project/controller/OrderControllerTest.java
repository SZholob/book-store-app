package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private EmployeeService employeeService;
    @MockBean
    private ClientService clientService;


    @Test
    @WithMockUser(username = "client@email.com", roles = "CUSTOMER")
    void getMyOrders_Customer_ShouldReturnView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getOrdersByClient(eq("client@email.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/orders/my")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("my-orders"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages"));

        verify(orderService).getOrdersByClient(eq("client@email.com"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getMyOrders_Employee_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/orders/my"))
                .andExpect(status().isForbidden()); // 403
    }


    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllOrders_Employee_ShouldReturnView() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Collections.emptyList());
        when(orderService.getAllOrders(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/orders/manage")
                        .param("status", "NEW")
                        .param("clientEmail", "some@client.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-orders"))
                .andExpect(model().attributeExists("orders", "statuses"))
                .andExpect(model().attribute("selectedStatus", "NEW"))
                .andExpect(model().attribute("selectedEmail", "some@client.com"));

        verify(orderService).getAllOrders(eq("NEW"), eq("some@client.com"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllOrders_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/orders/manage"))
                .andExpect(status().isForbidden());
    }



    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void updateOrderStatus_Employee_ShouldRedirect() throws Exception {
        Long orderId = 123L;

        mockMvc.perform(post("/orders/manage/{id}/status", orderId)
                        .with(csrf())
                        .param("status", "COMPLETED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/manage"));

        verify(orderService).updateOrderStatus(orderId, OrderStatus.COMPLETED);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateOrderStatus_Customer_ShouldBeForbidden() throws Exception {
        mockMvc.perform(post("/orders/manage/1/status")
                        .with(csrf())
                        .param("status", "CANCELED"))
                .andExpect(status().isForbidden());
    }
}