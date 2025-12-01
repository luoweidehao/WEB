package com.acc.lab.service;

import com.acc.lab.dto.AuthResponse;
import com.acc.lab.dto.LoginRequest;
import com.acc.lab.dto.MessageResponse;
import com.acc.lab.dto.RegisterRequest;
import com.acc.lab.dto.RequestResetRequest;
import com.acc.lab.dto.ResetPasswordRequest;
import com.acc.lab.entity.User;
import com.acc.lab.entity.VerificationCode;
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
    
    @Autowired
    private CaptchaService captchaService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    private static final Random random = new Random();
    
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    private String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * 发送注册邮箱验证码
     */
    @Transactional
    public MessageResponse sendRegisterCode(String email) {
        // 验证邮箱格式
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("请输入正确的邮箱地址。");
        }
        
        // 检查邮箱是否已被注册
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("该邮箱已被注册。");
        }
        
        // 检查是否可以重新发送（距离创建时间是否超过1分钟）
        long remainingCooldownSeconds = emailVerificationService.getRemainingCooldownSeconds(
            email,
            VerificationCode.CodeType.REGISTER
        );
        if (remainingCooldownSeconds > 0) {
            MessageResponse response = new MessageResponse();
            response.setMessage("验证码发送过于频繁，请稍后再试。");
            response.setRetryAfterSeconds((int) remainingCooldownSeconds);
            throw new TooManyRequestsException(response);
        }
        
        // 生成验证码
        String code = emailVerificationService.generateCode(email, VerificationCode.CodeType.REGISTER);
        
        // 发送邮件
        if (mailSender == null) {
            System.err.println("  ❌ JavaMailSender 未配置！");
            System.err.println("  请检查 application.yml 中的邮件配置");
            // 开发环境：直接返回验证码（仅用于测试）
            return new MessageResponse("验证码已生成（开发模式）: " + code);
        } else {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("2650090110@qq.com");
                message.setTo(email);
                message.setSubject("您的注册验证码");
                message.setText(String.format(
                    "尊敬的用户您好：\n\n" +
                    "您正在注册中欧心血管代谢学会账户。您的验证码是：\n\n" +
                    "%s\n\n" +
                    "此验证码将在5分钟内有效。如果您没有注册账户,请忽略本邮件。\n\n" +
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
                throw new IllegalArgumentException("发送验证码失败，请稍后重试。");
            }
        }
        
        return new MessageResponse("验证码已发送到您的邮箱，请查收。");
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 验证邮箱格式
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("请输入正确的邮箱地址。");
        }
        
        // 验证邮箱验证码
        if (request.getEmailCode() == null || request.getEmailCode().trim().isEmpty()) {
            throw new IllegalArgumentException("请填写邮箱验证码。");
        }
        
        if (!emailVerificationService.validateCode(request.getEmail(), request.getEmailCode(), VerificationCode.CodeType.REGISTER)) {
            throw new IllegalArgumentException("邮箱验证码错误或已过期，请重新获取。");
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
        // 验证图片验证码
        if (request.getCaptchaId() == null || request.getCaptcha() == null) {
            throw new IllegalArgumentException("请填写验证码。");
        }
        
        if (!captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptcha())) {
            throw new IllegalArgumentException("验证码错误或已过期，请刷新后重试。");
        }
        
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
        
        // 用户不存在，直接返回错误
        if (user == null) {
            throw new IllegalArgumentException("该邮箱地址未注册，无法找回密码。");
        }
        
        // 检查是否可以重新发送（距离创建时间是否超过1分钟）
        long remainingCooldownSeconds = emailVerificationService.getRemainingCooldownSeconds(
            request.getEmail(),
            VerificationCode.CodeType.FORGET_PASSWORD
        );
        if (remainingCooldownSeconds > 0) {
            MessageResponse response = new MessageResponse();
            response.setMessage("验证码发送过于频繁，请稍后再试。");
            response.setRetryAfterSeconds((int) remainingCooldownSeconds);
            throw new TooManyRequestsException(response);
        }
        
        // 生成验证码（使用VerificationCode表）
        String code = emailVerificationService.generateCode(
            request.getEmail(),
            VerificationCode.CodeType.FORGET_PASSWORD
        );
        
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
        
        // 验证验证码（使用VerificationCode表）
        boolean isValid = emailVerificationService.validateCode(
            request.getEmail(),
            request.getCode(),
            VerificationCode.CodeType.FORGET_PASSWORD
        );
        
        if (!isValid) {
            throw new IllegalArgumentException("验证码无效或已过期。");
        }
        
        // 查找用户
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
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

