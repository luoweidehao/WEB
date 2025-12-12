package com.acc.lab.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlogPostResponse {
    
    private Long id;
    
    private String title;
    
    private String content;
    
    private String summary;
    
    private Long authorId;
    
    private String authorName;
    
    private String coverImageUrl;
    
    private Boolean isPublished;
    
    private Long viewCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime publishedAt;
}

