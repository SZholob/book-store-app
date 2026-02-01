package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private Integer availableStock;

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
