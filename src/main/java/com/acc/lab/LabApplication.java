package com.acc.lab;

import com.acc.lab.entity.User;
import com.acc.lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.List;

@SpringBootApplication
public class LabApplication implements CommandLineRunner, ApplicationListener<ApplicationReadyEvent> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private Environment environment;
    
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
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ 应用启动成功！");
        System.out.println("=".repeat(80));
        System.out.println("🌐 本地访问地址：");
        System.out.println("   http://localhost:" + port + contextPath);
        System.out.println("   http://127.0.0.1:" + port + contextPath);
        System.out.println("=".repeat(80) + "\n");
    }
}

