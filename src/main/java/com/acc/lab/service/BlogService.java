package com.acc.lab.service;

import com.acc.lab.dto.BlogPostRequest;
import com.acc.lab.dto.BlogPostResponse;
import com.acc.lab.entity.BlogPost;
import com.acc.lab.entity.User;
import com.acc.lab.repository.BlogPostRepository;
import com.acc.lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {
    
    @Autowired
    private BlogPostRepository blogPostRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public BlogPostResponse createBlogPost(Long authorId, BlogPostRequest request) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
        
        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(request.getTitle());
        blogPost.setContent(request.getContent());
        blogPost.setSummary(request.getSummary());
        blogPost.setCoverImageUrl(request.getCoverImageUrl());
        blogPost.setAuthorId(authorId);
        blogPost.setAuthorName(author.getUsername());
        blogPost.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);
        blogPost.setViewCount(0L);
        
        blogPost = blogPostRepository.save(blogPost);
        return convertToResponse(blogPost);
    }
    
    @Transactional
    public BlogPostResponse updateBlogPost(Long blogId, Long authorId, BlogPostRequest request) {
        BlogPost blogPost = blogPostRepository.findById(blogId)
            .orElseThrow(() -> new IllegalArgumentException("博客文章不存在。"));
        
        // 检查权限：只有作者或管理员可以修改
        if (!blogPost.getAuthorId().equals(authorId)) {
            User user = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new IllegalArgumentException("无权修改此博客文章。");
            }
        }
        
        blogPost.setTitle(request.getTitle());
        blogPost.setContent(request.getContent());
        blogPost.setSummary(request.getSummary());
        blogPost.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getIsPublished() != null) {
            blogPost.setIsPublished(request.getIsPublished());
        }
        
        blogPost = blogPostRepository.save(blogPost);
        return convertToResponse(blogPost);
    }
    
    @Transactional
    public void deleteBlogPost(Long blogId, Long authorId) {
        BlogPost blogPost = blogPostRepository.findById(blogId)
            .orElseThrow(() -> new IllegalArgumentException("博客文章不存在。"));
        
        // 检查权限：只有作者或管理员可以删除
        if (!blogPost.getAuthorId().equals(authorId)) {
            User user = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                throw new IllegalArgumentException("无权删除此博客文章。");
            }
        }
        
        blogPostRepository.delete(blogPost);
    }
    
    @Transactional
    public BlogPostResponse incrementViewCount(Long blogId) {
        BlogPost blogPost = blogPostRepository.findById(blogId)
            .orElseThrow(() -> new IllegalArgumentException("博客文章不存在。"));
        
        blogPost.setViewCount(blogPost.getViewCount() + 1);
        blogPost = blogPostRepository.save(blogPost);
        return convertToResponse(blogPost);
    }
    
    public List<BlogPostResponse> getAllPublishedPosts() {
        List<BlogPost> posts = blogPostRepository.findByIsPublishedTrueOrderByPublishedAtDesc();
        return posts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public List<BlogPostResponse> getAllPosts() {
        List<BlogPost> posts = blogPostRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public BlogPostResponse getPublishedPostById(Long id) {
        BlogPost post = blogPostRepository.findByIdAndIsPublishedTrue(id)
            .orElseThrow(() -> new IllegalArgumentException("博客文章不存在或未发布。"));
        return convertToResponse(post);
    }
    
    public BlogPostResponse getPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("博客文章不存在。"));
        return convertToResponse(post);
    }
    
    public List<BlogPostResponse> searchPublishedPosts(String keyword) {
        List<BlogPost> posts = blogPostRepository.findByTitleContainingIgnoreCaseAndIsPublishedTrue(keyword);
        return posts.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private BlogPostResponse convertToResponse(BlogPost blogPost) {
        BlogPostResponse response = new BlogPostResponse();
        response.setId(blogPost.getId());
        response.setTitle(blogPost.getTitle());
        response.setContent(blogPost.getContent());
        response.setSummary(blogPost.getSummary());
        response.setAuthorId(blogPost.getAuthorId());
        response.setAuthorName(blogPost.getAuthorName());
        response.setCoverImageUrl(blogPost.getCoverImageUrl());
        response.setIsPublished(blogPost.getIsPublished());
        response.setViewCount(blogPost.getViewCount());
        response.setCreatedAt(blogPost.getCreatedAt());
        response.setUpdatedAt(blogPost.getUpdatedAt());
        response.setPublishedAt(blogPost.getPublishedAt());
        return response;
    }
}

