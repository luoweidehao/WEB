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
    
    @Autowired
    private SessionService sessionService;
    
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
        
        // 创建 session
        sessionService.createSession(user.getId(), token);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user.getUsername(), user.getRole(), user.getMembership());
        return new AuthResponse("用户注册成功!", token, userInfo);
    }
    
    public AuthResponse login(LoginRequest request) {
        // 判断输入是邮箱还是用户名
        String input = request.getUsernameOrEmail();
        User user = null;
        
        if (isValidEmail(input)) {
            // 如果是邮箱格式，通过邮箱查找
            user = userRepository.findByEmail(input).orElse(null);
        } else {
            // 否则通过用户名查找
            user = userRepository.findByUsername(input).orElse(null);
        }
        
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码无效。");
        }
        
        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        
        if (!passwordMatches) {
            throw new IllegalArgumentException("用户名或密码无效。");
        }
        
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 创建 session
        sessionService.createSession(user.getId(), token);
        
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
        if (mailSender == null) {
            System.err.println("  ❌ JavaMailSender 未配置！");
            System.err.println("  请检查 application.yml 中的邮件配置");
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
            // 验证 session 是否存在且有效
            if (!sessionService.isValidSession(token)) {
                throw new IllegalArgumentException("Session已失效，请重新登录。");
            }
            
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
            
            return new AuthResponse.UserInfo(user.getUsername(), user.getRole(), user.getMembership());
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的token或用户不存在。");
        }
    }
    
    /**
     * 退出登录：删除 session
     */
    @Transactional
    public MessageResponse logout(String token) {
        try {
            sessionService.logout(token);
            return new MessageResponse("退出登录成功。");
        } catch (Exception e) {
            throw new IllegalArgumentException("退出登录失败。");
        }
    }
}

