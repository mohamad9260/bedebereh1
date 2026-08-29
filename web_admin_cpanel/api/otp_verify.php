<?php
/**
 * Verify SMS OTP and issue single-use temporary registration token
 */
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config.php';

$method = $_SERVER['REQUEST_METHOD'];
if ($method !== 'POST') {
    jsonError('متد درخواست نامعتبر است.', 'METHOD_NOT_ALLOWED', 405);
}

$db = getDB();
ensureOtpTablesExist($db);

$input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
$rawPhone = trim($input['mobile'] ?? $input['phone'] ?? '');
$code = trim($input['code'] ?? '');
$mobile = normalizeIranianMobile($rawPhone);

// 1. Validate inputs
if (!isValidIranianMobile($mobile)) {
    jsonResponse('error', 'شماره موبایل وارد شده معتبر نیست.', null, 400, [
        'success' => false,
        'code' => 'INVALID_MOBILE'
    ]);
}

if (empty($code) || strlen($code) < 4 || strlen($code) > 8) {
    jsonResponse('error', 'کد تأیید وارد شده نامعتبر است.', null, 400, [
        'success' => false,
        'code' => 'OTP_INVALID'
    ]);
}

try {
    $db->beginTransaction();

    // 2. Retrieve latest unused OTP with row lock
    $stmt = $db->prepare("SELECT * FROM otp_codes WHERE mobile = ? AND is_used = 0 ORDER BY id DESC LIMIT 1 FOR UPDATE");
    $stmt->execute([$mobile]);
    $otp = $stmt->fetch();

    if (!$otp) {
        $db->rollBack();
        jsonResponse('error', 'کد تأیید معتبری یافت نشد. لطفاً مجدداً درخواست ارسال کد دهید.', null, 400, [
            'success' => false,
            'code' => 'OTP_INVALID'
        ]);
    }

    // 3. Check Expiry (3 minutes)
    if (strtotime($otp['expires_at']) < time()) {
        $db->prepare("UPDATE otp_codes SET is_used = 1 WHERE id = ?")->execute([$otp['id']]);
        $db->commit();
        jsonResponse('error', 'مهلت زمانی استفاده از این کد به پایان رسیده است. لطفاً کد جدید دریافت نمایید.', null, 400, [
            'success' => false,
            'code' => 'OTP_EXPIRED'
        ]);
    }

    // 4. Check Attempts (Max 5 attempts)
    if ((int)$otp['attempts'] >= 5) {
        $db->prepare("UPDATE otp_codes SET is_used = 1 WHERE id = ?")->execute([$otp['id']]);
        $db->commit();
        jsonResponse('error', 'تعداد دفعات ورود اشتباه کد بیش از ۵ بار بود. کد باطل شد، لطفاً کد جدید درخواست دهید.', null, 400, [
            'success' => false,
            'code' => 'OTP_MAX_ATTEMPTS'
        ]);
    }

    // 5. Increment attempts
    $db->prepare("UPDATE otp_codes SET attempts = attempts + 1 WHERE id = ?")->execute([$otp['id']]);

    // 6. Compare Hash using HMAC and timing attack safe comparison
    $pepper = getOtpPepper();
    $computedHash = hash_hmac('sha256', $code, $pepper);

    if (!hash_equals($otp['code_hash'], $computedHash)) {
        $db->commit();
        jsonResponse('error', 'کد وارد شده صحیح نیست.', null, 400, [
            'success' => false,
            'code' => 'OTP_INVALID'
        ]);
    }

    // 7. Atomic Mark as Used
    $db->prepare("UPDATE otp_codes SET is_used = 1 WHERE id = ?")->execute([$otp['id']]);

    // 8. Invalidate any previous registration tokens for this mobile
    $db->prepare("UPDATE registration_tokens SET is_used = 1 WHERE mobile = ? AND is_used = 0")->execute([$mobile]);

    // 9. Generate Single-Use Temporary Registration Token (Valid for 15 minutes)
    $rawToken = bin2hex(random_bytes(32));
    $tokenHash = hash_hmac('sha256', $rawToken, $pepper);

    $insToken = $db->prepare("INSERT INTO registration_tokens (mobile, token_hash, is_used, expires_at) VALUES (?, ?, 0, DATE_ADD(NOW(), INTERVAL 15 MINUTE))");
    $insToken->execute([$mobile, $tokenHash]);

    $db->commit();

    jsonResponse('success', 'شماره موبایل با موفقیت تأیید شد.', [
        'mobile' => $mobile,
        'registration_token' => $rawToken
    ], 200, [
        'success' => true,
        'verified' => true,
        'registration_token' => $rawToken
    ]);
} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    error_log("[BedeBere OTP Verify Error] " . $e->getMessage());
    jsonResponse('error', 'خطایی در پردازش تأیید کد رخ داد. لطفاً دوباره تلاش فرمایید.', null, 500, [
        'success' => false,
        'code' => 'SERVER_ERROR'
    ]);
}
