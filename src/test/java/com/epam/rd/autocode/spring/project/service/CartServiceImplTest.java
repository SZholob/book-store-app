package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.service.impl.CartServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {
    @Mock
    private BookService bookService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private CartServiceImpl cartService;

    private List<CartItem> cartList;
    private static final String CART_SESSION_KEY = "cart";

    @BeforeEach
    void setUp() {
        cartList = new ArrayList<>();
        lenient().when(session.getAttribute(CART_SESSION_KEY)).thenReturn(cartList);
    }

    @Test
    void getCart_ShouldUpdatePricesAndStock() {

        CartItem item = new CartItem();
        item.setBookId(1L);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(50));
        cartList.add(item);

        BookDTO book = new BookDTO();
        book.setId(1L);
        book.setPrice(BigDecimal.valueOf(100));
        book.setQuantity(1);

        when(bookService.getBookById(1L)).thenReturn(book);


        List<CartItem> result = cartService.getCart(session);


        CartItem resultItem = result.get(0);
        assertEquals(BigDecimal.valueOf(100), resultItem.getPrice(), "Price should be updated");
        assertEquals(1, resultItem.getAvailableStock(), "Stock should be updated");
        assertEquals(1, resultItem.getQuantity(), "Quantity should be reduced to available stock");
    }

    @Test
    void getCart_EmptySession_ShouldReturnEmptyList() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(null);

        List<CartItem> result = cartService.getCart(session);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(session).setAttribute(eq(CART_SESSION_KEY), any(List.class));
    }

    @Test
    void addItem_NewItem_ShouldAddToSession() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setName("Test Book");
        book.setPrice(BigDecimal.TEN);
        book.setQuantity(10);
        book.setImageUrl("img.jpg");

        when(bookService.getBookById(bookId)).thenReturn(book);

        cartService.addItem(session, bookId, 2);

        assertEquals(1, cartList.size());
        assertEquals(bookId, cartList.get(0).getBookId());
        assertEquals(2, cartList.get(0).getQuantity());
        assertEquals("img.jpg", cartList.get(0).getImageUrl());
    }

    @Test
    void addItem_ExistingItem_ShouldIncreaseQuantity() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setQuantity(10);
        book.setPrice(BigDecimal.TEN);

        CartItem item = new CartItem();
        item.setBookId(bookId);
        item.setQuantity(2);
        cartList.add(item);

        when(bookService.getBookById(bookId)).thenReturn(book);

        cartService.addItem(session, bookId, 3);

        assertEquals(1, cartList.size());
        assertEquals(5, cartList.get(0).getQuantity());
    }

    @Test
    void addItem_ExceedStock_ShouldCapAtMaxStock() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setQuantity(5);
        book.setPrice(BigDecimal.TEN);

        CartItem item = new CartItem();
        item.setBookId(bookId);
        item.setQuantity(4);
        cartList.add(item);

        when(bookService.getBookById(bookId)).thenReturn(book);


        cartService.addItem(session, bookId, 2);

        assertEquals(5, cartList.get(0).getQuantity());
    }

    @Test
    void removeItem_ShouldRemoveFromList() {
        CartItem item1 = new CartItem();
        item1.setBookId(1L);
        CartItem item2 = new CartItem();
        item2.setBookId(2L);
        cartList.add(item1);
        cartList.add(item2);

        cartService.removeItem(session, 1L);

        assertEquals(1, cartList.size());
        assertEquals(2L, cartList.get(0).getBookId());
    }

    @Test
    void updateItemQuantity_ValidQuantity_ShouldUpdate() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setQuantity(10);

        CartItem item = new CartItem();
        item.setBookId(bookId);
        item.setQuantity(1);
        cartList.add(item);

        when(bookService.getBookById(bookId)).thenReturn(book);

        cartService.updateItemQuantity(session, bookId, 5);

        assertEquals(5, cartList.get(0).getQuantity());
    }

    @Test
    void updateItemQuantity_ZeroQuantity_ShouldRemoveItem() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setQuantity(10);

        CartItem item = new CartItem();
        item.setBookId(bookId);
        item.setQuantity(5);
        cartList.add(item);

        when(bookService.getBookById(bookId)).thenReturn(book);

        cartService.updateItemQuantity(session, bookId, 0);

        assertTrue(cartList.isEmpty());
    }

    @Test
    void updateItemQuantity_ExceedStock_ShouldCap() {
        Long bookId = 1L;
        BookDTO book = new BookDTO();
        book.setId(bookId);
        book.setQuantity(5);

        CartItem item = new CartItem();
        item.setBookId(bookId);
        item.setQuantity(1);
        cartList.add(item);

        when(bookService.getBookById(bookId)).thenReturn(book);

        cartService.updateItemQuantity(session, bookId, 10);

        assertEquals(5, cartList.get(0).getQuantity());
    }

    @Test
    void calculateTotal_ShouldSumUpCorrectly() {
        CartItem item1 = new CartItem();
        item1.setPrice(BigDecimal.valueOf(100));
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setPrice(BigDecimal.valueOf(50));
        item2.setQuantity(1);

        List<CartItem> items = List.of(item1, item2);

        BigDecimal total = cartService.calculateTotal(items);

        assertEquals(BigDecimal.valueOf(250), total);
    }

    @Test
    void clearCart_ShouldRemoveAttribute() {
        cartService.clearCart(session);

        verify(session).removeAttribute(CART_SESSION_KEY);
    }

}
