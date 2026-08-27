<?php
require_once 'config.php';
requireAdminLogin();

$db = getDB();

// CSV Export Action
if (isset($_GET['export']) && $_GET['export'] === 'csv') {
    header('Content-Type: text/csv; charset=utf-8');
    header('Content-Disposition: attachment; filename=bedebere_transactions_' . date('Y-m-d') . '.csv');
    $output = fopen('php://output', 'w');
    fprintf($output, chr(0xEF).chr(0xBB).chr(0xBF)); // BOM for UTF-8 Excel support
    fputcsv($output, ['شناسه', 'نام کاربر', 'شماره تماس', 'مبلغ (تومان)', 'عنوان طرح', 'کد پیگیری زرین‌پال', 'وضعیت', 'تاریخ و ساعت']);

    $rows = $db->query("SELECT t.*, u.full_name as user_name, u.phone as user_phone 
                        FROM transactions t 
                        JOIN users u ON t.user_id = u.id 
                        ORDER BY t.id DESC")->fetchAll();
    foreach ($rows as $r) {
        fputcsv($output, [
            $r['id'],
            $r['user_name'],
            $r['user_phone'],
            $r['amount_toman'],
            $r['plan_name'],
            $r['zarinpal_ref_id'] ?? '---',
            $r['status'] === 'SUCCESS' ? 'موفق' : 'ناموفق',
            $r['created_at']
        ]);
    }
    fclose($output);
    exit;
}

$pageTitle = 'گزارشات مالی و تراکنش‌های زرین‌پال';
require_once 'header.php';

// Financial Metrics
$totalIncome = $db->query("SELECT COALESCE(SUM(amount_toman), 0) FROM transactions WHERE status = 'SUCCESS'")->fetchColumn();
$monthlyIncome = $db->query("SELECT COALESCE(SUM(amount_toman), 0) FROM transactions WHERE status = 'SUCCESS' AND created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)")->fetchColumn();
$goldSales = $db->query("SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS' AND plan_name LIKE '%طلایی%'")->fetchColumn();
$silverSales = $db->query("SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS' AND plan_name LIKE '%نقره‌ای%'")->fetchColumn();

// Fetch transactions with filter
$statusFilter = $_GET['status'] ?? '';
$search = trim($_GET['q'] ?? '');

$sql = "SELECT t.*, u.full_name as user_name, u.phone as user_phone 
        FROM transactions t 
        JOIN users u ON t.user_id = u.id 
        WHERE 1=1";
$params = [];

if (!empty($statusFilter)) {
    $sql .= " AND t.status = ?";
    $params[] = $statusFilter;
}

if (!empty($search)) {
    $sql .= " AND (u.full_name LIKE ? OR u.phone LIKE ? OR t.zarinpal_ref_id LIKE ?)";
    $term = "%$search%";
    $params[] = $term;
    $params[] = $term;
    $params[] = $term;
}

$sql .= " ORDER BY t.id DESC";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$transactions = $stmt->fetchAll();
?>

<!-- Financial KPI Cards -->
<div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <span class="text-slate-400 text-xs font-semibold">مجموع کل درآمد سامانه</span>
        <div class="text-2xl font-black text-emerald-400 mt-3"><?= formatToman($totalIncome) ?></div>
        <span class="text-xs text-slate-500 mt-2 block">از کلیه درگاه‌های زرین‌پال</span>
    </div>

    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <span class="text-slate-400 text-xs font-semibold">درآمد ۳۰ روز اخیر</span>
        <div class="text-2xl font-black text-teal-400 mt-3"><?= formatToman($monthlyIncome) ?></div>
        <span class="text-xs text-slate-500 mt-2 block">رشد فروش اشتراک</span>
    </div>

    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <span class="text-slate-400 text-xs font-semibold">تعداد فروش طرح طلایی</span>
        <div class="text-2xl font-black text-amber-400 mt-3"><?= persianNumber($goldSales) ?> <span class="text-xs font-normal text-slate-400">عدد</span></div>
        <span class="text-xs text-slate-500 mt-2 block">دسترسی VIP و آنی</span>
    </div>

    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <span class="text-slate-400 text-xs font-semibold">تعداد فروش طرح نقره‌ای</span>
        <div class="text-2xl font-black text-slate-300 mt-3"><?= persianNumber($silverSales) ?> <span class="text-xs font-normal text-slate-400">عدد</span></div>
        <span class="text-xs text-slate-500 mt-2 block">دسترسی با اولویت متوسط</span>
    </div>
</div>

<!-- Filter and Export Bar -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-wrap items-center justify-between gap-4">
    <form method="GET" action="financial.php" class="flex flex-wrap items-center gap-3 flex-1">
        <div class="relative min-w-[240px]">
            <input type="text" name="q" value="<?= htmlspecialchars($search) ?>" placeholder="جستجو نام کاربر، تلفن یا کد رهگیری..."
                   class="w-full bg-slate-950 border border-slate-700 rounded-2xl pr-4 pl-4 py-2.5 text-xs text-white focus:outline-none focus:border-teal-400">
        </div>
        <div>
            <select name="status" class="bg-slate-950 border border-slate-700 rounded-2xl px-4 py-2.5 text-xs text-white focus:outline-none focus:border-teal-400">
                <option value="">همه تراکنش‌ها</option>
                <option value="SUCCESS" <?= $statusFilter === 'SUCCESS' ? 'selected' : '' ?>>تراکنش‌های موفق</option>
                <option value="FAILED" <?= $statusFilter === 'FAILED' ? 'selected' : '' ?>>تراکنش‌های ناموفق</option>
            </select>
        </div>
        <button type="submit" class="bg-teal-500 hover:bg-teal-600 text-white font-bold px-5 py-2.5 rounded-2xl text-xs transition">
            اعمال فیلتر
        </button>
    </form>

    <a href="financial.php?export=csv" class="bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 px-4 py-2.5 rounded-2xl text-xs font-bold transition flex items-center gap-2">
        <i class="fa-solid fa-file-excel"></i>
        <span>خروجی اکسل / CSV</span>
    </a>
</div>

<!-- Transactions Table -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
    <div class="p-6 border-b border-slate-800 flex items-center justify-between">
        <h3 class="font-bold text-white text-base">ریز تراکنش‌های درگاه پرداخت</h3>
    </div>

    <div class="overflow-x-auto">
        <table class="w-full text-right text-sm">
            <thead class="bg-slate-950/80 text-slate-400 text-xs border-b border-slate-800">
                <tr>
                    <th class="py-4 px-6">شناسه</th>
                    <th class="py-4 px-6">کاربر</th>
                    <th class="py-4 px-6">مبلغ پرداختی</th>
                    <th class="py-4 px-6">شرح بسته / اشتراک</th>
                    <th class="py-4 px-6">کد پیگیری زرین‌پال (RefID)</th>
                    <th class="py-4 px-6">وضعیت</th>
                    <th class="py-4 px-6">تاریخ و زمان</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-slate-800/60 text-slate-300 text-xs">
                <?php if (empty($transactions)): ?>
                    <tr>
                        <td colspan="7" class="text-center py-12 text-slate-500">هیچ تراکنشی یافت نشد.</td>
                    </tr>
                <?php else: ?>
                    <?php foreach ($transactions as $tx): ?>
                        <tr class="hover:bg-slate-800/40 transition">
                            <td class="py-4 px-6 font-mono text-slate-500">#<?= $tx['id'] ?></td>
                            <td class="py-4 px-6 font-bold text-white">
                                <div><?= htmlspecialchars($tx['user_name']) ?></div>
                                <div class="font-mono text-slate-400 text-[11px]" dir="ltr"><?= htmlspecialchars($tx['user_phone']) ?></div>
                            </td>
                            <td class="py-4 px-6 font-bold text-emerald-400">
                                <?= formatToman($tx['amount_toman']) ?>
                            </td>
                            <td class="py-4 px-6 text-slate-300">
                                <?= htmlspecialchars($tx['plan_name']) ?>
                            </td>
                            <td class="py-4 px-6 font-mono text-slate-400">
                                <?= htmlspecialchars($tx['zarinpal_ref_id'] ?? '---') ?>
                            </td>
                            <td class="py-4 px-6">
                                <?php if ($tx['status'] === 'SUCCESS'): ?>
                                    <span class="bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 px-2.5 py-1 rounded-full font-bold">موفق</span>
                                <?php else: ?>
                                    <span class="bg-rose-500/10 text-rose-400 border border-rose-500/30 px-2.5 py-1 rounded-full font-bold">ناموفق</span>
                                <?php endif; ?>
                            </td>
                            <td class="py-4 px-6 text-slate-400 font-mono">
                                <?= htmlspecialchars($tx['created_at']) ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<?php require_once 'footer.php'; ?>
