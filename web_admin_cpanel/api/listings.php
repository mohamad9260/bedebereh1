<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$db = getDB();

if ($method === 'GET') {
    // Authenticate optionally to know user's tier
    $authUser = getAuthenticatedUser(false);
    $userTier = 'FREE';
    if ($authUser && !empty($authUser['tier'])) {
        // Check tier expiry
        if (!empty($authUser['tier_expires_at']) && strtotime($authUser['tier_expires_at']) < time()) {
            $userTier = 'FREE';
        } else {
            $userTier = $authUser['tier'];
        }
    } elseif (!empty($_GET['tier'])) {
        $userTier = strtoupper(trim($_GET['tier']));
    }

    // Early Access Settings (in hours)
    $goldEarlyHours = (int)getSetting('gold_early_access_hours', '2');
    $silverEarlyHours = (int)getSetting('silver_early_access_hours', '1');
    $diamondEarlyHours = (int)getSetting('diamond_early_access_hours', '2');

    // Filter Parameters
    $city = trim($_GET['city'] ?? '');
    $category = trim($_GET['category'] ?? '');
    $search = trim($_GET['search'] ?? $_GET['q'] ?? '');
    $type = trim($_GET['type'] ?? '');

    $sql = "SELECT l.id, l.title, l.description, l.category_id, l.type, l.status, l.city, l.approximate_location,
                   l.visibility_tier, l.is_reserved, l.discount_type, l.discount_percentage, l.discount_amount_toman,
                   l.discount_code, l.image_url, l.views_count, l.created_at,
                   c.name_fa as category_name, c.icon_name as category_icon,
                   u.full_name as owner_name
            FROM listings l
            JOIN users u ON l.user_id = u.id
            JOIN categories c ON l.category_id = c.id
            WHERE l.status = 'APPROVED'";
    $params = [];

    // Enforce server-side early access visibility logic
    if ($userTier === 'FREE') {
        // Free users only see listings that have been approved for longer than the early access window
        $sql .= " AND (
            l.visibility_tier = 'FREE' 
            OR l.created_at <= DATE_SUB(NOW(), INTERVAL ? HOUR)
        )";
        $params[] = $goldEarlyHours;
    } elseif ($userTier === 'SILVER') {
        $diffHours = max(0, $goldEarlyHours - $silverEarlyHours);
        $sql .= " AND (
            l.visibility_tier IN ('FREE', 'SILVER') 
            OR l.created_at <= DATE_SUB(NOW(), INTERVAL ? HOUR)
        )";
        $params[] = $diffHours;
    }
    // GOLD and DIAMOND tiers see all APPROVED listings immediately

    if (!empty($city)) {
        $sql .= " AND l.city = ?";
        $params[] = $city;
    }

    if (!empty($category)) {
        $mappedCat = mapCategoryId($category);
        $sql .= " AND (l.category_id = ? OR c.parent_id = ?)";
        $params[] = $mappedCat;
        $params[] = $mappedCat;
    }

    if (!empty($type)) {
        $sql .= " AND l.type = ?";
        $params[] = $type;
    }

    if (!empty($search)) {
        $sql .= " AND (l.title LIKE ? OR l.description LIKE ?)";
        $params[] = "%$search%";
        $params[] = "%$search%";
    }

    $sql .= " ORDER BY l.id DESC";

    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $listings = $stmt->fetchAll();

    // Map fields for backward and standard compatibility
    $formatted = array_map(function($item) {
        return [
            'id' => (string)$item['id'],
            'title' => $item['title'],
            'description' => $item['description'],
            'category_id' => $item['category_id'],
            'category_title' => $item['category_name'],
            'category_name' => $item['category_name'],
            'category_icon' => $item['category_icon'],
            'type' => $item['type'],
            'city' => $item['city'],
            'approximate_location' => $item['approximate_location'],
            'status' => $item['status'],
            'approval_status' => $item['status'],
            'is_reserved' => (bool)$item['is_reserved'],
            'image_url' => $item['image_url'],
            'discount_type' => $item['discount_type'],
            'discount_percentage' => $item['discount_percentage'],
            'discount_amount' => $item['discount_amount_toman'],
            'discount_code' => $item['discount_code'],
            'owner_name' => $item['owner_name'],
            // Note: owner_phone is strictly omitted for privacy until reserved
            'created_at' => $item['created_at']
        ];
    }, $listings);

    jsonResponse('success', 'لیست آگهی‌ها با موفقیت بارگذاری شد.', $formatted, 200, ['count' => count($formatted)]);
}

if ($method === 'POST') {
    // Authenticate user or identify by provided phone and name
    $user = getAuthenticatedUser(false);
    $input = json_decode(file_get_contents('php://input'), true) ?? $_POST;

    $ownerPhone = trim($input['owner_phone'] ?? $input['contact_phone'] ?? '');
    $ownerName = trim($input['owner_name'] ?? '');

    if (!$user) {
        if (!empty($ownerPhone)) {
            // Find existing user by phone
            $userStmt = $db->prepare("SELECT * FROM users WHERE phone = ?");
            $userStmt->execute([$ownerPhone]);
            $user = $userStmt->fetch();

            if (!$user) {
                // Register this user properly
                $displayName = !empty($ownerName) ? $ownerName : 'کاربر بده بره';
                $city = trim($input['city'] ?? 'تهران');
                $province = trim($input['province'] ?? 'تهران');
                $insUser = $db->prepare("INSERT INTO users (phone, full_name, city, province, tier) VALUES (?, ?, ?, ?, 'FREE')");
                $insUser->execute([$ownerPhone, $displayName, $city, $province]);
                $newUid = $db->lastInsertId();
                $user = ['id' => $newUid, 'phone' => $ownerPhone, 'full_name' => $displayName, 'tier' => 'FREE', 'is_banned' => 0, 'can_post_listing' => 1];
            } else if (!empty($ownerName) && ($user['full_name'] === 'کاربر بده بره' || empty($user['full_name']))) {
                // Update display name if user has default name
                $updName = $db->prepare("UPDATE users SET full_name = ? WHERE id = ?");
                $updName->execute([$ownerName, $user['id']]);
                $user['full_name'] = $ownerName;
            }
        } elseif (!empty($input['user_id'])) {
            $userId = (int)$input['user_id'];
            $userStmt = $db->prepare("SELECT * FROM users WHERE id = ?");
            $userStmt->execute([$userId]);
            $user = $userStmt->fetch();
        }
    }

    // If still no user, create a unique guest user record
    if (!$user) {
        $guestPhone = '09' . rand(100000000, 999999999);
        $displayName = !empty($ownerName) ? $ownerName : 'کاربر مهمان';
        $insGuest = $db->prepare("INSERT INTO users (phone, full_name, city, tier) VALUES (?, ?, 'تهران', 'FREE')");
        $insGuest->execute([$guestPhone, $displayName]);
        $user = ['id' => $db->lastInsertId(), 'phone' => $guestPhone, 'full_name' => $displayName, 'tier' => 'FREE', 'is_banned' => 0, 'can_post_listing' => 1];
    }

    if (!$user) {
        jsonError('کاربر معتبر یافت نشد. لطفاً مجدداً وارد شوید.', 'USER_NOT_FOUND', 404);
    }

    if ($user['is_banned'] == 1) {
        jsonError('حساب کاربری شما مسدود شده است: ' . ($user['ban_reason'] ?? ''), 'USER_BANNED', 403);
    }

    if ($user['can_post_listing'] == 0) {
        jsonError('امکان ثبت آگهی برای حساب کاربری شما توسط مدیر غیرفعال شده است.', 'POSTING_DISABLED', 403);
    }

    $title = trim($input['title'] ?? '');
    $description = trim($input['description'] ?? '');
    $rawCategory = trim($input['category_id'] ?? 'cat_tools');
    $categoryId = mapCategoryId($rawCategory);
    $type = trim($input['type'] ?? 'FREE_GIFT');
    $city = trim($input['city'] ?? 'تهران');
    $location = trim($input['approximate_location'] ?? $input['province'] ?? 'مرکز شهر');
    $imageUrl = trim($input['image_url'] ?? '');
    
    // Process and save Base64 Image to file if provided
    if (!empty($imageUrl) && strpos($imageUrl, 'data:image') === 0) {
        $uploadsDir = __DIR__ . '/../uploads';
        if (!is_dir($uploadsDir)) {
            @mkdir($uploadsDir, 0777, true);
        }
        if (preg_match('/^data:image\/(\w+);base64,/', $imageUrl, $typeMatches)) {
            $imageType = strtolower($typeMatches[1]);
            if (!in_array($imageType, ['jpg', 'jpeg', 'png', 'webp', 'gif'])) {
                $imageType = 'jpg';
            }
            $cleanBase64 = substr($imageUrl, strpos($imageUrl, ',') + 1);
            $decodedData = base64_decode($cleanBase64);
            if ($decodedData !== false) {
                $fileName = 'listing_' . time() . '_' . rand(1000, 9999) . '.' . $imageType;
                $filePath = $uploadsDir . '/' . $fileName;
                if (@file_put_contents($filePath, $decodedData)) {
                    $imageUrl = 'uploads/' . $fileName;
                }
            }
        }
    }

    $discountCode = trim($input['discount_code'] ?? '');
    $discountPercent = !empty($input['discount_percentage']) ? (int)$input['discount_percentage'] : null;
    $discountAmount = !empty($input['discount_amount']) ? (int)$input['discount_amount'] : null;

    if (empty($title)) {
        jsonError('عنوان آگهی الزامی است.', 'VALIDATION_ERROR', 400);
    }

    // Check Diamond requirement for discounts
    if ($type === 'DISCOUNT' && getSetting('require_diamond_for_discounts', '1') == '1') {
        if ($user['tier'] !== 'DIAMOND') {
            jsonError('ثبت کوپن و آگهی تخفیف‌دار نیازمند عضویت الماس (VIP تجاری) می‌باشد.', 'DIAMOND_REQUIRED', 403);
        }
    }

    // Check if category is locked
    $catStmt = $db->prepare("SELECT is_locked, lock_message FROM categories WHERE id = ?");
    $catStmt->execute([$categoryId]);
    $cat = $catStmt->fetch();

    if ($cat && $cat['is_locked'] == 1) {
        jsonError($cat['lock_message'] ?? 'ثبت آگهی در این بخش موقتاً متوقف است.', 'CATEGORY_LOCKED', 403);
    }

    // Scan forbidden words
    $forbiddenWords = $db->query("SELECT word, action_type FROM forbidden_words")->fetchAll();
    $fullContent = $title . ' ' . $description;
    foreach ($forbiddenWords as $fw) {
        if (mb_stripos($fullContent, $fw['word']) !== false && $fw['action_type'] === 'BLOCK') {
            jsonError("آگهی شما حاوی کلمه ممنوعه «{$fw['word']}» است و قابل ثبت نمی‌باشد.", 'FORBIDDEN_CONTENT', 400);
        }
    }

    // Save as PENDING for admin approval
    $stmt = $db->prepare("INSERT INTO listings (
        user_id, title, description, category_id, type, status, city, approximate_location,
        visibility_tier, image_url, discount_code, discount_percentage, discount_amount_toman
    ) VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)");

    $stmt->execute([
        $user['id'],
        $title,
        $description,
        $categoryId,
        $type,
        $city,
        $location,
        $user['tier'],
        $imageUrl ?: null,
        $discountCode ?: null,
        $discountPercent,
        $discountAmount
    ]);

    $newId = $db->lastInsertId();

    jsonResponse('success', 'آگهی با موفقیت ثبت شد و پس از بازبینی و تایید مدیریت منتشر خواهد شد.', [
        'id' => (int)$newId,
        'status' => 'PENDING',
        'title' => $title
    ], 201);
}
