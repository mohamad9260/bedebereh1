<?php
$pageTitle = 'پیام‌ها و تیکت‌های پشتیبانی کاربران';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

// Ensure table exists
$db->exec("CREATE TABLE IF NOT EXISTS `tickets` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `subject` VARCHAR(200) NOT NULL,
  `message` TEXT NOT NULL,
  `admin_reply` TEXT DEFAULT NULL,
  `status` ENUM('OPEN', 'ANSWERED', 'CLOSED') DEFAULT 'OPEN',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci");

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } else {
        if ($action === 'reply_ticket') {
            $ticketId = (int)($_POST['ticket_id'] ?? 0);
            $reply = trim($_POST['admin_reply'] ?? '');
            $status = trim($_POST['status'] ?? 'ANSWERED');

            if ($ticketId > 0 && !empty($reply)) {
                $up = $db->prepare("UPDATE tickets SET admin_reply = ?, status = ? WHERE id = ?");
                $up->execute([$reply, $status, $ticketId]);
                $message = "پاسخ تیکت شماره #{$ticketId} با موفقیت ثبت شد.";
            }
        } elseif ($action === 'close_ticket') {
            $ticketId = (int)($_POST['ticket_id'] ?? 0);
            if ($ticketId > 0) {
                $up = $db->prepare("UPDATE tickets SET status = 'CLOSED' WHERE id = ?");
                $up->execute([$ticketId]);
                $message = "تیکت شماره #{$ticketId} بسته شد.";
            }
        }
    }
}

// Fetch all tickets
$stmt = $db->query("SELECT t.*, u.full_name, u.phone as user_phone 
                    FROM tickets t 
                    LEFT JOIN users u ON t.user_id = u.id 
                    ORDER BY t.created_at DESC");
$tickets = $stmt->fetchAll();
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
            <h2 class="text-xl font-bold text-white">صندوق پیام‌ها و تیکت‌های پشتیبانی</h2>
            <p class="text-xs text-slate-400 mt-1">مشاهده و پاسخ‌گویی به پیام‌های ارسالی کاربران از داخل اپلیکیشن بده بره</p>
        </div>
        <a href="settings.php#contact_info" class="bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold px-4 py-2.5 rounded-xl border border-slate-700 transition flex items-center gap-2">
            <i class="fa-solid fa-headset text-teal-400"></i>
            تنظیم شماره و اطلاعات پشتیبانی
        </a>
    </div>

    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <?php if (empty($tickets)): ?>
            <div class="text-center py-12 text-slate-500 text-sm">
                <i class="fa-solid fa-envelope-open text-3xl mb-3 block text-slate-600"></i>
                هنوز هیچ پیامی از سمت کاربران ارسال نشده است.
            </div>
        <?php else: ?>
            <div class="space-y-4">
                <?php foreach ($tickets as $t): ?>
                <div class="bg-slate-950 border border-slate-800 rounded-2xl p-5 space-y-4">
                    <div class="flex items-start justify-between">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-xl bg-teal-500/10 text-teal-400 flex items-center justify-center font-bold">
                                <i class="fa-solid fa-user"></i>
                            </div>
                            <div>
                                <h4 class="font-bold text-white text-sm"><?= htmlspecialchars($t['subject']) ?></h4>
                                <div class="text-xs text-slate-400 mt-0.5">
                                    ارسال‌کننده: <span class="text-slate-200"><?= htmlspecialchars($t['full_name'] ?? 'کاربر مهمان') ?></span>
                                    (<?= htmlspecialchars($t['phone'] ?? $t['user_phone'] ?? 'بدون شماره') ?>)
                                    - <span class="text-slate-500 font-mono"><?= htmlspecialchars($t['created_at']) ?></span>
                                </div>
                            </div>
                        </div>

                        <span class="text-[11px] font-bold px-3 py-1 rounded-full border <?= 
                            $t['status'] === 'OPEN' ? 'bg-amber-500/10 text-amber-400 border-amber-500/30' : 
                            ($t['status'] === 'ANSWERED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30' : 'bg-slate-800 text-slate-400 border-slate-700')
                        ?>">
                            <?= $t['status'] === 'OPEN' ? 'در انتظار پاسخ' : ($t['status'] === 'ANSWERED' ? 'پاسخ داده شده' : 'بسته شده') ?>
                        </span>
                    </div>

                    <!-- Message Body -->
                    <div class="bg-slate-900/80 p-4 rounded-xl text-xs text-slate-300 leading-relaxed border border-slate-800/80">
                        <?= nl2br(htmlspecialchars($t['message'])) ?>
                    </div>

                    <!-- Reply Section -->
                    <?php if (!empty($t['admin_reply'])): ?>
                        <div class="bg-emerald-950/30 border border-emerald-500/30 p-4 rounded-xl text-xs text-emerald-200 space-y-1">
                            <div class="font-bold flex items-center gap-1.5 text-emerald-400">
                                <i class="fa-solid fa-reply"></i>
                                پاسخ مدیریت:
                            </div>
                            <div><?= nl2br(htmlspecialchars($t['admin_reply'])) ?></div>
                        </div>
                    <?php endif; ?>

                    <!-- Reply Form -->
                    <form method="POST" action="tickets.php" class="pt-2 border-t border-slate-800 flex flex-col md:flex-row items-stretch md:items-center gap-3">
                        <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                        <input type="hidden" name="action" value="reply_ticket">
                        <input type="hidden" name="ticket_id" value="<?= $t['id'] ?>">

                        <input type="text" name="admin_reply" placeholder="متن پاسخ مدیریت..." required
                               class="flex-1 bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-teal-400">

                        <button type="submit" class="bg-teal-600 hover:bg-teal-500 text-white text-xs font-bold px-4 py-2 rounded-xl transition flex items-center justify-center gap-1.5 whitespace-nowrap">
                            <i class="fa-solid fa-paper-plane"></i>
                            ارسال پاسخ
                        </button>
                    </form>
                </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>
    </div>
</div>

<?php require_once 'footer.php'; ?>
