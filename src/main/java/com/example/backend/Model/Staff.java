package com.example.backend.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_staff")
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "position", nullable = false, length = 50)
    private String position;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (hireDate == null) hireDate = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "ACTIVE";
    }
}