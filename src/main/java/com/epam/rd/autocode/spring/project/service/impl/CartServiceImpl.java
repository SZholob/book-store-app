package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final BookService bookService;
    private static final String CART_SESSION_KEY = "cart";

    @Override
    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);

        for (CartItem item : cart) {
            BookDTO book = bookService.getBookById(item.getBookId());

            item.setAvailableStock(book.getQuantity());
            item.setPrice(book.getPrice());

            if (item.getQuantity() > book.getQuantity()) {
                item.setQuantity(book.getQuantity());
            }
        }
        return cart;
    }

    @Override
    public void addItem(HttpSession session, Long bookId, int quantity) {
        List<CartItem> cart = getCartFromSession(session);
        BookDTO book = bookService.getBookById(bookId);

        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            item.setQuantity(Math.min(newQuantity, book.getQuantity()));
        } else {
            int finalQty = Math.min(quantity, book.getQuantity());
            if (finalQty > 0) {
                cart.add(new CartItem(
                        book.getId(),
                        book.getName(),
                        book.getPrice(),
                        finalQty,
                        book.getImageUrl(),
                        book.getQuantity()
                ));
            }
        }
    }

    @Override
    public void removeItem(HttpSession session, Long bookId) {
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getBookId().equals(bookId));
    }

    @Override
    public void updateItemQuantity(HttpSession session, Long bookId, int quantity) {
        List<CartItem> cart = getCartFromSession(session);
        BookDTO book = bookService.getBookById(bookId);

        cart.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity > book.getQuantity()) {
                        item.setQuantity(book.getQuantity());
                    } else if (quantity > 0) {
                        item.setQuantity(quantity);
                    } else {
                        cart.remove(item);
                    }
                    item.setAvailableStock(book.getQuantity());
                });
    }

    @Override
    public BigDecimal calculateTotal(List<CartItem> cart) {
        return cart.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
}
