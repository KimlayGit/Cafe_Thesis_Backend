package com.example.backend.Controller;

import com.example.backend.Service.KhqrService;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.KhqrCreateResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/khqr")
@CrossOrigin(origins = "http://localhost:5173")
public class KhqrController {

    private final KhqrService khqrService;

    public KhqrController(KhqrService khqrService) {
        this.khqrService = khqrService;
    }

    @GetMapping("/status/{orderId}")
    public Map<String, String> checkStatus(@PathVariable Integer orderId) {
        String status = khqrService.checkStatus(orderId);
        return Map.of("status", status);
    }
    @PostMapping("/create/{orderId}")
    public KhqrCreateResponse create(@PathVariable Integer orderId) {
        return khqrService.createForOrder(orderId);
    }
}