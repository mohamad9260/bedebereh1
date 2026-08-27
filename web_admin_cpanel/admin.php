<?php
/**
 * ==============================================================================
 * پنل مدیریت جامع و یکپارچه سامانه بده‌ببر (BedeBere Master Control Center)
 * سازگار ۱۰۰٪ با تمامی مدل‌ها، ویژگی‌ها و پکیج‌های اپلیکیشن اندروید بده‌ببر
 * ==============================================================================
 */
header('Content-Type: text/html; charset=utf-8');

// تنظیمات دیتابیس
$db_host = 'localhost';
$db_name = 'cp64397_bedebere';
$db_user = 'cp64397_adminbedebere';
$db_pass = 'رمز_دیتابیس_شما'; // <--- رمز عبور دیتابیس خود را اینجا وارد کنید

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8mb4", $db_user, $db_pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
    ]);
    
    // ۱. جدول تنظیمات پویای سیستم (System Dynamic Settings)
    $pdo->exec("CREATE TABLE IF NOT EXISTS system_settings (
        setting_key VARCHAR(50) PRIMARY KEY,
        setting_value VARCHAR(255) NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

    // تنظیمات پیش‌فرض منطبق بر اپلیکیشن
    $defaultSettings = [
        'gold_early_access_hours' => '2',
        'silver_early_access_hours' => '1',
        'diamond_early_access_hours' => '2',
        'just_became_available_hours' => '24',
        'require_diamond_for_discounts' => '1',
        'free_daily_reserve_limit' => '3',
        'silver_daily_reserve_limit' => '8',
        'gold_daily_reserve_limit' => '15',
        'diamond_daily_reserve_limit' => '25'
    ];
    foreach ($defaultSettings as $k => $v) {
        $pdo->prepare("INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES (?, ?)")->execute([$k, $v]);
    }

    // ۲. جدول پکیج‌های اشتراک (منطبق بر MembershipTier در اپ)
    $pdo->exec("CREATE TABLE IF NOT EXISTS membership_plans (
        id VARCHAR(30) PRIMARY KEY,
        title VARCHAR(100) NOT NULL,
        price_toman INT NOT NULL DEFAULT 0,
        duration_days INT NOT NULL DEFAULT 30,
        early_access_hours INT NOT NULL DEFAULT 0,
        daily_reserve_limit INT NOT NULL DEFAULT 3,
        can_post_discounts TINYINT(1) NOT NULL DEFAULT 0,
        description TEXT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

    // بررسی و ثبت پکیج‌های واقعی اپ (FREE, SILVER, GOLD, DIAMOND)
    $checkPlans = $pdo->query("SELECT COUNT(*) FROM membership_plans")->fetchColumn();
    if ($checkPlans == 0) {
        $pdo->exec("INSERT INTO membership_plans (id, title, price_toman, duration_days, early_access_hours, daily_reserve_limit, can_post_discounts, description) VALUES
            ('FREE', 'عادی (رایگان)', 0, 365, 0, 3, 0, 'امکان رزرو ۳ هدیه در روز، مشاهده عمومی آگهی‌ها پس از اتمام زمان زودهنگام'),
            ('SILVER', 'نقره‌ای', 49000, 30, 1, 8, 0, '۱ ساعت دسترسی زودهنگام به هدایا، سقف ۸ رزرو روزانه و نشان نقره‌ای'),
            ('GOLD', 'طلایی', 99000, 30, 2, 15, 0, '۲ ساعت دسترسی زودهنگام به تمام هدایا، سقف ۱۵ رزرو روزانه و نشان طلایی'),
            ('DIAMOND', 'الماس (VIP تجاری)', 149000, 30, 2, 25, 1, 'دسترسی زودهنگام ویژه، ۲۵ رزرو روزانه، مجوز انحصاری ثبت کدهای تخفیف و کوپن تجاری');");
    }

    // ۳. جدول دسته‌بندی‌ها با تفکیک نوع (type: FREE_GIFT, DISCOUNT, REQUEST)
    $pdo->exec("CREATE TABLE IF NOT EXISTS categories (
        id VARCHAR(50) PRIMARY KEY,
        title VARCHAR(100) NOT NULL,
        type ENUM('FREE_GIFT', 'DISCOUNT', 'REQUEST') NOT NULL DEFAULT 'FREE_GIFT',
        icon_name VARCHAR(50) DEFAULT 'category',
        parent_id VARCHAR(50) NULL,
        sort_order INT DEFAULT 0
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

    // ۴. جدول اعلان‌ها
    $pdo->exec("CREATE TABLE IF NOT EXISTS notifications (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NULL,
        title VARCHAR(200) NOT NULL,
        message TEXT NOT NULL,
        type VARCHAR(50) DEFAULT 'SYSTEM',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

} catch (PDOException $e) {
    die("<div style='font-family:tahoma;padding:25px;color:#b91c1c;background:#fee2e2;border-radius:10px;margin:20px;direction:rtl;'>خطا در اتصال به دیتابیس MySQL: <br><strong>" . htmlspecialchars($e->getMessage()) . "</strong><br><br>لطفاً نام دیتابیس، نام کاربری و پسورد را در ابتدای فایل بررسی فرمایید.</div>");
}

$tab = isset($_GET['tab']) ? $_GET['tab'] : 'dashboard';
$action = isset($_GET['action']) ? $_GET['action'] : '';
$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;
$msg = isset($_GET['msg']) ? $_GET['msg'] : '';

// --------------------------------------------------------------------------
// پردازش عملیات آگهی‌ها
// --------------------------------------------------------------------------
if ($id > 0) {
    if ($action === 'approve_listing') {
        $pdo->prepare("UPDATE listings SET approval_status = 'APPROVED', status = 'PUBLIC' WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=آگهی تایید شد و در وضعیت انتشار عمومی قرار گرفت"); exit();
    } elseif ($action === 'reject_listing') {
        $pdo->prepare("UPDATE listings SET approval_status = 'REJECTED', status = 'REJECTED' WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=آگهی با موفقیت رد شد"); exit();
    } elseif ($action === 'unreserve_listing') {
        // آزادسازی از رزرو
        $pdo->prepare("UPDATE listings SET is_reserved = 0, status = 'PUBLIC' WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=آگهی از حالت رزرو خارج و آزاد شد"); exit();
    } elseif ($action === 'complete_listing') {
        // تغییر وضعیت به تحویل شده / رفت!
        $pdo->prepare("UPDATE listings SET status = 'COMPLETED' WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=وضعیت آگهی به تحویل شده (رفت!) تغییر یافت"); exit();
    } elseif ($action === 'archive_listing') {
        $pdo->prepare("UPDATE listings SET status = 'ARCHIVED' WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=آگهی بایگانی شد"); exit();
    } elseif ($action === 'delete_listing') {
        $pdo->prepare("DELETE FROM listings WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=listings&msg=آگهی به طور کامل از سیستم حذف شد"); exit();
    }
}

// --------------------------------------------------------------------------
// پردازش تنظیمات پویای سیستم
// --------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save_system_settings'])) {
    foreach ($_POST['settings'] as $k => $v) {
        $pdo->prepare("INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?")->execute([$k, $v, $v]);
    }
    header("Location: admin.php?tab=system_settings&msg=تنظیمات پویای سیستم با موفقیت به‌روزرسانی شد"); exit();
}

// --------------------------------------------------------------------------
// به‌روزرسانی تعرفه پکیج‌ها (FREE, SILVER, GOLD, DIAMOND)
// --------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['update_plans'])) {
    foreach ($_POST['plans'] as $planId => $data) {
        $price = (int)$data['price'];
        $duration = (int)$data['duration'];
        $earlyHours = (int)$data['early_access_hours'];
        $dailyLimit = (int)$data['daily_reserve_limit'];
        $canPostDiscounts = isset($data['can_post_discounts']) ? 1 : 0;
        $desc = trim($data['desc']);

        $pdo->prepare("UPDATE membership_plans SET price_toman = ?, duration_days = ?, early_access_hours = ?, daily_reserve_limit = ?, can_post_discounts = ?, description = ? WHERE id = ?")
            ->execute([$price, $duration, $earlyHours, $dailyLimit, $canPostDiscounts, $desc, $planId]);
    }
    header("Location: admin.php?tab=plans&msg=تعرفه‌ها و قوانین پکیج‌ها با موفقیت ذخیره شد"); exit();
}

// --------------------------------------------------------------------------
// مدیریت کاربران
// --------------------------------------------------------------------------
if ($id > 0) {
    if ($action === 'verify_user') {
        $pdo->prepare("UPDATE users SET is_verified = 1 WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=users&msg=کاربر با موفقیت احراز هویت شد"); exit();
    } elseif ($action === 'toggle_ban') {
        $pdo->prepare("UPDATE users SET is_banned = IF(is_banned=1, 0, 1) WHERE id = ?")->execute([$id]);
        header("Location: admin.php?tab=users&msg=وضعیت دسترسی کاربر تغییر کرد"); exit();
    }
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['update_user_tier'])) {
    $uid = (int)$_POST['user_id'];
    $tier = strtoupper(trim($_POST['membership_type']));
    $pdo->prepare("UPDATE users SET membership_type = ? WHERE id = ?")->execute([$tier, $uid]);
    header("Location: admin.php?tab=users&msg=سطح اشتراک کاربر به $tier ارتقاء/تغییر یافت"); exit();
}

// --------------------------------------------------------------------------
// مدیریت دسته‌بندی‌ها
// --------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['save_category'])) {
    $cid = trim($_POST['cat_id']);
    $ctitle = trim($_POST['cat_title']);
    $ctype = trim($_POST['cat_type']);
    $cicon = trim($_POST['cat_icon']);
    $cparent = !empty($_POST['cat_parent']) ? trim($_POST['cat_parent']) : null;
    $csort = (int)$_POST['cat_sort'];

    if (!empty($cid) && !empty($ctitle)) {
        $pdo->prepare("INSERT INTO categories (id, title, type, icon_name, parent_id, sort_order) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title=?, type=?, icon_name=?, parent_id=?, sort_order=?")
            ->execute([$cid, $ctitle, $ctype, $cicon, $cparent, $csort, $ctitle, $ctype, $cicon, $cparent, $csort]);
        header("Location: admin.php?tab=categories&msg=دسته‌بندی با موفقیت ذخیره شد"); exit();
    }
}

if ($action === 'delete_category' && !empty($_GET['cat_id'])) {
    $pdo->prepare("DELETE FROM categories WHERE id = ?")->execute([$_GET['cat_id']]);
    header("Location: admin.php?tab=categories&msg=دسته‌بندی حذف شد"); exit();
}

// --------------------------------------------------------------------------
// ارسال اعلان همگانی
// --------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['send_broadcast'])) {
    $title = trim($_POST['notif_title']);
    $message = trim($_POST['notif_message']);
    if (!empty($title) && !empty($message)) {
        $users = $pdo->query("SELECT id FROM users")->fetchAll(PDO::FETCH_COLUMN);
        $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, 'SYSTEM')");
        foreach ($users as $uid) {
            $stmt->execute([$uid, $title, $message]);
        }
        header("Location: admin.php?tab=broadcast&msg=اعلان همگانی برای تمام کاربران ارسال شد"); exit();
    }
}

// آمار کلی
$totalListings = $pdo->query("SELECT COUNT(*) FROM listings")->fetchColumn();
$freeGiftCount = $pdo->query("SELECT COUNT(*) FROM listings WHERE type = 'FREE_GIFT' OR type IS NULL")->fetchColumn();
$discountCount = $pdo->query("SELECT COUNT(*) FROM listings WHERE type = 'DISCOUNT'")->fetchColumn();
$requestCount = $pdo->query("SELECT COUNT(*) FROM listings WHERE type = 'REQUEST'")->fetchColumn();

$pendingListings = $pdo->query("SELECT COUNT(*) FROM listings WHERE approval_status = 'PENDING' OR approval_status = 'PENDING_REVIEW'")->fetchColumn();
$reservedListings = $pdo->query("SELECT COUNT(*) FROM listings WHERE status = 'RESERVED' OR is_reserved = 1")->fetchColumn();
$completedListings = $pdo->query("SELECT COUNT(*) FROM listings WHERE status = 'COMPLETED'")->fetchColumn();

$totalUsers = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
$diamondUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE UPPER(membership_type) = 'DIAMOND'")->fetchColumn();
$goldUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE UPPER(membership_type) = 'GOLD'")->fetchColumn();
$silverUsers = $pdo->query("SELECT COUNT(*) FROM users WHERE UPPER(membership_type) = 'SILVER'")->fetchColumn();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>مدیریت جامع سامانه بده‌ببر (BedeBere)</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.rtl.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #f1f5f9; font-family: Tahoma, system-ui, -apple-system, sans-serif; font-size: 13.5px; color: #1e293b; }
        .sidebar { min-height: 100vh; background: #0f172a; color: #fff; }
        .sidebar .nav-link { color: #94a3b8; border-radius: 8px; margin-bottom: 6px; padding: 10px 14px; font-weight: 500; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background: #1e293b; color: #38bdf8; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
        .stat-card { border-right: 4px solid #3b82f6; }
        .badge-pending { background-color: #fef08a; color: #854d0e; font-weight: 600; }
        .badge-approved { background-color: #bbf7d0; color: #166534; font-weight: 600; }
        .badge-rejected { background-color: #fecaca; color: #991b1b; font-weight: 600; }
        .badge-reserved { background-color: #fed7aa; color: #9a3412; font-weight: 600; }
        .badge-completed { background-color: #e2e8f0; color: #475569; font-weight: 600; }
        .badge-diamond { background: linear-gradient(135deg, #00B4D8, #0077B6); color: #fff; font-weight: bold; }
        .badge-gold { background: linear-gradient(135deg, #F59E0B, #D97706); color: #fff; font-weight: bold; }
        .badge-silver { background: linear-gradient(135deg, #94A3B8, #64748B); color: #fff; font-weight: bold; }
        .badge-free { background-color: #e2e8f0; color: #334155; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <!-- سایدبار ناوبری -->
        <div class="col-md-3 col-lg-2 sidebar p-3">
            <div class="d-flex align-items-center mb-4 px-2">
                <i class="bi bi-gift-fill text-warning fs-3 me-2"></i>
                <div>
                    <h5 class="mb-0 fw-bold text-white">سامانه بده‌ببر</h5>
                    <small class="text-white-50">پنل فرماندهی و مدیریت</small>
                </div>
            </div>
            <hr class="border-secondary mb-3">
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'dashboard' ? 'active' : '' ?>" href="admin.php?tab=dashboard">
                        <i class="bi bi-speedometer2 me-2"></i> داشبورد و آمار سامانه
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'listings' ? 'active' : '' ?>" href="admin.php?tab=listings">
                        <i class="bi bi-card-checklist me-2"></i> مدیریت جامع آگهی‌ها
                        <?php if ($pendingListings > 0): ?>
                            <span class="badge bg-warning text-dark float-start"><?= $pendingListings ?></span>
                        <?php endif; ?>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'plans' ? 'active' : '' ?>" href="admin.php?tab=plans">
                        <i class="bi bi-tags-fill me-2 text-warning"></i> تعرفه پکیج‌های عضویت
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'system_settings' ? 'active' : '' ?>" href="admin.php?tab=system_settings">
                        <i class="bi bi-sliders me-2 text-info"></i> موتور تنظیمات پویای سیستم
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'categories' ? 'active' : '' ?>" href="admin.php?tab=categories">
                        <i class="bi bi-grid me-2"></i> مدیریت دسته‌بندی‌ها
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'users' ? 'active' : '' ?>" href="admin.php?tab=users">
                        <i class="bi bi-people me-2"></i> کاربران و اشتراک‌ها
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $tab === 'broadcast' ? 'active' : '' ?>" href="admin.php?tab=broadcast">
                        <i class="bi bi-megaphone me-2"></i> ارسال اعلان سراسری
                    </a>
                </li>
            </ul>
        </div>

        <!-- بخش محتوای اصلی -->
        <div class="col-md-9 col-lg-10 p-4">
            <?php if (!empty($msg)): ?>
                <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i> <?= htmlspecialchars($msg) ?>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            <?php endif; ?>

            <!-- ۱. داشبورد و آمار تحلیلی -->
            <?php if ($tab === 'dashboard'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-speedometer2 text-primary me-2"></i>داشبورد آمار و وضعیت آنلاین سیستم</h4>
                
                <!-- کارت‌های آمار آگهی‌ها -->
                <div class="row g-3 mb-4">
                    <div class="col-md-3">
                        <div class="card stat-card p-3" style="border-right-color: #3b82f6;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div><div class="text-muted small">کل آگهی‌ها</div><h3 class="mb-0 fw-bold"><?= number_format($totalListings) ?></h3></div>
                                <i class="bi bi-collection text-primary fs-1"></i>
                            </div>
                            <div class="mt-2 small text-muted">
                                <span class="badge bg-success me-1"><?= $freeGiftCount ?> هدیه</span>
                                <span class="badge bg-danger me-1"><?= $discountCount ?> تخفیف</span>
                                <span class="badge bg-info"><?= $requestCount ?> درخواست</span>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card p-3" style="border-right-color: #f59e0b;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div><div class="text-muted small">در انتظار بررسی</div><h3 class="mb-0 fw-bold text-warning"><?= number_format($pendingListings) ?></h3></div>
                                <i class="bi bi-hourglass-split text-warning fs-1"></i>
                            </div>
                            <div class="mt-2 small text-muted">نیاز به تایید مدیر سامانه</div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card p-3" style="border-right-color: #ea580c;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div><div class="text-muted small">آگهی‌های در رزرو</div><h3 class="mb-0 fw-bold text-danger"><?= number_format($reservedListings) ?></h3></div>
                                <i class="bi bi-bookmark-check text-danger fs-1"></i>
                            </div>
                            <div class="mt-2 small text-muted"><?= $completedListings ?> مورد واگذار و تمام شد</div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="card stat-card p-3" style="border-right-color: #10b981;">
                            <div class="d-flex justify-content-between align-items-center">
                                <div><div class="text-muted small">کاربران ثبت‌نامی</div><h3 class="mb-0 fw-bold text-success"><?= number_format($totalUsers) ?></h3></div>
                                <i class="bi bi-people-fill text-success fs-1"></i>
                            </div>
                            <div class="mt-2 small text-muted">
                                <span class="badge badge-diamond"><?= $diamondUsers ?> الماس</span>
                                <span class="badge badge-gold"><?= $goldUsers ?> طلا</span>
                                <span class="badge badge-silver"><?= $silverUsers ?> نقره</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- جدول جدیدترین آگهی‌ها -->
                <div class="card p-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="fw-bold mb-0">آخرین آگهی‌های اضافه شده در اپلیکیشن</h5>
                        <a href="admin.php?tab=listings" class="btn btn-sm btn-primary">مشاهده همه و فیلتر</a>
                    </div>
                    <?php
                    $latest = $pdo->query("SELECT l.*, u.full_name as owner_name FROM listings l LEFT JOIN users u ON l.user_id = u.id ORDER BY l.id DESC LIMIT 6")->fetchAll();
                    ?>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr><th>شناسه</th><th>نوع آگهی</th><th>عنوان</th><th>شهر</th><th>ثبت‌کننده</th><th>وضعیت</th><th>عملیات</th></tr>
                            </thead>
                            <tbody>
                                <?php if (empty($latest)): ?>
                                    <tr><td colspan="7" class="text-center text-muted py-3">هنوز آگهی‌ای در سیستم ثبت نشده است.</td></tr>
                                <?php else: ?>
                                    <?php foreach ($latest as $r): ?>
                                        <tr>
                                            <td>#<?= $r['id'] ?></td>
                                            <td>
                                                <?php if (($r['type'] ?? '') === 'DISCOUNT'): ?>
                                                    <span class="badge bg-danger">تخفیف و کوپن</span>
                                                <?php elseif (($r['type'] ?? '') === 'REQUEST'): ?>
                                                    <span class="badge bg-info text-dark">درخواست یاری</span>
                                                <?php else: ?>
                                                    <span class="badge bg-success">هدیه رایگان</span>
                                                <?php endif; ?>
                                            </td>
                                            <td class="fw-bold"><?= htmlspecialchars($r['title']) ?></td>
                                            <td><?= htmlspecialchars($r['city']) ?></td>
                                            <td><?= htmlspecialchars($r['owner_name'] ?? 'کاربر') ?></td>
                                            <td><span class="badge badge-<?= strtolower($r['approval_status'] ?? 'pending') ?>"><?= $r['approval_status'] ?? 'PENDING' ?></span></td>
                                            <td><a href="admin.php?tab=listings" class="btn btn-sm btn-outline-primary">مدیریت</a></td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>

            <!-- ۲. مدیریت پیشرفته آگهی‌ها و کالاها -->
            <?php elseif ($tab === 'listings'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-card-checklist text-primary me-2"></i>مدیریت جامع آگهی‌ها، هدایا، تخفیف‌ها و درخواست‌ها</h4>

                <!-- فیلتر و جستجوی پیشرفته -->
                <?php
                $s_query = isset($_GET['s']) ? trim($_GET['s']) : '';
                $s_type = isset($_GET['type']) ? trim($_GET['type']) : '';
                $s_cat = isset($_GET['cat']) ? trim($_GET['cat']) : '';
                $s_city = isset($_GET['city']) ? trim($_GET['city']) : '';
                $s_status = isset($_GET['status']) ? trim($_GET['status']) : '';
                $s_reserve = isset($_GET['reserve']) ? trim($_GET['reserve']) : '';

                $sql = "SELECT l.*, c.title as cat_title, u.full_name as owner_name, u.phone as owner_phone FROM listings l LEFT JOIN categories c ON l.category_id = c.id LEFT JOIN users u ON l.user_id = u.id WHERE 1=1";
                $params = [];
                if (!empty($s_query)) { $sql .= " AND (l.title LIKE ? OR l.description LIKE ?)"; $params[] = "%$s_query%"; $params[] = "%$s_query%"; }
                if (!empty($s_type)) { $sql .= " AND l.type = ?"; $params[] = $s_type; }
                if (!empty($s_cat)) { $sql .= " AND l.category_id = ?"; $params[] = $s_cat; }
                if (!empty($s_city)) { $sql .= " AND l.city LIKE ?"; $params[] = "%$s_city%"; }
                if (!empty($s_status)) { $sql .= " AND l.approval_status = ?"; $params[] = $s_status; }
                if ($s_reserve === '1') { $sql .= " AND (l.is_reserved = 1 OR l.status = 'RESERVED')"; }
                $sql .= " ORDER BY l.id DESC";
                $stmt = $pdo->prepare($sql);
                $stmt->execute($params);
                $allListings = $stmt->fetchAll();

                $categoriesList = $pdo->query("SELECT * FROM categories ORDER BY sort_order ASC")->fetchAll();
                ?>
                <div class="card p-3 mb-4 bg-light border">
                    <form method="GET" class="row g-2 align-items-center">
                        <input type="hidden" name="tab" value="listings">
                        <div class="col-md-3">
                            <input type="text" name="s" class="form-control form-control-sm" placeholder="جستجوی کلمه در عنوان یا توضیحات..." value="<?= htmlspecialchars($s_query) ?>">
                        </div>
                        <div class="col-md-2">
                            <select name="type" class="form-select form-select-sm">
                                <option value="">همه انواع آگهی</option>
                                <option value="FREE_GIFT" <?= $s_type === 'FREE_GIFT' ? 'selected' : '' ?>>🎁 هدایای رایگان</option>
                                <option value="DISCOUNT" <?= $s_type === 'DISCOUNT' ? 'selected' : '' ?>>🏷️ تخفیف‌ها و کوپن‌ها</option>
                                <option value="REQUEST" <?= $s_type === 'REQUEST' ? 'selected' : '' ?>>🙋 درخواست‌ها و نیازمندی‌ها</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <select name="cat" class="form-select form-select-sm">
                                <option value="">همه دسته‌ها</option>
                                <?php foreach ($categoriesList as $cl): ?>
                                    <option value="<?= $cl['id'] ?>" <?= $s_cat === $cl['id'] ? 'selected' : '' ?>><?= $cl['title'] ?> (<?= $cl['type'] ?>)</option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <input type="text" name="city" class="form-control form-control-sm" placeholder="شهر (مثال: تهران)" value="<?= htmlspecialchars($s_city) ?>">
                        </div>
                        <div class="col-md-1">
                            <select name="status" class="form-select form-select-sm">
                                <option value="">وضعیت</option>
                                <option value="PENDING" <?= $s_status === 'PENDING' ? 'selected' : '' ?>>در انتظار</option>
                                <option value="APPROVED" <?= $s_status === 'APPROVED' ? 'selected' : '' ?>>تایید شده</option>
                                <option value="REJECTED" <?= $s_status === 'REJECTED' ? 'selected' : '' ?>>رد شده</option>
                            </select>
                        </div>
                        <div class="col-md-2 d-flex gap-2">
                            <button type="submit" class="btn btn-sm btn-primary flex-grow-1"><i class="bi bi-search me-1"></i> فیلتر</button>
                            <a href="admin.php?tab=listings" class="btn btn-sm btn-outline-secondary">ریست</a>
                        </div>
                    </form>
                </div>

                <div class="card p-4">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th>شناسه</th>
                                    <th>نوع و دسته‌بندی</th>
                                    <th>عنوان و جزئیات</th>
                                    <th>شهر</th>
                                    <th>مالک / تماس</th>
                                    <th>وضعیت تایید</th>
                                    <th>وضعیت رزرو</th>
                                    <th>عملیات مدیر</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php if (empty($allListings)): ?>
                                    <tr><td colspan="8" class="text-center text-muted py-4">هیچ آگهی‌ای با فیلترهای انتخابی یافت نشد.</td></tr>
                                <?php else: ?>
                                    <?php foreach ($allListings as $row): ?>
                                        <tr>
                                            <td>#<?= $row['id'] ?></td>
                                            <td>
                                                <?php if (($row['type'] ?? '') === 'DISCOUNT'): ?>
                                                    <span class="badge bg-danger mb-1 d-inline-block">تخفیف</span>
                                                <?php elseif (($row['type'] ?? '') === 'REQUEST'): ?>
                                                    <span class="badge bg-info text-dark mb-1 d-inline-block">درخواست</span>
                                                <?php else: ?>
                                                    <span class="badge bg-success mb-1 d-inline-block">هدیه رایگان</span>
                                                <?php endif; ?>
                                                <div class="small text-muted"><?= htmlspecialchars($row['cat_title'] ?? $row['category_id']) ?></div>
                                            </td>
                                            <td>
                                                <div class="d-flex align-items-center">
                                                    <?php if (!empty($row['image_url'])): ?>
                                                        <img src="<?= htmlspecialchars($row['image_url']) ?>" class="rounded me-2 border" style="width: 42px; height: 42px; object-fit: cover;">
                                                    <?php endif; ?>
                                                    <div>
                                                        <div class="fw-bold"><?= htmlspecialchars($row['title']) ?></div>
                                                        <small class="text-muted"><?= htmlspecialchars(mb_substr($row['description'] ?? '', 0, 45)) ?>...</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td><?= htmlspecialchars($row['city']) ?></td>
                                            <td>
                                                <div><?= htmlspecialchars($row['owner_name'] ?? 'کاربر') ?></div>
                                                <small class="text-muted"><?= htmlspecialchars($row['contact_phone'] ?? '-') ?></small>
                                            </td>
                                            <td>
                                                <?php if ($row['approval_status'] === 'APPROVED'): ?>
                                                    <span class="badge badge-approved">تایید شده</span>
                                                <?php elseif ($row['approval_status'] === 'PENDING' || $row['approval_status'] === 'PENDING_REVIEW'): ?>
                                                    <span class="badge badge-pending">در انتظار بررسی</span>
                                                <?php else: ?>
                                                    <span class="badge badge-rejected">رد شده</span>
                                                <?php endif; ?>
                                            </td>
                                            <td>
                                                <?php if (($row['status'] ?? '') === 'RESERVED' || !empty($row['is_reserved'])): ?>
                                                    <span class="badge badge-reserved d-block mb-1">رزرو شده</span>
                                                    <a href="admin.php?tab=listings&action=unreserve_listing&id=<?= $row['id'] ?>" class="btn btn-xs btn-outline-danger w-100" onclick="return confirm('آیا این آگهی از حالت رزرو خارج و برای عموم آزاد شود؟')">آزادسازی</a>
                                                <?php elseif (($row['status'] ?? '') === 'COMPLETED'): ?>
                                                    <span class="badge badge-completed">واگذار شد (رفت!)</span>
                                                <?php else: ?>
                                                    <span class="badge bg-success">آماده دریافت</span>
                                                <?php endif; ?>
                                            </td>
                                            <td>
                                                <div class="btn-group btn-group-sm">
                                                    <a href="admin.php?tab=listings&action=approve_listing&id=<?= $row['id'] ?>" class="btn btn-outline-success" title="تایید آگه"><i class="bi bi-check-lg"></i></a>
                                                    <a href="admin.php?tab=listings&action=reject_listing&id=<?= $row['id'] ?>" class="btn btn-outline-warning" title="رد آگهی"><i class="bi bi-x-lg"></i></a>
                                                    <a href="admin.php?tab=listings&action=complete_listing&id=<?= $row['id'] ?>" class="btn btn-outline-secondary" title="ثبت به عنوان تحویل شده (رفت!)"><i class="bi bi-check2-all"></i></a>
                                                    <a href="admin.php?tab=listings&action=delete_listing&id=<?= $row['id'] ?>" class="btn btn-outline-danger" onclick="return confirm('آیا از حذف کامل این آگهی مطمئن هستید؟')" title="حذف کامل"><i class="bi bi-trash"></i></a>
                                                </div>
                                            </td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>

            <!-- ۳. تعرفه و قوانین پکیج‌های اشتراک (منطبق بر MembershipTier اپلیکیشن) -->
            <?php elseif ($tab === 'plans'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-tags-fill text-warning me-2"></i>مدیریت تعرفه و مشخصات پکیج‌های عضویت (منطبق بر اپلیکیشن)</h4>
                
                <?php $plans = $pdo->query("SELECT * FROM membership_plans ORDER BY price_toman ASC")->fetchAll(); ?>
                
                <form method="POST">
                    <input type="hidden" name="update_plans" value="1">
                    <div class="row g-4">
                        <?php foreach ($plans as $p): ?>
                            <?php 
                            $badgeClass = 'border-secondary';
                            if ($p['id'] === 'DIAMOND') $badgeClass = 'border-info';
                            if ($p['id'] === 'GOLD') $badgeClass = 'border-warning';
                            if ($p['id'] === 'SILVER') $badgeClass = 'border-primary';
                            ?>
                            <div class="col-md-6">
                                <div class="card p-4 border-top border-4 <?= $badgeClass ?>">
                                    <div class="d-flex justify-content-between align-items-center mb-3">
                                        <h5 class="fw-bold mb-0"><?= htmlspecialchars($p['title']) ?></h5>
                                        <span class="badge bg-dark"><?= $p['id'] ?></span>
                                    </div>
                                    <div class="mb-3">
                                        <label class="form-label fw-bold">هزینه پکیج (تومان):</label>
                                        <div class="input-group">
                                            <input type="number" name="plans[<?= $p['id'] ?>][price]" class="form-control fw-bold" value="<?= $p['price_toman'] ?>" required>
                                            <span class="input-group-text">تومان</span>
                                        </div>
                                    </div>
                                    <div class="row g-2 mb-3">
                                        <div class="col-4">
                                            <label class="form-label small text-muted">مدت اعتبار (روز):</label>
                                            <input type="number" name="plans[<?= $p['id'] ?>][duration]" class="form-control form-control-sm" value="<?= $p['duration_days'] ?>" required>
                                        </div>
                                        <div class="col-4">
                                            <label class="form-label small text-muted">ساعت دسترسی زودتر:</label>
                                            <input type="number" name="plans[<?= $p['id'] ?>][early_access_hours]" class="form-control form-control-sm" value="<?= $p['early_access_hours'] ?>" required>
                                        </div>
                                        <div class="col-4">
                                            <label class="form-label small text-muted">سقف رزرو روزانه:</label>
                                            <input type="number" name="plans[<?= $p['id'] ?>][daily_reserve_limit]" class="form-control form-control-sm" value="<?= $p['daily_reserve_limit'] ?>" required>
                                        </div>
                                    </div>
                                    <div class="form-check mb-3">
                                        <input class="form-check-input" type="checkbox" name="plans[<?= $p['id'] ?>][can_post_discounts]" id="disc_<?= $p['id'] ?>" <?= !empty($p['can_post_discounts']) ? 'checked' : '' ?>>
                                        <label class="form-check-label small fw-bold" for="disc_<?= $p['id'] ?>">
                                            مجوز انحصاری ثبت کوپن‌ها و کدهای تخفیف تجاری (ویژه الماس VIP)
                                        </label>
                                    </div>
                                    <div>
                                        <label class="form-label small text-muted">توضیحات و مزایا برای نمایش به کاربر:</label>
                                        <textarea name="plans[<?= $p['id'] ?>][desc]" class="form-control form-control-sm" rows="2"><?= htmlspecialchars($p['description'] ?? '') ?></textarea>
                                    </div>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    </div>
                    <div class="mt-4">
                        <button type="submit" class="btn btn-success btn-lg px-5 shadow-sm"><i class="bi bi-save me-2"></i> ذخیره تغییرات پکیج‌ها</button>
                    </div>
                </form>

            <!-- ۴. موتور تنظیمات پویای سیستم (SystemDynamicSettings) -->
            <?php elseif ($tab === 'system_settings'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-sliders text-info me-2"></i>موتور تنظیمات پویای سامانه (SystemDynamicSettings)</h4>
                
                <?php
                $settingsRows = $pdo->query("SELECT * FROM system_settings")->fetchAll(PDO::FETCH_KEY_PAIR);
                ?>
                <div class="card p-4 col-md-9">
                    <form method="POST">
                        <input type="hidden" name="save_system_settings" value="1">
                        
                        <h6 class="fw-bold text-primary mb-3 border-bottom pb-2">ساعات دسترسی زودهنگام (Early Access Window)</h6>
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label class="form-label">ساعات دسترسی زودتر برای کاربران طلایی و الماس:</label>
                                <div class="input-group">
                                    <input type="number" name="settings[gold_early_access_hours]" class="form-control" value="<?= $settingsRows['gold_early_access_hours'] ?? 2 ?>" required>
                                    <span class="input-group-text">ساعت</span>
                                </div>
                                <small class="text-muted">در این ساعات فقط اعضای طلایی و الماس می‌توانند هدیه را رزرو کنند.</small>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">ساعات دسترسی زودتر برای کاربران نقره‌ای:</label>
                                <div class="input-group">
                                    <input type="number" name="settings[silver_early_access_hours]" class="form-control" value="<?= $settingsRows['silver_early_access_hours'] ?? 1 ?>" required>
                                    <span class="input-group-text">ساعت</span>
                                </div>
                                <small class="text-muted">پس از سپری شدن ساعت طلایی، نقره‌ای‌ها نیز مجاز می‌شوند.</small>
                            </div>
                        </div>

                        <h6 class="fw-bold text-primary mb-3 border-bottom pb-2">قوانین بخش «⚡ همین الان رایگان شد» و تخفیف‌ها</h6>
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label class="form-label">مدت زمان ماندگاری در ردیف «همین الان رایگان شد»:</label>
                                <div class="input-group">
                                    <input type="number" name="settings[just_became_available_hours]" class="form-control" value="<?= $settingsRows['just_became_available_hours'] ?? 24 ?>" required>
                                    <span class="input-group-text">ساعت</span>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">الزام اشتراک الماس برای ثبت کوپن تخفیف:</label>
                                <select name="settings[require_diamond_for_discounts]" class="form-select">
                                    <option value="1" <?= ($settingsRows['require_diamond_for_discounts'] ?? '1') == '1' ? 'selected' : '' ?>>بله - فقط کاربران الماس می‌توانند تخفیف ثبت کنند</option>
                                    <option value="0" <?= ($settingsRows['require_diamond_for_discounts'] ?? '1') == '0' ? 'selected' : '' ?>>خیر - برای تمام کاربران آزاد باشد</option>
                                </select>
                            </div>
                        </div>

                        <h6 class="fw-bold text-primary mb-3 border-bottom pb-2">سقف رزرو روزانه برای سطوح مختلف</h6>
                        <div class="row g-3 mb-4">
                            <div class="col-md-3">
                                <label class="form-label small">سقف روزانه عادی (رایگان):</label>
                                <input type="number" name="settings[free_daily_reserve_limit]" class="form-control" value="<?= $settingsRows['free_daily_reserve_limit'] ?? 3 ?>" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small">سقف روزانه نقره‌ای:</label>
                                <input type="number" name="settings[silver_daily_reserve_limit]" class="form-control" value="<?= $settingsRows['silver_daily_reserve_limit'] ?? 8 ?>" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small">سقف روزانه طلایی:</label>
                                <input type="number" name="settings[gold_daily_reserve_limit]" class="form-control" value="<?= $settingsRows['gold_daily_reserve_limit'] ?? 15 ?>" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label small">سقف روزانه الماس VIP:</label>
                                <input type="number" name="settings[diamond_daily_reserve_limit]" class="form-control" value="<?= $settingsRows['diamond_daily_reserve_limit'] ?? 25 ?>" required>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary px-4"><i class="bi bi-save me-2"></i> ذخیره تنظیمات پویای سامانه</button>
                    </form>
                </div>

            <!-- ۵. مدیریت دسته‌بندی‌ها (منطبق بر CategoryData اپ) -->
            <?php elseif ($tab === 'categories'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-grid text-primary me-2"></i>مدیریت دسته‌بندی‌های اپلیکیشن (تفکیک نوع و والد)</h4>
                
                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="card p-4">
                            <h5 class="fw-bold mb-3">افزودن / ویرایش دسته</h5>
                            <form method="POST">
                                <input type="hidden" name="save_category" value="1">
                                <div class="mb-3">
                                    <label class="form-label">شناسه یکتا (کد لاتین)</label>
                                    <input type="text" name="cat_id" class="form-control" placeholder="مثال: cat_books, dc_online" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">عنوان دسته (فارسی)</label>
                                    <input type="text" name="cat_title" class="form-control" placeholder="مثال: کتاب و نشریات" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">نوع دسته‌بندی</label>
                                    <select name="cat_type" class="form-select" required>
                                        <option value="FREE_GIFT">🎁 هدایای رایگان (FREE_GIFT)</option>
                                        <option value="DISCOUNT">🏷️ تخفیف‌ها و کوپن‌ها (DISCOUNT)</option>
                                        <option value="REQUEST">🙋 درخواست‌ها و یاری (REQUEST)</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">نام آیکون متریال</label>
                                    <input type="text" name="cat_icon" class="form-control" placeholder="مثال: menu_book, chair, toys">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label">اولویت ترتیب</label>
                                    <input type="number" name="cat_sort" class="form-control" value="0">
                                </div>
                                <button type="submit" class="btn btn-primary w-100"><i class="bi bi-save me-1"></i> ذخیره دسته‌بندی</button>
                            </form>
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card p-4">
                            <h5 class="fw-bold mb-3">دسته‌بندی‌های فعال در اپلیکیشن</h5>
                            <?php $cats = $pdo->query("SELECT * FROM categories ORDER BY type ASC, sort_order ASC")->fetchAll(); ?>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle">
                                    <thead class="table-light"><tr><th>شناسه</th><th>عنوان دسته</th><th>نوع</th><th>آیکون</th><th>ترتیب</th><th>عملیات</th></tr></thead>
                                    <tbody>
                                        <?php if (empty($cats)): ?>
                                            <tr><td colspan="6" class="text-center text-muted py-3">دسته‌بندی‌ای ثبت نشده است.</td></tr>
                                        <?php else: ?>
                                            <?php foreach ($cats as $c): ?>
                                                <tr>
                                                    <td class="fw-bold text-primary"><?= $c['id'] ?></td>
                                                    <td><?= htmlspecialchars($c['title']) ?></td>
                                                    <td>
                                                        <?php if ($c['type'] === 'DISCOUNT'): ?>
                                                            <span class="badge bg-danger">تخفیف</span>
                                                        <?php elseif ($c['type'] === 'REQUEST'): ?>
                                                            <span class="badge bg-info text-dark">درخواست</span>
                                                        <?php else: ?>
                                                            <span class="badge bg-success">هدیه</span>
                                                        <?php endif; ?>
                                                    </td>
                                                    <td><span class="badge bg-light text-dark border"><?= $c['icon_name'] ?></span></td>
                                                    <td><?= $c['sort_order'] ?></td>
                                                    <td>
                                                        <a href="admin.php?tab=categories&action=delete_category&cat_id=<?= $c['id'] ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('آیا این دسته حذف شود؟')"><i class="bi bi-trash"></i></a>
                                                    </td>
                                                </tr>
                                            <?php endforeach; ?>
                                        <?php endif; ?>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

            <!-- ۶. کاربران و اشتراک‌ها -->
            <?php elseif ($tab === 'users'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-people text-primary me-2"></i>مدیریت کاربران، احراز هویت و ارتقاء سطح اشتراک</h4>
                <div class="card p-4">
                    <?php $usersList = $pdo->query("SELECT * FROM users ORDER BY id DESC")->fetchAll(); ?>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr><th>شناسه</th><th>نام کاربر</th><th>شماره تماس</th><th>شهر</th><th>سطح اشتراک</th><th>احراز هویت</th><th>تغییر اشتراک</th><th>عملیات</th></tr>
                            </thead>
                            <tbody>
                                <?php if (empty($usersList)): ?>
                                    <tr><td colspan="8" class="text-center text-muted py-3">هنوز کاربری در سامانه ثبت‌نام نکرده است.</td></tr>
                                <?php else: ?>
                                    <?php foreach ($usersList as $u): ?>
                                        <?php 
                                        $uTier = strtoupper($u['membership_type'] ?? 'FREE');
                                        ?>
                                        <tr>
                                            <td>#<?= $u['id'] ?></td>
                                            <td class="fw-bold"><?= htmlspecialchars($u['full_name']) ?></td>
                                            <td><?= htmlspecialchars($u['phone']) ?></td>
                                            <td><?= htmlspecialchars($u['city'] ?? 'تهران') ?></td>
                                            <td>
                                                <span class="badge badge-<?= strtolower($uTier) ?>"><?= $uTier ?></span>
                                            </td>
                                            <td>
                                                <?php if (!empty($u['is_verified'])): ?>
                                                    <span class="badge bg-success"><i class="bi bi-patch-check-fill me-1"></i> تایید شده</span>
                                                <?php else: ?>
                                                    <span class="badge bg-secondary">تایید نشده</span>
                                                <?php endif; ?>
                                            </td>
                                            <td>
                                                <form method="POST" class="d-flex gap-1 align-items-center">
                                                    <input type="hidden" name="update_user_tier" value="1">
                                                    <input type="hidden" name="user_id" value="<?= $u['id'] ?>">
                                                    <select name="membership_type" class="form-select form-select-sm" style="width: 110px;">
                                                        <option value="FREE" <?= $uTier === 'FREE' ? 'selected' : '' ?>>عادی (رایگان)</option>
                                                        <option value="SILVER" <?= $uTier === 'SILVER' ? 'selected' : '' ?>>نقره‌ای</option>
                                                        <option value="GOLD" <?= $uTier === 'GOLD' ? 'selected' : '' ?>>طلایی</option>
                                                        <option value="DIAMOND" <?= $uTier === 'DIAMOND' ? 'selected' : '' ?>>الماس VIP</option>
                                                    </select>
                                                    <button type="submit" class="btn btn-sm btn-outline-primary" title="اعمال تغییر"><i class="bi bi-check"></i></button>
                                                </form>
                                            </td>
                                            <td>
                                                <div class="btn-group btn-group-sm">
                                                    <?php if (empty($u['is_verified'])): ?>
                                                        <a href="admin.php?tab=users&action=verify_user&id=<?= $u['id'] ?>" class="btn btn-outline-success" title="تایید هویت"><i class="bi bi-patch-check"></i></a>
                                                    <?php endif; ?>
                                                    <a href="admin.php?tab=users&action=toggle_ban&id=<?= $u['id'] ?>" class="btn btn-outline-<?= !empty($u['is_banned']) ? 'info' : 'danger' ?>" title="مسدود / رفع مسدودی">
                                                        <i class="bi bi-<?= !empty($u['is_banned']) ? 'unlock' : 'lock' ?>"></i>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>

            <!-- ۷. اعلان سراسری -->
            <?php elseif ($tab === 'broadcast'): ?>
                <h4 class="fw-bold mb-4"><i class="bi bi-megaphone text-primary me-2"></i>ارسال اعلان و نوتیفیکیشن همگانی</h4>
                <div class="card p-4 col-md-8">
                    <form method="POST">
                        <input type="hidden" name="send_broadcast" value="1">
                        <div class="mb-3">
                            <label class="form-label fw-bold">عنوان اعلان</label>
                            <input type="text" name="notif_title" class="form-control" placeholder="مثال: هدایای جدید همین الان رایگان شدند!" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">متن پیام</label>
                            <textarea name="notif_message" rows="5" class="form-control" placeholder="متن پیام برای همه کاربران اپلیکیشن..." required></textarea>
                        </div>
                        <button type="submit" class="btn btn-warning fw-bold px-4"><i class="bi bi-send-fill me-2"></i> ارسال پیام به تمام کاربران</button>
                    </form>
                </div>
            <?php endif; ?>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
