package com.example.backend.Repository;

import com.example.backend.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContainingIgnoreCase(String q);

    List<Product> findByCategory_CategoryId(Long categoryId);

    List<Product> findByCategory_CategoryIdAndProductNameContainingIgnoreCase(
            Long categoryId,
            String q
    );

    // ✅ works with Java field name, not DB column
    List<Product> findByAvailableTrue();
}
