package com.example.backend.Repository;

import com.example.backend.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByProductNameContainingIgnoreCase(String q);

    List<Product> findByCategory_CategoryId(Integer categoryId);

    List<Product> findByCategory_CategoryIdAndProductNameContainingIgnoreCase(
            Integer categoryId,
            String q
    );

    // ✅ works with Java field name, not DB column
    List<Product> findByAvailableTrue();
}
