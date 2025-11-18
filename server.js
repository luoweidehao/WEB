/**
 * 静态文件服务器：
 * - 托管当前目录下的静态页面（index.html 等）
 * 
 * 注意：
 * - 登录注册功能已迁移到 Spring Boot 后端（端口 8080）
 * - 订阅功能已迁移到 Spring Boot 后端（端口 8080）
 *
 * 启动步骤：
 * 1. 安装依赖：`npm install express`
 * 2. 运行：`node server.js`
 * 3. 浏览器访问：http://localhost:3000
 */

const path = require('path');
const express = require('express');

const app = express();
const PORT = process.env.PORT || 3000;

//中间件设置
app.use(express.static(__dirname)); // 托管静态文件

app.use((_req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`静态文件服务已在 http://localhost:${PORT} 运行`);
  console.log('注意：所有 API 功能已迁移到 Spring Boot 后端（端口 8080）');
});