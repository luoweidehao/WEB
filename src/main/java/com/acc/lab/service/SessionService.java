package com.acc.lab.service;

import com.acc.lab.entity.UserSession;
import com.acc.lab.repository.UserSessionRepository;
import com.acc.lab.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SessionService {
    
    @Autowired
    private UserSessionRepository sessionRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * 创建新的 session
     */
    @Transactional
    public UserSession createSession(Long userId, String token) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setToken(token);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(expiration / 1000));
        session.setIsActive(true);
        return sessionRepository.save(session);
    }
    
    /**
     * 验证 token 是否有效（检查 session 是否存在且活跃）
     */
    public boolean isValidSession(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // 先验证 JWT token 本身是否有效
        try {
            if (jwtUtil.extractExpiration(token).before(new java.util.Date())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        // 检查 session 是否存在且活跃
        Optional<UserSession> sessionOpt = sessionRepository.findByTokenAndIsActiveTrue(token);
        return sessionOpt.isPresent();
    }
    
    /**
     * 根据 token 获取 session
     */
    public Optional<UserSession> getSessionByToken(String token) {
        return sessionRepository.findByTokenAndIsActiveTrue(token);
    }
    
    /**
     * 退出登录：删除 session
     */
    @Transactional
    public void logout(String token) {
        sessionRepository.deleteByToken(token);
    }
    
    /**
     * 删除用户的所有 session（用于强制下线等场景）
     */
    @Transactional
    public void logoutAllSessions(Long userId) {
        sessionRepository.deleteByUserId(userId);
    }
    
    /**
     * 清理过期的 session
     */
    @Transactional
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}

