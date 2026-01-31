package com.epam.rd.autocode.spring.project.model;


import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "CLIENTS")
public class Client extends User {
    private BigDecimal balance;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    public Client(long id, String email, String password, String name, BigDecimal balance) {
        super(id, email, password, name, Role.CUSTOMER);
        this.balance = balance;
    }
}
