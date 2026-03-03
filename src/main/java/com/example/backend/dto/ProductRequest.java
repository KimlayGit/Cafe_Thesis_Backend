package com.example.backend.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private String productName;
    private Double price;
    private Double cost;
    private Integer stockQty;
    private Boolean available;
    private String image;
    private Integer categoryId;   // IMPORTANT: frontend sends categoryId
}
