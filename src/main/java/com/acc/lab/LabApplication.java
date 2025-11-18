package com.acc.lab;

import com.acc.lab.entity.User;
import com.acc.lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class LabApplication implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    public static void main(String[] args) {
        SpringApplication.run(LabApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 等待数据库连接建立
        Thread.sleep(1000);
        
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("数据库连接成功！正在查询 users 表...");
            System.out.println("=".repeat(80));
            
            List<User> users = userRepository.findAll();
            
            if (users.isEmpty()) {
                System.out.println("users 表为空，暂无用户数据。");
            } else {
                System.out.println("users 表中共有 " + users.size() + " 条记录：\n");
                
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    System.out.println("【用户 " + (i + 1) + "】");
                    System.out.println("  ID: " + user.getId());
                    System.out.println("  用户名: " + user.getUsername());
                    System.out.println("  邮箱: " + user.getEmail());
                    System.out.println("  角色: " + (user.getRole() != null ? user.getRole() : "未设置"));
                    System.out.println("  密码哈希: " + (user.getPasswordHash() != null ? 
                        user.getPasswordHash().substring(0, Math.min(20, user.getPasswordHash().length())) + "..." : "无"));
                    System.out.println("  验证码: " + (user.getVerificationCode() != null ? user.getVerificationCode() : "无"));
                    System.out.println("  验证码过期时间: " + (user.getCodeExpiryTime() != null ? user.getCodeExpiryTime() : "无"));
                    System.out.println("  创建时间: " + (user.getCreatedAt() != null ? user.getCreatedAt() : "无"));
                    System.out.println();
                }
            }
            
            System.out.println("=".repeat(80));
            System.out.println("数据库查询完成！\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ 查询 users 表时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

