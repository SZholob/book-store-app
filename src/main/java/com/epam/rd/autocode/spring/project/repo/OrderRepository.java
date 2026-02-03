package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByClientEmail(String email, Pageable pageable);

    Page<Order> findAllByEmployeeEmail(String email, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

}
