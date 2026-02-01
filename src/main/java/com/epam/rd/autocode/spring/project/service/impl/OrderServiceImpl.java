package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.*;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final BookItemRepository bookItemRepository;

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

    @Override
    public void createOrderFromCart(List<CartItem> cart) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setPrice(BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);

        BigDecimal finalPrice = BigDecimal.ZERO;

        for (CartItem item : cart) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new NotFoundException("Book not found: " + item.getBookId()));


            if (book.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + book.getName());
            }


            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);


            BookItem bookItem = new BookItem();
            bookItem.setOrder(savedOrder);
            bookItem.setBook(book);
            bookItem.setQuantity(item.getQuantity());
            bookItemRepository.save(bookItem);


            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            finalPrice = finalPrice.add(itemTotal);


            if (client.getBalance().compareTo(finalPrice) < 0) {
                throw new RuntimeException("Insufficient funds! Balance: " + client.getBalance() + ", Total: " + finalPrice);
            }
            client.setBalance(client.getBalance().subtract(finalPrice));
            clientRepository.save(client);
        }

        savedOrder.setPrice(finalPrice);
        orderRepository.save(savedOrder);
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
