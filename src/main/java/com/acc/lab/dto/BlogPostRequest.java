package com.acc.lab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogPostRequest {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String summary;  // 摘要（可选）
    
    private String coverImageUrl;  // 封面图片URL（可选）
    
    private Boolean isPublished = false;  // 是否发布
}

