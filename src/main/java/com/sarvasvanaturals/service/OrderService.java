package com.sarvasvanaturals.service;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;

    @Value("${shipping.free.above:999}")
    private BigDecimal freeShippingAbove;

    @Value("${shipping.standard.charge:99}")
    private BigDecimal standardShippingCharge;

    @Value("${gst.rate:18}")
    private BigDecimal gstRate;

    public Order createOrderFromCart(Cart cart, Address address, Order.PaymentMethod paymentMethod, String couponCode) {
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal discount = BigDecimal.ZERO;

        // Apply coupon
        if (couponCode != null && !couponCode.isBlank()) {
            Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(couponCode);
            if (couponOpt.isPresent() && couponOpt.get().isValid(subtotal)) {
                Coupon coupon = couponOpt.get();
                discount = coupon.calculateDiscount(subtotal);
                coupon.setUsedCount(coupon.getUsedCount() + 1);
                couponRepository.save(coupon);
            }
        }

        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal shipping = afterDiscount.compareTo(freeShippingAbove) >= 0
                ? BigDecimal.ZERO : standardShippingCharge;
        BigDecimal gst = afterDiscount.multiply(gstRate).divide(BigDecimal.valueOf(100));
        BigDecimal total = afterDiscount.add(shipping).add(gst);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(cart.getUser())
                .shippingName(address.getFullName())
                .shippingPhone(address.getPhone())
                .shippingAddressLine1(address.getAddressLine1())
                .shippingAddressLine2(address.getAddressLine2())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingPincode(address.getPincode())
                .shippingCountry(address.getCountry())
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingCharge(shipping)
                .gstAmount(gst)
                .totalAmount(total)
                .paymentMethod(paymentMethod)
                .couponCode(couponCode)
                .build();

        // Build order items & decrement stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setSoldCount(product.getSoldCount() + cartItem.getQuantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .selectedWeight(cartItem.getSelectedWeight())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPriceAtAdd())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // Send confirmation email async
        emailService.sendOrderConfirmation(saved);

        return saved;
    }

    public void confirmPayment(String orderNumber, String paymentId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentId(paymentId);
        orderRepository.save(order);
    }

    public void failPayment(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        order.setPaymentStatus(Order.PaymentStatus.FAILED);
        order.setStatus(Order.OrderStatus.CANCELLED);
        // Restock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
            productRepository.save(p);
        }
        orderRepository.save(order);
    }

    public Page<Order> getUserOrders(User user, int page) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, 10));
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Order updateStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        if (status == Order.OrderStatus.SHIPPED) order.setShippedAt(LocalDateTime.now());
        if (status == Order.OrderStatus.DELIVERED) order.setDeliveredAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "SN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
