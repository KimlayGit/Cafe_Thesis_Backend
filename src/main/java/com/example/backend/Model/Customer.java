package com.example.backend.Model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_customer")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    private String customerType;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createAt;
}
