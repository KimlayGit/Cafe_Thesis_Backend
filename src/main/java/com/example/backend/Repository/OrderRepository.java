package com.example.backend.Repository;

import com.example.backend.Model.Order;
import com.example.backend.Model.OrderDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Override
    @EntityGraph(attributePaths = "orderDetails")
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = "orderDetails")
    Optional<Order> findById(Integer id);
}
