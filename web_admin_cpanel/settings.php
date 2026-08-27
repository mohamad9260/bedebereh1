<?php
$pageTitle = 'تنظیمات پلن‌ها، دسترسی زودهنگام و محدودیت‌های سامانه';
require_once 'header.php';

$db = getDB();
$message = '';
$messageType = 'success';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? '';
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $message = 'نشست شما منقضی شده است.';
        $messageType = 'error';
    } else {
        if ($action === 'save_settings') {
            $merchantId = trim($_POST['zarinpal_merchant_id'] ?? '');
            $sandbox = isset($_POST['zarinpal_sandbox']) ? '1' : '0';
            $silverPrice = trim($_POST['silver_plan_price'] ?? '49000');
            $goldPrice = trim($_POST['gold_plan_price'] ?? '99000');
            $diamondPrice = trim($_POST['diamond_plan_price'] ?? '149000');

            // Early access hours
            $goldEarlyHours = trim($_POST['gold_early_access_hours'] ?? '2');
            $silverEarlyHours = trim($_POST['silver_early_access_hours'] ?? '1');
            $diamondEarlyHours = trim($_POST['diamond_early_access_hours'] ?? '2');

            // Discount requirements
            $requireDiamondForDiscounts = isset($_POST['require_diamond_for_discounts']) ? '1' : '0';

            // Daily card reservation limits
            $freeDailyLimit = trim($_POST['free_daily_reserve_limit'] ?? '3');
            $silverDailyLimit = trim($_POST['silver_daily_reserve_limit'] ?? '8');
            $goldDailyLimit = trim($_POST['gold_daily_reserve_limit'] ?? '15');
            $diamondDailyLimit = trim($_POST['diamond_daily_reserve_limit'] ?? '25');

            setSetting('zarinpal_merchant_id', $merchantId, 'کد درگاه زرین‌پال');
            setSetting('zarinpal_sandbox', $sandbox, 'حالت آزمایشی زرین‌پال');
            setSetting('silver_plan_price', $silverPrice, 'قیمت اشتراک نقره‌ای به تومان');
            setSetting('gold_plan_price', $goldPrice, 'قیمت اشتراک طلایی به تومان');
            setSetting('diamond_plan_price', $diamondPrice, 'قیمت اشتراک الماس به تومان');

            setSetting('gold_early_access_hours', $goldEarlyHours, 'مدت زمان دسترسی زودهنگام پکیج طلایی (ساعت)');
            setSetting('silver_early_access_hours', $silverEarlyHours, 'مدت زمان دسترسی زودهنگام پکیج نقره‌ای (ساعت)');
            setSetting('diamond_early_access_hours', $diamondEarlyHours, 'مدت زمان دسترسی زودهنگام پکیج الماس (ساعت)');

            setSetting('require_diamond_for_discounts', $requireDiamondForDiscounts, 'الزام خرید پکیج الماس برای ثبت کوپن و تخفیف');

            setSetting('free_daily_reserve_limit', $freeDailyLimit, 'سقف مجاز رزرو روزانه برای کاربران عادی');
            setSetting('silver_daily_reserve_limit', $silverDailyLimit, 'سقف مجاز رزرو روزانه برای کاربران نقره‌ای');
            setSetting('gold_daily_reserve_limit', $goldDailyLimit, 'سقف مجاز رزرو روزانه برای کاربران طلایی');
            setSetting('diamond_daily_reserve_limit', $diamondDailyLimit, 'سقف مجاز رزرو روزانه برای کاربران الماس');

            $message = 'تنظیمات پلن‌ها، زمان دسترسی زودهنگام و محدودیت‌های رزرو روزانه با موفقیت ذخیره گردید.';
        } elseif ($action === 'change_password') {
            $oldPass = trim($_POST['old_password'] ?? '');
            $newPass = trim($_POST['new_password'] ?? '');

            $stmt = $db->prepare("SELECT * FROM admins WHERE id = ?");
            $stmt->execute([$_SESSION['admin_id']]);
            $admin = $stmt->fetch();

            if ($admin && password_verify($oldPass, $admin['password_hash'])) {
                if (strlen($newPass) >= 6) {
                    $newHash = password_hash($newPass, PASSWORD_BCRYPT);
                    $up = $db->prepare("UPDATE admins SET password_hash = ? WHERE id = ?");
                    $up->execute([$newHash, $_SESSION['admin_id']]);
                    $message = 'رمز عبور مدیر با موفقیت تغییر یافت.';
                } else {
                    $message = 'رمز عبور جدید باید حداقل ۶ کاراکتر باشد.';
                    $messageType = 'error';
                }
            } else {
                $message = 'رمز عبور فعلی اشتباه است.';
                $messageType = 'error';
            }
        }
    }
}

// Current Values
$merchantId = getSetting('zarinpal_merchant_id', '00000000-0000-0000-0000-000000000000');
$sandbox = getSetting('zarinpal_sandbox', '1');
$silverPrice = getSetting('silver_plan_price', '49000');
$goldPrice = getSetting('gold_plan_price', '99000');
$diamondPrice = getSetting('diamond_plan_price', '149000');

$goldEarlyHours = getSetting('gold_early_access_hours', '2');
$silverEarlyHours = getSetting('silver_early_access_hours', '1');
$diamondEarlyHours = getSetting('diamond_early_access_hours', '2');

$requireDiamondForDiscounts = getSetting('require_diamond_for_discounts', '1');

$freeDailyLimit = getSetting('free_daily_reserve_limit', '3');
$silverDailyLimit = getSetting('silver_daily_reserve_limit', '8');
$goldDailyLimit = getSetting('gold_daily_reserve_limit', '15');
$diamondDailyLimit = getSetting('diamond_daily_reserve_limit', '25');
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

<form method="POST" action="settings.php" class="space-y-8">
    <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
    <input type="hidden" name="action" value="save_settings">

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        <!-- 1. Early Access Window Settings -->
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <div class="flex items-center gap-3 pb-4 border-b border-slate-800">
                <div class="w-10 h-10 rounded-2xl bg-amber-500/10 text-amber-400 flex items-center justify-center">
                    <i class="fa-solid fa-clock-rotate-left text-lg"></i>
                </div>
                <div>
                    <h3 class="font-bold text-white text-base">زمان‌بندی دسترسی زودهنگام به آگهی‌ها (VIP)</h3>
                    <p class="text-xs text-slate-400">تنظیم مدت زمانی که مشترکین ویژه زودتر از بقیه آگهی‌ها را می‌بینند</p>
                </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="bg-slate-950 border border-amber-500/30 rounded-2xl p-4">
                    <label class="block text-amber-400 text-xs font-bold mb-1.5 flex items-center gap-1.5">
                        <i class="fa-solid fa-crown text-xs"></i>
                        دسترسی زودهنگام پلن طلایی (ساعت):
                    </label>
                    <input type="number" name="gold_early_access_hours" value="<?= htmlspecialchars($goldEarlyHours) ?>" min="0" max="72" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-amber-400">
                    <span class="text-[11px] text-slate-400 mt-1 block">پیش‌فرض: ۲ ساعت قبل از کاربران عادی</span>
                </div>

                <div class="bg-slate-950 border border-slate-700 rounded-2xl p-4">
                    <label class="block text-slate-300 text-xs font-bold mb-1.5 flex items-center gap-1.5">
                        <i class="fa-solid fa-medal text-xs text-slate-400"></i>
                        دسترسی زودهنگام پلن نقره‌ای (ساعت):
                    </label>
                    <input type="number" name="silver_early_access_hours" value="<?= htmlspecialchars($silverEarlyHours) ?>" min="0" max="72" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-teal-400">
                    <span class="text-[11px] text-slate-400 mt-1 block">پیش‌فرض: ۱ ساعت قبل از کاربران عادی</span>
                </div>
            </div>

            <div class="bg-slate-950 border border-cyan-500/30 rounded-2xl p-4">
                <label class="block text-cyan-400 text-xs font-bold mb-1.5 flex items-center gap-1.5">
                    <i class="fa-solid fa-gem text-xs"></i>
                    دسترسی زودهنگام پلن الماس (ساعت):
                </label>
                <input type="number" name="diamond_early_access_hours" value="<?= htmlspecialchars($diamondEarlyHours) ?>" min="0" max="72" required
                       class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-cyan-400">
                <span class="text-[11px] text-slate-400 mt-1 block">پیش‌فرض: ۲ ساعت (همزمان با طلایی با اولویت ویژه)</span>
            </div>
        </div>

        <!-- 2. Diamond Plan & Discounts Control -->
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <div class="flex items-center gap-3 pb-4 border-b border-slate-800">
                <div class="w-10 h-10 rounded-2xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
                    <i class="fa-solid fa-gem text-lg"></i>
                </div>
                <div>
                    <h3 class="font-bold text-white text-base">پکیج الماس و مدیریت کوپن‌های تخفیف</h3>
                    <p class="text-xs text-slate-400">تنظیم شرط خرید پکیج الماس برای ارسال کدهای تخفیف و بن‌ها</p>
                </div>
            </div>

            <!-- Toggle Require Diamond for Discounts -->
            <div class="bg-slate-950 border border-cyan-500/20 rounded-2xl p-4 flex items-center justify-between">
                <div class="space-y-1">
                    <span class="text-sm font-bold text-white block">الزام پکیج الماس برای ثبت کوپن و تخفیف</span>
                    <span class="text-xs text-slate-400 block max-w-sm">در صورت فعال بودن، کاربران عادی برای ثبت کوپن‌های تخفیف و آگهی‌های فروشگاهی باید حتماً پکیج الماس را خریداری کنند.</span>
                </div>
                <label class="relative inline-flex items-center cursor-pointer ml-3">
                    <input type="checkbox" name="require_diamond_for_discounts" value="1" class="sr-only peer" <?= $requireDiamondForDiscounts == '1' ? 'checked' : '' ?>>
                    <div class="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:right-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-cyan-500"></div>
                </label>
            </div>

            <div>
                <label class="block text-slate-300 text-xs font-semibold mb-2">قیمت اشتراک الماس (VIP تجاری) - تومان:</label>
                <input type="number" name="diamond_plan_price" value="<?= htmlspecialchars($diamondPrice) ?>" required
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm font-mono text-white focus:outline-none focus:border-cyan-400">
                <span class="text-[11px] text-slate-500 mt-1 block">پیش‌فرض: ۱۴۹,۰۰۰ تومان به ازای هر ماه</span>
            </div>
        </div>

        <!-- 3. Daily Card Reservation Limits -->
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <div class="flex items-center gap-3 pb-4 border-b border-slate-800">
                <div class="w-10 h-10 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
                    <i class="fa-solid fa-bookmark text-lg"></i>
                </div>
                <div>
                    <h3 class="font-bold text-white text-base">سقف مجاز رزرو روزانه کارت‌ها (Daily Limits)</h3>
                    <p class="text-xs text-slate-400">تنظیم حداکثر تعداد آگهی و هدیه‌ای که هر کاربر می‌تواند در روز رزرو کند</p>
                </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="bg-slate-950 border border-slate-800 rounded-2xl p-4">
                    <label class="block text-slate-300 text-xs font-bold mb-1.5">
                        سقف پکیج عادی / رایگان (کارت/روز):
                    </label>
                    <input type="number" name="free_daily_reserve_limit" value="<?= htmlspecialchars($freeDailyLimit) ?>" min="1" max="100" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-teal-400">
                    <span class="text-[11px] text-slate-500 mt-1 block">پیش‌فرض: ۳ کارت در روز</span>
                </div>

                <div class="bg-slate-950 border border-slate-800 rounded-2xl p-4">
                    <label class="block text-slate-300 text-xs font-bold mb-1.5">
                        سقف پکیج نقره‌ای (کارت/روز):
                    </label>
                    <input type="number" name="silver_daily_reserve_limit" value="<?= htmlspecialchars($silverDailyLimit) ?>" min="1" max="200" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-teal-400">
                    <span class="text-[11px] text-slate-500 mt-1 block">پیش‌فرض: ۸ کارت در روز</span>
                </div>

                <div class="bg-slate-950 border border-amber-500/20 rounded-2xl p-4">
                    <label class="block text-amber-400 text-xs font-bold mb-1.5">
                        سقف پکیج طلایی (کارت/روز):
                    </label>
                    <input type="number" name="gold_daily_reserve_limit" value="<?= htmlspecialchars($goldDailyLimit) ?>" min="1" max="500" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-amber-400">
                    <span class="text-[11px] text-slate-500 mt-1 block">پیش‌فرض: ۱۵ کارت در روز</span>
                </div>

                <div class="bg-slate-950 border border-cyan-500/20 rounded-2xl p-4">
                    <label class="block text-cyan-400 text-xs font-bold mb-1.5">
                        سقف پکیج الماس (کارت/روز):
                    </label>
                    <input type="number" name="diamond_daily_reserve_limit" value="<?= htmlspecialchars($diamondDailyLimit) ?>" min="1" max="1000" required
                           class="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2.5 text-sm font-mono text-white focus:outline-none focus:border-cyan-400">
                    <span class="text-[11px] text-slate-500 mt-1 block">پیش‌فرض: ۲۵ کارت در روز</span>
                </div>
            </div>
        </div>

        <!-- 4. Payment Gateway & Other Plan Prices -->
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <div class="flex items-center gap-3 pb-4 border-b border-slate-800">
                <div class="w-10 h-10 rounded-2xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center">
                    <i class="fa-solid fa-credit-card text-lg"></i>
                </div>
                <div>
                    <h3 class="font-bold text-white text-base">درگاه زرین‌پال و تعرفه‌های دیگر</h3>
                    <p class="text-xs text-slate-400">تنظیمات پرداخت آنلاین و قیمت سایر اشتراک‌ها</p>
                </div>
            </div>

            <div>
                <label class="block text-slate-300 text-xs font-semibold mb-2">کد درگاه زرین‌پال (Merchant ID):</label>
                <input type="text" name="zarinpal_merchant_id" value="<?= htmlspecialchars($merchantId) ?>" required
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm font-mono text-white focus:outline-none focus:border-teal-400 text-left" dir="ltr"
                       placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx">
            </div>

            <div class="bg-slate-950 border border-slate-800 rounded-2xl p-4 flex items-center justify-between">
                <div>
                    <span class="text-sm font-bold text-white block">حالت آزمایشی زرین‌پال (Sandbox)</span>
                    <span class="text-xs text-slate-400">جهت تست درگاه بدون تراکنش واقعی</span>
                </div>
                <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" name="zarinpal_sandbox" value="1" class="sr-only peer" <?= $sandbox == '1' ? 'checked' : '' ?>>
                    <div class="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:right-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-teal-500"></div>
                </label>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label class="block text-slate-300 text-xs font-semibold mb-2">قیمت اشتراک نقره‌ای (تومان):</label>
                    <input type="number" name="silver_plan_price" value="<?= htmlspecialchars($silverPrice) ?>" required
                           class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm font-mono text-white focus:outline-none focus:border-teal-400">
                </div>

                <div>
                    <label class="block text-slate-300 text-xs font-semibold mb-2">قیمت اشتراک طلایی (تومان):</label>
                    <input type="number" name="gold_plan_price" value="<?= htmlspecialchars($goldPrice) ?>" required
                           class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm font-mono text-white focus:outline-none focus:border-teal-400">
                </div>
            </div>
        </div>

    </div>

    <div class="flex justify-end">
        <button type="submit" class="bg-gradient-to-r from-teal-500 to-emerald-600 hover:from-teal-600 hover:to-emerald-700 text-white font-bold px-8 py-4 rounded-2xl text-sm transition shadow-lg shadow-teal-500/20 flex items-center gap-2">
            <i class="fa-solid fa-floppy-disk text-base"></i>
            ذخیره تمامی تنظیمات سامانه و پکیج‌ها
        </button>
    </div>
</form>

<!-- Admin Password Change Section -->
<div class="mt-8 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl max-w-xl">
    <div class="flex items-center gap-3 mb-6 pb-4 border-b border-slate-800">
        <div class="w-10 h-10 rounded-2xl bg-teal-500/10 text-teal-400 flex items-center justify-center">
            <i class="fa-solid fa-lock"></i>
        </div>
        <div>
            <h3 class="font-bold text-white text-base">تغییر کلمه عبور مدیر</h3>
            <p class="text-xs text-slate-400">تغییر رمز ورود امن به پنل مدیریت وب</p>
        </div>
    </div>

    <form method="POST" action="settings.php" class="space-y-4">
        <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
        <input type="hidden" name="action" value="change_password">

        <div>
            <label class="block text-slate-300 text-xs font-semibold mb-2">کلمه عبور فعلی:</label>
            <input type="password" name="old_password" required
                   class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
        </div>

        <div>
            <label class="block text-slate-300 text-xs font-semibold mb-2">کلمه عبور جدید:</label>
            <input type="password" name="new_password" required minlength="6"
                   class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
        </div>

        <button type="submit" class="w-full bg-slate-800 hover:bg-slate-700 text-white font-bold py-3.5 rounded-2xl text-sm transition">
            تغییر رمز عبور
        </button>
    </form>
</div>

<?php require_once 'footer.php'; ?>
