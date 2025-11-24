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
            // 静默连接数据库，不打印成功信息
            userRepository.findAll();
        } catch (Exception e) {
            System.err.println("\n❌ 查询 users 表时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

