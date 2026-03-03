package com.example.backend.dto;

import lombok.Data;

@Data
public class ProductResponse {

    private Integer productId;
    private String productName;
    private Double price;
    private Integer stockQty;
    private Boolean available;
    private String image;

    private Integer categoryId;
    private String categoryName;
}


