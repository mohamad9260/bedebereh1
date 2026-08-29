<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $db = getDB();
    $justFreeHours = (int)getSetting('just_free_hours', '24');
    $limit = (int)($_GET['limit'] ?? 20);

    $stmt = $db->prepare("SELECT l.*, u.full_name as author_name, u.phone as author_phone, c.name_fa as category_name, c.icon_name as category_icon
                          FROM listings l
                          LEFT JOIN users u ON l.user_id = u.id
                          LEFT JOIN categories c ON l.category_id = c.id
                          WHERE l.type = 'FREE_GIFT' AND l.status = 'APPROVED'
                          ORDER BY l.created_at DESC LIMIT ?");
    $stmt->bindValue(1, $limit, PDO::PARAM_INT);
    $stmt->execute();
    $listings = $stmt->fetchAll();

    echo json_encode([
        'status' => 'success',
        'hours_window' => $justFreeHours,
        'data' => $listings
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
