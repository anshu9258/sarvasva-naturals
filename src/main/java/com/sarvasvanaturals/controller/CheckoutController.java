package com.sarvasvanaturals.controller;

import com.sarvasvanaturals.model.*;
import com.sarvasvanaturals.repository.AddressRepository;
import com.sarvasvanaturals.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final AddressRepository addressRepository;

    @GetMapping
    public String checkout(Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session) {

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.getOrCreateCart(user, session.getId());

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        List<Address> addresses = addressRepository.findByUser(user);

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("cartCount", cart.getTotalItems());
        model.addAttribute("user", user);
        return "checkout/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam Long addressId,
                              @RequestParam String paymentMethod,
                              @RequestParam(required = false) String couponCode,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartService.getOrCreateCart(user, session.getId());

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        Order.PaymentMethod pm = Order.PaymentMethod.valueOf(paymentMethod);
        Order order = orderService.createOrderFromCart(cart, address, pm, couponCode);

        if (pm == Order.PaymentMethod.COD) {
            paymentService.processCOD(order);
            return "redirect:/checkout/success/" + order.getOrderNumber();
        }

        // Redirect to payment page
        session.setAttribute("pendingOrderNumber", order.getOrderNumber());
        return "redirect:/checkout/payment/" + order.getOrderNumber();
    }

    @GetMapping("/payment/{orderNumber}")
    public String paymentPage(@PathVariable String orderNumber,
                               Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {

        Order order = orderService.getOrderByNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Determine payment method
        if (order.getPaymentMethod() == Order.PaymentMethod.RAZORPAY) {
            Map<String, Object> razorpayData = paymentService.createRazorpayOrder(order);
            model.addAttribute("razorpayData", razorpayData);
        } else if (order.getPaymentMethod() == Order.PaymentMethod.STRIPE) {
            Map<String, Object> stripeData = paymentService.createStripePaymentIntent(order);
            model.addAttribute("stripeData", stripeData);
        }

        model.addAttribute("order", order);
        return "checkout/payment";
    }

    @PostMapping("/razorpay/verify")
    @ResponseBody
    public ResponseEntity<?> verifyRazorpay(@RequestBody Map<String, String> body) {
        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");
        String orderNumber = body.get("order_number");

        boolean valid = paymentService.verifyRazorpaySignature(razorpayOrderId, razorpayPaymentId, signature);

        if (valid) {
            orderService.confirmPayment(orderNumber, razorpayPaymentId);
            return ResponseEntity.ok(Map.of("success", true, "redirect", "/checkout/success/" + orderNumber));
        } else {
            orderService.failPayment(orderNumber);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Payment verification failed"));
        }
    }

    @PostMapping("/stripe/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmStripe(@RequestBody Map<String, String> body) {
        String paymentIntentId = body.get("paymentIntentId");
        String orderNumber = body.get("orderNumber");
        orderService.confirmPayment(orderNumber, paymentIntentId);
        return ResponseEntity.ok(Map.of("success", true, "redirect", "/checkout/success/" + orderNumber));
    }

    @GetMapping("/success/{orderNumber}")
    public String orderSuccess(@PathVariable String orderNumber,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {

        Order order = orderService.getOrderByNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        return "checkout/success";
    }

    @GetMapping("/failed/{orderNumber}")
    public String orderFailed(@PathVariable String orderNumber, Model model) {
        model.addAttribute("orderNumber", orderNumber);
        return "checkout/failed";
    }

    // Address management
    @PostMapping("/address/add")
    @ResponseBody
    public ResponseEntity<?> addAddress(@RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .user(user)
                .fullName(body.get("fullName"))
                .phone(body.get("phone"))
                .addressLine1(body.get("addressLine1"))
                .addressLine2(body.get("addressLine2"))
                .city(body.get("city"))
                .state(body.get("state"))
                .pincode(body.get("pincode"))
                .type(Address.AddressType.valueOf(body.getOrDefault("type", "HOME")))
                .build();

        Address saved = addressRepository.save(address);
        return ResponseEntity.ok(Map.of("success", true, "addressId", saved.getId(),
                "address", saved.getFormattedAddress()));
    }
}
