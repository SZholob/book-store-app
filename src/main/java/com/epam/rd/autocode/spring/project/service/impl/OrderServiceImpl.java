package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;

    @Override
    public List<OrderDTO> getOrdersByClient(String email) {
        return orderRepository.findAllByClientEmail(email)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderRepository.findAllByEmployeeEmail(employeeEmail)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public OrderDTO addOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());


        Client client = clientRepository.findByEmail(orderDTO.getClientEmail())
                .orElseThrow(()->new NotFoundException("Client not found: " + orderDTO.getClientEmail()));
        order.setClient(client);

        if (orderDTO.getEmployeeEmail() != null){
            Employee employee = employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                    .orElseThrow(()->new NotFoundException("Client not found: " + orderDTO.getEmployeeEmail()));
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


    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setClientEmail(order.getClient().getEmail());
        if (order.getEmployee() != null){
            dto.setEmployeeEmail(order.getEmployee().getEmail());
        }
        dto.setOrderDate(order.getOrderDate());
        dto.setPrice(order.getPrice());

        List<BookItemDTO> itemDTOs = order.getBookItems().stream()
                .map(item -> new BookItemDTO(item.getBook().getName(), item.getQuantity()))
                .toList();

        dto.setBookItems(itemDTOs);

        return dto;
    }
}
