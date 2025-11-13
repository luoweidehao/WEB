/**
 * 简单的订阅服务端：
 * - 提供 `/subscribe` 接口接收邮箱地址，并调用 nodemailer 发送邮件
 * - 同时托管当前目录下的静态页面（index.html 等）
 *
 * 启动步骤：
 * 1. 安装依赖：`npm install express nodemailer`
 * 2. 运行：`node server.js`
 * 3. 浏览器访问：http://localhost:3000
 */

const path = require('path');
const express = require('express');
const { sendEmail, DEFAULT_NEWSLETTER_TEMPLATE } = require('./send-email');

const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const cryptoRandomString = require('crypto-random-string');

const app = express();
const PORT = process.env.PORT || 3000;

//数据库连接配置
const dbConfig = {
    host: 'localhost',
    user: 'root',
    password: 'wxh83556050',
    database: 'acc_system_db'
};

//JWT配置
const JWT_SECRET = 'your-super-secret-key-that-no-one-knows';

//中间件设置
app.use(cors());
app.use(express.json());
app.use(express.static(__dirname)); // 托管静态文件

//邮箱验证
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

//邮件订阅接口
app.post('/subscribe', async (req, res) => {
  const { email } = req.body || {};

  if (!email || !isValidEmail(email)) {
    return res.status(400).json({ success: false, message: '请输入正确的邮箱地址。' });
  }

  try {
    await sendEmail(email, DEFAULT_NEWSLETTER_TEMPLATE);
    res.json({ success: true, message: '订阅成功，邮件已发送。' });
  } catch (error) {
    console.error('发送邮件失败:', error);
    res.status(500).json({ success: false, message: '邮件发送失败,请稍后再试。' });
  }
});

//用户注册
app.post('/api/register', async (req, res) => {
    try {
        const { username, email, password } = req.body;

        if (!username || !email || !password) {
            return res.status(400).json({ message: '所有字段均为必填项。' });
        }
        if (!isValidEmail(email)) {
             return res.status(400).json({ message: '请输入正确的邮箱地址。' });
        }

        const connection = await mysql.createConnection(dbConfig);
        try {
            const [existingUser] = await connection.execute(
                "SELECT * FROM users WHERE username = ? OR email = ?",
                [username, email]
            );

            if (existingUser.length > 0) {
                return res.status(409).json({ message: '用户名或电子邮箱已被注册。' });
            }

            const salt = await bcrypt.genSalt(10);
            const password_hash = await bcrypt.hash(password, salt);

            await connection.execute(
                "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)",
                [username, email, password_hash]
            );

            res.status(201).json({ message: '用户注册成功!' });

        } finally {
            await connection.end();
        }
    } catch (error) {
        console.error('注册失败:', error);
        res.status(500).json({ message: '服务器内部错误。' });
    }
});

//用户登录
app.post('/api/login', async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ message: '请输入用户名和密码。' });
        }

        const connection = await mysql.createConnection(dbConfig);
        try {
            const [rows] = await connection.execute(
                "SELECT * FROM users WHERE username = ?",
                [username]
            );

            if (rows.length === 0) {
                return res.status(401).json({ message: '用户名或密码无效。' });
            }

            const user = rows[0];
            const isMatch = await bcrypt.compare(password, user.password_hash);

            if (!isMatch) {
                return res.status(401).json({ message: '用户名或密码无效。' });
            }

            const tokenPayload = {
                userId: user.id,
                username: user.username,
                role: user.role
            };

            const token = jwt.sign(tokenPayload, JWT_SECRET, { expiresIn: '1h' }); 

            res.json({
                message: '登录成功!',
                token: token,
                user: { username: user.username, role: user.role }
            });

        } finally {
            await connection.end();
        }
    } catch (error) {
        console.error('登录失败:', error);
        res.status(500).json({ message: '服务器内部错误。' });
    }
});

//请求密码重置（发送验证码）
app.post('/api/request-reset', async (req, res) => {
    const { email } = req.body;
    if (!email || !isValidEmail(email)) {
        return res.status(400).json({ message: '请输入正确的邮箱地址。' });
    }

    const connection = await mysql.createConnection(dbConfig);
    try {
        const [users] = await connection.execute("SELECT * FROM users WHERE email = ?", [email]);
        
        if (users.length === 0) {
            return res.json({ message: '如果邮箱地址正确,您将收到一封包含验证码的邮件。' });
        }

        const code = cryptoRandomString.default({ length: 6, type: 'numeric' });
        const expiryTime = new Date(Date.now() + 15 * 60 * 1000); 

        await connection.execute(
            "UPDATE users SET verification_code = ?, code_expiry_time = ? WHERE email = ?",
            [code, expiryTime, email]
        );

        const subject = '您的密码重置验证码';
        const htmlContent = `
            <p>尊敬的用户您好：</p>
            <p>您正在请求重置密码。您的验证码是：</p>
            <h2 style="font-family: Arial, sans-serif; color: #0066CC;">${code}</h2>
            <p>此验证码将在15分钟内有效。如果您没有请求重置密码,请忽略本邮件。</p>
            <p>—— 中欧心血管代谢学会</p>
        `;
        
        await sendEmail(email, htmlContent, subject); 

        res.json({ message: '如果邮箱地址正确,您将收到一封包含验证码的邮件。' });

    } catch (error) {
        console.error('请求重置失败:', error);
        res.status(500).json({ message: '服务器错误。' });
    } finally {
        await connection.end();
    }
});

//验证并重置密码
app.post('/api/reset-password', async (req, res) => {
    const { email, code, newPassword } = req.body;

    if (!email || !code || !newPassword) {
        return res.status(400).json({ message: '所有字段均为必填项。' });
    }

    const connection = await mysql.createConnection(dbConfig);
    try {
        const [users] = await connection.execute(
            "SELECT * FROM users WHERE email = ? AND verification_code = ?",
            [email, code]
        );

        if (users.length === 0) {
            return res.status(400).json({ message: '验证码无效或已过期。' });
        }

        const user = users[0];

        if (new Date() > new Date(user.code_expiry_time)) {
            return res.status(400).json({ message: '验证码无效或已过期。' });
        }

        const salt = await bcrypt.genSalt(10);
        const password_hash = await bcrypt.hash(newPassword, salt);

        await connection.execute(
            "UPDATE users SET password_hash = ?, verification_code = NULL, code_expiry_time = NULL WHERE id = ?",
            [password_hash, user.id]
        );

        res.json({ message: '密码重置成功!您现在可以使用新密码登录。' });

    } catch (error) {
        console.error('重置密码失败:', error);
        res.status(500).json({ message: '服务器错误。' });
    } finally {
        await connection.end();
    }
});

//安全中间件（API守卫）
function verifyToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (token == null) {
        return res.status(401).json({ message: '未授权: 缺少 Token。' });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ message: '禁止访问: Token 无效或已过期。' });
        }
        req.user = user;
        next();
    });
}

// 角色检查中间件
function checkRole(...allowedRoles) {
    return (req, res, next) => {
        if (!req.user) {
            return res.status(401).json({ message: '未授权。' });
        }
        if (!allowedRoles.includes(req.user.role)) {
            return res.status(403).json({ message: '禁止访问: 权限不足。' });
        }
        next();
    };
}

app.use((_req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`订阅服务已在 http://localhost:${PORT} 运行`);
  console.log('用户认证API已激活。');
});