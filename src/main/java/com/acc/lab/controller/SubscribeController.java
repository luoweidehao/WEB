package com.acc.lab.controller;

import com.acc.lab.dto.SubscribeRequest;
import com.acc.lab.dto.SubscribeResponse;
import com.acc.lab.service.SubscribeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SubscribeController {
    
    @Autowired
    private SubscribeService subscribeService;
    
    @PostMapping("/subscribe")
    public ResponseEntity<SubscribeResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        try {
            // 验证邮箱格式
            if (!subscribeService.isValidEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SubscribeResponse(false, "请输入正确的邮箱地址。"));
            }
            
            // 发送订阅邮件
            subscribeService.sendNewsletterEmail(request.getEmail());
            
            return ResponseEntity.ok(new SubscribeResponse(true, "订阅成功，邮件已发送。"));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SubscribeResponse(false, "邮件服务未配置，请联系管理员。"));
        } catch (Exception e) {
            System.err.println("订阅处理失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SubscribeResponse(false, "邮件发送失败,请稍后再试。"));
        }
    }
}

