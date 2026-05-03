package com.example.backend.Repository;

import com.example.backend.Model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("""
        SELECT i
        FROM Ingredient i
        WHERE i.isActive = true
          AND i.stockQty <= i.minStockLevel
    """)
    List<Ingredient> findLowStockIngredients();
}