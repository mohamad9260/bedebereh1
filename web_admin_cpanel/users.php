<?php
$pageTitle = 'مدیریت کاربران و مسدودسازی';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

// Handle Ban / Unban / Toggle Posting Action
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $userId = (int)($_POST['user_id'] ?? 0);
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } elseif ($userId > 0) {
        if ($action === 'ban') {
            $reason = trim($_POST['ban_reason'] ?? 'نقض قوانین سامانه');
            $stmt = $db->prepare("UPDATE users SET is_banned = 1, ban_reason = ?, can_post_listing = 0 WHERE id = ?");
            $stmt->execute([$reason, $userId]);
            $message = "کاربر با شناسه #$userId با موفقیت مسدود شد.";
        } elseif ($action === 'unban') {
            $stmt = $db->prepare("UPDATE users SET is_banned = 0, ban_reason = NULL WHERE id = ?");
            $stmt->execute([$userId]);
            $message = "مسدودیت کاربر با شناسه #$userId برطرف شد.";
        } elseif ($action === 'toggle_posting') {
            $current = (int)($_POST['current_status'] ?? 0);
            $newStatus = $current === 1 ? 0 : 1;
            $stmt = $db->prepare("UPDATE users SET can_post_listing = ? WHERE id = ?");
            $stmt->execute([$newStatus, $userId]);
            $message = $newStatus === 1 ? 'امکان ثبت آگهی برای کاربر فعال شد.' : 'امکان ثبت آگهی برای کاربر مسدود شد.';
        } elseif ($action === 'change_tier') {
            $newTier = $_POST['new_tier'] ?? 'FREE';
            $stmt = $db->prepare("UPDATE users SET tier = ? WHERE id = ?");
            $stmt->execute([$newTier, $userId]);
            $message = "سطح اشتراک کاربر به $newTier ارتقا یافت.";
        }
    }
}

// Search & Filter
$search = trim($_GET['q'] ?? '');
$tierFilter = $_GET['tier'] ?? '';
$banFilter = $_GET['banned'] ?? '';

$sql = "SELECT u.*, 
        (SELECT COUNT(*) FROM listings WHERE user_id = u.id) as listings_count,
        (SELECT COUNT(*) FROM transactions WHERE user_id = u.id AND status = 'SUCCESS') as tx_count
        FROM users u WHERE 1=1";
$params = [];

if (!empty($search)) {
    $sql .= " AND (u.full_name LIKE ? OR u.phone LIKE ? OR u.national_id LIKE ? OR u.city LIKE ?)";
    $term = "%$search%";
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
}

if (!empty($tierFilter)) {
    $sql .= " AND u.tier = ?";
    $params[] = $tierFilter;
}

if ($banFilter !== '') {
    $sql .= " AND u.is_banned = ?";
    $params[] = (int)$banFilter;
}

$sql .= " ORDER BY u.id DESC";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$users = $stmt->fetchAll();
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

<!-- Search & Filter Controls -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
    <form method="GET" action="users.php" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        
        <!-- Search input -->
        <div class="lg:col-span-2 relative">
            <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none text-slate-500">
                <i class="fa-solid fa-magnifying-glass"></i>
            </div>
            <input type="text" name="q" value="<?= htmlspecialchars($search) ?>" placeholder="جستجو با نام، شماره تماس، کدملی یا شهر..."
                   class="w-full bg-slate-950 border border-slate-700 rounded-2xl pr-11 pl-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-teal-400">
        </div>

        <!-- Tier Filter -->
        <div>
            <select name="tier" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                <option value="">همه سطوح کاربری</option>
                <option value="FREE" <?= $tierFilter === 'FREE' ? 'selected' : '' ?>>کاربر عادی (رایگان)</option>
                <option value="SILVER" <?= $tierFilter === 'SILVER' ? 'selected' : '' ?>>کاربر نقره‌ای</option>
                <option value="GOLD" <?= $tierFilter === 'GOLD' ? 'selected' : '' ?>>کاربر طلایی</option>
                <option value="DIAMOND" <?= $tierFilter === 'DIAMOND' ? 'selected' : '' ?>>کاربر الماس (VIP)</option>
            </select>
        </div>

        <!-- Ban Filter & Submit -->
        <div class="flex items-center gap-2">
            <select name="banned" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                <option value="">همه وضعیت‌ها</option>
                <option value="0" <?= $banFilter === '0' ? 'selected' : '' ?>>کاربران فعال</option>
                <option value="1" <?= $banFilter === '1' ? 'selected' : '' ?>>کاربران مسدود</option>
            </select>

            <button type="submit" class="bg-teal-500 hover:bg-teal-600 text-white font-bold px-6 py-3 rounded-2xl text-sm transition">
                فیلتر
            </button>
        </div>

    </form>
</div>

<!-- Users Table Card -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
    
    <div class="p-6 border-b border-slate-800 flex items-center justify-between">
        <h3 class="font-bold text-white text-base">لیست کاربران سامانه (<?= persianNumber(count($users)) ?> کاربر)</h3>
    </div>

    <div class="overflow-x-auto">
        <table class="w-full text-right text-sm">
            <thead class="bg-slate-950/80 text-slate-400 text-xs border-b border-slate-800">
                <tr>
                    <th class="py-4 px-6">شناسه</th>
                    <th class="py-4 px-6">نام کاربر</th>
                    <th class="py-4 px-6">شماره موبایل</th>
                    <th class="py-4 px-6">شهر / کدملی</th>
                    <th class="py-4 px-6">سطح اشتراک</th>
                    <th class="py-4 px-6">تعداد آگهی‌ها</th>
                    <th class="py-4 px-6">وضعیت حساب</th>
                    <th class="py-4 px-6 text-center">اقدامات مدیریتی</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/60 text-slate-300">
                <?php if (empty($users)): ?>
                    <tr>
                        <td colspan="8" class="text-center py-12 text-slate-500">کاربری با مشخصات درخواستی یافت نشد.</td>
                    </tr>
                <?php else: ?>
                    <?php foreach ($users as $user): ?>
                        <tr class="hover:bg-slate-800/40 transition">
                            <td class="py-4 px-6 font-mono text-xs text-slate-500">#<?= $user['id'] ?></td>
                            <td class="py-4 px-6 font-bold text-white">
                                <?= htmlspecialchars($user['full_name']) ?>
                            </td>
                            <td class="py-4 px-6 font-mono text-slate-300" dir="ltr">
                                <?= htmlspecialchars($user['phone']) ?>
                            </td>
                            <td class="py-4 px-6 text-xs text-slate-400">
                                <div><?= htmlspecialchars($user['city']) ?></div>
                                <div class="font-mono text-slate-500 mt-0.5"><?= htmlspecialchars($user['national_id'] ?? 'ثبت نشده') ?></div>
                            </td>
                            <td class="py-4 px-6">
                                <?php if ($user['tier'] === 'DIAMOND'): ?>
                                    <span class="bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 text-xs px-2.5 py-1 rounded-full font-bold">الماس (VIP تجاری)</span>
                                <?php elseif ($user['tier'] === 'GOLD'): ?>
                                    <span class="bg-amber-500/10 text-amber-400 border border-amber-500/20 text-xs px-2.5 py-1 rounded-full font-bold">اشتراک طلایی</span>
                                <?php elseif ($user['tier'] === 'SILVER'): ?>
                                    <span class="bg-slate-500/10 text-slate-300 border border-slate-500/20 text-xs px-2.5 py-1 rounded-full font-bold">اشتراک نقره‌ای</span>
                                <?php else: ?>
                                    <span class="bg-slate-800 text-slate-400 text-xs px-2.5 py-1 rounded-full">عادی (رایگان)</span>
                                <?php endif; ?>
                            </td>
                            <td class="py-4 px-6 font-bold text-teal-400">
                                <?= persianNumber($user['listings_count']) ?> آگهی
                            </td>
                            <td class="py-4 px-6">
                                <?php if ($user['is_banned'] == 1): ?>
                                    <span class="bg-rose-500/10 text-rose-400 border border-rose-500/30 text-xs px-2.5 py-1 rounded-full font-bold flex items-center gap-1.5 w-max">
                                        <i class="fa-solid fa-ban"></i>
                                        <span>مسدود شده</span>
                                    </span>
                                    <?php if (!empty($user['ban_reason'])): ?>
                                        <span class="text-[10px] text-slate-500 block mt-1"><?= htmlspecialchars($user['ban_reason']) ?></span>
                                    <?php endif; ?>
                                <?php elseif ($user['can_post_listing'] == 0): ?>
                                    <span class="bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs px-2.5 py-1 rounded-full font-bold flex items-center gap-1.5 w-max">
                                        <i class="fa-solid fa-circle-exclamation"></i>
                                        <span>ثبت آگهی قفل</span>
                                    </span>
                                <?php else: ?>
                                    <span class="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs px-2.5 py-1 rounded-full font-bold flex items-center gap-1.5 w-max">
                                        <i class="fa-solid fa-check"></i>
                                        <span>فعال و مجاز</span>
                                    </span>
                                <?php endif; ?>
                            </td>
                            <td class="py-4 px-6 text-center">
                                <div class="flex items-center justify-center gap-2">
                                    
                                    <!-- Ban / Unban Button with Prompt Modal -->
                                    <?php if ($user['is_banned'] == 1): ?>
                                        <form method="POST" onsubmit="return confirm('آیا از رفع مسدودیت این کاربر اطمینان دارید؟');">
                                            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                                            <input type="hidden" name="action" value="unban">
                                            <input type="hidden" name="user_id" value="<?= $user['id'] ?>">
                                            <button type="submit" class="bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 px-3 py-1.5 rounded-xl text-xs font-bold transition">
                                                رفع مسدودی
                                            </button>
                                        </form>
                                    <?php else: ?>
                                        <button onclick="openBanModal(<?= $user['id'] ?>, '<?= htmlspecialchars($user['full_name'], ENT_QUOTES) ?>')"
                                                class="bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 px-3 py-1.5 rounded-xl text-xs font-bold transition">
                                            مسدودسازی
                                        </button>
                                    <?php endif; ?>

                                    <!-- Toggle Posting -->
                                    <form method="POST">
                                        <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                                        <input type="hidden" name="action" value="toggle_posting">
                                        <input type="hidden" name="user_id" value="<?= $user['id'] ?>">
                                        <input type="hidden" name="current_status" value="<?= $user['can_post_listing'] ?>">
                                        <button type="submit" title="<?= $user['can_post_listing'] == 1 ? 'قفل ثبت آگهی' : 'آزادسازی ثبت آگهی' ?>"
                                                class="w-8 h-8 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 flex items-center justify-center text-xs transition">
                                            <i class="fa-solid <?= $user['can_post_listing'] == 1 ? 'fa-pen-slash text-amber-400' : 'fa-pen text-emerald-400' ?>"></i>
                                        </button>
                                    </form>

                                </div>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>

</div>

<!-- Ban User Modal -->
<div id="banModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center">
                    <i class="fa-solid fa-user-xmark"></i>
                </div>
                <h3 class="font-bold text-white text-base">مسدودسازی حساب کاربر</h3>
            </div>
            <button onclick="closeBanModal()" class="text-slate-400 hover:text-white">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <form method="POST" action="users.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="ban">
            <input type="hidden" id="banUserId" name="user_id" value="">

            <p class="text-slate-300 text-sm">
                آیا از مسدود کردن کاربر <b id="banUserName" class="text-white"></b> اطمینان دارید؟
            </p>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">علت مسدودسازی (جهت نمایش به کاربر):</label>
                <textarea name="ban_reason" rows="3" required
                          class="w-full bg-slate-950 border border-slate-700 rounded-2xl p-3 text-sm text-white focus:outline-none focus:border-rose-400"
                          placeholder="مثال: ثبت آگهی‌های نامعتبر، درخواست مبالغ مشکوک، عدم رعایت قوانین سامانه..."></textarea>
            </div>

            <div class="flex items-center gap-3 pt-2">
                <button type="submit" class="flex-1 bg-rose-500 hover:bg-rose-600 text-white font-bold py-3 rounded-2xl text-sm transition">
                    تایید مسدودسازی
                </button>
                <button type="button" onclick="closeBanModal()" class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-2xl text-sm transition">
                    انصراف
                </button>
            </div>
        </form>
    </div>
</div>

<script>
function openBanModal(userId, userName) {
    document.getElementById('banUserId').value = userId;
    document.getElementById('banUserName').textContent = userName;
    document.getElementById('banModal').classList.remove('hidden');
}
function closeBanModal() {
    document.getElementById('banModal').classList.add('hidden');
}
</script>

<?php require_once 'footer.php'; ?>
