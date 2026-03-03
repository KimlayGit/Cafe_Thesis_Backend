package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderRequest {
    private String paymentType; // CASH / ABA / CARD
    private BigDecimal paidAmount;
}
