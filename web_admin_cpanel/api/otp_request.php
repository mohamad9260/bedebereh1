<?php
/**
 * Request SMS OTP for NEW USER initial registration
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config.php';

$method = $_SERVER['REQUEST_METHOD'];
if ($method !== 'POST') {
    jsonError('متد درخواست نامعتبر است.', 'METHOD_NOT_ALLOWED', 405);
}

$db = getDB();
ensureOtpTablesExist($db);
cleanupExpiredOtps($db);

$input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
$rawPhone = trim($input['mobile'] ?? $input['phone'] ?? '');
$mobile = normalizeIranianMobile($rawPhone);
$clientIp = getClientIp();

// 1. Validate Mobile Format
if (!isValidIranianMobile($mobile)) {
    jsonResponse('error', 'شماره موبایل وارد شده معتبر نیست. لطفاً شماره ۱۱ رقمی را به صورت ۰۹۱۲۳۴۵۶۷۸۹ وارد کنید.', null, 400, [
        'success' => false,
        'code' => 'INVALID_MOBILE'
    ]);
}

// 2. IMPORTANT: Check if user already exists in `users` table
$stmt = $db->prepare("SELECT id FROM users WHERE phone = ? LIMIT 1");
$stmt->execute([$mobile]);
if ($stmt->fetch()) {
    // User already registered -> DO NOT SEND REGISTRATION OTP
    jsonResponse('error', 'این شماره قبلاً ثبت‌نام شده است. لطفاً از بخش ورود وارد شوید.', null, 400, [
        'success' => false,
        'code' => 'USER_ALREADY_EXISTS'
    ]);
}

// 3. IP Rate Limiting (Max 10 requests per 15 minutes per IP)
$ipStmt = $db->prepare("SELECT COUNT(*) FROM otp_rate_limits WHERE ip_address = ? AND action = 'otp_request' AND created_at > DATE_SUB(NOW(), INTERVAL 15 MINUTE)");
$ipStmt->execute([$clientIp]);
if ((int)$ipStmt->fetchColumn() >= 10) {
    jsonResponse('error', 'تعداد درخواست‌ها از این آدرس اینترنتی بیش از حد مجاز است. لطفاً دقایقی دیگر تلاش فرمایید.', null, 429, [
        'success' => false,
        'code' => 'RATE_LIMITED'
    ]);
}

// 4. Mobile Cooldown Rate Limiting (60 seconds minimum interval)
$cooldownStmt = $db->prepare("SELECT TIMESTAMPDIFF(SECOND, created_at, NOW()) as elapsed FROM otp_codes WHERE mobile = ? ORDER BY id DESC LIMIT 1");
$cooldownStmt->execute([$mobile]);
$lastRow = $cooldownStmt->fetch();
if ($lastRow && (int)$lastRow['elapsed'] < 60) {
    $remain = 60 - (int)$lastRow['elapsed'];
    jsonResponse('error', "لطفاً {$remain} ثانیه منتظر بمانید و سپس مجدداً درخواست دهید.", null, 429, [
        'success' => false,
        'code' => 'OTP_COOLDOWN',
        'resend_after' => $remain
    ]);
}

// 5. Invalidate previous unused OTPs for this mobile
$db->prepare("UPDATE otp_codes SET is_used = 1 WHERE mobile = ? AND is_used = 0")->execute([$mobile]);

// 6. Generate Secure 5-Digit OTP
$code = (string)random_int(10000, 99999);
$pepper = getOtpPepper();
$codeHash = hash_hmac('sha256', $code, $pepper);

// 7. Store OTP in database (3 minutes expiry)
$ins = $db->prepare("INSERT INTO otp_codes (mobile, code_hash, attempts, is_used, expires_at) VALUES (?, ?, 0, 0, DATE_ADD(NOW(), INTERVAL 3 MINUTE))");
$ins->execute([$mobile, $codeHash]);
$otpId = $db->lastInsertId();

// 8. Send SMS through SMS.ir Fast Send Template
$smsResult = sendSmsIrOtp($mobile, $code);
if (!$smsResult['success']) {
    // Invalidate the OTP so it cannot be used
    $db->prepare("UPDATE otp_codes SET is_used = 1 WHERE id = ?")->execute([$otpId]);
    
    jsonResponse('error', $smsResult['message'] ?? 'ارسال کد تأیید با مشکل مواجه شد. لطفاً دوباره تلاش کنید.', null, 500, [
        'success' => false,
        'code' => $smsResult['code'] ?? 'SMS_SERVICE_ERROR'
    ]);
}

// 9. Record IP Rate Limit
$db->prepare("INSERT INTO otp_rate_limits (ip_address, mobile, action) VALUES (?, ?, 'otp_request')")->execute([$clientIp, $mobile]);

// 10. Return Success
jsonResponse('success', 'کد تأیید ارسال شد.', null, 200, [
    'success' => true,
    'code' => 'OTP_SENT',
    'expires_in' => 180,
    'resend_after' => 60
]);
