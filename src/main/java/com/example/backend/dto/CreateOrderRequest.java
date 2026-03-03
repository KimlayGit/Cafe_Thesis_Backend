package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer userId;
    private Integer customerId; // nullable
    private Integer tableId;    // nullable
    private String orderType;
    private String orderStatus;
    private List<CreateOrderItem> items;
}

