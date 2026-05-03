package com.example.backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_khqr_payment")
@Data
public class KhqrPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer orderId;

    @Column(length = 1000)
    private String qr;

    private String md5;

    private String status; // PENDING, PAID

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}