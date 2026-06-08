package com.sarvasvanaturals.service;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(User user, String sessionId) {
        if (user != null) {
            return cartRepository.findByUser(user).orElseGet(() -> {
                Cart c = Cart.builder().user(user).build();
                return cartRepository.save(c);
            });
        } else {
            return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
                Cart c = Cart.builder().sessionId(sessionId).build();
                return cartRepository.save(c);
            });
        }
    }

    public Cart addToCart(Cart cart, Long productId, int quantity, String selectedWeight) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem item = CartItem.builder()
                .product(product)
                .quantity(quantity)
                .selectedWeight(selectedWeight)
                .priceAtAdd(product.getPrice())
                .build();

        cart.addItem(item);
        return cartRepository.save(cart);
    }

    public Cart updateQuantity(Cart cart, Long itemId, int quantity) {
        cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cart.getItems().remove(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        return cartRepository.save(cart);
    }

    public Cart removeItem(Cart cart, Long itemId) {
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    public Cart clearCart(Cart cart) {
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    // Merge guest cart into user cart after login
    public void mergeGuestCartToUser(String sessionId, User user) {
        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(sessionId);
        if (guestCartOpt.isEmpty()) return;

        Cart guestCart = guestCartOpt.get();
        if (guestCart.getItems().isEmpty()) return;

        Cart userCart = getOrCreateCart(user, null);
        for (CartItem item : guestCart.getItems()) {
            CartItem newItem = CartItem.builder()
                    .product(item.getProduct())
                    .quantity(item.getQuantity())
                    .selectedWeight(item.getSelectedWeight())
                    .priceAtAdd(item.getPriceAtAdd())
                    .build();
            userCart.addItem(newItem);
        }
        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }
}
