package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.repository.*;
import com.sarvasvanaturals.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final UserService userService;

    // =================== DASHBOARD ===================

    @GetMapping("")
    public String dashboard(Model model) {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.countPaidOrders();
        BigDecimal revenue = orderRepository.getTotalRevenue();

        List<Order> recentOrders = orderRepository.findByStatusOrderByCreatedAtDesc(
                Order.OrderStatus.PENDING).stream().limit(10).toList();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        model.addAttribute("recentOrders", recentOrders);
        return "admin/dashboard";
    }

    // =================== PRODUCTS ===================

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> products = productRepository.findAll(
                PageRequest.of(page, 20, Sort.by("createdAt").descending()));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                               @RequestParam Long categoryId,
                               @RequestParam(required = false) String imageUrl,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);

            if (imageUrl != null && !imageUrl.isBlank()) {
                ProductImage img = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .mainImage(true)
                        .product(product)
                        .build();
                product.getImages().add(img);
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Product saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        product.setActive(!product.isActive());
        productRepository.save(product);
        return ResponseEntity.ok(Map.of("active", product.isActive()));
    }

    // =================== ORDERS ===================

    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String status,
                          Model model) {
        Page<Order> orders = orderRepository.findAll(
                PageRequest.of(page, 20, Sort.by("createdAt").descending()));
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", Order.OrderStatus.values());
        model.addAttribute("currentStatus", status);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestParam(required = false) String trackingNumber,
                                     @RequestParam(required = false) String courierName,
                                     RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        orderService.updateStatus(id, Order.OrderStatus.valueOf(status));
        if (trackingNumber != null && !trackingNumber.isBlank()) {
            order.setTrackingNumber(trackingNumber);
            order.setCourierName(courierName);
            orderRepository.save(order);
        }
        redirectAttributes.addFlashAttribute("success", "Order status updated.");
        return "redirect:/admin/orders/" + id;
    }

    // =================== CATEGORIES ===================

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(category.getName().toLowerCase().replaceAll("\\s+", "-"));
        }
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Category saved.");
        return "redirect:/admin/categories";
    }

    // =================== COUPONS ===================

    @GetMapping("/coupons")
    public String coupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        return "admin/coupons";
    }

    @PostMapping("/coupons/save")
    public String saveCoupon(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("success", "Coupon saved.");
        return "redirect:/admin/coupons";
    }

    // =================== REVIEWS ===================

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/approve")
    @ResponseBody
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setApproved(!review.isApproved());
        reviewRepository.save(review);

        // Recalculate product rating
        Product product = review.getProduct();
        List<Review> approved = reviewRepository.findByProductAndApprovedTrue(product);
        double avg = approved.stream().mapToInt(Review::getRating).average().orElse(0.0);
        product.setAverageRating(avg);
        product.setReviewCount(approved.size());
        productRepository.save(product);

        return ResponseEntity.ok(Map.of("approved", review.isApproved()));
    }

    // =================== SETTINGS ===================

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User admin = userService.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("admin", admin);
        return "admin/settings";
    }

    @PostMapping("/settings/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user -> {
            boolean changed = userService.changePassword(user, currentPassword, newPassword);
            if (changed) {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
            }
        });
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/update-profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String phone,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        userService.findByEmail(userDetails.getUsername()).ifPresent(user ->
            userService.updateProfile(user, firstName, lastName, phone));
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/admin/settings";
    }

}