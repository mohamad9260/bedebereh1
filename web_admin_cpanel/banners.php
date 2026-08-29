<?php
$pageTitle = 'مدیریت بنرهای صفحات اپلیکیشن';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

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

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } else {
        if ($action === 'save_banner') {
            $pageKey = trim($_POST['page'] ?? '');
            $title = trim($_POST['title'] ?? '');
            $subtitle = trim($_POST['subtitle'] ?? '');
            $badge = trim($_POST['badge_text'] ?? 'بده بره');
            $imageUrl = trim($_POST['image_url'] ?? '');
            $isActive = isset($_POST['is_active']) ? 1 : 0;

            if (!empty($pageKey) && !empty($title)) {
                $stmt = $db->prepare("INSERT INTO banners (page, title, subtitle, badge_text, image_url, is_active) 
                                      VALUES (?, ?, ?, ?, ?, ?) 
                                      ON DUPLICATE KEY UPDATE 
                                      title = VALUES(title), 
                                      subtitle = VALUES(subtitle), 
                                      badge_text = VALUES(badge_text), 
                                      image_url = VALUES(image_url), 
                                      is_active = VALUES(is_active)");
                $stmt->execute([$pageKey, $title, $subtitle, $badge, $imageUrl, $isActive]);
                $message = "بنر صفحه «{$pageKey}» با موفقیت به‌روزرسانی شد.";
            } else {
                $message = 'لطفاً عنوان و شناسه صفحه را وارد نمایید.';
                $messageType = 'error';
            }
        }
    }
}

// Fetch all banners
$stmt = $db->query("SELECT * FROM banners ORDER BY id ASC");
$bannersRaw = $stmt->fetchAll();
$banners = [];
foreach ($bannersRaw as $b) {
    $banners[$b['page']] = $b;
}

$pageDefinitions = [
    'home' => [
        'name' => 'صفحه اول / خانه (Hero Banner)',
        'desc' => 'بنر بالای صفحه اصلی اپلیکیشن (مثلاً: بده بره، مهربونی رو تکثیر کن)',
        'default_title' => 'بده بره، مهربونی رو تکثیر کن 🌱',
        'default_subtitle' => 'وسایلی که نیاز نداری رو به بقیه ببخش و دنیای قشنگ‌تری بساز',
        'default_badge' => 'مهربانی ماندگار',
        'default_img' => 'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=800&auto=format&fit=crop&q=80'
    ],
    'free_gift' => [
        'name' => 'صفحه هدیه‌های رایگان (Free Gifts)',
        'desc' => 'بنر اختصاصی فیلتر هدایای بدون هزینه',
        'default_title' => 'هدیه‌های بدون هزینه و کارآمد 🎁',
        'default_subtitle' => 'کتاب، لوازم منزل، وسایل دیجیتال و هر چیزی که لازم نداری',
        'default_badge' => 'بخش رایگان',
        'default_img' => 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800&auto=format&fit=crop&q=80'
    ],
    'discount' => [
        'name' => 'صفحه کوپن و تخفیف‌ها (Discounts)',
        'desc' => 'بنر کوپن‌های تخفیف و بن‌های خرید فروشگاه‌ها',
        'default_title' => 'کوپن‌ها و بن‌های تخفیف باورنکردنی 🏷️',
        'default_subtitle' => 'تخفیف‌های ویژه رستوران، پوشاک، دوره‌های آموزشی و فروشگاه‌ها',
        'default_badge' => 'کوپن‌های ویژه',
        'default_img' => 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&auto=format&fit=crop&q=80'
    ],
    'request' => [
        'name' => 'صفحه درخواست‌های یاری (Requests)',
        'desc' => 'بنر اعلام نیازها و درخواست وسایل کم‌بضاعت',
        'default_title' => 'دیوار نیاز و یاری‌رسانی 🤝',
        'default_subtitle' => 'اگر نیازمند وسیله‌ای هستی یا می‌خوای گره‌ای باز کنی',
        'default_badge' => 'یاری‌رسانی',
        'default_img' => 'https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=800&auto=format&fit=crop&q=80'
    ],
    'vip' => [
        'name' => 'صفحه اشتراک‌های VIP و ارتقا',
        'desc' => 'بنر تشویق به ارتقا به حساب طلایی و نقره‌ای',
        'default_title' => 'اشتراک‌های ویژه VIP و دسترسی زودهنگام 💎',
        'default_subtitle' => 'مشاهده آگهی‌ها ۲ ساعت زودتر از عموم و رزرو نامحدود',
        'default_badge' => 'دسترسی سریع',
        'default_img' => 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&auto=format&fit=crop&q=80'
    ]
];
?>

<!-- Feedback Alert -->
<?php if (!empty($message)): ?>
    <div class="<?= $messageType === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' : 'bg-rose-500/10 border-rose-500/30 text-rose-400' ?> border p-4 rounded-2xl text-sm flex items-center justify-between shadow-lg mb-6">
        <div class="flex items-center gap-3">
            <i class="fa-solid <?= $messageType === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation' ?> text-lg"></i>
            <span><?= htmlspecialchars($message) ?></span>
        </div>
    </div>
<?php endif; ?>

<div class="space-y-6">
    <div class="flex items-center justify-between">
        <div>
            <h2 class="text-xl font-bold text-white">مدیریت بنرهای تصویری و متنی صفحات</h2>
            <p class="text-xs text-slate-400 mt-1">تغییر عنوان، زیرعنوان و تصویر بنرهای مختلف اپلیکیشن بدون نیاز به آپدیت اپ</p>
        </div>
    </div>

    <div class="grid grid-cols-1 gap-6">
        <?php foreach ($pageDefinitions as $pageKey => $info): 
            $curr = $banners[$pageKey] ?? [
                'title' => $info['default_title'],
                'subtitle' => $info['default_subtitle'],
                'badge_text' => $info['default_badge'],
                'image_url' => $info['default_img'],
                'is_active' => 1
            ];
        ?>
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
            <form method="POST" action="banners.php" class="space-y-4">
                <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                <input type="hidden" name="action" value="save_banner">
                <input type="hidden" name="page" value="<?= htmlspecialchars($pageKey) ?>">

                <div class="flex items-center justify-between pb-4 border-b border-slate-800">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-2xl bg-teal-500/10 text-teal-400 flex items-center justify-center font-bold">
                            <i class="fa-solid fa-image"></i>
                        </div>
                        <div>
                            <h3 class="font-bold text-white text-base"><?= htmlspecialchars($info['name']) ?></h3>
                            <p class="text-xs text-slate-400"><?= htmlspecialchars($info['desc']) ?></p>
                        </div>
                    </div>
                    <label class="relative inline-flex items-center cursor-pointer">
                        <input type="checkbox" name="is_active" value="1" class="sr-only peer" <?= !empty($curr['is_active']) ? 'checked' : '' ?>>
                        <div class="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:right-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-teal-500"></div>
                        <span class="mr-3 text-xs text-slate-300">فعال در اپ</span>
                    </label>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label class="block text-slate-300 text-xs font-semibold mb-1.5">عنوان اصلی بنر:</label>
                        <input type="text" name="title" value="<?= htmlspecialchars($curr['title']) ?>" required
                               class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-400">
                    </div>

                    <div>
                        <label class="block text-slate-300 text-xs font-semibold mb-1.5">نشان / بج گوشه بنر:</label>
                        <input type="text" name="badge_text" value="<?= htmlspecialchars($curr['badge_text']) ?>"
                               class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-400">
                    </div>

                    <div class="md:col-span-2">
                        <label class="block text-slate-300 text-xs font-semibold mb-1.5">زیرعنوان / متن توضیحات بنر:</label>
                        <input type="text" name="subtitle" value="<?= htmlspecialchars($curr['subtitle']) ?>" required
                               class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-teal-400">
                    </div>

                    <div class="md:col-span-2">
                        <label class="block text-slate-300 text-xs font-semibold mb-1.5">آدرس تصویر یا پوستر بنر (Image URL):</label>
                        <input type="url" name="image_url" value="<?= htmlspecialchars($curr['image_url'] ?? '') ?>" placeholder="https://..."
                               class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-teal-400 text-left" dir="ltr">
                    </div>
                </div>

                <!-- Banner Live Preview Card -->
                <div class="mt-3 p-4 rounded-2xl bg-gradient-to-r from-teal-950/80 to-slate-900 border border-teal-800/40 relative overflow-hidden">
                    <div class="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                        <div class="space-y-1">
                            <span class="inline-block bg-teal-500/20 text-teal-300 text-[11px] px-2.5 py-0.5 rounded-full font-bold border border-teal-500/30">
                                <?= htmlspecialchars($curr['badge_text']) ?>
                            </span>
                            <h4 class="text-base font-black text-white"><?= htmlspecialchars($curr['title']) ?></h4>
                            <p class="text-xs text-slate-300"><?= htmlspecialchars($curr['subtitle']) ?></p>
                        </div>
                        <?php if (!empty($curr['image_url'])): ?>
                            <img src="<?= htmlspecialchars($curr['image_url']) ?>" alt="Preview" class="w-24 h-16 object-cover rounded-xl border border-teal-500/30 shadow-md">
                        <?php endif; ?>
                    </div>
                </div>

                <div class="flex justify-end pt-2">
                    <button type="submit" class="bg-teal-600 hover:bg-teal-500 text-white font-bold px-6 py-2.5 rounded-xl text-xs transition flex items-center gap-2">
                        <i class="fa-solid fa-floppy-disk"></i>
                        ذخیره بنر این صفحه
                    </button>
                </div>
            </form>
        </div>
        <?php endforeach; ?>
    </div>
</div>

<?php require_once 'footer.php'; ?>
