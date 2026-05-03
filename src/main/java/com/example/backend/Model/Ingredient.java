package com.example.backend.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_ingredient")
@Data
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "stock_qty", nullable = false)
    private Double stockQty;

    @Column(name = "min_stock_level", nullable = false)
    private Double minStockLevel;

    @Column(name = "cost_per_unit")
    private Double costPerUnit;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (stockQty == null) stockQty = 0.0;
        if (minStockLevel == null) minStockLevel = 0.0;
        if (costPerUnit == null) costPerUnit = 0.0;
        if (isActive == null) isActive = true;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}