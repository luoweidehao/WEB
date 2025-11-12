/**
 * 使用 QQ 邮箱 SMTP 向指定收件人发送固定内容邮件的示例脚本。
 *
 * 使用步骤：
 * 1. 运行 `npm init -y && npm install nodemailer` 安装依赖。
 * 2. 将 `QQ_SMTP_AUTH_CODE` 替换成你在 QQ 邮箱后台申请的“授权码”（不是登陆密码）。
 * 3. 本地运行 `node send-email.js` 发送测试邮件。
 */

const nodemailer = require('nodemailer');

async function main() {
  // 创建 SMTP 发送器（以 QQ 邮箱为例）
  const transporter = nodemailer.createTransport({
    host: 'smtp.qq.com',
    port: 465,
    secure: true,
    auth: {
      user: '2650090110@qq.com',
      pass: process.env.QQ_SMTP_AUTH_CODE || '在这里放你的授权码',
    },
  });

  // 预设邮件内容
  const mailOptions = {
    from: '"中欧心血管代谢学会" <2650090110@qq.com>',
    to: '3054467021@qq.com',
    subject: '订阅确认：最新心血管资讯',
    html: `
      <p>尊敬的订阅者您好：</p>
      <p>感谢您关注中欧心血管代谢学会。以下是本期精选内容：</p>
      <ul>
        <li>2025 年心血管代谢与泛血管疾病医学大会参会指南</li>
        <li>最新房颤诊断与治疗临床指南更新</li>
        <li>心衰患者长期管理的三项关键策略</li>
      </ul>
      <p>如需了解更多详情，请访问我们的官网或回复本邮件。</p>
      <p>—— 中欧心血管代谢学会</p>
    `,
  };

  // 发送邮件
  const info = await transporter.sendMail(mailOptions);
  console.log(`邮件发送成功：${info.messageId}`);
}

main().catch((error) => {
  console.error('邮件发送失败：', error);
  process.exit(1);
});


