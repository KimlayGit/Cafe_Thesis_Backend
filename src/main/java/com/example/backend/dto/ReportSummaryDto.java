package com.example.backend.dto;

import java.util.List;

public class ReportSummaryDto {
    private Double totalSales;
    private Long totalOrders;
    private Long totalStaff;
    private Double monthlyPayroll;
    private List<SalesByDayDto> salesByDay;
    private List<TopItemDto> topItems;

    public ReportSummaryDto(
            Double totalSales,
            Long totalOrders,
            Long totalStaff,
            Double monthlyPayroll,
            List<SalesByDayDto> salesByDay,
            List<TopItemDto> topItems
    ) {
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.totalStaff = totalStaff;
        this.monthlyPayroll = monthlyPayroll;
        this.salesByDay = salesByDay;
        this.topItems = topItems;
    }

    public Double getTotalSales() { return totalSales; }
    public Long getTotalOrders() { return totalOrders; }
    public Long getTotalStaff() { return totalStaff; }
    public Double getMonthlyPayroll() { return monthlyPayroll; }
    public List<SalesByDayDto> getSalesByDay() { return salesByDay; }
    public List<TopItemDto> getTopItems() { return topItems; }
}