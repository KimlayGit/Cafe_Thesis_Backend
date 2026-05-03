package com.example.backend.Controller;

import com.example.backend.Model.Order;
import com.example.backend.Service.OrderService;
import com.example.backend.dto.CreateOrderRequest;
import com.example.backend.dto.PayOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ✅ Create order (SAVED)
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(req));
    }

    // ✅ Pay order (create payment + mark PAID)
    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> pay(
            @PathVariable Integer id,
            @RequestBody PayOrderRequest req
    ) {
        return ResponseEntity.ok(orderService.payOrder(id, req));
    }

    // ✅ Update status (optional)
    // ✅ Update saved order items/status
    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Integer id, @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.updateOrder(id, req));
    }

    // ✅ Cancel order (restore stock)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable Integer id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
    // Optional: list all
    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}