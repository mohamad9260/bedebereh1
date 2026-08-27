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
define('DB_USER', 'root');           // E.g. 'cpaneluser_bedebere'
define('DB_PASS', '');               // E.g. 'YourSecurePassword123'
define('DB_NAME', 'bedebere_db');    // E.g. 'cpaneluser_bedebere_db'
define('DB_PORT', '3306');

// ----------------------------------------------------
// SECURITY & SESSION CONFIG
// ----------------------------------------------------
define('SITE_URL', 'http://localhost/bedebere_admin'); // Update to your domain, e.g. 'https://yourdomain.com/admin'

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
