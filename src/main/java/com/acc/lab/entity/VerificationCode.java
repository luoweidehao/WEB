package com.acc.lab.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes", indexes = {
    @Index(name = "idx_identifier_type", columnList = "identifier,type"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String code; // 验证码值
    
    @Column(nullable = false, length = 255)
    private String identifier; // 标识符：图片验证码的captchaId或邮箱地址
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CodeType type; // 验证码类型
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 过期时间
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 创建时间
    
    @Column(nullable = false)
    private Boolean used = false; // 是否已使用
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * 验证码类型枚举
     */
    public enum CodeType {
        CAPTCHA,         // 图片验证码（用于登录，存储在内存中）
        REGISTER,        // 注册验证码
        FORGET_PASSWORD  // 找回密码验证码
    }
}

