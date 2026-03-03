package com.example.backend.Model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tbl_cafe_table")
@Data

public class CafeTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tableId;
    @Column(name = "table_number",nullable = false)
    private int tableNumber;
    @Column(name = "table_status")
    private String tableStatus;
}
