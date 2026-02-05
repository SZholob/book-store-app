package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;

    @Override
    public Page<OrderDTO> getOrdersByClient(String email, Pageable pageable) {
        Page<Order> orderPage;
        orderPage = orderRepository.findAllByClientEmail(email, pageable);
        return orderPage.map(this::mapToDTO);
    }


    @Override
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setOrderDate(LocalDateTime.now());


        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(()->new NotFoundException("Client not found: " + orderDTO.getClientEmail()));
        order.setClient(client);

        if (orderDTO.getEmployeeEmail() != null){
            Employee employee = employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                    .orElseThrow(()->new NotFoundException("Employee not found: " + orderDTO.getEmployeeEmail()));
            order.setEmployee(employee);
        }

        List<BookItem> bookItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (BookItemDTO itemDTO : orderDTO.getBookItems()){
            Book book = bookRepository.findByName(itemDTO.getBookName())
                    .orElseThrow(()->new NotFoundException("Book not found: " + itemDTO.getBookName()));

            BookItem bookItem = new BookItem();
            bookItem.setBook(book);
            bookItem.setQuantity(itemDTO.getQuantity());
            bookItem.setOrder(order);

            bookItems.add(bookItem);

            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
        order.setBookItems(bookItems);
        order.setPrice(totalPrice);

        Order saveOrder = orderRepository.save(order);
        return mapToDTO(saveOrder);
    }

    @Override
    @Transactional
    public void createOrderFromCart(List<CartItem> cart) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        for (CartItem item : cart) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new NotFoundException("Book not found: " + item.getBookId()));

            if (book.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + book.getName());
            }

            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalOrderPrice = totalOrderPrice.add(itemTotal);
        }

        if (client.getBalance().compareTo(totalOrderPrice) < 0) {
            throw new RuntimeException("Insufficient funds! Balance: " + client.getBalance() + ", Total: " + totalOrderPrice);
        }

        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setPrice(totalOrderPrice);

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cart) {
            Book book = bookRepository.findById(item.getBookId()).get();

            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);

            BookItem bookItem = new BookItem();
            bookItem.setOrder(savedOrder);
            bookItem.setBook(book);
            bookItem.setQuantity(item.getQuantity());
            bookItemRepository.save(bookItem);
        }

        client.setBalance(client.getBalance().subtract(totalOrderPrice));
        clientRepository.save(client);
    }

    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Page<OrderDTO> getAllOrders(String statusStr, String clientEmail, Pageable pageable) {
        Page<Order> orders;

        if (clientEmail != null && !clientEmail.isEmpty()) {
            orders = orderRepository.findAllByClientEmail(clientEmail, pageable);
        }
        else if (statusStr != null && !statusStr.isEmpty()) {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            orders = orderRepository.findAllByStatus(status, pageable);
        }

        else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(this::mapToDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        order.setStatus(status);
        orderRepository.save(order);
    }


    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setClientEmail(order.getClient().getEmail());
        if (order.getEmployee() != null){
            dto.setEmployeeEmail(order.getEmployee().getEmail());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setPrice(order.getPrice());
        dto.setStatus(order.getStatus());

        List<BookItemDTO> itemDTOs = order.getBookItems().stream()
                .map(item -> new BookItemDTO(item.getBook().getName(), item.getQuantity()))
                .toList();

        dto.setBookItems(itemDTOs);

        return dto;
    }
}
