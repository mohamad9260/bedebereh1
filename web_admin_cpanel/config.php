<?php
/**
 * Configuration and Database Connection for BedeBere Web Admin Panel
 */

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// ----------------------------------------------------
// DATABASE CREDENTIALS (UPDATE THESE FOR YOUR CPANEL)
// ----------------------------------------------------
define('DB_HOST', 'localhost');
define('DB_USER', 'cp64397_adminbedebere');
define('DB_PASS', 'Meftah9260');
define('DB_NAME', 'cp64397_bedebere');
define('DB_PORT', '3306');

// ----------------------------------------------------
// SECURITY & SESSION CONFIG
// ----------------------------------------------------
define('SITE_URL', 'http://localhost/bedebere_admin'); // Update to your domain, e.g. 'https://yourdomain.com/admin'
define('APP_DOWNLOAD_URL', 'bedebere.apk');
define('APP_VERSION_NAME', '1.0.0');
define('APP_SIZE_MB', '23.8');

function getDB() {
    static $pdo = null;
    if ($pdo === null) {
        try {
            $dsn = "mysql:host=" . DB_HOST . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=utf8mb4";
            $options = [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
                PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_persian_ci"
            ];
            $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
        } catch (PDOException $e) {
            die("<div dir='rtl' style='font-family:Tahoma;padding:20px;color:#b91c1c;background:#fee2e2;border:1px solid #f87171;margin:30px;border-radius:8px;'>
                <h3>خطا در اتصال به پایگاه داده</h3>
                <p>لطفاً اطلاعات دیتابیس در فایل <code>config.php</code> را بررسی نمایید.</p>
                <small>جزئیات خطا: " . htmlspecialchars($e->getMessage()) . "</small>
            </div>");
        }
    }
    return $pdo;
}

// Auth Check Helper
function requireAdminLogin() {
    if (empty($_SESSION['admin_logged_in']) || empty($_SESSION['admin_id'])) {
        header('Location: login.php');
        exit;
    }
}

// ----------------------------------------------------
// API HELPER FUNCTIONS (FOR ANDROID & REST CONSUMPTION)
// ----------------------------------------------------

function jsonResponse($status, $message = '', $data = null, $httpCode = 200, $extra = []) {
    http_response_code($httpCode);
    header('Content-Type: application/json; charset=utf-8');
    $payload = [
        'status' => $status,
        'message' => $message,
        'data' => $data
    ];
    if (!empty($extra)) {
        $payload = array_merge($payload, $extra);
    }
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

function jsonError($message, $code = 'ERROR', $httpCode = 400) {
    http_response_code($httpCode);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'status' => 'error',
        'message' => $message,
        'code' => $code
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Generate simple HMAC-based bearer token for a user with 30-day expiration
define('API_SECRET_KEY', 'BedeBere_Secret_Token_Key_2026_@Secured');

function generateUserToken($userId, $phone) {
    $issuedAt = time();
    $expiresAt = $issuedAt + (30 * 86400); // 30 days validity
    $raw = "$userId|$phone|$issuedAt|$expiresAt";
    $signature = hash_hmac('sha256', $raw, API_SECRET_KEY);
    return base64_encode("$raw|$signature");
}

function getAuthenticatedUser($requireAuth = true) {
    $db = getDB();
    $authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';
    
    // Check if passed in headers or GET/POST token
    $token = '';
    if (preg_match('/Bearer\s(\S+)/i', $authHeader, $matches)) {
        $token = $matches[1];
    } elseif (!empty($_GET['token'])) {
        $token = $_GET['token'];
    } elseif (!empty($_POST['token'])) {
        $token = $_POST['token'];
    }

    if (!empty($token)) {
        $decoded = base64_decode($token, true);
        if ($decoded) {
            $parts = explode('|', $decoded);
            if (count($parts) === 5) {
                list($userId, $phone, $issuedAt, $expiresAt, $signature) = $parts;
                $expectedSig = hash_hmac('sha256', "$userId|$phone|$issuedAt|$expiresAt", API_SECRET_KEY);
                if (hash_equals($expectedSig, $signature)) {
                    // Check expiration
                    if ((int)$expiresAt < time()) {
                        if ($requireAuth) {
                            jsonError('توکن نشست شما منقضی شده است. لطفاً مجدداً وارد شوید.', 'TOKEN_EXPIRED', 401);
                        }
                        return null;
                    }

                    $stmt = $db->prepare("SELECT * FROM users WHERE id = ? AND phone = ? LIMIT 1");
                    $stmt->execute([(int)$userId, $phone]);
                    $user = $stmt->fetch();
                    if ($user) {
                        return $user;
                    }
                }
            } elseif (count($parts) === 4) {
                // Legacy 4-part token support with fallback grace period
                list($userId, $phone, $time, $signature) = $parts;
                $expectedSig = hash_hmac('sha256', "$userId|$phone|$time", API_SECRET_KEY);
                if (hash_equals($expectedSig, $signature)) {
                    $stmt = $db->prepare("SELECT * FROM users WHERE id = ? AND phone = ? LIMIT 1");
                    $stmt->execute([(int)$userId, $phone]);
                    $user = $stmt->fetch();
                    if ($user) {
                        return $user;
                    }
                }
            }
        }
    }

    if ($requireAuth) {
        jsonError('احراز هویت انجام نشده است یا نشست شما نامعتبر است.', 'UNAUTHORIZED', 401);
    }
    return null;
}

// Canonical category mapper
function mapCategoryId($inputCategory) {
    if (empty($inputCategory)) return 'cat_tools';
    $cat = strtolower(trim($inputCategory));
    
    $mapping = [
        'book' => 'cat_books',
        'books' => 'cat_books',
        'cat_books' => 'cat_books',
        'fg_books' => 'cat_books',
        'home' => 'cat_home',
        'kitchen' => 'cat_home',
        'cat_home' => 'cat_home',
        'fg_home' => 'cat_home',
        'fg_furniture' => 'cat_home',
        'tools' => 'cat_tools',
        'tool' => 'cat_tools',
        'cat_tools' => 'cat_tools',
        'fg_tools' => 'cat_tools',
        'digital' => 'cat_digital',
        'cat_digital' => 'cat_digital',
        'fg_digital' => 'cat_digital',
        'clothes' => 'cat_personal',
        'clothing' => 'cat_personal',
        'personal' => 'cat_personal',
        'cat_personal' => 'cat_personal',
        'fg_clothes' => 'cat_personal',
        'toys' => 'cat_kids',
        'toy' => 'cat_kids',
        'kids' => 'cat_kids',
        'cat_kids' => 'cat_kids',
        'fg_toys' => 'cat_kids',
        'discounts' => 'cat_discounts',
        'discount' => 'cat_discounts',
        'dc_food' => 'cat_discounts',
        'requests' => 'cat_requests',
        'request' => 'cat_requests',
        'rq_kids' => 'cat_requests'
    ];

    if (isset($mapping[$cat])) {
        return $mapping[$cat];
    }
    
    // Check if exists in DB directly
    try {
        $db = getDB();
        $stmt = $db->prepare("SELECT id FROM categories WHERE id = ? LIMIT 1");
        $stmt->execute([$inputCategory]);
        if ($stmt->fetch()) {
            return $inputCategory;
        }
    } catch (Exception $e) {}

    return 'cat_tools';
}

// Helper: Format Persian numbers
function persianNumber($number) {
    $en = ['0','1','2','3','4','5','6','7','8','9'];
    $fa = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
    return str_replace($en, $fa, (string)$number);
}

// Helper: Format Currency (Toman)
function formatToman($amount) {
    return persianNumber(number_format((float)$amount)) . ' تومان';
}

// Helper: Get System Setting
function getSetting($key, $default = '') {
    try {
        $db = getDB();
        $stmt = $db->prepare("SELECT key_value FROM settings WHERE key_name = ? LIMIT 1");
        $stmt->execute([$key]);
        $row = $stmt->fetch();
        return $row ? $row['key_value'] : $default;
    } catch (Exception $e) {
        return $default;
    }
}

// Helper: Save System Setting
function setSetting($key, $value, $description = '') {
    $db = getDB();
    $stmt = $db->prepare("INSERT INTO settings (key_name, key_value, description) 
                          VALUES (?, ?, ?) 
                          ON DUPLICATE KEY UPDATE key_value = VALUES(key_value)");
    return $stmt->execute([$key, $value, $description]);
}

// Helper: CSRF Token
function generateCsrfToken() {
    if (empty($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf_token'];
}

function verifyCsrfToken($token) {
    return !empty($_SESSION['csrf_token']) && hash_equals($_SESSION['csrf_token'], $token ?? '');
}

// ----------------------------------------------------
// MOBILE & OTP SECURITY HELPERS
// ----------------------------------------------------
require_once __DIR__ . '/sms_service.php';

function normalizeIranianMobile($phone) {
    if (empty($phone)) return '';
    $persian = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
    $arabic  = ['٠','١','٢','٣','٤','٥','٦','٧','٨','٩'];
    $latin   = ['0','1','2','3','4','5','6','7','8','9'];
    $phone = str_replace($persian, $latin, (string)$phone);
    $phone = str_replace($arabic, $latin, $phone);
    $phone = preg_replace('/[^\d+]/', '', trim($phone));
    if (str_starts_with($phone, '+98')) {
        $phone = '0' . substr($phone, 3);
    } elseif (str_starts_with($phone, '0098')) {
        $phone = '0' . substr($phone, 4);
    } elseif (str_starts_with($phone, '98') && strlen($phone) === 12) {
        $phone = '0' . substr($phone, 2);
    }
    return $phone;
}

function isValidIranianMobile($phone) {
    return (bool)preg_match('/^09\d{9}$/', $phone);
}

function getClientIp() {
    $ip = $_SERVER['HTTP_CF_CONNECTING_IP'] 
        ?? $_SERVER['HTTP_X_FORWARDED_FOR'] 
        ?? $_SERVER['HTTP_CLIENT_IP'] 
        ?? $_SERVER['REMOTE_ADDR'] 
        ?? '127.0.0.1';
    if (str_contains($ip, ',')) {
        $parts = explode(',', $ip);
        $ip = trim($parts[0]);
    }
    return filter_var($ip, FILTER_VALIDATE_IP) ? $ip : '127.0.0.1';
}

function getOtpPepper() {
    return getEnvVar('OTP_PEPPER') ?: 'da50aedbb775c81025496211c86e7cf0c9f227d7a50cc7047714011d5e9bef49';
}

function ensureOtpTablesExist($db) {
    static $checked = false;
    if ($checked) return;
    try {
        $db->exec("CREATE TABLE IF NOT EXISTS `otp_codes` (
            `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
            `mobile` VARCHAR(15) NOT NULL,
            `code_hash` CHAR(64) NOT NULL,
            `attempts` TINYINT UNSIGNED NOT NULL DEFAULT 0,
            `is_used` TINYINT(1) NOT NULL DEFAULT 0,
            `expires_at` DATETIME NOT NULL,
            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            INDEX `idx_otp_mobile_created` (`mobile`, `created_at`),
            INDEX `idx_otp_expires` (`expires_at`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;");

        $db->exec("CREATE TABLE IF NOT EXISTS `registration_tokens` (
            `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
            `mobile` VARCHAR(15) NOT NULL,
            `token_hash` CHAR(64) NOT NULL,
            `is_used` TINYINT(1) NOT NULL DEFAULT 0,
            `expires_at` DATETIME NOT NULL,
            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            INDEX `idx_reg_token` (`token_hash`),
            INDEX `idx_reg_mobile` (`mobile`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;");

        $db->exec("CREATE TABLE IF NOT EXISTS `otp_rate_limits` (
            `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
            `ip_address` VARCHAR(45) NOT NULL,
            `mobile` VARCHAR(15) DEFAULT NULL,
            `action` VARCHAR(30) NOT NULL,
            `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            INDEX `idx_rate_ip` (`ip_address`, `created_at`),
            INDEX `idx_rate_mobile` (`mobile`, `created_at`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;");
        $checked = true;
    } catch (Exception $e) {
        // Log silently
        error_log("OTP schema ensure error: " . $e->getMessage());
    }
}

function cleanupExpiredOtps($db) {
    // Opportunistic cleanup with ~5% probability
    if (random_int(1, 20) === 1) {
        try {
            $db->exec("DELETE FROM otp_codes WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 DAY)");
            $db->exec("DELETE FROM registration_tokens WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 DAY)");
            $db->exec("DELETE FROM otp_rate_limits WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 DAY)");
        } catch (Exception $e) {}
    }
}

