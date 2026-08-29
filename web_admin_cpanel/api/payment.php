<?php
/**
 * ZarinPal Payment Gateway Integration Endpoint
 * Full lifecycle: Request -> Authority -> Redirect -> Callback -> Verify -> Tier Upgrade
 */
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$action = $_GET['action'] ?? 'request';
$db = getDB();

$merchantId = getSetting('zarinpal_merchant_id', '00000000-0000-0000-0000-000000000000');
$isSandbox = getSetting('zarinpal_sandbox', '1') == '1';

// 1. INITIATE PAYMENT (REQUEST)
if ($action === 'request') {
    // Authenticate user via Token or fallback user_id
    $user = getAuthenticatedUser(false);
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;

    if (!$user) {
        $userId = (int)($input['user_id'] ?? 0);
        if ($userId > 0) {
            $stmt = $db->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
            $stmt->execute([$userId]);
            $user = $stmt->fetch();
        }
    }

    if (!$user) {
        jsonError('کاربر احراز هویت نشده است. لطفاً ابتدا وارد حساب کاربری خود شوید.', 'UNAUTHORIZED', 401);
    }

    $planType = strtoupper(trim($input['plan'] ?? $input['tier'] ?? 'GOLD'));
    if (!in_array($planType, ['SILVER', 'GOLD', 'DIAMOND'])) {
        $planType = 'GOLD';
    }

    // Read canonical prices strictly from settings (never trust client amounts)
    $amount = 0;
    $durationDays = 30;
    if ($planType === 'SILVER') {
        $amount = (int)getSetting('silver_plan_price', '49000');
        $planName = 'اشتراک نقره‌ای ۳۰ روزه';
    } elseif ($planType === 'GOLD') {
        $amount = (int)getSetting('gold_plan_price', '99000');
        $planName = 'اشتراک طلایی (VIP) ۳۰ روزه';
    } elseif ($planType === 'DIAMOND') {
        $amount = (int)getSetting('diamond_plan_price', '149000');
        $planName = 'اشتراک الماس (کسب‌وکارها) ۳۰ روزه';
    }

    $callbackUrl = trim($input['callback_url'] ?? (SITE_URL . '/api/payment.php?action=verify'));

    // ZarinPal API expects amount in Rials (1 Toman = 10 Rials)
    $amountInRials = $amount * 10;

    $data = [
        'merchant_id' => $merchantId,
        'amount' => $amountInRials,
        'description' => "خرید $planName در سامانه بده بره",
        'callback_url' => $callbackUrl,
        'metadata' => [
            'user_id' => (string)$user['id'],
            'phone' => (string)$user['phone'],
            'plan_type' => $planType
        ]
    ];

    $apiUrl = $isSandbox 
        ? 'https://sandbox.zarinpal.com/pg/v4/payment/request.json' 
        : 'https://api.zarinpal.com/pg/v4/payment/request.json';

    $ch = curl_init($apiUrl);
    curl_setopt($ch, CURLOPT_USERAGENT, 'ZarinPal Rest Api v4 (BedeBere)');
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);
    
    $result = curl_exec($ch);
    $err = curl_error($ch);
    curl_close($ch);

    if ($err) {
        jsonError('خطا در برقراری ارتباط با درگاه پرداخت زرین‌پال: ' . $err, 'GATEWAY_ERROR', 500);
    }

    $res = json_decode($result, true);

    if (!empty($res['data']['authority']) && $res['data']['code'] == 100) {
        $authority = $res['data']['authority'];

        // Save transaction as PENDING
        $stmt = $db->prepare("INSERT INTO transactions (user_id, amount_toman, plan_name, zarinpal_authority, status, description) 
                              VALUES (?, ?, ?, ?, 'PENDING', ?)");
        $stmt->execute([$user['id'], $amount, $planName, $authority, "در انتظار پرداخت - طرح $planType"]);
        $txId = $db->lastInsertId();

        $paymentUrl = $isSandbox 
            ? "https://sandbox.zarinpal.com/pg/StartPay/$authority" 
            : "https://www.zarinpal.com/pg/StartPay/$authority";

        jsonResponse('success', 'شناسه پرداخت با موفقیت ایجاد شد.', [
            'transaction_id' => (int)$txId,
            'authority' => $authority,
            'amount_toman' => $amount,
            'plan_type' => $planType,
            'payment_url' => $paymentUrl
        ]);
    } else {
        $msg = $res['errors']['message'] ?? 'درگاه پرداخت پاسخ ناموفق ارسال کرد.';
        jsonError($msg, 'ZARINPAL_REQUEST_FAILED', 400);
    }
}

// 2. VERIFY PAYMENT (CALLBACK)
if ($action === 'verify') {
    $authority = $_GET['Authority'] ?? '';
    $status = $_GET['Status'] ?? '';

    if ($status !== 'OK' || empty($authority)) {
        // Record failed transaction if exists
        $up = $db->prepare("UPDATE transactions SET status = 'CANCELLED' WHERE zarinpal_authority = ? AND status = 'PENDING'");
        $up->execute([$authority]);

        header('Content-Type: text/html; charset=utf-8');
        echo "<div dir='rtl' style='font-family:Tahoma,system-ui;max-width:500px;margin:50px auto;padding:30px;background:#fee2e2;color:#991b1b;border-radius:12px;text-align:center;box-shadow:0 4px 12px rgba(0,0,0,0.05);'>
                <h3 style='margin-top:0;'>❌ پرداخت لغو شد</h3>
                <p>عملیات پرداخت توسط کاربر لغو گردید یا با خطا مواجه شد. مبلغی از حساب شما کسر نشده است.</p>
                <a href='#' onclick='window.close()' style='display:inline-block;padding:10px 20px;background:#991b1b;color:#fff;text-decoration:none;border-radius:8px;margin-top:15px;'>بازگشت به برنامه</a>
              </div>";
        exit;
    }

    // Find pending transaction
    $stmt = $db->prepare("SELECT * FROM transactions WHERE zarinpal_authority = ? LIMIT 1");
    $stmt->execute([$authority]);
    $tx = $stmt->fetch();

    if (!$tx) {
        header('Content-Type: text/html; charset=utf-8');
        die("<div dir='rtl' style='font-family:Tahoma;padding:30px;text-align:center;'>تراکنش مورد نظر یافت نشد.</div>");
    }

    // Idempotency check: if already processed, do not duplicate upgrade
    if ($tx['status'] === 'SUCCESS') {
        header('Content-Type: text/html; charset=utf-8');
        echo "<div dir='rtl' style='font-family:Tahoma,system-ui;max-width:500px;margin:50px auto;padding:30px;background:#dcfce7;color:#166534;border-radius:12px;text-align:center;'>
                <h3 style='margin-top:0;'>✅ این تراکنش قبلاً با موفقیت تایید شده است</h3>
                <p>کد پیگیری: <b>{$tx['zarinpal_ref_id']}</b></p>
                <a href='#' onclick='window.close()' style='display:inline-block;padding:10px 20px;background:#166534;color:#fff;text-decoration:none;border-radius:8px;margin-top:15px;'>بازگشت به برنامه</a>
              </div>";
        exit;
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
    curl_setopt($ch, CURLOPT_USERAGENT, 'ZarinPal Rest Api v4 (BedeBere)');
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);
    
    $result = curl_exec($ch);
    curl_close($ch);
    $res = json_decode($result, true);

    $code = $res['data']['code'] ?? 0;

    // Code 100 = First time success, Code 101 = Already verified
    if ($code == 100 || $code == 101) {
        $refId = $res['data']['ref_id'] ?? $tx['zarinpal_ref_id'] ?? ('REF_' . time());
        $cardPan = $res['data']['card_pan'] ?? null;

        // Update transaction status
        $up = $db->prepare("UPDATE transactions SET status = 'SUCCESS', zarinpal_ref_id = ?, card_pan = ? WHERE id = ?");
        $up->execute([$refId, $cardPan, $tx['id']]);

        // Determine plan tier
        $plan = 'SILVER';
        if (stripos($tx['plan_name'], 'الماس') !== false || stripos($tx['description'], 'DIAMOND') !== false) {
            $plan = 'DIAMOND';
        } elseif (stripos($tx['plan_name'], 'طلایی') !== false || stripos($tx['description'], 'GOLD') !== false) {
            $plan = 'GOLD';
        }

        // Upgrade User Tier with 30 days extension
        $userUp = $db->prepare("UPDATE users SET tier = ?, tier_expires_at = DATE_ADD(IF(tier_expires_at > NOW(), tier_expires_at, NOW()), INTERVAL 30 DAY) WHERE id = ?");
        $userUp->execute([$plan, $tx['user_id']]);

        header('Content-Type: text/html; charset=utf-8');
        echo "<div dir='rtl' style='font-family:Tahoma,system-ui;max-width:500px;margin:50px auto;padding:30px;background:#dcfce7;color:#166534;border-radius:12px;text-align:center;box-shadow:0 4px 12px rgba(0,0,0,0.05);'>
                <h3 style='margin-top:0;'>🎉 پرداخت با موفقیت انجام شد!</h3>
                <p>اشتراک <b>{$tx['plan_name']}</b> با موفقیت برای حساب شما فعال گردید.</p>
                <p style='background:#bbf7d0;padding:10px;border-radius:8px;margin:15px 0;'>شماره پیگیری پرداخت: <b>$refId</b></p>
                <p>اکنون می‌توانید به اپلیکیشن «بده بره» بازگردید و از امکانات سطح جدید لذت ببرید.</p>
                <a href='#' onclick='window.close()' style='display:inline-block;padding:10px 24px;background:#166534;color:#fff;text-decoration:none;border-radius:8px;font-weight:bold;margin-top:10px;'>بازگشت به برنامه</a>
              </div>";
    } else {
        $up = $db->prepare("UPDATE transactions SET status = 'FAILED' WHERE id = ?");
        $up->execute([$tx['id']]);

        header('Content-Type: text/html; charset=utf-8');
        echo "<div dir='rtl' style='font-family:Tahoma,system-ui;max-width:500px;margin:50px auto;padding:30px;background:#fee2e2;color:#991b1b;border-radius:12px;text-align:center;'>
                <h3 style='margin-top:0;'>❌ خطا در تایید تراکنش بانکی</h3>
                <p>کد پاسخ درگاه: " . htmlspecialchars($code) . "</p>
                <a href='#' onclick='window.close()' style='display:inline-block;padding:10px 20px;background:#991b1b;color:#fff;text-decoration:none;border-radius:8px;margin-top:15px;'>بازگشت به برنامه</a>
              </div>";
    }
    exit;
}
