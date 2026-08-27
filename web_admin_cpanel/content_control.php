<?php
$pageTitle = 'کنترل محتوا و کلمات ممنوعه';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';
$scanResult = null;

// Handle Add / Delete Forbidden Word
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } else {
        if ($action === 'add_word') {
            $word = trim($_POST['word'] ?? '');
            $actionType = $_POST['action_type'] ?? 'BLOCK';
            if (!empty($word)) {
                try {
                    $stmt = $db->prepare("INSERT INTO forbidden_words (word, action_type) VALUES (?, ?)");
                    $stmt->execute([$word, $actionType]);
                    $message = "واژه «" . htmlspecialchars($word) . "» به لیست سیاه محتوا اضافه گردید.";
                } catch (Exception $e) {
                    $message = 'این واژه قبلاً در لیست ثبت شده است.';
                    $messageType = 'error';
                }
            }
        } elseif ($action === 'delete_word') {
            $wordId = (int)($_POST['word_id'] ?? 0);
            $stmt = $db->prepare("DELETE FROM forbidden_words WHERE id = ?");
            $stmt->execute([$wordId]);
            $message = "واژه از لیست سیاه حذف شد.";
        } elseif ($action === 'test_scanner') {
            $testText = trim($_POST['test_text'] ?? '');
            $words = $db->query("SELECT word, action_type FROM forbidden_words")->fetchAll();
            $detected = [];
            foreach ($words as $w) {
                if (mb_stripos($testText, $w['word']) !== false) {
                    $detected[] = $w;
                }
            }
            $scanResult = [
                'text' => $testText,
                'detected' => $detected,
                'is_clean' => empty($detected)
            ];
        }
    }
}

// Fetch all forbidden words
$words = $db->query("SELECT * FROM forbidden_words ORDER BY id DESC")->fetchAll();
?>

<!-- Feedback Alert -->
<?php if (!empty($message)): ?>
    <div class="<?= $messageType === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400' : 'bg-rose-500/10 border-rose-500/30 text-rose-400' ?> border p-4 rounded-2xl text-sm flex items-center justify-between shadow-lg">
        <div class="flex items-center gap-3">
            <i class="fa-solid <?= $messageType === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation' ?> text-lg"></i>
            <span><?= htmlspecialchars($message) ?></span>
        </div>
    </div>
<?php endif; ?>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    
    <!-- Add New Forbidden Word Form -->
    <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl h-fit">
        <div class="flex items-center gap-3 mb-6 pb-4 border-b border-slate-800">
            <div class="w-10 h-10 rounded-2xl bg-rose-500/10 text-rose-400 flex items-center justify-center">
                <i class="fa-solid fa-ban"></i>
            </div>
            <div>
                <h3 class="font-bold text-white text-base">افزودن واژه ممنوعه جدید</h3>
                <p class="text-xs text-slate-400">جلوگیری خودکار از ثبت در متن آگهی‌ها</p>
            </div>
        </div>

        <form method="POST" action="content_control.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="add_word">

            <div>
                <label class="block text-slate-300 text-xs font-semibold mb-2">کلمه یا عبارت ممنوعه:</label>
                <input type="text" name="word" required placeholder="مثال: فروش قرص، قمار، سلاح..."
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-rose-400">
            </div>

            <div>
                <label class="block text-slate-300 text-xs font-semibold mb-2">نوع اقدام سیستم:</label>
                <select name="action_type" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-rose-400">
                    <option value="BLOCK">مسدودسازی خودکار (جلوگیری کامل از ثبت)</option>
                    <option value="FLAG_FOR_REVIEW">ارجاع به صف بازبینی ویژه مدیر</option>
                </select>
            </div>

            <button type="submit" class="w-full bg-rose-500 hover:bg-rose-600 text-white font-bold py-3.5 rounded-2xl text-sm transition flex items-center justify-center gap-2">
                <i class="fa-solid fa-plus"></i>
                <span>افزودن به لیست سیاه</span>
            </button>
        </form>
    </div>

    <!-- Forbidden Words List -->
    <div class="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <div class="flex items-center justify-between mb-6 pb-4 border-b border-slate-800">
            <div class="flex items-center gap-3">
                <i class="fa-solid fa-list-check text-rose-400"></i>
                <h3 class="font-bold text-white text-base">لیست کلمات و عبارات مسدود (<?= persianNumber(count($words)) ?> واژه)</h3>
            </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <?php if (empty($words)): ?>
                <div class="col-span-2 text-center py-8 text-slate-500 text-sm">لیست کلمات ممنوعه خالی است.</div>
            <?php else: ?>
                <?php foreach ($words as $w): ?>
                    <div class="bg-slate-950 border border-slate-800/80 rounded-2xl p-3.5 flex items-center justify-between hover:border-slate-700 transition">
                        <div>
                            <span class="font-bold text-white text-sm"><?= htmlspecialchars($w['word']) ?></span>
                            <span class="block text-[11px] <?= $w['action_type'] === 'BLOCK' ? 'text-rose-400' : 'text-amber-400' ?> mt-0.5">
                                <?= $w['action_type'] === 'BLOCK' ? '● مسدودسازی کامل' : '● ارجاع به صف بررسی' ?>
                            </span>
                        </div>

                        <form method="POST" onsubmit="return confirm('آیا از حذف این واژه اطمینان دارید؟');">
                            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                            <input type="hidden" name="action" value="delete_word">
                            <input type="hidden" name="word_id" value="<?= $w['id'] ?>">
                            <button type="submit" class="w-8 h-8 rounded-xl bg-slate-900 hover:bg-rose-500/20 text-slate-500 hover:text-rose-400 flex items-center justify-center transition">
                                <i class="fa-solid fa-trash text-xs"></i>
                            </button>
                        </form>
                    </div>
                <?php endforeach; ?>
            <?php endif; ?>
        </div>
    </div>

</div>

<!-- Interactive Scanner Sandbox Tester -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl mt-8">
    <div class="flex items-center gap-3 mb-6 pb-4 border-b border-slate-800">
        <i class="fa-solid fa-flask-vial text-teal-400"></i>
        <h3 class="font-bold text-white text-base">محیط تست هوشمند اسکنر محتوا</h3>
    </div>

    <form method="POST" action="content_control.php" class="space-y-4">
        <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
        <input type="hidden" name="action" value="test_scanner">

        <div>
            <label class="block text-slate-300 text-xs font-semibold mb-2">متن یا عنوان تستی برای بررسی سلامت محتوا:</label>
            <textarea name="test_text" rows="3" required
                      class="w-full bg-slate-950 border border-slate-700 rounded-2xl p-4 text-sm text-white focus:outline-none focus:border-teal-400"
                      placeholder="یک متن نمونه وارد کنید تا بررسی شود آیا کلمات ممنوعه در آن وجود دارد یا خیر..."></textarea>
        </div>

        <button type="submit" class="bg-teal-500 hover:bg-teal-600 text-white font-bold px-6 py-3 rounded-2xl text-sm transition flex items-center gap-2">
            <i class="fa-solid fa-magnifying-glass"></i>
            <span>تست اسکن متن</span>
        </button>
    </form>

    <?php if ($scanResult !== null): ?>
        <div class="mt-6 pt-6 border-t border-slate-800">
            <?php if ($scanResult['is_clean']): ?>
                <div class="bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 p-4 rounded-2xl text-sm flex items-center gap-3">
                    <i class="fa-solid fa-circle-check text-lg"></i>
                    <span>متن کاملاً پاک و مجاز است. هیچ کلمه ممنوعه‌ای شناسایی نشد.</span>
                </div>
            <?php else: ?>
                <div class="bg-rose-500/10 border border-rose-500/30 text-rose-400 p-4 rounded-2xl text-sm">
                    <div class="flex items-center gap-2 font-bold mb-2">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <span>کلمات ممنوعه شناسایی شدند:</span>
                    </div>
                    <div class="flex flex-wrap gap-2 mt-2">
                        <?php foreach ($scanResult['detected'] as $d): ?>
                            <span class="bg-rose-950 border border-rose-800 text-rose-300 px-3 py-1 rounded-xl text-xs font-bold">
                                <?= htmlspecialchars($d['word']) ?> (<?= $d['action_type'] ?>)
                            </span>
                        <?php endforeach; ?>
                    </div>
                </div>
            <?php endif; ?>
        </div>
    <?php endif; ?>
</div>

<?php require_once 'footer.php'; ?>
