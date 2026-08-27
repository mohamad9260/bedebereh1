<?php
$pageTitle = 'داشبورد و آمار کلی سامانه';
require_once 'header.php';

$db = getDB();

// 1. Fetch KPI counts
$totalUsers = $db->query("SELECT COUNT(*) FROM users")->fetchColumn();
$bannedUsers = $db->query("SELECT COUNT(*) FROM users WHERE is_banned = 1")->fetchColumn();
$goldUsers = $db->query("SELECT COUNT(*) FROM users WHERE tier = 'GOLD'")->fetchColumn();

$totalListings = $db->query("SELECT COUNT(*) FROM listings")->fetchColumn();
$pendingListings = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'PENDING'")->fetchColumn();
$approvedListings = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'APPROVED'")->fetchColumn();

$totalRevenue = $db->query("SELECT COALESCE(SUM(amount_toman), 0) FROM transactions WHERE status = 'SUCCESS'")->fetchColumn();
$totalTransactions = $db->query("SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS'")->fetchColumn();

$lockedCategories = $db->query("SELECT COUNT(*) FROM categories WHERE is_locked = 1")->fetchColumn();
$forbiddenWordsCount = $db->query("SELECT COUNT(*) FROM forbidden_words")->fetchColumn();

// Recent pending listings for quick action
$stmt = $db->query("SELECT l.*, u.full_name as owner_name, u.phone as owner_phone, c.name_fa as category_name 
                    FROM listings l 
                    JOIN users u ON l.user_id = u.id 
                    JOIN categories c ON l.category_id = c.id 
                    ORDER BY l.id DESC LIMIT 5");
$recentListings = $stmt->fetchAll();

// Recent Transactions
$stmt2 = $db->query("SELECT t.*, u.full_name as user_name, u.phone as user_phone 
                     FROM transactions t 
                     JOIN users u ON t.user_id = u.id 
                     ORDER BY t.id DESC LIMIT 5");
$recentTransactions = $stmt2->fetchAll();
?>

<!-- Metric Cards Grid -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">

    <!-- Card 1: Users -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 relative overflow-hidden shadow-xl hover:border-slate-700 transition">
        <div class="flex items-center justify-between">
            <span class="text-slate-400 text-xs font-semibold">کل کاربران عضو</span>
            <div class="w-10 h-10 rounded-2xl bg-teal-500/10 text-teal-400 flex items-center justify-center text-lg">
                <i class="fa-solid fa-users"></i>
            </div>
        </div>
        <div class="text-3xl font-black text-white mt-4"><?= persianNumber($totalUsers) ?> <span class="text-sm font-normal text-slate-400">نفر</span></div>
        <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>کاربران مسدود: <b class="text-rose-400"><?= persianNumber($bannedUsers) ?></b></span>
            <span>طلایی: <b class="text-amber-400"><?= persianNumber($goldUsers) ?></b></span>
        </div>
    </div>

    <!-- Card 2: Listings -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 relative overflow-hidden shadow-xl hover:border-slate-700 transition">
        <div class="flex items-center justify-between">
            <span class="text-slate-400 text-xs font-semibold">آگهی‌های ثبت شده</span>
            <div class="w-10 h-10 rounded-2xl bg-amber-500/10 text-amber-400 flex items-center justify-center text-lg">
                <i class="fa-solid fa-layer-group"></i>
            </div>
        </div>
        <div class="text-3xl font-black text-white mt-4"><?= persianNumber($totalListings) ?> <span class="text-sm font-normal text-slate-400">آگهی</span></div>
        <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>در انتظار تایید: <b class="text-amber-400"><?= persianNumber($pendingListings) ?></b></span>
            <span>منتشر شده: <b class="text-teal-400"><?= persianNumber($approvedListings) ?></b></span>
        </div>
    </div>

    <!-- Card 3: Revenue -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 relative overflow-hidden shadow-xl hover:border-slate-700 transition">
        <div class="flex items-center justify-between">
            <span class="text-slate-400 text-xs font-semibold">درآمد کل از زرین‌پال</span>
            <div class="w-10 h-10 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center text-lg">
                <i class="fa-solid fa-credit-card"></i>
            </div>
        </div>
        <div class="text-2xl font-black text-emerald-400 mt-4"><?= formatToman($totalRevenue) ?></div>
        <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>تراکنش‌های موفق: <b class="text-white"><?= persianNumber($totalTransactions) ?></b></span>
            <a href="financial.php" class="text-teal-400 hover:underline">مشاهده جزئیات</a>
        </div>
    </div>

    <!-- Card 4: Security & Restrictions -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 relative overflow-hidden shadow-xl hover:border-slate-700 transition">
        <div class="flex items-center justify-between">
            <span class="text-slate-400 text-xs font-semibold">امنیت و نظارت هوشمند</span>
            <div class="w-10 h-10 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center text-lg">
                <i class="fa-solid fa-shield-halved"></i>
            </div>
        </div>
        <div class="text-3xl font-black text-white mt-4"><?= persianNumber($forbiddenWordsCount) ?> <span class="text-sm font-normal text-slate-400">کلمه ممنوعه</span></div>
        <div class="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>بخش‌های قفل‌شده: <b class="<?= $lockedCategories > 0 ? 'text-amber-400' : 'text-slate-400' ?>"><?= persianNumber($lockedCategories) ?></b></span>
            <a href="content_control.php" class="text-rose-400 hover:underline">فیلترینگ</a>
        </div>
    </div>

</div>

<!-- Quick Action Shortcuts Bar -->
<div class="bg-slate-900/60 border border-slate-800 rounded-3xl p-6 flex flex-wrap items-center justify-between gap-4">
    <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-teal-500/20 text-teal-400 flex items-center justify-center text-lg">
            <i class="fa-solid fa-bolt"></i>
        </div>
        <div>
            <h3 class="font-bold text-white text-sm">دسترسی‌های سریع مدیریتی</h3>
            <p class="text-xs text-slate-400">اقدامات پرکاربرد برای کنترل لحظه‌ای سیستم</p>
        </div>
    </div>

    <div class="flex items-center flex-wrap gap-3">
        <a href="listings.php?status=PENDING" class="bg-amber-500/10 border border-amber-500/30 hover:bg-amber-500/20 text-amber-400 px-4 py-2.5 rounded-xl text-xs font-bold transition flex items-center gap-2">
            <i class="fa-solid fa-check-double"></i>
            <span>بررسی آگهی‌های در انتظار</span>
        </a>
        <a href="categories.php" class="bg-indigo-500/10 border border-indigo-500/30 hover:bg-indigo-500/20 text-indigo-400 px-4 py-2.5 rounded-xl text-xs font-bold transition flex items-center gap-2">
            <i class="fa-solid fa-lock"></i>
            <span>قفل‌گذاری بخش‌های خاص</span>
        </a>
        <a href="content_control.php" class="bg-rose-500/10 border border-rose-500/30 hover:bg-rose-500/20 text-rose-400 px-4 py-2.5 rounded-xl text-xs font-bold transition flex items-center gap-2">
            <i class="fa-solid fa-ban"></i>
            <span>افزودن واژه ممنوعه</span>
        </a>
        <a href="settings.php" class="bg-slate-800 border border-slate-700 hover:bg-slate-700 text-slate-300 px-4 py-2.5 rounded-xl text-xs font-bold transition flex items-center gap-2">
            <i class="fa-solid fa-key"></i>
            <span>تنظیم مرچنت‌کد زرین‌پال</span>
        </a>
    </div>
</div>

<!-- Two Column Section: Recent Listings and Recent Transactions -->
<div class="grid grid-cols-1 lg:grid-cols-2 gap-8">

    <!-- Recent Listings -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div class="flex items-center justify-between mb-6 pb-4 border-b border-slate-800">
            <div class="flex items-center gap-3">
                <i class="fa-solid fa-bullhorn text-teal-400"></i>
                <h3 class="font-bold text-white text-base">آخرین آگهی‌های ثبت شده</h3>
            </div>
            <a href="listings.php" class="text-xs text-teal-400 hover:underline">مشاهده همه</a>
        </div>

        <div class="space-y-4">
            <?php if (empty($recentListings)): ?>
                <div class="text-center py-8 text-slate-500 text-sm">هیچ آگهی‌ای هنوز ثبت نشده است.</div>
            <?php else: ?>
                <?php foreach ($recentListings as $listing): ?>
                    <div class="bg-slate-950/70 border border-slate-800/80 rounded-2xl p-4 flex items-center justify-between hover:border-slate-700 transition">
                        <div class="space-y-1 max-w-[70%]">
                            <div class="flex items-center gap-2">
                                <span class="text-xs font-bold text-white line-clamp-1"><?= htmlspecialchars($listing['title']) ?></span>
                                <?php if ($listing['status'] === 'APPROVED'): ?>
                                    <span class="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] px-2 py-0.5 rounded-full font-bold">تایید شده</span>
                                <?php elseif ($listing['status'] === 'PENDING'): ?>
                                    <span class="bg-amber-500/10 text-amber-400 border border-amber-500/20 text-[10px] px-2 py-0.5 rounded-full font-bold">در انتظار</span>
                                <?php else: ?>
                                    <span class="bg-rose-500/10 text-rose-400 border border-rose-500/20 text-[10px] px-2 py-0.5 rounded-full font-bold">رد شده</span>
                                <?php endif; ?>
                            </div>
                            <div class="text-xs text-slate-400 flex items-center gap-2">
                                <span><?= htmlspecialchars($listing['category_name']) ?></span>
                                <span>•</span>
                                <span>کاربر: <?= htmlspecialchars($listing['owner_name']) ?> (<?= htmlspecialchars($listing['city']) ?>)</span>
                            </div>
                        </div>
                        <a href="listings.php" class="text-slate-400 hover:text-teal-400 text-xs px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 transition">
                            بررسی
                        </a>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>

    <!-- Recent Transactions -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div class="flex items-center justify-between mb-6 pb-4 border-b border-slate-800">
            <div class="flex items-center gap-3">
                <i class="fa-solid fa-receipt text-emerald-400"></i>
                <h3 class="font-bold text-white text-base">تراکنش‌های اخیر زرین‌پال</h3>
            </div>
            <a href="financial.php" class="text-xs text-teal-400 hover:underline">مشاهده کامل</a>
        </div>

        <div class="space-y-4">
            <?php if (empty($recentTransactions)): ?>
                <div class="text-center py-8 text-slate-500 text-sm">هیچ تراکنشی هنوز ثبت نشده است.</div>
            <?php else: ?>
                <?php foreach ($recentTransactions as $tx): ?>
                    <div class="bg-slate-950/70 border border-slate-800/80 rounded-2xl p-4 flex items-center justify-between hover:border-slate-700 transition">
                        <div class="space-y-1">
                            <div class="flex items-center gap-2">
                                <span class="text-xs font-bold text-white"><?= htmlspecialchars($tx['plan_name']) ?></span>
                                <span class="text-xs font-bold text-emerald-400"><?= formatToman($tx['amount_toman']) ?></span>
                            </div>
                            <div class="text-xs text-slate-400">
                                خریدار: <?= htmlspecialchars($tx['user_name']) ?> (<?= htmlspecialchars($tx['user_phone']) ?>)
                            </div>
                        </div>
                        <div class="text-left">
                            <span class="text-[11px] font-mono text-slate-400 block">کد پیگیری: <?= htmlspecialchars($tx['zarinpal_ref_id'] ?? '---') ?></span>
                            <span class="text-[10px] text-emerald-400 font-bold bg-emerald-500/10 px-2 py-0.5 rounded-md">موفق</span>
                        </div>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>

</div>

<?php require_once 'footer.php'; ?>
