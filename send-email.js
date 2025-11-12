/**
 * 使用 QQ 邮箱 SMTP 向订阅者批量发送固定内容邮件的示例脚本。
 *
 * 使用步骤：
 * 1. 运行 `npm init -y && npm install nodemailer` 安装依赖。
 * 2. 直接修改下方 `QQ_SMTP_AUTH_CODE` 常量为你的 QQ 邮箱授权码。
 * 3. 用 `node send-email.js` 启动脚本，或通过 `require('./send-email').sendNewsletterEmail` 在其他模块中调用。
 */

const nodemailer = require('nodemailer');

// 在这里维护订阅者邮箱列表
const recipients = [
  '3054467021@qq.com',
  // 'another@example.com',
];

// ⚠️ 注意：以下常量包含真实授权码时，请妥善保管，避免上传到公开仓库
const QQ_SMTP_AUTH_CODE = 'jcgxyafodrtvecdg';

const transporter = nodemailer.createTransport({
  host: 'smtp.qq.com',
  port: 465,
  secure: true,
  auth: {
    user: '2650090110@qq.com',
    pass: QQ_SMTP_AUTH_CODE,
  },
});

const DEFAULT_SUBJECT = '订阅确认：最新心血管资讯';
const DEFAULT_NEWSLETTER_TEMPLATE = `
  <p>尊敬的订阅者您好：</p>
  <p>感谢您关注中欧心血管代谢学会。以下是本期精选内容：</p>
  <ul>
    <li>2025 年心血管代谢与泛血管疾病医学大会参会指南</li>
    <li>最新房颤诊断与治疗临床指南更新</li>
    <li>心衰患者长期管理的三项关键策略</li>
  </ul>
  <p>如需了解更多详情，请访问我们的官网或回复本邮件。</p>
  <p>—— 中欧心血管代谢学会</p>
`;

function buildMailOptions(toAddress, htmlContent, subject = DEFAULT_SUBJECT) {
  return {
    from: '"中欧心血管代谢学会" <2650090110@qq.com>',
    to: toAddress,
    subject,
    html: htmlContent,
  };
}

async function sendEmail(toAddress, htmlContent, subject = DEFAULT_SUBJECT) {
  if (!toAddress || !htmlContent) {
    throw new Error('sendEmail 需要提供收件人邮箱和邮件内容。');
  }

  const info = await transporter.sendMail(buildMailOptions(toAddress, htmlContent, subject));
  console.log(`邮件已发送至 ${toAddress}，MessageId: ${info.messageId}`);
  return info;
}

async function sendBulkEmails(list = recipients) {
  for (const email of list) {
    await sendEmail(email, DEFAULT_NEWSLETTER_TEMPLATE);
  }
}

async function sendNewsletterEmail(toAddress) {
  return sendEmail(toAddress, DEFAULT_NEWSLETTER_TEMPLATE);
}

if (require.main === module) {
  sendBulkEmails().catch((error) => {
    console.error('邮件发送失败：', error);
    process.exit(1);
  });
}

module.exports = {
  sendEmail,
  sendNewsletterEmail,
  sendBulkEmails,
  recipients,
  DEFAULT_NEWSLETTER_TEMPLATE,
};
