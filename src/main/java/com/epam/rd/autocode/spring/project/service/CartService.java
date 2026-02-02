package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.CartItem;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    List<CartItem> getCart(HttpSession session);

    void addItem(HttpSession session, Long bookId, int quantity);

    void removeItem(HttpSession session, Long bookId);

    void updateItemQuantity(HttpSession session, Long bookId, int quantity);

    BigDecimal calculateTotal(List<CartItem> cart);

    void clearCart(HttpSession session);

}
