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

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.static(__dirname));

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

app.post('/subscribe', async (req, res) => {
  const { email } = req.body || {};

  if (!email || !isValidEmail(email)) {
    return res.status(400).json({ success: false, message: '请输入正确的邮箱地址。' });
  }

  try {
    await sendEmail(email, DEFAULT_NEWSLETTER_TEMPLATE);
    res.json({ success: true, message: '订阅成功，邮件已发送。' });
  } catch (error) {
    console.error('发送邮件失败：', error);
    res.status(500).json({ success: false, message: '邮件发送失败，请稍后再试。' });
  }
});

app.use((_req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`订阅服务已在 http://localhost:${PORT} 运行`);
});


