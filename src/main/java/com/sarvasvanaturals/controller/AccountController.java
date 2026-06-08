package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.model.Order;
import com.sarvasvanaturals.model.User;
import com.sarvasvanaturals.service.OrderService;
import com.sarvasvanaturals.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String account(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Order> orders = orderService.getUserOrders(user, 0);
        model.addAttribute("user", user);
        model.addAttribute("recentOrders", orders.getContent());
        return "account/dashboard";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "0") int page,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Page<Order> orders = orderService.getUserOrders(user, page);
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        return "account/orders";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetail(@PathVariable String orderNumber,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderService.getOrderByNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/account/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("user", user);
        return "account/order-detail";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "account/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String phone,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        userService.updateProfile(user, firstName, lastName, phone);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/account/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean changed = userService.changePassword(user, currentPassword, newPassword);
        if (changed) {
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect.");
        }
        return "redirect:/account/profile";
    }
}
