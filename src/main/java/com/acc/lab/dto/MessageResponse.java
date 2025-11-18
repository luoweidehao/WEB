package com.acc.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private Integer retryAfterMinutes; // 用于密码重置的冷却时间
    
    // 只接受 message 参数的构造器
    public MessageResponse(String message) {
        this.message = message;
        this.retryAfterMinutes = null;
    }
}

