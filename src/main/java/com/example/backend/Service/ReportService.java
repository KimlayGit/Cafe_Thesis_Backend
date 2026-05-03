package com.example.backend.Service;

import com.example.backend.Repository.OrderRepository;
import com.example.backend.Repository.StaffRepository;
import com.example.backend.dto.ReportSummaryDto;
import com.example.backend.dto.SalesByDayDto;
import com.example.backend.dto.TopItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final StaffRepository staffRepository;

    public ReportService(OrderRepository orderRepository, StaffRepository staffRepository) {
        this.orderRepository = orderRepository;
        this.staffRepository = staffRepository;
    }

    public ReportSummaryDto getSummary() {
        Double totalSales = orderRepository.getTotalSales();
        Long totalOrders = orderRepository.getTotalPaidOrders();
        Long totalStaff = staffRepository.count();
        Double monthlyPayroll = staffRepository.getMonthlyPayroll();

        List<TopItemDto> topItems = orderRepository.getTopSellingItems();

        List<SalesByDayDto> salesByDay = List.of(
                new SalesByDayDto("Mon", 0.0),
                new SalesByDayDto("Tue", 0.0),
                new SalesByDayDto("Wed", 0.0),
                new SalesByDayDto("Thu", 0.0),
                new SalesByDayDto("Fri", 0.0),
                new SalesByDayDto("Sat", 0.0),
                new SalesByDayDto("Sun", totalSales)
        );

        return new ReportSummaryDto(
                totalSales,
                totalOrders,
                totalStaff,
                monthlyPayroll,
                salesByDay,
                topItems
        );
    }
}