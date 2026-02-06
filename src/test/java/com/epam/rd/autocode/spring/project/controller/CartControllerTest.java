package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.security.JwtUtils;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;


    @BeforeEach
    void setUp() {
        lenient().when(clientService.getClientByEmail(anyString())).thenReturn(new ClientDTO());
        lenient().when(employeeService.getEmployeeByEmail(anyString())).thenReturn(new EmployeeDTO());
    }

    @Test
    @WithAnonymousUser
    void viewCart_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "rich@client.com", roles = "CUSTOMER")
    void viewCart_Authenticated_ShouldShowBalance() throws Exception {
        // Given
        ClientDTO client = new ClientDTO();
        client.setBalance(BigDecimal.valueOf(1000));
        when(clientService.getClientByEmail("rich@client.com")).thenReturn(client);

        when(cartService.getCart(any())).thenReturn(Collections.emptyList());
        when(cartService.calculateTotal(any())).thenReturn(BigDecimal.ZERO);

        // When & Then
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart")) // Перевіряємо ім'я view
                .andExpect(model().attribute("userBalance", BigDecimal.valueOf(1000)));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void addToCart_Customer_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(cartService).addItem(any(HttpSession.class), eq(1L), eq(2));
    }

    @Test
    @WithAnonymousUser
    void addToCart_Anonymous_ShouldRedirectToLogin() throws Exception {

        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void removeFromCart_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .with(csrf())
                        .param("bookId", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeItem(any(HttpSession.class), eq(5L));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateCart_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/update")
                        .with(csrf())
                        .param("bookId", "5")
                        .param("quantity", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateItemQuantity(any(HttpSession.class), eq(5L), eq(10));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void checkout_Success_ShouldRedirectToMyOrders() throws Exception {
        List<CartItem> cart = List.of(new CartItem());
        when(cartService.getCart(any())).thenReturn(cart);

        mockMvc.perform(post("/cart/checkout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));

        verify(orderService).createOrderFromCart(cart);
        verify(cartService).clearCart(any(HttpSession.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void checkout_EmptyCart_ShouldRedirectToCart() throws Exception {
        when(cartService.getCart(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/cart/checkout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(orderService, never()).createOrderFromCart(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void checkout_ServiceError_ShouldRedirectToCartWithError() throws Exception {
        List<CartItem> cart = List.of(new CartItem());
        when(cartService.getCart(any())).thenReturn(cart);


        doThrow(new RuntimeException("Not enough money"))
                .when(orderService).createOrderFromCart(cart);

        mockMvc.perform(post("/cart/checkout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart?error=Not enough money"));

        verify(cartService, never()).clearCart(any());
    }
}