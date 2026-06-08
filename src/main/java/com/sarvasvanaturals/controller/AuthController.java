package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("error", "Invalid email or password");
        if (logout != null) model.addAttribute("message", "Logged out successfully");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String phone,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.register(firstName, lastName, email, password, phone);
            redirectAttributes.addFlashAttribute("success",
                    "Account created! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-email/{token}")
    public String verifyEmail(@PathVariable String token, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            redirectAttributes.addFlashAttribute("success", "Email verified! You can now log in.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired verification link.");
        }
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        userService.initiatePasswordReset(email);
        redirectAttributes.addFlashAttribute("success",
                "If an account exists with that email, you'll receive reset instructions shortly.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password/{token}")
    public String resetPasswordPage(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        boolean success = userService.resetPassword(token, password);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Password reset successful. Please log in.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired reset link.");
            return "redirect:/forgot-password";
        }
    }
}
