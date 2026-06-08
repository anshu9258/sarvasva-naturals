package com.sarvasvanaturals.service;

import com.sarvasvanaturals.model.Order;
import com.sarvasvanaturals.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.name}")
    private String appName;

    @Async
    public void sendVerificationEmail(User user) {
        String subject = "Verify your Sarvasva Naturals account";
        String link = baseUrl + "/verify-email/" + user.getEmailVerificationToken();
        String html = buildEmailWrapper(
                "Verify Your Email",
                "<p>Hello <strong>" + user.getFirstName() + "</strong>,</p>" +
                "<p>Thank you for registering with Sarvasva Naturals. Please verify your email address to get started.</p>" +
                "<a href='" + link + "' class='btn'>Verify Email Address</a>" +
                "<p style='margin-top:20px;font-size:12px;color:#888;'>Link expires in 24 hours. If you didn't create an account, ignore this email.</p>"
        );
        sendEmail(user.getEmail(), subject, html);
    }

    @Async
    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Reset your Sarvasva Naturals password";
        String link = baseUrl + "/reset-password/" + token;
        String html = buildEmailWrapper(
                "Password Reset",
                "<p>Hello <strong>" + user.getFirstName() + "</strong>,</p>" +
                "<p>You requested a password reset. Click below to set a new password:</p>" +
                "<a href='" + link + "' class='btn'>Reset Password</a>" +
                "<p style='margin-top:20px;font-size:12px;color:#888;'>This link expires in 1 hour. If you didn't request this, ignore this email.</p>"
        );
        sendEmail(user.getEmail(), subject, html);
    }

    @Async
    public void sendOrderConfirmation(Order order) {
        String subject = "Order Confirmed – " + order.getOrderNumber() + " | Sarvasva Naturals";
        StringBuilder items = new StringBuilder();
        for (var item : order.getItems()) {
            items.append("<tr>")
                 .append("<td style='padding:8px;border-bottom:1px solid #eee;'>").append(item.getProductName())
                 .append(item.getSelectedWeight() != null ? " (" + item.getSelectedWeight() + ")" : "").append("</td>")
                 .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:center;'>").append(item.getQuantity()).append("</td>")
                 .append("<td style='padding:8px;border-bottom:1px solid #eee;text-align:right;'>₹").append(item.getSubtotal()).append("</td>")
                 .append("</tr>");
        }
        String html = buildEmailWrapper(
                "Order Confirmed!",
                "<p>Hello <strong>" + order.getShippingName() + "</strong>,</p>" +
                "<p>Your order has been confirmed. We'll notify you once it ships.</p>" +
                "<div style='background:#f9f7f4;padding:16px;border-radius:8px;margin:16px 0;'>" +
                "<strong>Order #</strong> " + order.getOrderNumber() + "<br>" +
                "<strong>Payment:</strong> " + order.getPaymentMethod() + " — " + order.getPaymentStatus() +
                "</div>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +
                "<tr><th align='left' style='padding:8px;background:#f0ebe3;'>Item</th>" +
                "<th style='padding:8px;background:#f0ebe3;'>Qty</th>" +
                "<th align='right' style='padding:8px;background:#f0ebe3;'>Total</th></tr>" +
                items +
                "<tr><td colspan='2' style='padding:8px;text-align:right;'><strong>Subtotal</strong></td>" +
                "<td style='padding:8px;text-align:right;'>₹" + order.getSubtotal() + "</td></tr>" +
                "<tr><td colspan='2' style='padding:8px;text-align:right;'>Shipping</td>" +
                "<td style='padding:8px;text-align:right;'>" + (order.getShippingCharge().compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "₹" + order.getShippingCharge()) + "</td></tr>" +
                "<tr><td colspan='2' style='padding:8px;text-align:right;'><strong>Total</strong></td>" +
                "<td style='padding:8px;text-align:right;font-size:18px;font-weight:bold;color:#2d5016;'>₹" + order.getTotalAmount() + "</td></tr>" +
                "</table>" +
                "<p style='margin-top:20px;'><strong>Shipping to:</strong><br>" + order.getShippingAddressLine1() + ", " + order.getShippingCity() + " - " + order.getShippingPincode() + "</p>" +
                "<a href='" + baseUrl + "/account/orders/" + order.getOrderNumber() + "' class='btn'>Track Order</a>"
        );
        sendEmail(order.getUser().getEmail(), subject, html);
    }

    @Async
    public void sendShippingUpdate(Order order) {
        String subject = "Your order has shipped! – " + order.getOrderNumber();
        String html = buildEmailWrapper(
                "Your Order Has Shipped",
                "<p>Hello <strong>" + order.getShippingName() + "</strong>,</p>" +
                "<p>Great news! Your order <strong>" + order.getOrderNumber() + "</strong> is on its way.</p>" +
                (order.getTrackingNumber() != null ?
                    "<p><strong>Tracking Number:</strong> " + order.getTrackingNumber() + " via " + order.getCourierName() + "</p>" : "") +
                "<a href='" + baseUrl + "/account/orders/" + order.getOrderNumber() + "' class='btn'>Track Order</a>"
        );
        sendEmail(order.getUser().getEmail(), subject, html);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@sarvasvanaturals.com", appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildEmailWrapper(String title, String content) {
        return """
            <!DOCTYPE html>
            <html><head><style>
            body{font-family:'Georgia',serif;color:#333;margin:0;padding:0;background:#f5f0e8;}
            .container{max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);}
            .header{background:#1a2e0f;padding:30px 40px;text-align:center;}
            .header h1{color:#d4a853;font-size:22px;margin:0;letter-spacing:2px;}
            .header p{color:#8aab6a;font-size:12px;margin:4px 0 0;}
            .body{padding:30px 40px;}
            .body h2{color:#1a2e0f;font-size:24px;margin-top:0;}
            .btn{display:inline-block;margin:20px 0;padding:14px 32px;background:#1a2e0f;color:#d4a853 !important;text-decoration:none;border-radius:6px;font-weight:bold;letter-spacing:1px;}
            .footer{background:#f0ebe3;padding:20px 40px;text-align:center;font-size:12px;color:#888;}
            </style></head><body>
            <div class="container">
              <div class="header">
                <h1>SARVASVA NATURALS</h1>
                <p>Purity in Every Grain</p>
              </div>
              <div class="body">
                <h2>""" + title + """
                </h2>
                """ + content + """
              </div>
              <div class="footer">
                © 2024 Sarvasva Naturals · Purity Without Compromise<br>
                <a href="https://sarvasvanaturals.com/unsubscribe" style="color:#aaa;">Unsubscribe</a>
              </div>
            </div>
            </body></html>
            """;
    }
}
