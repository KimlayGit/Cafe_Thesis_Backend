package com.example.backend.Model;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="tbl_payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    private String paymentType;
    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount;
    private LocalDateTime paymentDate;

    @Column(name = "change_amount", precision = 18, scale = 2)
    private BigDecimal changeAmount;


    // No Order field here ✅ because tbl_payment has NO order_id
}
