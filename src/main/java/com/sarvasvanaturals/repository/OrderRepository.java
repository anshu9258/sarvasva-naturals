package com.sarvasvanaturals.repository;
import com.sarvasvanaturals.model.Order;
import com.sarvasvanaturals.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Order> findByStripePaymentIntentId(String intentId);

    // JPQL queries - no table name issues with H2
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = 'PAID'")
    long countPaidOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenue();
}
