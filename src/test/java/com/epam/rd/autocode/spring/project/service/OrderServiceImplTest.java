package com.epam.rd.autocode.spring.project.service;


import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BookRepository bookRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Book book;
    private List<CartItem> cart;


    @BeforeEach
    void setUp(){
        SecurityContextHolder.setContext(securityContext);

        client = new Client();
        client.setEmail("test@email.com");
        client.setBalance(new BigDecimal("1000.00"));

        book = new Book();
        book.setId(1L);
        book.setName("Test Book");
        book.setPrice(new BigDecimal("200.00"));
        book.setQuantity(10);

        cart = new ArrayList<>();

        CartItem item = new CartItem(1L, "Test Book"
                , new BigDecimal("200.00"),2,"img", 10);

        cart.add(item);

    }

    @Test
    void createOrderFromCart_ShouldSucceed_WhenBalanceIsEnough(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@email.com");
        when(clientRepository.findByEmail("test@email.com")).thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));


        orderService.createOrderFromCart(cart);

        // Баланс клієнта мав зменшитись на 400 (2 * 200)
        // Було 1000, стало 600
        assertEquals(0, client.getBalance().compareTo(new BigDecimal("600.00")));

        // Кількість книг мала зменшитись на 2
        // Було 10, стало 8
        assertEquals(8, book.getQuantity());

        verify(clientRepository, times(1)).save(client);
        verify(bookRepository, times(1)).save(book);
        verify(orderRepository, times(1)).save(any(Order.class));

    }

    @Test
    void createOrderFromCart_ShouldThrowException_WhenNotEnoughMoney() {
        client.setBalance(new BigDecimal("100.00"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@email.com");
        when(clientRepository.findByEmail("test@email.com")).thenReturn(Optional.of(client));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(cart);
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));

        assertEquals(new BigDecimal("100.00"), client.getBalance());
        assertEquals(10, book.getQuantity());

        verify(orderRepository, never()).save(any(Order.class));
        verify(clientRepository, never()).save(any(Client.class));
    }
}
