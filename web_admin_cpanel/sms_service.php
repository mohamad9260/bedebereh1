<?php
/**
 * SMS.ir Fast Send / Verify Service for BedeBere Application
 * Handles secure OTP delivery via SMS.ir verify template API.
 */

if (!defined('SMSIR_API_URL')) {
    define('SMSIR_API_URL', 'https://api.sms.ir/v1/send/verify');
}

/**
 * Get environment variable with fallback support
 */
function getEnvVar($key, $default = '') {
    $val = getenv($key);
    if ($val !== false && $val !== '') {
        return $val;
    }
    if (!empty($_ENV[$key])) {
        return $_ENV[$key];
    }
    if (!empty($_SERVER[$key])) {
        return $_SERVER[$key];
    }
    // Attempt loading from root .env if present
    static $envFileVars = null;
    if ($envFileVars === null) {
        $envFileVars = [];
        $envPaths = [
            __DIR__ . '/.env',
            __DIR__ . '/../.env',
            dirname(__DIR__, 2) . '/.env'
        ];
        foreach ($envPaths as $p) {
            if (file_exists($p) && is_readable($p)) {
                $lines = file($p, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
                foreach ($lines as $line) {
                    $line = trim($line);
                    if ($line === '' || str_starts_with($line, '#')) continue;
                    if (str_contains($line, '=')) {
                        list($k, $v) = explode('=', $line, 2);
                        $envFileVars[trim($k)] = trim(trim($v), '"\'');
                    }
                }
            }
        }
    }
    return $envFileVars[$key] ?? $default;
}

/**
 * Send OTP Verification Code via SMS.ir Fast Send Template
 * 
 * @param string $mobile Normalized Iranian mobile number (e.g. 09123456789)
 * @param string $code 5 or 6 digit verification code
 * @return array ['success' => bool, 'message' => string, 'code' => string]
 */
function sendSmsIrOtp($mobile, $code) {
    $apiKey = getEnvVar('SMSIR_API_KEY', 'byJKEgfVch7jK8pUaFTZhhxchokDdCDuSMO7ugSTszt1qXnz');
    $templateId = (int)getEnvVar('SMSIR_TEMPLATE_ID', '944749');

    $maskedMobile = substr($mobile, 0, 4) . '****' . substr($mobile, -3);

    if (empty($apiKey) || $templateId <= 0) {
        // Log configuration notice without revealing secrets
        error_log("[BedeBere OTP] SMS.ir API key or Template ID not configured in environment for $maskedMobile.");
        return [
            'success' => false,
            'code' => 'SMS_CONFIG_MISSING',
            'message' => 'تنظیمات ارسال پیامک بر روی سرور پیکربندی نشده است. لطفاً با پشتیبانی تماس بگیرید.'
        ];
    }

    $payload = [
        'mobile' => $mobile,
        'templateId' => $templateId,
        'parameters' => [
            [
                'name' => 'Code',
                'value' => (string)$code
            ]
        ]
    ];

    $ch = curl_init(SMSIR_API_URL);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE),
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 10,
        CURLOPT_CONNECTTIMEOUT => 5,
        CURLOPT_HTTPHEADER => [
            'x-api-key: ' . $apiKey,
            'Content-Type: application/json',
            'Accept: application/json'
        ]
    ]);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);

    if ($response === false || !empty($curlError)) {
        error_log("[BedeBere OTP] SMS.ir cURL error for $maskedMobile: $curlError");
        return [
            'success' => false,
            'code' => 'SMS_SERVICE_ERROR',
            'message' => 'ارسال پیامک با خطا مواجه شد. لطفاً چند لحظه بعد مجدداً تلاش کنید.'
        ];
    }

    $result = json_decode($response, true);
    if ($httpCode === 200 && isset($result['status']) && (int)$result['status'] === 1) {
        error_log("[BedeBere OTP] OTP sent successfully via SMS.ir to $maskedMobile.");
        return [
            'success' => true,
            'code' => 'OTP_SENT',
            'message' => 'کد تأیید با موفقیت پیامک شد.'
        ];
    }

    // Handle SMS.ir API non-success response
    $apiMsg = $result['message'] ?? "HTTP $httpCode";
    error_log("[BedeBere OTP] SMS.ir API error for $maskedMobile: $apiMsg (HTTP $httpCode)");

    return [
        'success' => false,
        'code' => 'SMS_SERVICE_ERROR',
        'message' => 'ارسال کد تأیید از سمت سرویس پیامک با خطا مواجه شد. لطفاً دوباره تلاش کنید.'
    ];
}
