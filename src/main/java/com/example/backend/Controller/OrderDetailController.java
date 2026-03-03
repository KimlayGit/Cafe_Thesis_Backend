package com.example.backend.Controller;

import com.example.backend.Model.OrderDetail;
import com.example.backend.Repository.OrderDetailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderDetailController {

    private final OrderDetailRepository repo;

    public OrderDetailController(OrderDetailRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetail>> getByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(repo.findByOrder_OrderId(orderId));
    }
}