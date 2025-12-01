package com.acc.lab.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final int EXPIRE_MINUTES = 5;
    
    private final Random random = new Random();
    
    // 内存存储验证码：key为captchaId，value为验证码信息
    private final Map<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();
    
    private Thread cleanupThread;
    private volatile boolean running = true;
    
    // 初始化清理线程
    @PostConstruct
    public void init() {
        cleanupThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000); // 每分钟清理一次
                    cleanupExpiredCaptchas();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    // 停止清理线程
    @PreDestroy
    public void destroy() {
        running = false;
        if (cleanupThread != null) {
            cleanupThread.interrupt();
        }
    }
    
    /**
     * 生成验证码图片
     * @param captchaId 验证码ID（可以是sessionId或其他唯一标识）
     * @return 包含图片Base64和验证码ID的Map
     */
    public Map<String, String> generateCaptcha(String captchaId) {
        // 清理过期验证码
        cleanupExpiredCaptchas();
        
        // 生成验证码字符串
        String code = generateRandomCode();
        
        // 创建验证码图片
        BufferedImage image = createCaptchaImage(code);
        
        // 将图片转换为Base64
        String imageBase64 = imageToBase64(image);
        
        // 存储验证码到内存（覆盖旧的验证码）
        long expiryTime = System.currentTimeMillis() + EXPIRE_MINUTES * 60 * 1000;
        captchaStore.put(captchaId, new CaptchaInfo(code, expiryTime));
        
        Map<String, String> result = new HashMap<>();
        result.put("image", "data:image/png;base64," + imageBase64);
        result.put("captchaId", captchaId);
        
        return result;
    }
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param userInput 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean validateCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        
        CaptchaInfo info = captchaStore.get(captchaId);
        if (info == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > info.expiryTime) {
            captchaStore.remove(captchaId);
            return false;
        }
        
        // 验证码验证（不区分大小写）
        boolean isValid = info.code.equalsIgnoreCase(userInput.trim());
        
        // 验证后删除验证码（一次性使用）
        if (isValid) {
            captchaStore.remove(captchaId);
        }
        
        return isValid;
    }
    
    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    /**
     * 创建验证码图片
     */
    private BufferedImage createCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 28));
        int x = 20;
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(new Color(
                random.nextInt(100) + 50,
                random.nextInt(100) + 50,
                random.nextInt(100) + 50
            ));
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.3;
            g.rotate(angle, x, HEIGHT / 2);
            
            g.drawString(String.valueOf(code.charAt(i)), x, HEIGHT / 2 + 10);
            
            g.rotate(-angle, x, HEIGHT / 2);
            x += 25;
        }
        
        // 绘制干扰点
        for (int i = 0; i < 30; i++) {
            int dotX = random.nextInt(WIDTH);
            int dotY = random.nextInt(HEIGHT);
            g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            g.fillOval(dotX, dotY, 2, 2);
        }
        
        g.dispose();
        return image;
    }
    
    /**
     * 将图片转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanupExpiredCaptchas() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
    
    /**
     * 验证码信息内部类
     */
    private static class CaptchaInfo {
        final String code;
        final long expiryTime;
        
        CaptchaInfo(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}

