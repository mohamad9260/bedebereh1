<?php
$pageTitle = 'مدیریت و تایید آگهی‌ها';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

// Handle Moderation Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $listingId = (int)($_POST['listing_id'] ?? 0);
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } elseif ($listingId > 0) {
        if ($action === 'approve') {
            $stmt = $db->prepare("UPDATE listings SET status = 'APPROVED', rejection_reason = NULL WHERE id = ?");
            $stmt->execute([$listingId]);
            $message = "آگهی با شناسه #$listingId تایید و منتشر شد.";
        } elseif ($action === 'reject') {
            $reason = trim($_POST['rejection_reason'] ?? 'عدم رعایت قوانین ثبت آگهی');
            $stmt = $db->prepare("UPDATE listings SET status = 'REJECTED', rejection_reason = ? WHERE id = ?");
            $stmt->execute([$reason, $listingId]);
            $message = "آگهی با شناسه #$listingId رد شد.";
        } elseif ($action === 'delete') {
            $stmt = $db->prepare("DELETE FROM listings WHERE id = ?");
            $stmt->execute([$listingId]);
            $message = "آگهی به صورت دائم حذف گردید.";
        }
    }
}

// Filters
$statusFilter = $_GET['status'] ?? '';
$typeFilter = $_GET['type'] ?? '';
$search = trim($_GET['q'] ?? '');

$sql = "SELECT l.*, u.full_name as owner_name, u.phone as owner_phone, u.tier as owner_tier, c.name_fa as category_name 
        FROM listings l 
        JOIN users u ON l.user_id = u.id 
        JOIN categories c ON l.category_id = c.id 
        WHERE 1=1";
$params = [];

if (!empty($statusFilter)) {
    $sql .= " AND l.status = ?";
    $params[] = $statusFilter;
}

if (!empty($typeFilter)) {
    $sql .= " AND l.type = ?";
    $params[] = $typeFilter;
}

if (!empty($search)) {
    $sql .= " AND (l.title LIKE ? OR l.description LIKE ? OR u.full_name LIKE ? OR u.phone LIKE ? OR l.city LIKE ?)";
    $term = "%$search%";
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
}

$sql .= " ORDER BY (l.status = 'PENDING') DESC, l.id DESC";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$listings = $stmt->fetchAll();
?>

<!-- Action Feedback Alert -->
<?php if (!empty($message)): ?>
    <div class="<?= $messageType === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' : 'bg-rose-500/10 border-rose-500/30 text-rose-400' ?> border p-4 rounded-2xl text-sm flex items-center justify-between shadow-lg">
        <div class="flex items-center gap-3">
            <i class="fa-solid <?= $messageType === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation' ?> text-lg"></i>
            <span><?= htmlspecialchars($message) ?></span>
        </div>
    </div>
<?php endif; ?>

<!-- Filter & Search Bar -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
    <form method="GET" action="listings.php" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        
        <div class="lg:col-span-2 relative">
            <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none text-slate-500">
                <i class="fa-solid fa-magnifying-glass"></i>
            </div>
            <input type="text" name="q" value="<?= htmlspecialchars($search) ?>" placeholder="جستجو در عنوان آگهی، توضیحات، نام کاربر یا شهر..."
                   class="w-full bg-slate-950 border border-slate-700 rounded-2xl pr-11 pl-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-400">
        </div>

        <div>
            <select name="status" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                <option value="">همه وضعیت‌ها</option>
                <option value="PENDING" <?= $statusFilter === 'PENDING' ? 'selected' : '' ?>>⏳ در انتظار بررسی</option>
                <option value="APPROVED" <?= $statusFilter === 'APPROVED' ? 'selected' : '' ?>>✅ منتشر شده</option>
                <option value="REJECTED" <?= $statusFilter === 'REJECTED' ? 'selected' : '' ?>>❌ رد شده</option>
                <option value="RESERVED" <?= $statusFilter === 'RESERVED' ? 'selected' : '' ?>>🤝 واگذار / رزرو شده</option>
            </select>
        </div>

        <div class="flex items-center gap-2">
            <select name="type" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                <option value="">همه انواع آگهی</option>
                <option value="FREE_GIFT" <?= $typeFilter === 'FREE_GIFT' ? 'selected' : '' ?>>هدیه ۱۰۰٪ رایگان</option>
                <option value="DISCOUNT" <?= $typeFilter === 'DISCOUNT' ? 'selected' : '' ?>>تخفیف و کوپن</option>
                <option value="REQUEST" <?= $typeFilter === 'REQUEST' ? 'selected' : '' ?>>درخواست کالا/یاری</option>
            </select>

            <button type="submit" class="bg-teal-500 hover:bg-teal-600 text-white font-bold px-6 py-3 rounded-2xl text-sm transition">
                فیلتر
            </button>
        </div>

    </form>
</div>

<!-- Listings Cards Grid / Table -->
<div class="space-y-4">
    <?php if (empty($listings)): ?>
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-12 text-center text-slate-500">
            <i class="fa-solid fa-folder-open text-4xl mb-3 block text-slate-600"></i>
            هیچ آگهی‌ای با این فیلترها یافت نشد.
        </div>
    <?php else: ?>
        <?php foreach ($listings as $item): ?>
            <div class="bg-slate-900 border <?= $item['status'] === 'PENDING' ? 'border-amber-500/40 bg-amber-500/[0.02]' : 'border-slate-800' ?> rounded-3xl p-6 shadow-xl transition">
                <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
                    
                    <!-- Main Content -->
                    <div class="space-y-3 flex-1">
                        <div class="flex flex-wrap items-center gap-2">
                            
                            <!-- Type Badge -->
                            <?php if ($item['type'] === 'FREE_GIFT'): ?>
                                <span class="bg-teal-500/10 text-teal-400 border border-teal-500/30 text-xs px-2.5 py-1 rounded-full font-bold">🌿 هدیه رایگان</span>
                            <?php elseif ($item['type'] === 'DISCOUNT'): ?>
                                <span class="bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs px-2.5 py-1 rounded-full font-bold">🏷️ کوپن تخفیف</span>
                            <?php else: ?>
                                <span class="bg-indigo-500/10 text-indigo-400 border border-indigo-500/30 text-xs px-2.5 py-1 rounded-full font-bold">🤝 درخواست نیازمندی</span>
                            <?php endif; ?>

                            <!-- Category -->
                            <span class="bg-slate-800 text-slate-300 text-xs px-2.5 py-1 rounded-full">دسته‌بندی: <?= htmlspecialchars($item['category_name']) ?></span>

                            <!-- Status Badge -->
                            <?php if ($item['status'] === 'APPROVED'): ?>
                                <span class="bg-emerald-500/10 text-emerald-400 text-xs px-2.5 py-1 rounded-full font-bold">منتشر شده</span>
                            <?php elseif ($item['status'] === 'PENDING'): ?>
                                <span class="bg-amber-500 text-slate-950 text-xs px-2.5 py-1 rounded-full font-bold animate-pulse">⏳ نیازمند بررسی مدیر</span>
                            <?php elseif ($item['status'] === 'REJECTED'): ?>
                                <span class="bg-rose-500/10 text-rose-400 text-xs px-2.5 py-1 rounded-full font-bold">رد شده</span>
                            <?php else: ?>
                                <span class="bg-slate-700 text-slate-300 text-xs px-2.5 py-1 rounded-full font-bold">رزرو / واگذار شده</span>
                            <?php endif; ?>

                            <span class="text-xs text-slate-500 font-mono">شناسه: #<?= $item['id'] ?></span>
                        </div>

                        <!-- Title -->
                        <h4 class="text-lg font-bold text-white"><?= htmlspecialchars($item['title']) ?></h4>

                        <!-- Description -->
                        <p class="text-slate-300 text-sm leading-relaxed"><?= nl2br(htmlspecialchars($item['description'])) ?></p>

                        <!-- Discount Details if any -->
                        <?php if ($item['type'] === 'DISCOUNT'): ?>
                            <div class="bg-amber-500/10 border border-amber-500/20 rounded-xl p-3 text-xs text-amber-300 flex items-center gap-4">
                                <span>درصد تخفیف: <b><?= $item['discount_percentage'] ? persianNumber($item['discount_percentage']) . '٪' : '---' ?></b></span>
                                <span>مبلغ تخفیف: <b><?= $item['discount_amount_toman'] ? formatToman($item['discount_amount_toman']) : '---' ?></b></span>
                                <span>کد کوپن: <code class="bg-slate-900 px-2 py-0.5 rounded font-mono text-white"><?= htmlspecialchars($item['discount_code'] ?? 'ندارد') ?></code></span>
                            </div>
                        <?php endif; ?>

                        <!-- Rejection Reason if any -->
                        <?php if (!empty($item['rejection_reason'])): ?>
                            <div class="bg-rose-500/10 border border-rose-500/20 rounded-xl p-3 text-xs text-rose-300">
                                <b>علت رد آگهی:</b> <?= htmlspecialchars($item['rejection_reason']) ?>
                            </div>
                        <?php endif; ?>

                        <!-- User & Location info -->
                        <div class="flex flex-wrap items-center gap-4 text-xs text-slate-400 pt-2 border-t border-slate-800/80">
                            <span><i class="fa-solid fa-user text-slate-500 ml-1"></i> ثبات: <b class="text-white"><?= htmlspecialchars($item['owner_name']) ?></b> (<?= htmlspecialchars($item['owner_phone']) ?>)</span>
                            <span><i class="fa-solid fa-location-dot text-slate-500 ml-1"></i> <?= htmlspecialchars($item['city']) ?> <?= $item['approximate_location'] ? '• ' . htmlspecialchars($item['approximate_location']) : '' ?></span>
                            <span><i class="fa-solid fa-calendar text-slate-500 ml-1"></i> ثبت: <?= htmlspecialchars($item['created_at']) ?></span>
                        </div>
                    </div>

                    <!-- Action Buttons -->
                    <div class="flex lg:flex-col items-center gap-2 border-t lg:border-t-0 lg:border-r border-slate-800 pt-4 lg:pt-0 lg:pr-6 shrink-0">
                        
                        <!-- Approve -->
                        <?php if ($item['status'] !== 'APPROVED'): ?>
                            <form method="POST" class="w-full">
                                <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                                <input type="hidden" name="action" value="approve">
                                <input type="hidden" name="listing_id" value="<?= $item['id'] ?>">
                                <button type="submit" class="w-full bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold py-2.5 px-4 rounded-xl transition flex items-center justify-center gap-2">
                                    <i class="fa-solid fa-check"></i>
                                    <span>تایید و انتشار</span>
                                </button>
                            </form>
                        <?php endif; ?>

                        <!-- Reject -->
                        <?php if ($item['status'] !== 'REJECTED'): ?>
                            <button onclick="openRejectModal(<?= $item['id'] ?>, '<?= htmlspecialchars($item['title'], ENT_QUOTES) ?>')"
                                    class="w-full bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/20 text-xs font-bold py-2.5 px-4 rounded-xl transition flex items-center justify-center gap-2">
                                <i class="fa-solid fa-xmark"></i>
                                <span>رد آگهی</span>
                            </button>
                        <?php endif; ?>

                        <!-- Delete -->
                        <form method="POST" class="w-full" onsubmit="return confirm('آیا از حذف دائمی این آگهی مطمئن هستید؟');">
                            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="listing_id" value="<?= $item['id'] ?>">
                            <button type="submit" class="w-full bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-xs font-bold py-2 px-4 rounded-xl transition flex items-center justify-center gap-2">
                                <i class="fa-solid fa-trash"></i>
                                <span>حذف دائم</span>
                            </button>
                        </form>

                    </div>

                </div>
            </div>
        <?php endforeach; ?>
    <?php endif; ?>
</div>

<!-- Reject Modal -->
<div id="rejectModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <h3 class="font-bold text-white text-base">رد آگهی</h3>
            <button onclick="closeRejectModal()" class="text-slate-400 hover:text-white">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <form method="POST" action="listings.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="reject">
            <input type="hidden" id="rejectListingId" name="listing_id" value="">

            <p class="text-slate-300 text-sm">
                علت رد آگهی <b id="rejectListingTitle" class="text-white"></b>:
            </p>

            <div>
                <textarea name="rejection_reason" rows="3" required
                          class="w-full bg-slate-950 border border-slate-700 rounded-2xl p-3 text-sm text-white focus:outline-none focus:border-amber-400"
                          placeholder="مثال: درج شماره حساب یا درخواست وجه در هدیه رایگان، کالای ممنوعه، عکس نامناسب..."></textarea>
            </div>

            <div class="flex items-center gap-3">
                <button type="submit" class="flex-1 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold py-3 rounded-2xl text-sm transition">
                    ثبت رد آگهی
                </button>
                <button type="button" onclick="closeRejectModal()" class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-2xl text-sm transition">
                    انصراف
                </button>
            </div>
        </form>
    </div>
</div>

<script>
function openRejectModal(id, title) {
    document.getElementById('rejectListingId').value = id;
    document.getElementById('rejectListingTitle').textContent = title;
    document.getElementById('rejectModal').classList.remove('hidden');
}
function closeRejectModal() {
    document.getElementById('rejectModal').classList.add('hidden');
}
</script>

<?php require_once 'footer.php'; ?>
