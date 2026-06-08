package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping("/")
    public String home(Model model,
                       @AuthenticationPrincipal UserDetails userDetails,
                       HttpSession session) {

        Page<Product> featured = productService.getFeaturedProducts(0, 8);
        List<Product> topSellers = productService.getTopSellers(4);
        List<Category> categories = productService.getAllActiveCategories();

        model.addAttribute("featured", featured.getContent());
        model.addAttribute("topSellers", topSellers);
        model.addAttribute("categories", categories);
        model.addAttribute("cartCount", getCartCount(userDetails, session));

        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model,
                         @AuthenticationPrincipal UserDetails userDetails,
                         HttpSession session) {

        Page<Product> results = productService.searchProducts(q, page, 12);
        model.addAttribute("products", results);
        model.addAttribute("query", q);
        model.addAttribute("categories", productService.getAllActiveCategories());
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "shop/search";
    }

    @GetMapping("/our-process")
    public String ourProcess(Model model,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/our-process";
    }

    @GetMapping("/lab-reports")
    public String labReports(Model model,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/lab-reports";
    }

    @GetMapping("/about")
    public String about(Model model,
                        @AuthenticationPrincipal UserDetails userDetails,
                        HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/about";
    }

    @GetMapping("/contact")
    public String contact(Model model,
                          @AuthenticationPrincipal UserDetails userDetails,
                          HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/contact";
    }

    @GetMapping("/verify-purity")
    public String verifyPurity(@RequestParam(required = false) String batch,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session) {
        model.addAttribute("batch", batch);
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/verify-purity";
    }

    private int getCartCount(UserDetails userDetails, HttpSession session) {
        try {
            User user = userDetails != null ?
                    userService.findByEmail(userDetails.getUsername()).orElse(null) : null;
            String sessionId = session.getId();
            Cart cart = cartService.getOrCreateCart(user, sessionId);
            return cart.getTotalItems();
        } catch (Exception e) {
            return 0;
        }
    }

    @GetMapping("/refund-policy")
    public String refundPolicy(Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/refund-policy";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model,
                                @AuthenticationPrincipal UserDetails userDetails,
                                HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/privacy-policy";
    }


    @GetMapping("/shipping-info")
    public String shippingInfo(Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session) {
        model.addAttribute("cartCount", getCartCount(userDetails, session));
        return "pages/shipping-info";
    }

}