package com.acc.lab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewApplicationRequest {
    
    @NotBlank(message = "审核状态不能为空")
    private String status; // APPROVED 或 REJECTED
    
    private String adminNotes;
}

