package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import com.epam.rd.autocode.spring.project.service.ClientService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final BookService bookService;
    private final OrderService orderService;
    private final ClientService clientService;

    @GetMapping
    public String viewCart(HttpSession session, Model model, Principal principal) {
        List<CartItem> cart = getCartFromSession(session);


        for (CartItem item : cart) {
            BookDTO book = bookService.getBookById(item.getBookId());
            item.setAvailableStock(book.getQuantity());


            if (item.getQuantity() > book.getQuantity()) {
                item.setQuantity(book.getQuantity());
            }
        }

        BigDecimal total = cart.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cart);
        model.addAttribute("totalPrice", total);


        if (principal != null) {
            try {

                ClientDTO client = clientService.getClientByEmail(principal.getName());
                model.addAttribute("userBalance", client.getBalance());
            } catch (Exception e) {

                model.addAttribute("userBalance", BigDecimal.ZERO);
            }
        } else {
            model.addAttribute("userBalance", BigDecimal.ZERO);
        }

        return "cart";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String addToCart(@RequestParam Long bookId, @RequestParam int quantity, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        BookDTO book = bookService.getBookById(bookId);

        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getBookId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (newQuantity > book.getQuantity()) {
                newQuantity = book.getQuantity();
            }
            item.setQuantity(newQuantity);
            item.setAvailableStock(book.getQuantity());
        } else {

            int finalQty = Math.min(quantity, book.getQuantity());
            cart.add(new CartItem(book.getId(), book.getName(), book.getPrice(), finalQty, book.getImageUrl(), book.getQuantity()));
        }

        return "redirect:/books";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long bookId, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getBookId().equals(bookId));
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String checkout(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        if (cart.isEmpty()) return "redirect:/cart";
        orderService.createOrderFromCart(cart);
        session.removeAttribute("cart");
        return "redirect:/orders/my";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Long bookId, @RequestParam int quantity, HttpSession session) {
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

        return "redirect:/cart";
    }




    private List<CartItem> getCartFromSession(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
