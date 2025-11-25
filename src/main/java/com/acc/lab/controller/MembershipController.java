package com.acc.lab.controller;

import com.acc.lab.dto.MembershipApplicationRequest;
import com.acc.lab.dto.MembershipApplicationResponse;
import com.acc.lab.dto.MessageResponse;
import com.acc.lab.dto.ReviewApplicationRequest;
import com.acc.lab.service.MembershipService;
import com.acc.lab.service.SessionService;
import com.acc.lab.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/membership")
@CrossOrigin(origins = "*")
public class MembershipController {
    
    @Autowired
    private MembershipService membershipService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SessionService sessionService;
    
    // 提交会员申请
    @PostMapping("/apply")
    public ResponseEntity<?> applyForMembership(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MembershipApplicationRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("请先登录。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            Long userId = jwtUtil.extractUserId(token);
            
            MembershipApplicationResponse response = membershipService.submitApplication(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("提交申请失败，请稍后重试。"));
        }
    }
    
    // 获取用户的申请记录
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("请先登录。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            Long userId = jwtUtil.extractUserId(token);
            
            List<MembershipApplicationResponse> applications = membershipService.getUserApplications(userId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取申请记录失败。"));
        }
    }
    
    // 更新会员申请
    @PutMapping("/update/{applicationId}")
    public ResponseEntity<?> updateApplication(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long applicationId,
            @Valid @RequestBody MembershipApplicationRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("请先登录。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            Long userId = jwtUtil.extractUserId(token);
            
            MembershipApplicationResponse response = membershipService.updateApplication(userId, applicationId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("更新申请失败，请稍后重试。"));
        }
    }
    
    // 管理员：获取所有申请
    @GetMapping("/admin/applications")
    public ResponseEntity<?> getAllApplications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status) {
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
                    .body(new MessageResponse("无权访问。"));
            }
            
            List<MembershipApplicationResponse> applications = membershipService.getAllApplications(status);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("获取申请列表失败。"));
        }
    }
    
    // 管理员：审核申请
    @PostMapping("/admin/review/{applicationId}")
    public ResponseEntity<?> reviewApplication(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long applicationId,
            @Valid @RequestBody ReviewApplicationRequest request) {
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
            
            Long adminId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);
            
            if (!"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("无权访问。"));
            }
            
            if (!"APPROVED".equalsIgnoreCase(request.getStatus()) && 
                !"REJECTED".equalsIgnoreCase(request.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("审核状态无效，必须是 APPROVED 或 REJECTED。"));
            }
            
            MembershipApplicationResponse response = membershipService.reviewApplication(
                applicationId, adminId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("审核失败，请稍后重试。"));
        }
    }
    
    // 上传图片
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("请先登录。"));
            }
            
            String token = authHeader.substring(7);
            
            // 验证 session
            if (!sessionService.isValidSession(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Session已失效，请重新登录。"));
            }
            
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("文件不能为空。"));
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("只能上传图片文件。"));
            }
            
            // 检查文件大小（限制为 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("图片大小不能超过 5MB。"));
            }
            
            // 创建上传目录
            String uploadDir = "photos/membership/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            String filepath = uploadDir + filename;
            
            // 保存文件
            Path path = Paths.get(filepath);
            Files.write(path, file.getBytes());
            
            // 返回文件路径
            Map<String, String> response = new HashMap<>();
            response.put("url", "/" + filepath.replace("\\", "/"));
            response.put("message", "上传成功");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("文件上传失败：" + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("上传失败，请稍后重试。"));
        }
    }
}
