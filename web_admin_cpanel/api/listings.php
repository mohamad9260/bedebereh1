<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

if ($method === 'GET') {
    // Return listings according to user tier & delays
    $tier = $_GET['tier'] ?? 'FREE';
    $delayMinutes = (int)getSetting('free_tier_delay_minutes', '30');

    $sql = "SELECT l.*, u.full_name as owner_name, u.phone as owner_phone, c.name_fa as category_name, c.icon_name as category_icon
            FROM listings l
            JOIN users u ON l.user_id = u.id
            JOIN categories c ON l.category_id = c.id
            WHERE l.status = 'APPROVED'";

    // If FREE tier, delay visibility
    if ($tier === 'FREE') {
        $sql .= " AND l.created_at <= DATE_SUB(NOW(), INTERVAL $delayMinutes MINUTE)";
    }

    $sql .= " ORDER BY l.id DESC";

    $stmt = $db->query($sql);
    $listings = $stmt->fetchAll();

    echo json_encode([
        'status' => 'success',
        'delay_applied_minutes' => ($tier === 'FREE' ? $delayMinutes : 0),
        'data' => $listings
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($method === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
    
    $userId = (int)($input['user_id'] ?? 0);
    $title = trim($input['title'] ?? '');
    $description = trim($input['description'] ?? '');
    $categoryId = trim($input['category_id'] ?? '');
    $type = trim($input['type'] ?? 'FREE_GIFT');
    $city = trim($input['city'] ?? 'تهران');
    $location = trim($input['approximate_location'] ?? '');
    
    // 1. Check if user is banned or blocked from posting
    $userStmt = $db->prepare("SELECT is_banned, can_post_listing, ban_reason, tier FROM users WHERE id = ?");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();

    if (!$user) {
        http_response_code(404);
        echo json_encode(['status' => 'error', 'message' => 'کاربر یافت نشد.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ($user['is_banned'] == 1) {
        http_response_code(403);
        echo json_encode(['status' => 'error', 'message' => 'حساب کاربری شما مسدود شده است: ' . ($user['ban_reason'] ?? '')], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ($user['can_post_listing'] == 0) {
        http_response_code(403);
        echo json_encode(['status' => 'error', 'message' => 'امکان ثبت آگهی برای حساب کاربری شما توسط مدیر غیرفعال شده است.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    // 2. Check if category is locked
    $catStmt = $db->prepare("SELECT is_locked, lock_message FROM categories WHERE id = ?");
    $catStmt->execute([$categoryId]);
    $cat = $catStmt->fetch();

    if ($cat && $cat['is_locked'] == 1) {
        http_response_code(403);
        echo json_encode(['status' => 'error', 'message' => $cat['lock_message'] ?? 'ثبت آگهی در این بخش موقتاً متوقف است.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    // 3. Scan forbidden words in title and description
    $forbiddenWords = $db->query("SELECT word, action_type FROM forbidden_words")->fetchAll();
    $fullContent = $title . ' ' . $description;
    $hasBlockWord = false;
    $blockedWord = '';

    foreach ($forbiddenWords as $fw) {
        if (mb_stripos($fullContent, $fw['word']) !== false) {
            if ($fw['action_type'] === 'BLOCK') {
                $hasBlockWord = true;
                $blockedWord = $fw['word'];
                break;
            }
        }
    }

    if ($hasBlockWord) {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => "آگهی شما حاوی کلمه ممنوعه «{$blockedWord}» است و قابل ثبت نمی‌باشد."], JSON_UNESCAPED_UNICODE);
        exit;
    }

    // 4. Save listing into Database
    $visibilityTier = $user['tier'] ?? 'FREE';
    $stmt = $db->prepare("INSERT INTO listings (user_id, title, description, category_id, type, status, city, approximate_location, visibility_tier) 
                          VALUES (?, ?, ?, ?, ?, 'APPROVED', ?, ?, ?)");
    $stmt->execute([$userId, $title, $description, $categoryId, $type, $city, $location, $visibilityTier]);
    $newId = $db->lastInsertId();

    echo json_encode([
        'status' => 'success',
        'message' => 'آگهی با موفقیت ثبت و منتشر گردید.',
        'listing_id' => $newId
    ], JSON_UNESCAPED_UNICODE);
    exit;
}
