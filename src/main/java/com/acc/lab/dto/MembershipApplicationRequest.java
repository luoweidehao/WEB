package com.acc.lab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MembershipApplicationRequest {
    
    @NotBlank(message = "姓名不能为空")
    private String fullName;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCard;
    
    private String phone;
    
    @NotBlank(message = "单位不能为空")
    private String institution;
    
    @NotBlank(message = "职务/职称不能为空")
    private String position;
    
    @NotBlank(message = "执业医师证照片不能为空")
    private String doctorCertificateUrl;
    
    @NotBlank(message = "在职证明不能为空")
    private String employmentProofUrl;
    
    private String notes;  // 备注（可选）
}

