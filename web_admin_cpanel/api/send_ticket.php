<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $db = getDB();
    // Ensure table exists
    $db->exec("CREATE TABLE IF NOT EXISTS `tickets` (
      `id` INT AUTO_INCREMENT PRIMARY KEY,
      `user_id` INT DEFAULT NULL,
      `phone` VARCHAR(20) DEFAULT NULL,
      `subject` VARCHAR(200) NOT NULL,
      `message` TEXT NOT NULL,
      `admin_reply` TEXT DEFAULT NULL,
      `status` ENUM('OPEN', 'ANSWERED', 'CLOSED') DEFAULT 'OPEN',
      `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci");

    $user = getAuthenticatedUser(false);
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;

    $subject = trim($input['subject'] ?? 'پیام از اپلیکیشن');
    $message = trim($input['message'] ?? '');
    $phone = trim($input['phone'] ?? ($user ? $user['phone'] : ''));

    if (empty($message)) {
        jsonError('متن پیام نمی‌تواند خالی باشد.', 'EMPTY_MESSAGE', 400);
    }

    $stmt = $db->prepare("INSERT INTO tickets (user_id, phone, subject, message, status) VALUES (?, ?, ?, ?, 'OPEN')");
    $stmt->execute([
        $user ? $user['id'] : null,
        $phone,
        !empty($subject) ? $subject : 'پیام ارتباط با مدیر',
        $message
    ]);

    jsonResponse('success', 'پیام شما با موفقیت برای مدیریت سامانه بده بره ارسال گردید.', [
        'ticket_id' => $db->lastInsertId()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
