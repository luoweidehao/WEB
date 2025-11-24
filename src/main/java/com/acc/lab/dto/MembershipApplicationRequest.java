package com.acc.lab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipApplicationRequest {
    
    @NotBlank(message = "姓名不能为空")
    private String fullName;
    
    private String phone;
    
    private String institution;
    
    private String position;
    
    private String specialization;
    
    private Integer yearsOfExperience;
    
    private String educationBackground;
    
    private String researchInterests;
    
    @NotBlank(message = "申请动机不能为空")
    private String motivation;
}

