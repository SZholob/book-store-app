package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable);

    Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable);

    OrderDTO addOrder(OrderDTO order);

    void createOrderFromCart(List<CartItem> cart);

    void updateOrderStatus(Long orderId, OrderStatus status);

    Page<OrderDTO> getAllOrders(String status, String clientEmail, Pageable pageable);
}
