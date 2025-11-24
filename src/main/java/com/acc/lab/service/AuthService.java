package com.acc.lab.service;

import com.acc.lab.dto.AuthResponse;
import com.acc.lab.dto.LoginRequest;
import com.acc.lab.dto.MessageResponse;
import com.acc.lab.dto.RegisterRequest;
import com.acc.lab.dto.RequestResetRequest;
import com.acc.lab.dto.ResetPasswordRequest;
import com.acc.lab.entity.User;
import com.acc.lab.exception.TooManyRequestsException;
import com.acc.lab.repository.UserRepository;
import com.acc.lab.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class AuthService {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    private static final Random random = new Random();
    
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 验证邮箱格式
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("请输入正确的邮箱地址。");
        }
        
        // 检查用户名或邮箱是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已被注册。");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("电子邮箱已被注册。");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        
        user = userRepository.save(user);
        
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user.getUsername(), user.getRole(), user.getMembership());
        return new AuthResponse("用户注册成功!", token, userInfo);
    }
    
    public AuthResponse login(LoginRequest request) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【登录请求】");
        System.out.println("  输入用户名: " + request.getUsername());
        System.out.println("  输入密码: " + request.getPassword());
        System.out.println("  密码长度: " + (request.getPassword() != null ? request.getPassword().length() : 0));
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);
        
        if (user == null) {
            System.out.println("  ❌ 用户不存在: " + request.getUsername());
            System.out.println("  数据库中所有用户名:");
            userRepository.findAll().forEach(u -> System.out.println("    - " + u.getUsername() + " (ID: " + u.getId() + ")"));
            System.out.println("=".repeat(80) + "\n");
            throw new IllegalArgumentException("用户名或密码无效。");
        }
        
        System.out.println("  ✅ 找到用户:");
        System.out.println("    ID: " + user.getId());
        System.out.println("    用户名: " + user.getUsername());
        System.out.println("    邮箱: " + user.getEmail());
        System.out.println("    角色: " + user.getRole());
        System.out.println("    数据库密码哈希: " + (user.getPasswordHash() != null ? 
            user.getPasswordHash().substring(0, Math.min(30, user.getPasswordHash().length())) + "..." : "无"));
        
        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        System.out.println("  密码匹配结果: " + (passwordMatches ? "✅ 匹配" : "❌ 不匹配"));
        
        if (!passwordMatches) {
            System.out.println("  ❌ 密码验证失败");
            System.out.println("    输入的密码: " + request.getPassword());
            System.out.println("    数据库密码哈希长度: " + (user.getPasswordHash() != null ? user.getPasswordHash().length() : 0));
            System.out.println("=".repeat(80) + "\n");
            throw new IllegalArgumentException("用户名或密码无效。");
        }
        
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        System.out.println("  ✅ 登录成功！");
        System.out.println("    生成的 Token: " + token.substring(0, Math.min(50, token.length())) + "...");
        System.out.println("=".repeat(80) + "\n");
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user.getUsername(), user.getRole(), user.getMembership());
        return new AuthResponse("登录成功!", token, userInfo);
    }
    
    @Transactional
    public MessageResponse requestPasswordReset(RequestResetRequest request) {
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("请输入正确的邮箱地址。");
        }
        
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        
        // 为了安全，即使用户不存在也返回成功消息
        if (user == null) {
            return new MessageResponse("如果邮箱地址正确,您将收到一封包含验证码的邮件。");
        }
        
        // 检查是否有有效的验证码
        LocalDateTime now = LocalDateTime.now();
        if (user.getVerificationCode() != null && 
            user.getCodeExpiryTime() != null && 
            user.getCodeExpiryTime().isAfter(now)) {
            long remainingMinutes = java.time.Duration.between(now, user.getCodeExpiryTime()).toMinutes();
            MessageResponse response = new MessageResponse();
            response.setMessage("验证码已发送，请在 " + remainingMinutes + " 分钟后再试。");
            response.setRetryAfterMinutes((int) remainingMinutes);
            throw new TooManyRequestsException(response);
        }
        
        // 生成验证码
        String code = generateVerificationCode();
        LocalDateTime expiryTime = now.plusMinutes(5);
        
        user.setVerificationCode(code);
        user.setCodeExpiryTime(expiryTime);
        userRepository.save(user);
        
        // 发送邮件
        System.out.println("\n" + "=".repeat(80));
        System.out.println("【发送密码重置验证码邮件】");
        System.out.println("  收件人邮箱: " + user.getEmail());
        System.out.println("  验证码: " + code);
        
        if (mailSender == null) {
            System.err.println("  ❌ JavaMailSender 未配置！");
            System.err.println("  请检查 application.yml 中的邮件配置");
            System.out.println("=".repeat(80) + "\n");
        } else {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("2650090110@qq.com"); // 设置发件人
                message.setTo(user.getEmail());
                message.setSubject("您的密码重置验证码");
                message.setText(String.format(
                    "尊敬的用户您好：\n\n" +
                    "您正在请求重置密码。您的验证码是：\n\n" +
                    "%s\n\n" +
                    "此验证码将在5分钟内有效。如果您没有请求重置密码,请忽略本邮件。\n\n" +
                    "—— 中欧心血管代谢学会",
                    code
                ));
                
                System.out.println("  发件人: 2650090110@qq.com");
                System.out.println("  主题: 您的密码重置验证码");
                System.out.println("  正在发送邮件...");
                
                mailSender.send(message);
                
                System.out.println("  ✅ 邮件发送成功！");
                System.out.println("=".repeat(80) + "\n");
            } catch (Exception e) {
                System.err.println("  ❌ 发送邮件失败！");
                System.err.println("  错误类型: " + e.getClass().getName());
                System.err.println("  错误消息: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("  原因: " + e.getCause().getMessage());
                }
                System.err.println("  完整堆栈跟踪:");
                e.printStackTrace();
                System.out.println("=".repeat(80) + "\n");
                // 记录错误但不抛出异常，避免泄露信息
            }
        }
        
        return new MessageResponse("如果邮箱地址正确,您将收到一封包含验证码的邮件。");
    }
    
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("请输入正确的邮箱地址。");
        }
        
        User user = userRepository.findByEmailAndVerificationCode(
            request.getEmail(), 
            request.getCode()
        ).orElseThrow(() -> new IllegalArgumentException("验证码无效或已过期。"));
        
        // 检查验证码是否过期
        if (user.getCodeExpiryTime() == null || 
            LocalDateTime.now().isAfter(user.getCodeExpiryTime())) {
            throw new IllegalArgumentException("验证码无效或已过期。");
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null);
        user.setCodeExpiryTime(null);
        userRepository.save(user);
        
        return new MessageResponse("密码重置成功!您现在可以使用新密码登录。");
    }
    
    public AuthResponse.UserInfo getCurrentUserInfo(String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
            
            return new AuthResponse.UserInfo(user.getUsername(), user.getRole(), user.getMembership());
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的token或用户不存在。");
        }
    }
}

