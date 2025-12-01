package com.acc.lab.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(length = 20)
    private String role = "USER"; // 默认角色
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(length = 20)
    private String membership; // 会员状态: "member" 表示会员, null 表示非会员
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

