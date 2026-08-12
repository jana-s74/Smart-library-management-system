/**
 * 📧 LibraAI OTP Verification Server
 * Node.js + Nodemailer microservice (port 3001)
 *
 * Endpoints:
 *   POST /otp/send    — generates a 6-digit OTP and emails it to the admin
 *   POST /otp/verify  — checks the submitted OTP for that email
 *   GET  /otp/health  — liveness check
 *
 * Constraint: Admin may skip OTP if last verified login was < 6 hours ago
 * (this constraint is enforced client-side via localStorage timestamp)
 */

require('dotenv').config();

const express    = require('express');
const nodemailer = require('nodemailer');
const cors       = require('cors');
const crypto     = require('crypto');

const app  = express();
const PORT = process.env.OTP_PORT || 3001;

// ── Middleware ────────────────────────────────────────────────────────────────
app.use(express.json());
app.use(cors({
    origin: ['http://localhost:8080', 'http://127.0.0.1:8080'],
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type']
}));

// ── In-memory OTP store  { email → { otp, expiresAt, attempts } } ────────────
const otpStore = new Map();
const OTP_TTL_MS    = 10 * 60 * 1000;  // OTP valid for 10 minutes
const MAX_ATTEMPTS  = 5;               // Lockout after 5 wrong tries

// ── Nodemailer Transporter ────────────────────────────────────────────────────
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.MAIL_USER,
        pass: process.env.MAIL_PASS      // Gmail App Password (16 chars)
    }
});

// Verify connection on startup
transporter.verify((err) => {
    if (err) {
        console.error('[OTP] ⚠️  Mailer not connected:', err.message);
        console.error('[OTP]    Make sure MAIL_USER and MAIL_PASS are set in .env');
    } else {
        console.log('[OTP] ✅ Mailer ready —', process.env.MAIL_USER);
    }
});

// ── Helper: generate secure 6-digit OTP ──────────────────────────────────────
function generateOTP() {
    return String(crypto.randomInt(100000, 999999));
}

// ── Helper: build HTML email ──────────────────────────────────────────────────
function buildOTPEmail(otp, email) {
    const now = new Date().toLocaleString('en-IN', {
        dateStyle: 'long', timeStyle: 'short', timeZone: 'Asia/Kolkata'
    });
    return {
        from: `"LibraAI System 📚" <${process.env.MAIL_USER}>`,
        to:   email,
        subject: `🔐 LibraAI Admin Login OTP — ${otp}`,
        html: `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <style>
    body { margin:0; padding:0; background:#F8FAFF; font-family:'Segoe UI',Arial,sans-serif; }
    .wrap { max-width:520px; margin:2rem auto; background:#fff; border-radius:16px;
            border:1px solid #E2E8F0; box-shadow:0 8px 32px rgba(124,58,237,.1); overflow:hidden; }
    .hdr  { background:linear-gradient(135deg,#7C3AED,#0EA5E9); padding:2rem 2rem 1.5rem; text-align:center; }
    .hdr .logo { color:#fff; font-size:1.6rem; font-weight:900; letter-spacing:-0.5px; }
    .hdr .logo span { color:#FDE68A; }
    .hdr p { color:rgba(255,255,255,.8); font-size:.85rem; margin:.4rem 0 0; }
    .body { padding:2rem; }
    .otp-box { background:linear-gradient(135deg,#EDE9FE,#E0F2FE); border:2px dashed #A78BFA;
               border-radius:12px; text-align:center; padding:1.5rem 2rem; margin:1.5rem 0; }
    .otp-label { font-size:.75rem; font-weight:700; color:#7C3AED; text-transform:uppercase;
                 letter-spacing:.1em; margin-bottom:.5rem; }
    .otp-code { font-size:2.8rem; font-weight:900; letter-spacing:8px; color:#5B21B6;
                font-family:monospace; }
    .meta { font-size:.82rem; color:#64748B; line-height:1.7; }
    .meta strong { color:#1E1B4B; }
    .warn { background:#FEF3C7; border-left:4px solid #F59E0B; border-radius:6px;
            padding:.75rem 1rem; margin:1rem 0; font-size:.82rem; color:#92400E; }
    .footer { background:#F1F5F9; padding:1rem 2rem; text-align:center;
              font-size:.75rem; color:#94A3B8; border-top:1px solid #E2E8F0; }
  </style>
</head>
<body>
  <div class="wrap">
    <div class="hdr">
      <div class="logo">📚 Libra<span>AI</span></div>
      <p>Smart Library Management System</p>
    </div>
    <div class="body">
      <p class="meta">Hello, <strong>System Administrator</strong> 👋</p>
      <p class="meta" style="margin-top:.5rem;">
        A login attempt was made for your LibraAI admin account at
        <strong>${now} IST</strong>. Use the OTP below to verify your identity:
      </p>
      <div class="otp-box">
        <div class="otp-label">Your One-Time Password</div>
        <div class="otp-code">${otp}</div>
      </div>
      <div class="warn">
        ⏱ This OTP <strong>expires in 10 minutes</strong>. Do not share it with anyone.
      </div>
      <p class="meta">
        If you did NOT attempt to login, please <strong>change your password immediately</strong>
        and contact your system administrator.
      </p>
    </div>
    <div class="footer">
      © 2026 LibraAI System &nbsp;·&nbsp; Automated Security Email &nbsp;·&nbsp; Do not reply
    </div>
  </div>
</body>
</html>`
    };
}

// ── Route: Health check ───────────────────────────────────────────────────────
app.get('/otp/health', (req, res) => {
    res.json({ success: true, service: 'LibraAI OTP Server', port: PORT });
});

// ── Route: Send OTP ───────────────────────────────────────────────────────────
app.post('/otp/send', async (req, res) => {
    const { email } = req.body;

    if (!email || typeof email !== 'string' || !email.includes('@')) {
        return res.status(400).json({ success: false, message: 'Valid email required.' });
    }

    const otp = generateOTP();
    otpStore.set(email.toLowerCase(), {
        otp,
        expiresAt: Date.now() + OTP_TTL_MS,
        attempts: 0
    });

    console.log(`[OTP] Sending OTP to ${email}`);

    try {
        await transporter.sendMail(buildOTPEmail(otp, email));
        console.log(`[OTP] ✅ OTP sent → ${email}`);
        res.json({ success: true, message: `OTP sent to ${email}` });
    } catch (err) {
        console.error('[OTP] ❌ Send error:', err.message);
        otpStore.delete(email.toLowerCase());
        res.status(500).json({ success: false, message: 'Failed to send OTP email. Check server config.' });
    }
});

// ── Route: Verify OTP ─────────────────────────────────────────────────────────
app.post('/otp/verify', (req, res) => {
    const { email, otp } = req.body;

    if (!email || !otp) {
        return res.status(400).json({ success: false, message: 'Email and OTP required.' });
    }

    const key    = email.toLowerCase();
    const record = otpStore.get(key);

    if (!record) {
        return res.json({ success: false, message: 'No OTP found. Please request a new one.' });
    }

    if (Date.now() > record.expiresAt) {
        otpStore.delete(key);
        return res.json({ success: false, message: 'OTP expired. Please request a new one.' });
    }

    record.attempts++;

    if (record.attempts > MAX_ATTEMPTS) {
        otpStore.delete(key);
        return res.json({ success: false, message: 'Too many attempts. Please request a new OTP.' });
    }

    if (record.otp !== String(otp).trim()) {
        return res.json({
            success: false,
            message: `Invalid OTP. ${MAX_ATTEMPTS - record.attempts + 1} attempt(s) remaining.`
        });
    }

    // ✅ Valid OTP
    otpStore.delete(key);
    console.log(`[OTP] ✅ Verified for ${email}`);
    res.json({ success: true, message: 'OTP verified successfully.' });
});

// ── Start ─────────────────────────────────────────────────────────────────────
app.listen(PORT, () => {
    console.log(`\n╔══════════════════════════════════════════════╗`);
    console.log(`║  📧 LibraAI OTP Server running on :${PORT}   ║`);
    console.log(`╚══════════════════════════════════════════════╝\n`);
});
