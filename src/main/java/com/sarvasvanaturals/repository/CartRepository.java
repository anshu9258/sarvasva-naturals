package com.sarvasvanaturals.repository;
import com.sarvasvanaturals.model.Cart;
import com.sarvasvanaturals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findBySessionId(String sessionId);
}
