<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

if ($method !== 'POST') {
    jsonError('متد نامعتبر است. فقط متد POST پشتیبانی می‌شود.', 'METHOD_NOT_ALLOWED', 405);
}

// 1. Authenticate user
$user = getAuthenticatedUser(true);
$input = json_decode(file_get_contents('php://input'), true) ?? $_POST;
$listingId = (int)($input['listing_id'] ?? 0);
$action = trim($input['action'] ?? 'reserve'); // 'reserve' or 'cancel' or 'complete'

if ($listingId <= 0) {
    jsonError('شناسه آگهی الزامی است.', 'INVALID_LISTING_ID', 400);
}

try {
    $db->beginTransaction();

    // Lock listing row for update to prevent race condition
    $stmt = $db->prepare("SELECT l.*, u.phone as owner_phone, u.full_name as owner_name 
                          FROM listings l 
                          JOIN users u ON l.user_id = u.id 
                          WHERE l.id = ? FOR UPDATE");
    $stmt->execute([$listingId]);
    $listing = $stmt->fetch();

    if (!$listing) {
        $db->rollBack();
        jsonError('آگهی یافت نشد.', 'NOT_FOUND', 404);
    }

    if ($action === 'reserve') {
        if ($listing['status'] !== 'APPROVED' || $listing['is_reserved'] == 1) {
            $db->rollBack();
            jsonError('این آگهی قبلاً توسط کاربر دیگری رزرو شده یا در دسترس نمی‌باشد.', 'ALREADY_RESERVED', 409);
        }

        if ($listing['user_id'] == $user['id']) {
            $db->rollBack();
            jsonError('شما نمی‌توانید آگهی ثبت‌شده توسط خودتان را رزرو کنید.', 'CANNOT_RESERVE_OWN', 400);
        }

        // Check daily reservation limit
        $limitKey = strtolower($user['tier']) . '_daily_reserve_limit';
        $dailyLimit = (int)getSetting($limitKey, '3');

        $resCountStmt = $db->prepare("SELECT COUNT(*) FROM listings 
                                      WHERE reserved_by_user_id = ? 
                                      AND DATE(created_at) = CURDATE()");
        $resCountStmt->execute([$user['id']]);
        $currentReservedToday = (int)$resCountStmt->fetchColumn();

        if ($currentReservedToday >= $dailyLimit) {
            $db->rollBack();
            jsonError("سقف رزرو روزانه شما ({$dailyLimit} عدد) برای سطح عضویت {$user['tier']} تکمیل شده است. جهت افزایش سقف می‌توانید عضویت خود را ارتقا دهید.", 'DAILY_LIMIT_EXCEEDED', 429);
        }

        // Perform atomic reservation
        $up = $db->prepare("UPDATE listings SET is_reserved = 1, reserved_by_user_id = ?, status = 'RESERVED' WHERE id = ?");
        $up->execute([$user['id'], $listingId]);

        $db->commit();

        jsonResponse('success', 'آگهی با موفقیت برای شما رزرو شد. اکنون می‌توانید با اهداکننده تماس بگیرید.', [
            'listing_id' => $listingId,
            'status' => 'RESERVED',
            'is_reserved' => true,
            'contact_phone' => $listing['owner_phone'],
            'owner_name' => $listing['owner_name']
        ]);
    } elseif ($action === 'cancel') {
        if ($listing['reserved_by_user_id'] != $user['id']) {
            $db->rollBack();
            jsonError('شما مجاز به لغو رزرو این آگهی نیستید.', 'FORBIDDEN', 403);
        }

        $up = $db->prepare("UPDATE listings SET is_reserved = 0, reserved_by_user_id = NULL, status = 'APPROVED' WHERE id = ?");
        $up->execute([$listingId]);

        $db->commit();

        jsonResponse('success', 'رزرو با موفقیت لغو شد و آگهی مجدداً برای همگان در دسترس قرار گرفت.', [
            'listing_id' => $listingId,
            'status' => 'APPROVED',
            'is_reserved' => false
        ]);
    } elseif ($action === 'complete') {
        // Only owner or reserver can complete
        if ($listing['user_id'] != $user['id'] && $listing['reserved_by_user_id'] != $user['id']) {
            $db->rollBack();
            jsonError('شما دسترسی لازم برای تایید تحویل این کالا را ندارید.', 'FORBIDDEN', 403);
        }

        // Move to EXPIRED/COMPLETED so it disappears from public feed but stays archived in admin
        $up = $db->prepare("UPDATE listings SET status = 'EXPIRED' WHERE id = ?");
        $up->execute([$listingId]);

        $db->commit();

        jsonResponse('success', 'انتقال کالا با موفقیت تایید و آگهی بایگانی شد. از حس سخاوت شما سپاسگزاریم!', [
            'listing_id' => $listingId,
            'status' => 'EXPIRED'
        ]);
    } else {
        $db->rollBack();
        jsonError('عملیات نامعتبر است.', 'INVALID_ACTION', 400);
    }
} catch (Exception $e) {
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    jsonError('خطای پایگاه داده هنگام انجام عملیات: ' . $e->getMessage(), 'DATABASE_ERROR', 500);
}
