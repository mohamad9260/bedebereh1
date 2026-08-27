<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

if ($method === 'GET') {
    $phone = $_GET['phone'] ?? '';
    $userId = (int)($_GET['id'] ?? 0);

    if (empty($phone) && $userId <= 0) {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'شماره تلفن یا شناسه کاربر الزامی است.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if (!empty($phone)) {
        $stmt = $db->prepare("SELECT * FROM users WHERE phone = ? LIMIT 1");
        $stmt->execute([$phone]);
    } else {
        $stmt = $db->prepare("SELECT * FROM users WHERE id = ? LIMIT 1");
        $stmt->execute([$userId]);
    }
    
    $user = $stmt->fetch();

    if ($user) {
        echo json_encode([
            'status' => 'success',
            'data' => $user
        ], JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(404);
        echo json_encode(['status' => 'error', 'message' => 'کاربر یافت نشد.'], JSON_UNESCAPED_UNICODE);
    }
    exit;
}

if ($method === 'POST') {
    // Register / Login with Phone Number
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
    $phone = trim($input['phone'] ?? '');
    $name = trim($input['full_name'] ?? 'کاربر مهمان');
    $city = trim($input['city'] ?? 'تهران');

    if (empty($phone)) {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'شماره موبایل الزامی است.'], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $stmt = $db->prepare("SELECT * FROM users WHERE phone = ? LIMIT 1");
    $stmt->execute([$phone]);
    $user = $stmt->fetch();

    if (!$user) {
        $ins = $db->prepare("INSERT INTO users (phone, full_name, city, tier) VALUES (?, ?, ?, 'FREE')");
        $ins->execute([$phone, $name, $city]);
        $newId = $db->lastInsertId();

        $stmt = $db->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->execute([$newId]);
        $user = $stmt->fetch();
    }

    echo json_encode([
        'status' => 'success',
        'data' => $user
    ], JSON_UNESCAPED_UNICODE);
    exit;
}
