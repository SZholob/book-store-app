package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CartItem;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.ClientService;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final ClientService clientService;

    @GetMapping
    public String viewCart(HttpSession session, Model model, Principal principal) {
        List<CartItem> cart = cartService.getCart(session);

        BigDecimal total = cartService.calculateTotal(cart);

        model.addAttribute("cartItems", cart);
        model.addAttribute("totalPrice", total);

        BigDecimal userBalance = BigDecimal.ZERO;
        if (principal != null) {
            try {
                ClientDTO client = clientService.getClientByEmail(principal.getName());
                userBalance = client.getBalance();
            } catch (Exception e) {
                //
            }
        }
        model.addAttribute("userBalance", userBalance);

        return "cart";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String addToCart(@RequestParam Long bookId, @RequestParam int quantity, HttpSession session) {
        cartService.addItem(session, bookId, quantity);
        return "redirect:/books";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long bookId, HttpSession session) {
        cartService.removeItem(session, bookId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String checkout(HttpSession session) {
        List<CartItem> cart = cartService.getCart(session);

        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        try {
            orderService.createOrderFromCart(cart);
            cartService.clearCart(session);
            return "redirect:/orders/my";
        } catch (Exception e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Long bookId, @RequestParam int quantity, HttpSession session) {
        cartService.updateItemQuantity(session, bookId, quantity);
        return "redirect:/cart";
    }
}
