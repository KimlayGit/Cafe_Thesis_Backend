package com.example.backend.dto;

import lombok.Data;

@Data
public class CreateOrderItem {
    private Integer productId;
    private Integer quantity;
    private Double unitPrice;
}
