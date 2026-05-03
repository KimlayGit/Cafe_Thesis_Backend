package com.example.backend.Repository;

import com.example.backend.Model.KhqrPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhqrPaymentRepository extends JpaRepository<KhqrPayment, Integer> {
    Optional<KhqrPayment> findByOrderId(Integer orderId);
    Optional<KhqrPayment> findByMd5(String md5);
}