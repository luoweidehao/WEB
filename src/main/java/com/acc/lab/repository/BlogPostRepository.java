package com.acc.lab.repository;

import com.acc.lab.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    // 查找所有已发布的博客，按发布时间倒序
    List<BlogPost> findByIsPublishedTrueOrderByPublishedAtDesc();
    
    // 查找所有博客（包括未发布的），按创建时间倒序
    List<BlogPost> findAllByOrderByCreatedAtDesc();
    
    // 根据作者ID查找博客
    List<BlogPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // 根据标题模糊搜索
    List<BlogPost> findByTitleContainingIgnoreCaseAndIsPublishedTrue(String keyword);
    
    // 根据ID查找已发布的博客
    Optional<BlogPost> findByIdAndIsPublishedTrue(Long id);
}

