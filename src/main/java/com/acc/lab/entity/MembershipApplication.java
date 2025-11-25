package com.acc.lab.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "membership_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(name = "id_card", nullable = false, length = 18)
    private String idCard;  // 身份证号
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "institution", nullable = false, length = 200)
    private String institution;  // 单位
    
    @Column(name = "position", nullable = false, length = 100)
    private String position;  // 职务/职称
    
    @Column(name = "doctor_certificate_url", nullable = false, length = 500)
    private String doctorCertificateUrl;  // 执业医师证照片路径
    
    @Column(name = "employment_proof_url", nullable = false, length = 500)
    private String employmentProofUrl;  // 在职证明照片路径
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;  // 备注
    
    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

