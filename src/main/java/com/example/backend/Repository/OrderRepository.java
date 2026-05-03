package com.example.backend.Repository;

import com.example.backend.Model.Order;
import com.example.backend.Model.OrderDetail;
import com.example.backend.dto.TopItemDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Override
    @EntityGraph(attributePaths = "orderDetails")
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = "orderDetails")
    Optional<Order> findById(Integer id);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE UPPER(o.orderStatus) = 'PAID'")
    Double getTotalSales();

    @Query("SELECT COUNT(o) FROM Order o WHERE UPPER(o.orderStatus) = 'PAID'")
    Long getTotalPaidOrders();

    @Query("""
    SELECT new com.example.backend.dto.TopItemDto(
        d.product.productName,
        SUM(d.quantity),
        d.product.image
    )
    FROM OrderDetail d
    WHERE UPPER(d.order.orderStatus) = 'PAID'
    GROUP BY d.product.productName, d.product.image
    ORDER BY SUM(d.quantity) DESC
""")
    List<TopItemDto> getTopSellingItems();
}
