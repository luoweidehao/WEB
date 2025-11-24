package com.acc.lab.repository;

import com.acc.lab.entity.MembershipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipApplicationRepository extends JpaRepository<MembershipApplication, Long> {
    
    List<MembershipApplication> findByUserId(Long userId);
    
    List<MembershipApplication> findByStatus(String status);
    
    Optional<MembershipApplication> findByUserIdAndStatus(Long userId, String status);
    
    boolean existsByUserIdAndStatus(Long userId, String status);
}

