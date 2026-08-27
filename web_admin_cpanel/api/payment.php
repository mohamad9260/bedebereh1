<?php
/**
 * ZarinPal Payment Gateway Integration Endpoint
 * Supports REST API v4 & Sandbox
 */
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$action = $_GET['action'] ?? 'request';
$db = getDB();

$merchantId = getSetting('zarinpal_merchant_id', '00000000-0000-0000-0000-000000000000');
$isSandbox = getSetting('zarinpal_sandbox', '1') == '1';

// 1. INITIATE PAYMENT (REQUEST)
if ($action === 'request') {
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
    
    $userId = (int)($input['user_id'] ?? 0);
    $planType = strtoupper(trim($input['plan'] ?? 'GOLD')); // 'GOLD' or 'SILVER'
    $callbackUrl = trim($input['callback_url'] ?? (SITE_URL . '/api/payment.php?action=verify'));

    $amount = ($planType === 'GOLD') 
        ? (int)getSetting('gold_plan_price', '99000') 
        : (int)getSetting('silver_plan_price', '49000');
    
    $planName = ($planType === 'GOLD') ? 'اشتراک طلایی ۳۰ روزه' : 'اشتراک نقره‌ای ۳۰ روزه';

    // ZarinPal API expects amount in Rials (1 Toman = 10 Rials)
    $amountInRials = $amount * 10;

    $data = [
        'merchant_id' => $merchantId,
        'amount' => $amountInRials,
        'description' => "خرید $planName در سامانه بده بره",
        'callback_url' => $callbackUrl,
        'metadata' => [
            'user_id' => $userId,
            'plan_type' => $planType
        ]
    ];

    $apiUrl = $isSandbox 
        ? 'https://sandbox.zarinpal.com/pg/v4/payment/request.json' 
        : 'https://api.zarinpal.com/pg/v4/payment/request.json';

    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_USERAGENT, 'ZarinPal Rest Api v4');
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    
    $result = curl_exec($ch);
    $err = curl_error($ch);
    curl_close($ch);

    if ($err) {
        http_response_code(500);
        echo json_encode(['status' => 'error', 'message' => 'خطا در ارتباط با درگاه بانکی: ' . $err], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $res = json_decode($result, true);

    if (!empty($res['data']['authority']) && $res['data']['code'] == 100) {
        $authority = $res['data']['authority'];

        // Save transaction as PENDING
        $stmt = $db->prepare("INSERT INTO transactions (user_id, amount_toman, plan_name, zarinpal_authority, status, description) 
                              VALUES (?, ?, ?, ?, 'PENDING', ?)");
        $stmt->execute([$userId, $amount, $planName, $authority, "در انتظار پرداخت - طرح $planType"]);

        $paymentUrl = $isSandbox 
            ? "https://sandbox.zarinpal.com/pg/StartPay/$authority" 
            : "https://www.zarinpal.com/pg/StartPay/$authority";

        echo json_encode([
            'status' => 'success',
            'authority' => $authority,
            'payment_url' => $paymentUrl
        ], JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(400);
        echo json_encode([
            'status' => 'error',
            'message' => 'درگاه پرداخت پاسخ ناموفق ارسال کرد.',
            'zarinpal_response' => $res
        ], JSON_UNESCAPED_UNICODE);
    }
    exit;
}

// 2. VERIFY PAYMENT (CALLBACK)
if ($action === 'verify') {
    $authority = $_GET['Authority'] ?? '';
    $status = $_GET['Status'] ?? '';

    if ($status !== 'OK' || empty($authority)) {
        echo "<div dir='rtl' style='font-family:Tahoma;padding:30px;background:#fee2e2;color:#991b1b;border-radius:12px;margin:20px;text-align:center;'>
                <h2>پرداخت لغو شد یا با خطا مواجه گردید</h2>
                <p>تراکنش شما انجام نشد. مبلغی از حساب شما کسر نگردیده است.</p>
              </div>";
        exit;
    }

    // Find pending transaction
    $stmt = $db->prepare("SELECT * FROM transactions WHERE zarinpal_authority = ? LIMIT 1");
    $stmt->execute([$authority]);
    $tx = $stmt->fetch();

    if (!$tx) {
        die("تراکنش یافت نشد.");
    }

    $amountInRials = $tx['amount_toman'] * 10;

    $data = [
        'merchant_id' => $merchantId,
        'authority' => $authority,
        'amount' => $amountInRials
    ];

    $verifyUrl = $isSandbox 
        ? 'https://sandbox.zarinpal.com/pg/v4/payment/verify.json' 
        : 'https://api.zarinpal.com/pg/v4/payment/verify.json';

    $ch = curl_init($verifyUrl);
    curl_setopt($ch, CURLOPT_USERAGENT, 'ZarinPal Rest Api v4');
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    
    $result = curl_exec($ch);
    curl_close($ch);
    $res = json_decode($result, true);

    if (!empty($res['data']['code']) && ($res['data']['code'] == 100 || $res['data']['code'] == 101)) {
        $refId = $res['data']['ref_id'];
        $cardPan = $res['data']['card_pan'] ?? null;

        // Update transaction
        $up = $db->prepare("UPDATE transactions SET status = 'SUCCESS', zarinpal_ref_id = ?, card_pan = ? WHERE id = ?");
        $up->execute([$refId, $cardPan, $tx['id']]);

        // Upgrade User Tier
        $plan = (stripos($tx['plan_name'], 'طلایی') !== false) ? 'GOLD' : 'SILVER';
        $userUp = $db->prepare("UPDATE users SET tier = ?, tier_expires_at = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE id = ?");
        $userUp->execute([$plan, $tx['user_id']]);

        echo "<div dir='rtl' style='font-family:Tahoma;padding:30px;background:#dcfce7;color:#166534;border-radius:12px;margin:20px;text-align:center;'>
                <h2>پرداخت با موفقیت انجام شد!</h2>
                <p>اشتراک <b>{$tx['plan_name']}</b> با موفقیت برای شما فعال گردید.</p>
                <p>کد پیگیری تراکنش: <b>$refId</b></p>
                <p>می‌توانید به اپلیکیشن بازگردید.</p>
              </div>";
    } else {
        $up = $db->prepare("UPDATE transactions SET status = 'FAILED' WHERE id = ?");
        $up->execute([$tx['id']]);

        echo "<div dir='rtl' style='font-family:Tahoma;padding:30px;background:#fee2e2;color:#991b1b;border-radius:12px;margin:20px;text-align:center;'>
                <h2>خطا در تایید تراکنش بانکی</h2>
                <p>کد خطا: " . ($res['errors']['code'] ?? 'نامشخص') . "</p>
              </div>";
    }
    exit;
}
