package com.acc.lab.repository;

import com.acc.lab.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    /**
     * 根据标识符和类型查找未使用且未过期的验证码
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.identifier = :identifier " +
           "AND v.type = :type AND v.used = false AND v.expiresAt > :now " +
           "ORDER BY v.createdAt DESC")
    Optional<VerificationCode> findValidCode(
        @Param("identifier") String identifier,
        @Param("type") VerificationCode.CodeType type,
        @Param("now") LocalDateTime now
    );
    
    /**
     * 根据标识符和类型查找验证码（不检查过期和是否使用）
     */
    Optional<VerificationCode> findByIdentifierAndType(String identifier, VerificationCode.CodeType type);
    
    /**
     * 删除过期的验证码
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
    
    /**
     * 删除指定标识符和类型的所有验证码
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.identifier = :identifier AND v.type = :type")
    void deleteByIdentifierAndType(@Param("identifier") String identifier, @Param("type") VerificationCode.CodeType type);
}

