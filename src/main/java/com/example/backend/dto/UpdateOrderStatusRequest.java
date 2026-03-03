package com.example.backend.dto;

public class UpdateOrderStatusRequest {
    private String orderStatus; // "PAID", "SAVED"
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}