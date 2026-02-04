package com.epam.rd.autocode.spring.project.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("order.status.NEW"),
    COMPLETED("order.status.COMPLETED"),
    CANCELED("order.status.CANCELED"),
    CONFIRMED("order.status.CONFIRMED");

    private final String key;

    OrderStatus(String key) {
        this.key = key;
    }

}
