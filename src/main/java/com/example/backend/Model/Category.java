package com.example.backend.Model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tbl_category")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    private String categoryName;
}
