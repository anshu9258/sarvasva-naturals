package com.sarvasvanaturals.repository;
import com.sarvasvanaturals.model.Product;
import com.sarvasvanaturals.model.Review;
import com.sarvasvanaturals.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductAndApprovedTrue(Product product);
    boolean existsByProductAndUser(Product product, User user);
}
