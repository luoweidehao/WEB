package com.acc.lab.service;

import com.acc.lab.entity.User;
import com.acc.lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        
        // 计算今日新增用户
        LocalDate today = LocalDate.now();
        long todayUsers = allUsers.stream()
            .filter(user -> user.getCreatedAt() != null && 
                   user.getCreatedAt().toLocalDate().equals(today))
            .count();
        
        // 计算管理员数量（不区分大小写）
        long admins = allUsers.stream()
            .filter(user -> user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole()))
            .count();
        
        // 订阅用户数（这里暂时返回0，后续可以添加订阅功能）
        long subscribers = 0;
        
        stats.put("totalUsers", totalUsers);
        stats.put("todayUsers", todayUsers);
        stats.put("subscribers", subscribers);
        stats.put("admins", admins);
        
        return stats;
    }
    
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("membership", user.getMembership());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
        }).collect(Collectors.toList());
    }
}

