package com.example.backend.dto;

import lombok.Data;

@Data
public class CreateOrderItem {
    private Long productId;
    private Long quantity;
    private Double unitPrice;
}
