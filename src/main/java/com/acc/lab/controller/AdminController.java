package com.acc.lab.controller;

import com.acc.lab.dto.AuthResponse;
import com.acc.lab.dto.LoginRequest;
import com.acc.lab.dto.MessageResponse;
import com.acc.lab.service.AuthService;
import com.acc.lab.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AdminService adminService;
    
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
        try {
            // 先进行普通登录验证
            AuthResponse response = authService.login(request);
            
            // 检查是否为管理员（不区分大小写）
            if (response.getUser() == null || !"ADMIN".equalsIgnoreCase(response.getUser().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("该账户不是管理员账户，无权访问管理员系统。"));
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("服务器内部错误。"));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 简单的权限检查（实际应该使用JWT验证）
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            Map<String, Object> statistics = adminService.getStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取统计数据失败。"));
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 简单的权限检查（实际应该使用JWT验证）
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            return ResponseEntity.ok(adminService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取用户列表失败。"));
        }
    }
}

