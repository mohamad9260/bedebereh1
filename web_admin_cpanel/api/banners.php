<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $db = getDB();
    // Ensure table exists
    $db->exec("CREATE TABLE IF NOT EXISTS `banners` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `page` VARCHAR(50) NOT NULL UNIQUE,
      `title` VARCHAR(200) NOT NULL,
      `subtitle` VARCHAR(255) NOT NULL,
      `badge_text` VARCHAR(50) DEFAULT 'بده بره',
      `image_url` VARCHAR(500) DEFAULT NULL,
      `action_url` VARCHAR(255) DEFAULT NULL,
      `is_active` TINYINT(1) DEFAULT 1,
      `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci");

    $page = trim($_GET['page'] ?? '');
    if (!empty($page)) {
        $stmt = $db->prepare("SELECT * FROM banners WHERE page = ? AND is_active = 1 LIMIT 1");
        $stmt->execute([$page]);
        $banner = $stmt->fetch();
        echo json_encode([
            'status' => 'success',
            'data' => $banner ?: null
        ], JSON_UNESCAPED_UNICODE);
    } else {
        $stmt = $db->query("SELECT * FROM banners WHERE is_active = 1");
        $banners = $stmt->fetchAll();
        echo json_encode([
            'status' => 'success',
            'data' => $banners
        ], JSON_UNESCAPED_UNICODE);
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
