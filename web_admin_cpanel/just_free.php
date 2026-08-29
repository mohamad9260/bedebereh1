<?php
$pageTitle = 'مدیریت بخش «همین الان رایگان شد»';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } else {
        if ($action === 'save_hours') {
            $hours = (int)($_POST['just_free_hours'] ?? 24);
            if ($hours < 1) $hours = 24;
            setSetting('just_free_hours', (string)$hours, 'بازه زمانی نمایش در بخش همین الان رایگان شد (ساعت)');
            $message = "بازه زمانی نمایش به {$hours} ساعت تغییر یافت.";
        } elseif ($action === 'release_instant') {
            $listingId = (int)($_POST['listing_id'] ?? 0);
            if ($listingId > 0) {
                $up = $db->prepare("UPDATE listings SET visibility_tier = 'FREE', status = 'APPROVED', created_at = NOW() WHERE id = ?");
                $up->execute([$listingId]);
                $message = "آگهی با شناسه #{$listingId} همین حالا برای همه کاربران آزاد و در صدر لیست قرار گرفت.";
            }
        } elseif ($action === 'make_free') {
            $listingId = (int)($_POST['listing_id'] ?? 0);
            if ($listingId > 0) {
                $up = $db->prepare("UPDATE listings SET type = 'FREE_GIFT', status = 'APPROVED', created_at = NOW() WHERE id = ?");
                $up->execute([$listingId]);
                $message = "آگهی با شناسه #{$listingId} به بخش هدایای رایگان منتقل شد.";
            }
        }
    }
}

$justFreeHours = (int)getSetting('just_free_hours', '24');

// Query listings that are FREE_GIFT and approved, ordered by created_at DESC (within the just_free_hours window)
$stmt = $db->prepare("SELECT l.*, u.full_name as author_name, u.phone as author_phone, c.name_fa as category_name
                      FROM listings l
                      LEFT JOIN users u ON l.user_id = u.id
                      LEFT JOIN categories c ON l.category_id = c.id
                      WHERE l.type = 'FREE_GIFT' AND l.status = 'APPROVED'
                      ORDER BY l.created_at DESC LIMIT 50");
$stmt->execute();
$justFreeListings = $stmt->fetchAll();

// Query VIP items that can be released to free
$vipStmt = $db->prepare("SELECT l.*, u.full_name as author_name, c.name_fa as category_name
                         FROM listings l
                         LEFT JOIN users u ON l.user_id = u.id
                         LEFT JOIN categories c ON l.category_id = c.id
                         WHERE l.visibility_tier IN ('SILVER', 'GOLD', 'DIAMOND') AND l.status = 'APPROVED'
                         ORDER BY l.created_at DESC LIMIT 20");
$vipStmt->execute();
$vipListings = $vipStmt->fetchAll();
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

<div class="space-y-8">
    <!-- Top Settings Card -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div class="space-y-1">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-amber-500/10 text-amber-400 flex items-center justify-center">
                    <i class="fa-solid fa-bolt-lightning text-lg"></i>
                </div>
                <h2 class="text-xl font-bold text-white">تنظیمات و مانیتورینگ «همین الان رایگان شد»</h2>
            </div>
            <p class="text-xs text-slate-400 mr-13">آگهی‌های رایگانی که تازه اضافه شده‌اند یا دوره انحصار طلایی آن‌ها تمام شده و به رایگان عمومی تبدیل شده‌اند.</p>
        </div>

        <form method="POST" action="just_free.php" class="flex items-center gap-3 bg-slate-950 p-2.5 rounded-2xl border border-slate-800">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="save_hours">
            <label class="text-xs text-slate-300 font-semibold whitespace-nowrap">بازه زمانی تازه‌ها:</label>
            <input type="number" name="just_free_hours" value="<?= $justFreeHours ?>" min="1" max="168" class="w-20 bg-slate-900 border border-slate-700 rounded-xl px-3 py-1.5 text-sm font-mono text-center text-white focus:outline-none focus:border-amber-400">
            <span class="text-xs text-slate-400">ساعت</span>
            <button type="submit" class="bg-amber-600 hover:bg-amber-500 text-white text-xs font-bold px-4 py-2 rounded-xl transition">
                ذخیره
            </button>
        </form>
    </div>

    <!-- Active Just-Free Feed -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div class="flex items-center justify-between pb-4 mb-6 border-b border-slate-800">
            <h3 class="font-bold text-white text-base flex items-center gap-2">
                <i class="fa-solid fa-gift text-emerald-400"></i>
                آگهی‌های فعال در تب «همین الان رایگان شد» (<?= persianNumber(count($justFreeListings)) ?> مورد)
            </h3>
            <span class="text-xs text-slate-400">به ترتیب جدیدترین به قدیمی‌ترین</span>
        </div>

        <?php if (empty($justFreeListings)): ?>
            <div class="text-center py-12 text-slate-500 text-sm">
                هیچ آگهی رایگانی در حال حاضر یافت نشد.
            </div>
        <?php else: ?>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <?php foreach ($justFreeListings as $item): ?>
                <div class="bg-slate-950 border border-slate-800 rounded-2xl p-4 flex flex-col justify-between hover:border-emerald-500/40 transition">
                    <div>
                        <div class="flex items-start justify-between gap-2 mb-2">
                            <span class="bg-emerald-500/10 text-emerald-400 text-[11px] font-bold px-2.5 py-0.5 rounded-full border border-emerald-500/20">
                                هدیه رایگان
                            </span>
                            <span class="text-[11px] text-slate-500 font-mono"><?= htmlspecialchars(substr($item['created_at'], 5, 11)) ?></span>
                        </div>
                        <h4 class="font-bold text-white text-sm line-clamp-1 mb-1"><?= htmlspecialchars($item['title']) ?></h4>
                        <p class="text-xs text-slate-400 line-clamp-2 mb-3"><?= htmlspecialchars($item['description']) ?></p>
                        
                        <div class="text-[11px] text-slate-400 space-y-1 mb-4">
                            <div><i class="fa-solid fa-folder text-slate-500 ml-1"></i> دسته‌بندی: <span class="text-slate-300"><?= htmlspecialchars($item['category_name'] ?? 'لوازم') ?></span></div>
                            <div><i class="fa-solid fa-location-dot text-slate-500 ml-1"></i> شهر: <span class="text-slate-300"><?= htmlspecialchars($item['city']) ?></span></div>
                            <div><i class="fa-solid fa-user text-slate-500 ml-1"></i> ثبت‌کننده: <span class="text-slate-300"><?= htmlspecialchars($item['author_name'] ?? $item['author_phone']) ?></span></div>
                        </div>
                    </div>

                    <div class="pt-3 border-t border-slate-800/80 flex items-center justify-between">
                        <span class="text-xs font-semibold text-emerald-400">
                            <?= $item['is_reserved'] ? '🔒 رزرو شده' : '✅ آماده دریافت' ?>
                        </span>
                        <a href="listings.php?search=<?= urlencode($item['title']) ?>" class="text-xs text-teal-400 hover:underline">
                            مشاهده در پنل
                        </a>
                    </div>
                </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>

    <!-- VIP Items Quick Release Section -->
    <?php if (!empty($vipListings)): ?>
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div class="flex items-center justify-between pb-4 mb-6 border-b border-slate-800">
            <h3 class="font-bold text-white text-base flex items-center gap-2">
                <i class="fa-solid fa-crown text-amber-400"></i>
                آگهی‌های VIP در دوره اختصاصی (انتشار زودهنگام به رایگان عمومی)
            </h3>
            <span class="text-xs text-slate-400">امکان انتشار فوری بدون انتظار برای پایان ساعت طلایی</span>
        </div>

        <div class="space-y-3">
            <?php foreach ($vipListings as $vip): ?>
            <div class="bg-slate-950 border border-amber-500/20 rounded-2xl p-4 flex items-center justify-between gap-4">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="bg-amber-500/10 text-amber-400 text-[11px] font-bold px-2 py-0.5 rounded-md border border-amber-500/30">
                            پلن <?= htmlspecialchars($vip['visibility_tier']) ?>
                        </span>
                        <h4 class="font-bold text-white text-sm"><?= htmlspecialchars($vip['title']) ?></h4>
                    </div>
                    <p class="text-xs text-slate-400">ثبت‌شده توسط <?= htmlspecialchars($vip['author_name']) ?> در <?= htmlspecialchars($vip['city']) ?> (<?= htmlspecialchars($vip['created_at']) ?>)</p>
                </div>

                <form method="POST" action="just_free.php">
                    <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                    <input type="hidden" name="action" value="release_instant">
                    <input type="hidden" name="listing_id" value="<?= $vip['id'] ?>">
                    <button type="submit" class="bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold px-4 py-2 rounded-xl transition flex items-center gap-1.5 whitespace-nowrap">
                        <i class="fa-solid fa-unlock"></i>
                        انتشار فوری در «همین الان رایگان شد»
                    </button>
                </form>
            </div>
            <?php endforeach; ?>
        </div>
    </div>
    <?php endif; ?>
</div>

<?php require_once 'footer.php'; ?>
