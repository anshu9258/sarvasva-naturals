package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/shop")
    public String shop(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(defaultValue = "") String sort,
                       @RequestParam(required = false) String category,
                       Model model,
                       @AuthenticationPrincipal UserDetails userDetails,
                       HttpSession session) {

        Page<Product> products;
        if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category, page, size, sort);
            model.addAttribute("activeCategory", category);
        } else {
            products = productService.getAllActiveProducts(page, size, sort);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllActiveCategories());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "shop/shop";
    }

    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable String slug,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails,
                                HttpSession session) {

        Product product = productService.getProductBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Product> related = productService.getAllActiveProducts(0, 4, "bestseller").getContent();

        model.addAttribute("product", product);
        model.addAttribute("related", related);
        model.addAttribute("categories", productService.getAllActiveCategories());
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "product/detail";
    }

    @GetMapping("/category/{slug}")
    public String categoryPage(@PathVariable String slug,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "") String sort,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session) {

        Page<Product> products = productService.getProductsByCategory(slug, page, 12, sort);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllActiveCategories());
        model.addAttribute("activeCategory", slug);
        model.addAttribute("currentSort", sort);
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "shop/shop";
    }

    // =================== CART API ===================

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        HttpSession session) {
        try {
            Long productId = Long.valueOf(body.get("productId").toString());
            int quantity = Integer.parseInt(body.getOrDefault("quantity", "1").toString());
            String weight = (String) body.get("weight");

            User user = userDetails != null ?
                    userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
            Cart cart = cartService.getOrCreateCart(user, session.getId());
            cartService.addToCart(cart, productId, quantity, weight);

            return ResponseEntity.ok(Map.of("success", true, "cartCount", cart.getTotalItems()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCart(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         HttpSession session) {
        try {
            Long itemId = Long.valueOf(body.get("itemId").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());

            User user = userDetails != null ?
                    userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
            Cart cart = cartService.getOrCreateCart(user, session.getId());
            cartService.updateQuantity(cart, itemId, quantity);

            return ResponseEntity.ok(Map.of("success", true,
                    "cartCount", cart.getTotalItems(),
                    "subtotal", cart.getSubtotal()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             HttpSession session) {
        try {
            Long itemId = Long.valueOf(body.get("itemId").toString());
            User user = userDetails != null ?
                    userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
            Cart cart = cartService.getOrCreateCart(user, session.getId());
            cartService.removeItem(cart, itemId);
            return ResponseEntity.ok(Map.of("success", true, "cartCount", cart.getTotalItems()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/cart")
    public String viewCart(Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session) {
        User user = userDetails != null ?
                userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
        Cart cart = cartService.getOrCreateCart(user, session.getId());

        model.addAttribute("cart", cart);
        model.addAttribute("cartCount", cart.getTotalItems());
        model.addAttribute("freeShippingAbove", 999);
        return "cart/cart";
    }

    private int getCartCount(UserDetails userDetails, HttpSession session) {
        try {
            User user = userDetails != null ?
                    userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
            Cart cart = cartService.getOrCreateCart(user, session.getId());
            return cart.getTotalItems();
        } catch (Exception e) {
            return 0;
        }
    }
}
