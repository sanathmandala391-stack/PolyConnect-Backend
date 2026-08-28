const https = require('https');
const http = require('http');
const nodemailer = require('nodemailer');

async function main() {
  const backendUrl = (process.env.RENDER_BACKEND_URL || '').trim().replace(/\/$/, '');
  const secret = (process.env.AUTOMATION_SECRET || '').trim();
  const gmailUser = (process.env.GMAIL_USER || '').trim();
  const gmailPass = (process.env.GMAIL_APP_PASSWORD || '').replace(/\s+/g, '').trim();

  if (!backendUrl) {
    console.error('❌ Error: RENDER_BACKEND_URL secret is not configured in GitHub Secrets!');
    process.exit(1);
  }
  if (!gmailUser || !gmailPass) {
    console.error('❌ Error: MAIL_USERNAME or MAIL_PASSWORD secret is not configured in GitHub Secrets!');
    process.exit(1);
  }

  console.log('📡 1. Calling Render backend to process SBTET attendance and sync MySQL database...');
  console.log(`Target URL: ${backendUrl}/automation/attendance`);

  const targetUrl = new URL(`${backendUrl}/automation/attendance`);
  const postData = '';

  const options = {
    hostname: targetUrl.hostname,
    port: targetUrl.port || (targetUrl.protocol === 'https:' ? 443 : 80),
    path: targetUrl.pathname + targetUrl.search,
    method: 'POST',
    headers: {
      'X-Automation-Secret': secret,
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(postData)
    }
  };

  const client = targetUrl.protocol === 'https:' ? https : http;

  const responseData = await new Promise((resolve, reject) => {
    const req = client.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          return reject(new Error(`Backend returned HTTP ${res.statusCode}: ${data}`));
        }
        try {
          resolve(JSON.parse(data));
        } catch (e) {
          reject(new Error(`Failed to parse backend JSON response: ${data}`));
        }
      });
    });

    req.on('error', (err) => reject(new Error(`Network error calling backend: ${err.message}`)));
    req.write(postData);
    req.end();
  });

  console.log(`✅ Backend processed students. Total: ${responseData.totalProcessed || 0}`);
  const emails = responseData.emails || [];
  console.log(`📬 Total student emails to send: ${emails.length}`);

  if (emails.length === 0) {
    console.log('ℹ️ No student attendance emails to dispatch today.');
    return;
  }

  console.log('🔐 2. Connecting to Gmail SMTP from GitHub Actions runner...');
  const transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    secure: true, // SSL
    auth: {
      user: gmailUser,
      pass: gmailPass
    }
  });

  await transporter.verify();
  console.log('✅ Gmail SMTP connected and verified successfully!');

  let sentCount = 0;
  let failCount = 0;

  for (const item of emails) {
    try {
      console.log(`📤 Sending to [${item.pin || 'STUDENT'}] ${item.to}...`);
      await transporter.sendMail({
        from: `"PolyConnect SBTET Attendance" <${gmailUser}>`,
        to: item.to,
        subject: item.subject,
        text: item.body
      });
      console.log(`    ✅ [SUCCESS] Delivered to ${item.to}`);
      sentCount++;
      // 300ms pause to respect Gmail rate limits
      await new Promise(r => setTimeout(r, 300));
    } catch (err) {
      console.error(`    ❌ [FAILED] Could not send to ${item.to}: ${err.message}`);
      failCount++;
    }
  }

  console.log('\n========================================');
  console.log('Attendance Email Automation Summary:');
  console.log(`Total Students Processed: ${responseData.totalProcessed || 0}`);
  console.log(`Emails Delivered: ${sentCount}`);
  console.log(`Emails Failed: ${failCount}`);
  console.log('========================================');

  if (failCount > 0 && sentCount === 0) {
    process.exit(1);
  }
}

main().catch(err => {
  console.error('❌ Fatal error in automation workflow:', err.message);
  process.exit(1);
});
