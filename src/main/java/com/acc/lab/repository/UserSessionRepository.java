package com.acc.lab.repository;

import com.acc.lab.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findByToken(String token);
    
    Optional<UserSession> findByTokenAndIsActiveTrue(String token);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.token = :token")
    void deleteByToken(@Param("token") String token);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.isActive = true")
    long countActiveSessionsByUserId(@Param("userId") Long userId);
}

