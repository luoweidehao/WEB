package com.acc.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipApplicationResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String idCard;
    private String phone;
    private String institution;
    private String position;
    private String doctorCertificateUrl;
    private String employmentProofUrl;
    private String notes;
    private String status;
    private String adminNotes;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

