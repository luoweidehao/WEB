package com.acc.lab.controller;

import com.acc.lab.dto.BlogPostRequest;
import com.acc.lab.dto.BlogPostResponse;
import com.acc.lab.dto.MessageResponse;
import com.acc.lab.service.BlogService;
import com.acc.lab.service.SessionService;
import com.acc.lab.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin(origins = "*")
public class BlogController {
    
    @Autowired
    private BlogService blogService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SessionService sessionService;
    
    // ========== 前台公开接口 ==========
    
    // 获取所有已发布的博客列表
    @GetMapping("/posts")
    public ResponseEntity<?> getPublishedPosts() {
        try {
            List<BlogPostResponse> posts = blogService.getAllPublishedPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取博客列表失败。"));
        }
    }
    
    // 根据ID获取已发布的博客详情
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPublishedPostById(@PathVariable Long id) {
        try {
            BlogPostResponse post = blogService.getPublishedPostById(id);
            // 增加浏览次数
            blogService.incrementViewCount(id);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取博客详情失败。"));
        }
    }
    
    // 搜索已发布的博客
    @GetMapping("/posts/search")
    public ResponseEntity<?> searchPublishedPosts(@RequestParam String keyword) {
        try {
            List<BlogPostResponse> posts = blogService.searchPublishedPosts(keyword);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("搜索失败。"));
        }
    }
    
    // ========== 后台管理接口 ==========
    
    // 创建博客（需要管理员权限）
    @PostMapping("/admin/posts")
    public ResponseEntity<?> createBlogPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody BlogPostRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            String role = jwtUtil.extractRole(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问，需要管理员权限。"));
            }
            
            Long authorId = jwtUtil.extractUserId(token);
            BlogPostResponse response = blogService.createBlogPost(authorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("创建博客失败，请稍后重试。"));
        }
    }
    
    // 更新博客（需要管理员权限）
    @PutMapping("/admin/posts/{id}")
    public ResponseEntity<?> updateBlogPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            String role = jwtUtil.extractRole(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问，需要管理员权限。"));
            }
            
            Long authorId = jwtUtil.extractUserId(token);
            BlogPostResponse response = blogService.updateBlogPost(id, authorId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("更新博客失败，请稍后重试。"));
        }
    }
    
    // 删除博客（需要管理员权限）
    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<?> deleteBlogPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            String role = jwtUtil.extractRole(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问，需要管理员权限。"));
            }
            
            Long authorId = jwtUtil.extractUserId(token);
            blogService.deleteBlogPost(id, authorId);
            return ResponseEntity.ok(new MessageResponse("博客删除成功。"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("删除博客失败，请稍后重试。"));
        }
    }
    
    // 获取所有博客（包括未发布的，需要管理员权限）
    @GetMapping("/admin/posts")
    public ResponseEntity<?> getAllPosts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            String role = jwtUtil.extractRole(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问，需要管理员权限。"));
            }
            
            List<BlogPostResponse> posts = blogService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取博客列表失败。"));
        }
    }
    
    // 根据ID获取博客（包括未发布的，需要管理员权限）
    @GetMapping("/admin/posts/{id}")
    public ResponseEntity<?> getPostById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("未授权访问。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            String role = jwtUtil.extractRole(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问，需要管理员权限。"));
            }
            
            BlogPostResponse post = blogService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取博客详情失败。"));
        }
    }
}

