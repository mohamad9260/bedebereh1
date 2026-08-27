<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $db = getDB();
    $stmt = $db->query("SELECT id, name_fa, icon_name, type, is_locked, lock_message, display_order 
                        FROM categories ORDER BY display_order ASC");
    $categories = $stmt->fetchAll();

    echo json_encode([
        'status' => 'success',
        'data' => $categories
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
