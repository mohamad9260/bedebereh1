<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $db = getDB();
    $stmt = $db->query("SELECT key_name, key_value FROM settings");
    $rows = $stmt->fetchAll();
    $settings = [];
    foreach ($rows as $r) {
        $settings[$r['key_name']] = $r['key_value'];
    }

    echo json_encode([
        'status' => 'success',
        'data' => [
            'silver_plan_price' => (int)($settings['silver_plan_price'] ?? 49000),
            'gold_plan_price' => (int)($settings['gold_plan_price'] ?? 99000),
            'diamond_plan_price' => (int)($settings['diamond_plan_price'] ?? 149000),
            'gold_early_access_hours' => (int)($settings['gold_early_access_hours'] ?? 2),
            'silver_early_access_hours' => (int)($settings['silver_early_access_hours'] ?? 1),
            'diamond_early_access_hours' => (int)($settings['diamond_early_access_hours'] ?? 2),
            'require_diamond_for_discounts' => ($settings['require_diamond_for_discounts'] ?? '1') === '1',
            'support_phone' => $settings['support_phone'] ?? '021-88889260',
            'support_email' => $settings['support_email'] ?? 'admin@bedebere.ir',
            'support_telegram' => $settings['support_telegram'] ?? '@bedebere_admin',
            'support_hours' => $settings['support_hours'] ?? 'پاسخگویی سریع ۲۴ ساعته',
            'just_free_hours' => (int)($settings['just_free_hours'] ?? 24)
        ]
    ], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
