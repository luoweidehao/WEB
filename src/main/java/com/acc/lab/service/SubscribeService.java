package com.acc.lab.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SubscribeService {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    
    private static final String DEFAULT_SUBJECT = "订阅确认：最新心血管资讯";
    private static final String DEFAULT_NEWSLETTER_TEMPLATE = 
        "尊敬的订阅者您好：\n\n" +
        "感谢您关注中欧心血管代谢学会。以下是本期精选内容：\n\n" +
        "• 2025 年心血管代谢与泛血管疾病医学大会参会指南\n" +
        "• 最新房颤诊断与治疗临床指南更新\n" +
        "• 心衰患者长期管理的三项关键策略\n\n" +
        "如需了解更多详情，请访问我们的官网或回复本邮件。\n\n" +
        "—— 中欧心血管代谢学会";
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public void sendNewsletterEmail(String email) {
        if (mailSender == null) {
            System.err.println("  ❌ JavaMailSender 未配置！");
            System.err.println("  请检查 application.yml 中的邮件配置");
            throw new IllegalStateException("邮件服务未配置");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("2650090110@qq.com");
            message.setTo(email);
            message.setSubject(DEFAULT_SUBJECT);
            message.setText(DEFAULT_NEWSLETTER_TEMPLATE);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("  ❌ 发送邮件失败！");
            System.err.println("  错误类型: " + e.getClass().getName());
            System.err.println("  错误消息: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("  原因: " + e.getCause().getMessage());
            }
            System.err.println("  完整堆栈跟踪:");
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}

