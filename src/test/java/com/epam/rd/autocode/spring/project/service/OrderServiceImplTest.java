package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookItemRepository bookItemRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getOrdersByClient_ShouldReturnPage() {
        String email = "client@test.com";
        Pageable pageable = PageRequest.of(0, 10);

        Order order = createTestOrder();
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        when(orderRepository.findAllByClientEmail(email, pageable)).thenReturn(orderPage);

        Page<OrderDTO> result = orderService.getOrdersByClient(email, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(email, result.getContent().get(0).getClientEmail());
    }

    @Test
    void addOrder_Success() {
        OrderDTO inputDto = new OrderDTO();
        inputDto.setClientEmail("client@test.com");

        BookItemDTO itemDTO = new BookItemDTO();
        itemDTO.setBookName("Java Book");
        itemDTO.setQuantity(2);
        inputDto.setBookItems(List.of(itemDTO));

        Client client = new Client();
        client.setEmail("client@test.com");

        Book book = new Book();
        book.setName("Java Book");
        book.setPrice(BigDecimal.valueOf(100));

        when(clientRepository.findByEmail("client@test.com")).thenReturn(Optional.of(client));
        when(bookRepository.findByName("Java Book")).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });


        OrderDTO result = orderService.addOrder(inputDto);


        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getPrice());
        verify(orderRepository).save(any(Order.class));
    }



    @Test
    void createOrderFromCart_Success() {
        String email = "rich@test.com";
        when(authentication.getName()).thenReturn(email);

        Client client = new Client();
        client.setEmail(email);
        client.setBalance(BigDecimal.valueOf(1000));

        Book book = new Book();
        book.setId(1L);
        book.setName("Book 1");
        book.setPrice(BigDecimal.valueOf(100));
        book.setQuantity(10);

        CartItem cartItem = new CartItem();
        cartItem.setBookId(1L);
        cartItem.setQuantity(2);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));


        orderService.createOrderFromCart(List.of(cartItem));


        assertEquals(BigDecimal.valueOf(800), client.getBalance());


        assertEquals(8, book.getQuantity());

        verify(clientRepository).save(client);
        verify(bookRepository).save(book);
        verify(orderRepository).save(any(Order.class));
        verify(bookItemRepository).save(any(BookItem.class));
    }

    @Test
    void createOrderFromCart_InsufficientFunds_ShouldThrowException() {
        String email = "poor@test.com";
        when(authentication.getName()).thenReturn(email);

        Client client = new Client();
        client.setEmail(email);
        client.setBalance(BigDecimal.valueOf(50));

        Book book = new Book();
        book.setId(1L);
        book.setPrice(BigDecimal.valueOf(100));
        book.setQuantity(10);

        CartItem cartItem = new CartItem();
        cartItem.setBookId(1L);
        cartItem.setQuantity(1);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));


        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(List.of(cartItem)));

        assertTrue(ex.getMessage().contains("Insufficient funds"));


        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderFromCart_NotEnoughStock_ShouldThrowException() {
        String email = "user@test.com";
        when(authentication.getName()).thenReturn(email);

        Client client = new Client();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Book book = new Book();
        book.setId(1L);
        book.setName("Rare Book");
        book.setQuantity(1);

        CartItem cartItem = new CartItem();
        cartItem.setBookId(1L);
        cartItem.setQuantity(5);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));


        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrderFromCart(List.of(cartItem)));

        assertTrue(ex.getMessage().contains("Not enough stock"));
    }


    @Test
    void getAllOrders_FilterByClientEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        String clientEmail = "filter@test.com";

        when(orderRepository.findAllByClientEmail(clientEmail, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        orderService.getAllOrders(null, clientEmail, pageable);

        verify(orderRepository).findAllByClientEmail(clientEmail, pageable);
        verify(orderRepository, never()).findAllByStatus(any(), any());
    }

    @Test
    void getAllOrders_FilterByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        String status = "NEW";

        when(orderRepository.findAllByStatus(OrderStatus.NEW, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        orderService.getAllOrders(status, null, pageable);

        verify(orderRepository).findAllByStatus(OrderStatus.NEW, pageable);
    }

    @Test
    void updateOrderStatus_Success() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.updateOrderStatus(99L, OrderStatus.COMPLETED));
    }


    private Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setPrice(BigDecimal.TEN);
        order.setOrderDate(LocalDateTime.now());

        Client client = new Client();
        client.setEmail("client@test.com");
        order.setClient(client);

        Book book = new Book();
        book.setName("Test Book");

        BookItem item = new BookItem();
        item.setBook(book);
        item.setQuantity(1);
        order.setBookItems(List.of(item));

        return order;
    }
}