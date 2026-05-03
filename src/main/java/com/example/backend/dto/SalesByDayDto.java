package com.example.backend.dto;

public class SalesByDayDto {
    private String day;
    private Double sales;

    public SalesByDayDto(String day, Double sales) {
        this.day = day;
        this.sales = sales;
    }

    public String getDay() { return day; }
    public Double getSales() { return sales; }
}