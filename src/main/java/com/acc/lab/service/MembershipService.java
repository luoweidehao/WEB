package com.acc.lab.service;

import com.acc.lab.dto.MembershipApplicationRequest;
import com.acc.lab.dto.MembershipApplicationResponse;
import com.acc.lab.dto.ReviewApplicationRequest;
import com.acc.lab.entity.MembershipApplication;
import com.acc.lab.entity.User;
import com.acc.lab.repository.MembershipApplicationRepository;
import com.acc.lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipService {
    
    @Autowired
    private MembershipApplicationRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public MembershipApplicationResponse submitApplication(Long userId, MembershipApplicationRequest request) {
        // 检查用户是否已经是会员
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
        
        if ("member".equalsIgnoreCase(user.getMembership())) {
            throw new IllegalArgumentException("您已经是会员，无需重复申请。");
        }
        
        // 检查是否存在待审核的申请
        MembershipApplication existingApplication = applicationRepository
            .findByUserIdAndStatus(userId, "PENDING")
            .orElse(null);
        
        MembershipApplication application;
        if (existingApplication != null) {
            // 如果存在待审核的申请，更新它
            application = existingApplication;
            application.setFullName(request.getFullName());
            application.setIdCard(request.getIdCard());
            application.setPhone(request.getPhone());
            application.setInstitution(request.getInstitution());
            application.setPosition(request.getPosition());
            application.setDoctorCertificateUrl(request.getDoctorCertificateUrl());
            application.setEmploymentProofUrl(request.getEmploymentProofUrl());
            application.setNotes(request.getNotes());
            // 保持状态为PENDING，清除之前的审核信息
            application.setStatus("PENDING");
            application.setAdminNotes(null);
            application.setReviewedBy(null);
            application.setReviewedAt(null);
        } else {
            // 创建新的申请记录
            application = new MembershipApplication();
            application.setUserId(userId);
            application.setFullName(request.getFullName());
            application.setIdCard(request.getIdCard());
            application.setPhone(request.getPhone());
            application.setInstitution(request.getInstitution());
            application.setPosition(request.getPosition());
            application.setDoctorCertificateUrl(request.getDoctorCertificateUrl());
            application.setEmploymentProofUrl(request.getEmploymentProofUrl());
            application.setNotes(request.getNotes());
            application.setStatus("PENDING");
        }
        
        application = applicationRepository.save(application);
        
        return convertToResponse(application, user);
    }
    
    @Transactional
    public MembershipApplicationResponse updateApplication(Long userId, Long applicationId, MembershipApplicationRequest request) {
        // 检查用户是否已经是会员
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
        
        if ("member".equalsIgnoreCase(user.getMembership())) {
            throw new IllegalArgumentException("您已经是会员，无需修改申请。");
        }
        
        // 查找申请记录
        MembershipApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("申请记录不存在。"));
        
        // 检查申请是否属于该用户
        if (!application.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此申请。");
        }
        
        // 只能修改待审核状态的申请
        if (!"PENDING".equals(application.getStatus())) {
            throw new IllegalArgumentException("只能修改待审核状态的申请。");
        }
        
        // 更新申请信息
        application.setFullName(request.getFullName());
        application.setIdCard(request.getIdCard());
        application.setPhone(request.getPhone());
        application.setInstitution(request.getInstitution());
        application.setPosition(request.getPosition());
        application.setDoctorCertificateUrl(request.getDoctorCertificateUrl());
        application.setEmploymentProofUrl(request.getEmploymentProofUrl());
        application.setNotes(request.getNotes());
        // 清除之前的审核信息（如果有）
        application.setAdminNotes(null);
        application.setReviewedBy(null);
        application.setReviewedAt(null);
        
        application = applicationRepository.save(application);
        
        return convertToResponse(application, user);
    }
    
    public List<MembershipApplicationResponse> getUserApplications(Long userId) {
        List<MembershipApplication> applications = applicationRepository.findByUserId(userId);
        return applications.stream().map(app -> {
            User user = userRepository.findById(app.getUserId()).orElse(null);
            return convertToResponse(app, user);
        }).collect(Collectors.toList());
    }
    
    public List<MembershipApplicationResponse> getAllApplications(String status) {
        List<MembershipApplication> applications;
        if (status != null && !status.isEmpty()) {
            applications = applicationRepository.findByStatus(status);
        } else {
            applications = applicationRepository.findAll();
        }
        
        return applications.stream().map(app -> {
            User user = userRepository.findById(app.getUserId()).orElse(null);
            return convertToResponse(app, user);
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public MembershipApplicationResponse reviewApplication(Long applicationId, Long adminId, ReviewApplicationRequest request) {
        MembershipApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("申请记录不存在。"));
        
        if (!"PENDING".equals(application.getStatus())) {
            throw new IllegalArgumentException("该申请已经审核过了。");
        }
        
        // 更新申请状态
        application.setStatus(request.getStatus());
        application.setAdminNotes(request.getAdminNotes());
        application.setReviewedBy(adminId);
        application.setReviewedAt(LocalDateTime.now());
        application = applicationRepository.save(application);
        
        // 如果审核通过，更新用户的membership字段
        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
            user.setMembership("member");
            userRepository.save(user);
        }
        
        User user = userRepository.findById(application.getUserId()).orElse(null);
        return convertToResponse(application, user);
    }
    
    private MembershipApplicationResponse convertToResponse(MembershipApplication application, User user) {
        MembershipApplicationResponse response = new MembershipApplicationResponse();
        response.setId(application.getId());
        response.setUserId(application.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
        }
        response.setFullName(application.getFullName());
        response.setIdCard(application.getIdCard());
        response.setPhone(application.getPhone());
        response.setInstitution(application.getInstitution());
        response.setPosition(application.getPosition());
        response.setDoctorCertificateUrl(application.getDoctorCertificateUrl());
        response.setEmploymentProofUrl(application.getEmploymentProofUrl());
        response.setNotes(application.getNotes());
        response.setStatus(application.getStatus());
        response.setAdminNotes(application.getAdminNotes());
        if (application.getReviewedBy() != null) {
            User reviewer = userRepository.findById(application.getReviewedBy()).orElse(null);
            if (reviewer != null) {
                response.setReviewedBy(reviewer.getUsername());
            }
        }
        response.setReviewedAt(application.getReviewedAt());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}

