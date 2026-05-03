package com.example.backend.Repository;

import com.example.backend.Model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByFullNameContainingIgnoreCase(String keyword);
    List<Staff> findByPositionContainingIgnoreCase(String keyword);

    @Query("SELECT COALESCE(SUM(s.salary), 0) FROM Staff s")
    Double getMonthlyPayroll();
}