package com.example.backend.Model;

import com.example.backend.Model.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_order")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private LocalDateTime orderDate;
    private double totalAmount;
    private String orderStatus;
    private String orderType;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne @JoinColumn(name = "cus_id")
    private Customer customer;

    @ManyToOne @JoinColumn(name = "table_id")
    private CafeTable table;

    // tbl_order has payment_id ✅
    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderDetail> orderDetails = new ArrayList<>();
    @Column(name = "receipt_no", unique = true)
    private String receiptNo;
}
