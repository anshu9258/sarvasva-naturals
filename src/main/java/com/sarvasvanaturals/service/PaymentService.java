package com.sarvasvanaturals.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.sarvasvanaturals.model.Order;
import com.sarvasvanaturals.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // =================== RAZORPAY ===================

    public Map<String, Object> createRazorpayOrder(Order order) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            // Razorpay expects paise (1 INR = 100 paise)
            options.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", order.getOrderNumber());
            options.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("order_number", order.getOrderNumber());
            notes.put("customer_name", order.getShippingName());
            options.put("notes", notes);

            com.razorpay.Order razorpayOrder = client.orders.create(options);

            // Store razorpay order id
            order.setRazorpayOrderId(razorpayOrder.get("id").toString());
            orderRepository.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", razorpayOrder.get("id").toString());
            response.put("amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            response.put("currency", "INR");
            response.put("keyId", razorpayKeyId);
            response.put("orderNumber", order.getOrderNumber());
            response.put("customerName", order.getShippingName());
            response.put("customerEmail", order.getUser().getEmail());
            response.put("customerPhone", order.getShippingPhone());
            return response;

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed. Please try again.");
        }
    }

    public boolean verifyRazorpaySignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String generatedSignature = com.razorpay.Utils.getHash(payload, razorpayKeySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // =================== STRIPE ===================

    public Map<String, Object> createStripePaymentIntent(Order order) {
        try {
            // Stripe expects cents (USD)
            long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("inr")
                    .setDescription("Sarvasva Naturals - Order " + order.getOrderNumber())
                    .putMetadata("order_number", order.getOrderNumber())
                    .putMetadata("customer_email", order.getUser().getEmail())
                    .setReceiptEmail(order.getUser().getEmail())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            order.setStripePaymentIntentId(intent.getId());
            orderRepository.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentIntentId", intent.getId());
            response.put("publishableKey", stripePublishableKey);
            response.put("orderNumber", order.getOrderNumber());
            return response;

        } catch (StripeException e) {
            log.error("Stripe payment intent creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment initialization failed. Please try again.");
        }
    }

    // =================== COD ===================

    public void processCOD(Order order) {
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
}
