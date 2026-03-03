package com.example.backend.Model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tbl_product")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;
    @Column(name = "product_name", nullable = false)
    private String productName;

    private Double price;

    private Double cost;

    @Column(name = "stock_qty")
    private Integer stockQty;

    private Boolean available;

    @Column(length = 500)
    private String image;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
