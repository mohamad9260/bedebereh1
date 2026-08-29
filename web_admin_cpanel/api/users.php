<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();
ensureOtpTablesExist($db);

if ($method === 'GET') {
    $user = getAuthenticatedUser(false);
    if (!$user) {
        $rawPhone = trim($_GET['phone'] ?? '');
        $phone = normalizeIranianMobile($rawPhone);
        $userId = (int)($_GET['id'] ?? 0);
        if (!empty($phone)) {
            $stmt = $db->prepare("SELECT * FROM users WHERE phone = ? LIMIT 1");
            $stmt->execute([$phone]);
            $user = $stmt->fetch();
        } elseif ($userId > 0) {
            $stmt = $db->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
            $stmt->execute([$userId]);
            $user = $stmt->fetch();
        }
    }

    if ($user) {
        unset($user['national_id']);
        jsonResponse('success', 'اطلاعات کاربر با موفقیت بازیابی شد.', $user);
    } else {
        jsonError('کاربر یافت نشد.', 'USER_NOT_FOUND', 404);
    }
}

if ($method === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
    $rawPhone = trim($input['phone'] ?? $input['mobile'] ?? '');
    $phone = normalizeIranianMobile($rawPhone);
    $name = trim($input['full_name'] ?? $input['name'] ?? '');
    $city = trim($input['city'] ?? 'تهران');
    $province = trim($input['province'] ?? 'تهران');
    $nationalId = trim($input['national_id'] ?? '');
    $action = trim($input['action'] ?? '');
    $registrationToken = trim($input['registration_token'] ?? '');

    if (empty($phone) || !isValidIranianMobile($phone)) {
        jsonResponse('error', 'شماره موبایل وارد شده نامعتبر است (مثال: ۰۹۱۲۳۴۵۶۷۸۹).', null, 400, [
            'success' => false,
            'code' => 'INVALID_MOBILE'
        ]);
    }

    // Check if user already exists
    $stmt = $db->prepare("SELECT * FROM users WHERE phone = ? LIMIT 1");
    $stmt->execute([$phone]);
    $existingUser = $stmt->fetch();

    // ----------------------------------------------------
    // PATH 1: LOGIN (EXISTING USERS ONLY)
    // ----------------------------------------------------
    if ($action === 'login' || (empty($registrationToken) && $existingUser)) {
        if (!$existingUser) {
            jsonResponse('error', 'حساب کاربری با این شماره یافت نشد. لطفاً از بخش «ثبت‌نام جدید» اقدام فرمایید.', null, 404, [
                'success' => false,
                'code' => 'USER_NOT_FOUND'
            ]);
        }

        if ($existingUser['is_banned'] == 1) {
            jsonResponse('error', 'حساب کاربری شما مسدود شده است: ' . ($existingUser['ban_reason'] ?? ''), null, 403, [
                'success' => false,
                'code' => 'USER_BANNED'
            ]);
        }

        // Update profile name if provided and wasn't set
        if (!empty($name) && ($existingUser['full_name'] === 'کاربر بده بره' || empty($existingUser['full_name']))) {
            $up = $db->prepare("UPDATE users SET full_name = ? WHERE id = ?");
            $up->execute([$name, $existingUser['id']]);
            $existingUser['full_name'] = $name;
        }

        $token = generateUserToken($existingUser['id'], $existingUser['phone']);
        unset($existingUser['national_id']);
        $existingUser['token'] = $token;

        jsonResponse('success', 'ورود با موفقیت انجام شد.', $existingUser, 200, [
            'success' => true,
            'token' => $token
        ]);
    }

    // ----------------------------------------------------
    // PATH 2: REGISTRATION (NEW USERS - REQUIRES OTP REGISTRATION TOKEN)
    // ----------------------------------------------------
    if ($existingUser) {
        jsonResponse('error', 'این شماره قبلاً ثبت‌نام شده است. لطفاً از بخش ورود وارد شوید.', null, 400, [
            'success' => false,
            'code' => 'USER_ALREADY_EXISTS'
        ]);
    }

    if (empty($registrationToken)) {
        jsonResponse('error', 'برای ثبت‌نام جدید، تأیید شماره موبایل با پیامک الزامی است.', null, 400, [
            'success' => false,
            'code' => 'REGISTRATION_TOKEN_REQUIRED'
        ]);
    }

    // Validate Registration Token
    $pepper = getOtpPepper();
    $tokenHash = hash_hmac('sha256', $registrationToken, $pepper);

    try {
        $db->beginTransaction();

        $tokStmt = $db->prepare("SELECT * FROM registration_tokens WHERE mobile = ? AND is_used = 0 AND expires_at > NOW() ORDER BY id DESC LIMIT 1 FOR UPDATE");
        $tokStmt->execute([$phone]);
        $tokenRecord = $tokStmt->fetch();

        if (!$tokenRecord || !hash_equals($tokenRecord['token_hash'], $tokenHash)) {
            $db->rollBack();
            jsonResponse('error', 'توکن تأیید شماره نامعتبر یا منقضی شده است. لطفاً مجدداً شماره موبایل خود را با پیامک تأیید فرمایید.', null, 400, [
                'success' => false,
                'code' => 'REGISTRATION_TOKEN_INVALID'
            ]);
        }

        // Mark registration token as used immediately
        $db->prepare("UPDATE registration_tokens SET is_used = 1 WHERE id = ?")->execute([$tokenRecord['id']]);

        // Insert new user with SERVER-GENERATED ID
        $displayName = !empty($name) ? $name : 'کاربر بده بره';
        $ins = $db->prepare("INSERT INTO users (phone, full_name, city, province, national_id, tier) VALUES (?, ?, ?, ?, ?, 'FREE')");
        $ins->execute([
            $phone,
            $displayName,
            $city,
            $province,
            !empty($nationalId) ? $nationalId : null
        ]);
        $newUserId = $db->lastInsertId();

        $fetchStmt = $db->prepare("SELECT * FROM users WHERE id = ?");
        $fetchStmt->execute([$newUserId]);
        $newUser = $fetchStmt->fetch();

        $db->commit();

        $token = generateUserToken($newUser['id'], $newUser['phone']);
        unset($newUser['national_id']);
        $newUser['token'] = $token;

        jsonResponse('success', 'ثبت‌نام و ورود با موفقیت انجام شد.', $newUser, 200, [
            'success' => true,
            'token' => $token
        ]);
    } catch (Exception $e) {
        if ($db->inTransaction()) {
            $db->rollBack();
        }
        error_log("[BedeBere Registration Error] " . $e->getMessage());
        jsonResponse('error', 'خطایی در ایجاد حساب کاربری رخ داد: ' . $e->getMessage(), null, 500, [
            'success' => false,
            'code' => 'REGISTRATION_FAILED'
        ]);
    }
}

