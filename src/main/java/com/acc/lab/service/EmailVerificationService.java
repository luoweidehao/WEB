package com.acc.lab.service;

import com.acc.lab.entity.VerificationCode;
import com.acc.lab.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {
    
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 5;
    
    private final Random random = new Random();
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    /**
     * 生成验证码
     * @param email 邮箱地址
     * @return 6位数字验证码
     */
    @Transactional
    public String generateCode(String email) {
        // 清理过期验证码
        cleanupExpiredCodes();
        
        String emailLower = email.toLowerCase();
        
        // 查找该邮箱是否已有验证码记录
        Optional<VerificationCode> existingCode = verificationCodeRepository.findByIdentifierAndType(
            emailLower,
            VerificationCode.CodeType.REGISTER
        );
        
        // 生成6位数字验证码
        String code = String.format("%06d", random.nextInt(1000000));
        
        VerificationCode verificationCode;
        if (existingCode.isPresent()) {
            // 如果存在，更新现有记录
            verificationCode = existingCode.get();
            verificationCode.setCode(code);
            verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            verificationCode.setUsed(false);
            verificationCode.setCreatedAt(LocalDateTime.now()); // 更新创建时间
        } else {
            // 如果不存在，创建新记录
            verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setIdentifier(emailLower);
            verificationCode.setType(VerificationCode.CodeType.REGISTER);
            verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            verificationCode.setUsed(false);
        }
        
        verificationCodeRepository.save(verificationCode);
        
        return code;
    }
    
    /**
     * 验证验证码
     * @param email 邮箱地址
     * @param userInput 用户输入的验证码
     * @return 是否验证通过
     */
    @Transactional
    public boolean validateCode(String email, String userInput) {
        if (email == null || userInput == null) {
            return false;
        }
        
        // 从数据库查找有效的验证码
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findValidCode(
            email.toLowerCase(),
            VerificationCode.CodeType.REGISTER,
            LocalDateTime.now()
        );
        
        if (optionalCode.isEmpty()) {
            return false;
        }
        
        VerificationCode verificationCode = optionalCode.get();
        
        // 验证码验证（精确匹配）
        boolean isValid = verificationCode.getCode().equals(userInput.trim());
        
        // 验证后标记为已使用（一次性使用）
        if (isValid) {
            verificationCode.setUsed(true);
            verificationCodeRepository.save(verificationCode);
        }
        
        return isValid;
    }
    
    /**
     * 检查是否已发送验证码且未过期
     * @param email 邮箱地址
     * @return 是否已发送且未过期
     */
    public boolean hasValidCode(String email) {
        if (email == null) {
            return false;
        }
        
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findValidCode(
            email.toLowerCase(),
            VerificationCode.CodeType.REGISTER,
            LocalDateTime.now()
        );
        
        return optionalCode.isPresent();
    }
    
    /**
     * 检查是否可以重新发送验证码（距离创建时间是否超过1分钟）
     * @param email 邮箱地址
     * @return 剩余冷却时间（秒），如果为0或负数则表示可以发送
     */
    public long getRemainingCooldownSeconds(String email) {
        return getRemainingCooldownSeconds(email, VerificationCode.CodeType.REGISTER);
    }
    
    /**
     * 检查是否可以重新发送验证码（距离创建时间是否超过1分钟）
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 剩余冷却时间（秒），如果为0或负数则表示可以发送
     */
    public long getRemainingCooldownSeconds(String email, VerificationCode.CodeType type) {
        if (email == null) {
            return 0;
        }
        
        // 查找最新的验证码记录（包括已过期和已使用的，因为我们需要检查创建时间）
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findByIdentifierAndType(
            email.toLowerCase(),
            type
        );
        
        if (optionalCode.isEmpty()) {
            return 0; // 没有记录，可以发送
        }
        
        VerificationCode code = optionalCode.get();
        LocalDateTime createdAt = code.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        
        // 计算距离创建时间是否超过1分钟
        long secondsSinceCreation = java.time.Duration.between(createdAt, now).getSeconds();
        long cooldownSeconds = 60; // 1分钟冷却时间
        
        // 如果验证码已过期（超过5分钟），或者创建时间超过1分钟，都可以发送
        if (code.getExpiresAt().isBefore(now) || secondsSinceCreation >= cooldownSeconds) {
            return 0; // 可以发送
        } else {
            // 返回剩余冷却时间，确保不超过60秒
            long remaining = cooldownSeconds - secondsSinceCreation;
            return Math.max(0, Math.min(remaining, 60)); // 确保在0-60秒之间
        }
    }
    
    /**
     * 生成验证码（支持不同类型）
     * @param email 邮箱地址
     * @param type 验证码类型
     * @return 6位数字验证码
     */
    @Transactional
    public String generateCode(String email, VerificationCode.CodeType type) {
        // 清理过期验证码
        cleanupExpiredCodes();
        
        String emailLower = email.toLowerCase();
        
        // 查找该邮箱是否已有验证码记录
        Optional<VerificationCode> existingCode = verificationCodeRepository.findByIdentifierAndType(
            emailLower,
            type
        );
        
        // 生成6位数字验证码
        String code = String.format("%06d", random.nextInt(1000000));
        
        VerificationCode verificationCode;
        if (existingCode.isPresent()) {
            // 如果存在，更新现有记录
            verificationCode = existingCode.get();
            verificationCode.setCode(code);
            verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            verificationCode.setUsed(false);
            verificationCode.setCreatedAt(LocalDateTime.now()); // 更新创建时间
        } else {
            // 如果不存在，创建新记录
            verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setIdentifier(emailLower);
            verificationCode.setType(type);
            verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            verificationCode.setUsed(false);
        }
        
        verificationCodeRepository.save(verificationCode);
        
        return code;
    }
    
    /**
     * 验证验证码（支持不同类型）
     * @param email 邮箱地址
     * @param userInput 用户输入的验证码
     * @param type 验证码类型
     * @return 是否验证通过
     */
    @Transactional
    public boolean validateCode(String email, String userInput, VerificationCode.CodeType type) {
        if (email == null || userInput == null) {
            return false;
        }
        
        // 从数据库查找有效的验证码
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findValidCode(
            email.toLowerCase(),
            type,
            LocalDateTime.now()
        );
        
        if (optionalCode.isEmpty()) {
            return false;
        }
        
        VerificationCode verificationCode = optionalCode.get();
        
        // 验证码验证（精确匹配）
        boolean isValid = verificationCode.getCode().equals(userInput.trim());
        
        // 验证后标记为已使用（一次性使用）
        if (isValid) {
            verificationCode.setUsed(true);
            verificationCodeRepository.save(verificationCode);
        }
        
        return isValid;
    }
    
    /**
     * 清理过期的验证码
     */
    @Transactional
    public void cleanupExpiredCodes() {
        verificationCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}

