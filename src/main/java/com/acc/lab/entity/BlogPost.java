package com.acc.lab.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;  // 标题
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 内容（支持HTML）
    
    @Column(length = 500)
    private String summary;  // 摘要
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;  // 作者ID
    
    @Column(name = "author_name", length = 100)
    private String authorName;  // 作者名称
    
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;  // 封面图片URL
    
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;  // 是否发布
    
    @Column(name = "view_count")
    private Long viewCount = 0L;  // 浏览次数
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;  // 发布时间
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPublished && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (isPublished && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}

