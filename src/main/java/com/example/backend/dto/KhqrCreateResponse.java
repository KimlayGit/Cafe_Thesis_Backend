package com.example.backend.dto;

public class KhqrCreateResponse {
    private String qr;
    private String md5;
    private Double amount;
    private String status;

    public KhqrCreateResponse(String qr, String md5, Double amount, String status) {
        this.qr = qr;
        this.md5 = md5;
        this.amount = amount;
        this.status = status;
    }

    public String getQr() { return qr; }
    public String getMd5() { return md5; }
    public Double getAmount() { return amount; }
    public String getStatus() { return status; }
}