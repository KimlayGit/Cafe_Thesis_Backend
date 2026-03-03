package com.example.backend.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tbl_role")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    private String roleName;
}
