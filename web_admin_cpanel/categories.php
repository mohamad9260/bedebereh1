<?php
$pageTitle = 'مدیریت دسته‌بندی‌ها (والد و فرزند) و قفل بخش‌ها';
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
        if ($action === 'toggle_lock') {
            $catId = $_POST['category_id'] ?? '';
            $currentLock = (int)($_POST['current_lock'] ?? 0);
            $newLock = $currentLock === 1 ? 0 : 1;
            $lockMsg = trim($_POST['lock_message'] ?? 'ثبت آگهی در این بخش موقتاً غیرفعال است.');

            $stmt = $db->prepare("UPDATE categories SET is_locked = ?, lock_message = ? WHERE id = ?");
            $stmt->execute([$newLock, $lockMsg, $catId]);
            $message = $newLock === 1 ? "بخش $catId با موفقیت قفل شد." : "قفل بخش $catId باز شد.";
        } elseif ($action === 'add_category') {
            $id = trim($_POST['id'] ?? '');
            $parentId = !empty($_POST['parent_id']) ? trim($_POST['parent_id']) : null;
            $nameFa = trim($_POST['name_fa'] ?? '');
            $iconName = trim($_POST['icon_name'] ?? 'card_giftcard');
            $type = $_POST['type'] ?? 'FREE_GIFT';
            $displayOrder = (int)($_POST['display_order'] ?? 0);

            if (empty($id) || empty($nameFa)) {
                $message = 'شناسه و نام دسته‌بندی الزامی است.';
                $messageType = 'error';
            } else {
                try {
                    $stmt = $db->prepare("INSERT INTO categories (id, parent_id, name_fa, icon_name, type, display_order) VALUES (?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$id, $parentId, $nameFa, $iconName, $type, $displayOrder]);
                    $message = "دسته‌بندی «{$nameFa}» با موفقیت افزوده شد.";
                } catch (Exception $e) {
                    $message = 'خطا در ثبت: شناسه ممکن است تکراری باشد. ' . $e->getMessage();
                    $messageType = 'error';
                }
            }
        } elseif ($action === 'edit_category') {
            $id = trim($_POST['id'] ?? '');
            $parentId = !empty($_POST['parent_id']) ? trim($_POST['parent_id']) : null;
            $nameFa = trim($_POST['name_fa'] ?? '');
            $iconName = trim($_POST['icon_name'] ?? 'card_giftcard');
            $type = $_POST['type'] ?? 'FREE_GIFT';
            $displayOrder = (int)($_POST['display_order'] ?? 0);

            if (empty($id) || empty($nameFa)) {
                $message = 'شناسه و نام دسته‌بندی الزامی است.';
                $messageType = 'error';
            } else {
                try {
                    $stmt = $db->prepare("UPDATE categories SET parent_id = ?, name_fa = ?, icon_name = ?, type = ?, display_order = ? WHERE id = ?");
                    $stmt->execute([$parentId, $nameFa, $iconName, $type, $displayOrder, $id]);
                    $message = "دسته‌بندی «{$nameFa}» با موفقیت ویرایش شد.";
                } catch (Exception $e) {
                    $message = 'خطا در ویرایش: ' . $e->getMessage();
                    $messageType = 'error';
                }
            }
        } elseif ($action === 'delete_category') {
            $id = trim($_POST['id'] ?? '');
            try {
                $stmt = $db->prepare("DELETE FROM categories WHERE id = ?");
                $stmt->execute([$id]);
                $message = "دسته‌بندی با موفقیت حذف شد.";
            } catch (Exception $e) {
                $message = 'امکان حذف این دسته‌بندی وجود ندارد (ممکن است آگهی یا زیردسته‌های وابسته داشته باشد).';
                $messageType = 'error';
            }
        }
    }
}

// Fetch categories with parent names and listing counts
$stmt = $db->query("SELECT c.*, p.name_fa as parent_name_fa,
                    (SELECT COUNT(*) FROM listings WHERE category_id = c.id) as listings_count,
                    (SELECT COUNT(*) FROM listings WHERE category_id = c.id AND status = 'APPROVED') as approved_count,
                    (SELECT COUNT(*) FROM categories WHERE parent_id = c.id) as subcategories_count
                    FROM categories c 
                    LEFT JOIN categories p ON c.parent_id = p.id
                    ORDER BY c.display_order ASC, c.id ASC");
$allCategories = $stmt->fetchAll();

// Group into Parents and Children
$parentCategories = array_filter($allCategories, fn($c) => empty($c['parent_id']));
$childCategoriesMap = [];
foreach ($allCategories as $c) {
    if (!empty($c['parent_id'])) {
        $childCategoriesMap[$c['parent_id']][] = $c;
    }
}
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

<!-- Header Actions & Summary -->
<div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl flex flex-col md:flex-row items-center justify-between gap-4">
    <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-teal-500/10 text-teal-400 flex items-center justify-center text-xl">
            <i class="fa-solid fa-sitemap"></i>
        </div>
        <div>
            <h3 class="font-bold text-white text-base">مدیریت ساختار والد و فرزند دسته‌بندی‌ها</h3>
            <p class="text-xs text-slate-400 mt-1">امکان تعریف دسته‌های اصلی (کتاب، لوازم منزل، وسایل شخصی، ابزار، دیجیتال) و زیردسته‌های تخصصی با قابلیت قفل اختصاصی</p>
        </div>
    </div>
    <button onclick="openAddModal()" class="bg-teal-500 hover:bg-teal-600 text-white text-sm font-bold px-5 py-3 rounded-2xl transition flex items-center gap-2 shadow-lg shadow-teal-500/20">
        <i class="fa-solid fa-plus"></i>
        <span>افزودن دسته‌بندی جدید</span>
    </button>
</div>

<!-- Hierarchical Category Cards -->
<div class="space-y-6">
    <?php foreach ($parentCategories as $parent): ?>
        <?php 
            $children = $childCategoriesMap[$parent['id']] ?? [];
        ?>
        <div class="bg-slate-900 border <?= $parent['is_locked'] == 1 ? 'border-rose-500/40 bg-rose-500/[0.02]' : 'border-slate-800' ?> rounded-3xl p-6 shadow-xl">
            <!-- Parent Category Header -->
            <div class="flex flex-col md:flex-row items-start md:items-center justify-between pb-4 border-b border-slate-800 gap-4">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-teal-500/10 text-teal-400 flex items-center justify-center text-lg">
                        <i class="fa-solid fa-folder-tree"></i>
                    </div>
                    <div>
                        <div class="flex items-center gap-2">
                            <h4 class="text-base font-bold text-white"><?= htmlspecialchars($parent['name_fa']) ?></h4>
                            <span class="bg-teal-500/10 text-teal-400 border border-teal-500/30 text-[10px] px-2 py-0.5 rounded-full font-bold">دسته والد</span>
                            <?php if ($parent['is_locked'] == 1): ?>
                                <span class="bg-rose-500/10 text-rose-400 border border-rose-500/30 text-[10px] px-2 py-0.5 rounded-full font-bold">قفل شده</span>
                            <?php endif; ?>
                        </div>
                        <div class="text-xs text-slate-500 font-mono mt-0.5">شناسه: <?= htmlspecialchars($parent['id']) ?> | بخش: <?= $parent['type'] ?> | ترتیب: <?= $parent['display_order'] ?></div>
                    </div>
                </div>

                <div class="flex items-center gap-2">
                    <button onclick='openAddModal("<?= $parent['id'] ?>", "<?= $parent['type'] ?>")' class="bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs px-3 py-2 rounded-xl transition flex items-center gap-1.5">
                        <i class="fa-solid fa-plus text-[10px]"></i>
                        <span>افزودن زیردسته</span>
                    </button>
                    <button onclick='openEditModal(<?= json_encode($parent) ?>)' class="bg-slate-800 hover:bg-slate-700 text-amber-400 text-xs px-3 py-2 rounded-xl transition flex items-center gap-1.5">
                        <i class="fa-solid fa-pen text-[10px]"></i>
                        <span>ویرایش</span>
                    </button>
                    <?php if ($parent['is_locked'] == 1): ?>
                        <form method="POST" class="inline">
                            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                            <input type="hidden" name="action" value="toggle_lock">
                            <input type="hidden" name="category_id" value="<?= $parent['id'] ?>">
                            <input type="hidden" name="current_lock" value="1">
                            <button type="submit" class="bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-xs px-3 py-2 rounded-xl transition flex items-center gap-1">
                                <i class="fa-solid fa-lock-open text-[10px]"></i>
                                <span>باز کردن قفل</span>
                            </button>
                        </form>
                    <?php else: ?>
                        <button onclick="openLockModal('<?= $parent['id'] ?>', '<?= htmlspecialchars($parent['name_fa'], ENT_QUOTES) ?>')" class="bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 text-xs px-3 py-2 rounded-xl transition flex items-center gap-1">
                            <i class="fa-solid fa-lock text-[10px]"></i>
                            <span>قفل کردن</span>
                        </button>
                    <?php endif; ?>
                </div>
            </div>

            <!-- Subcategories Grid -->
            <?php if (!empty($children)): ?>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 mt-4 pt-2">
                    <?php foreach ($children as $child): ?>
                        <div class="bg-slate-950 border <?= $child['is_locked'] == 1 ? 'border-rose-500/30' : 'border-slate-800' ?> rounded-2xl p-4 flex flex-col justify-between">
                            <div>
                                <div class="flex items-center justify-between mb-2">
                                    <span class="text-[11px] font-bold text-slate-200"><?= htmlspecialchars($child['name_fa']) ?></span>
                                    <?php if ($child['is_locked'] == 1): ?>
                                        <span class="text-rose-400 text-[10px]"><i class="fa-solid fa-lock"></i> قفل</span>
                                    <?php endif; ?>
                                </div>
                                <div class="text-[10px] text-slate-500 font-mono mb-2">شناسه: <?= htmlspecialchars($child['id']) ?> | آیکون: <?= htmlspecialchars($child['icon_name']) ?></div>
                                <div class="text-[11px] text-slate-400">آگهی‌ها: <b class="text-teal-400"><?= persianNumber($child['listings_count']) ?></b></div>
                            </div>
                            <div class="flex items-center justify-end gap-1.5 pt-3 border-t border-slate-900 mt-3">
                                <button onclick='openEditModal(<?= json_encode($child) ?>)' class="text-amber-400 hover:text-amber-300 text-xs p-1.5 rounded">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button onclick="openLockModal('<?= $child['id'] ?>', '<?= htmlspecialchars($child['name_fa'], ENT_QUOTES) ?>')" class="text-rose-400 hover:text-rose-300 text-xs p-1.5 rounded">
                                    <i class="fa-solid fa-lock"></i>
                                </button>
                                <form method="POST" onsubmit="return confirm('آیا از حذف این زیردسته اطمینان دارید؟')" class="inline">
                                    <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
                                    <input type="hidden" name="action" value="delete_category">
                                    <input type="hidden" name="id" value="<?= $child['id'] ?>">
                                    <button type="submit" class="text-slate-500 hover:text-rose-400 text-xs p-1.5 rounded">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>
                                </form>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            <?php else: ?>
                <div class="text-xs text-slate-500 py-3 text-center">زیردسته‌ای برای این دسته تعریف نشده است.</div>
            <?php endif; ?>
        </div>
    <?php endforeach; ?>
</div>

<!-- Modal: Add Category -->
<div id="addCatModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <h3 class="font-bold text-white text-base">افزودن دسته‌بندی / زیردسته</h3>
            <button onclick="closeAddModal()" class="text-slate-400 hover:text-white">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <form method="POST" action="categories.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="add_category">

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">دسته‌بندی والد (اختیاری برای زیردسته‌ها):</label>
                <select name="parent_id" id="addParentId" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                    <option value="">-- بدون والد (دسته‌بندی اصلی) --</option>
                    <?php foreach ($parentCategories as $p): ?>
                        <option value="<?= $p['id'] ?>"><?= htmlspecialchars($p['name_fa']) ?> (<?= $p['id'] ?>)</option>
                    <?php endforeach; ?>
                </select>
            </div>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">شناسه یکتا (انگلیسی):</label>
                <input type="text" name="id" placeholder="مثال: cat_tools_garden" required
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white font-mono focus:outline-none focus:border-teal-400">
            </div>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">نام فارسی دسته‌بندی:</label>
                <input type="text" name="name_fa" placeholder="مثال: ابزار باغبانی و کشاورزی" required
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
            </div>

            <div class="grid grid-cols-2 gap-3">
                <div>
                    <label class="block text-slate-400 text-xs font-medium mb-1.5">نوع بخش:</label>
                    <select name="type" id="addType" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-3 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                        <option value="FREE_GIFT">هدیه رایگان</option>
                        <option value="DISCOUNT">تخفیف</option>
                        <option value="REQUEST">درخواست</option>
                    </select>
                </div>
                <div>
                    <label class="block text-slate-400 text-xs font-medium mb-1.5">نام آیکون Material:</label>
                    <input type="text" name="icon_name" value="card_giftcard"
                           class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-3 py-3 text-sm text-white font-mono focus:outline-none focus:border-teal-400">
                </div>
            </div>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">ترتیب نمایش:</label>
                <input type="number" name="display_order" value="10"
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
            </div>

            <div class="flex items-center gap-3 pt-2">
                <button type="submit" class="flex-1 bg-teal-500 hover:bg-teal-600 text-white font-bold py-3 rounded-2xl text-sm transition">
                    ثبت دسته‌بندی
                </button>
                <button type="button" onclick="closeAddModal()" class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-2xl text-sm transition">
                    انصراف
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Edit Category -->
<div id="editCatModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <h3 class="font-bold text-white text-base">ویرایش دسته‌بندی</h3>
            <button onclick="closeEditModal()" class="text-slate-400 hover:text-white">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <form method="POST" action="categories.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="edit_category">
            <input type="hidden" id="editId" name="id" value="">

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">دسته‌بندی والد:</label>
                <select name="parent_id" id="editParentId" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                    <option value="">-- بدون والد (دسته‌بندی اصلی) --</option>
                    <?php foreach ($parentCategories as $p): ?>
                        <option value="<?= $p['id'] ?>"><?= htmlspecialchars($p['name_fa']) ?> (<?= $p['id'] ?>)</option>
                    <?php endforeach; ?>
                </select>
            </div>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">نام فارسی دسته‌بندی:</label>
                <input type="text" name="name_fa" id="editNameFa" required
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
            </div>

            <div class="grid grid-cols-2 gap-3">
                <div>
                    <label class="block text-slate-400 text-xs font-medium mb-1.5">نوع بخش:</label>
                    <select name="type" id="editType" class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-3 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
                        <option value="FREE_GIFT">هدیه رایگان</option>
                        <option value="DISCOUNT">تخفیف</option>
                        <option value="REQUEST">درخواست</option>
                    </select>
                </div>
                <div>
                    <label class="block text-slate-400 text-xs font-medium mb-1.5">نام آیکون:</label>
                    <input type="text" name="icon_name" id="editIconName"
                           class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-3 py-3 text-sm text-white font-mono focus:outline-none focus:border-teal-400">
                </div>
            </div>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">ترتیب نمایش:</label>
                <input type="number" name="display_order" id="editDisplayOrder"
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-teal-400">
            </div>

            <div class="flex items-center gap-3 pt-2">
                <button type="submit" class="flex-1 bg-amber-500 hover:bg-amber-600 text-white font-bold py-3 rounded-2xl text-sm transition">
                    ذخیره تغییرات
                </button>
                <button type="button" onclick="closeEditModal()" class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-2xl text-sm transition">
                    انصراف
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Category Lock -->
<div id="lockCatModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 hidden flex items-center justify-center p-4">
    <div class="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full p-6 shadow-2xl">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <h3 class="font-bold text-white text-base">قفل‌گذاری بخش</h3>
            <button onclick="closeLockModal()" class="text-slate-400 hover:text-white">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <form method="POST" action="categories.php" class="space-y-4">
            <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">
            <input type="hidden" name="action" value="toggle_lock">
            <input type="hidden" id="lockCategoryId" name="category_id" value="">
            <input type="hidden" name="current_lock" value="0">

            <p class="text-slate-300 text-sm">
                قفل کردن ثبت آگهی در بخش <b id="lockCategoryName" class="text-white"></b>
            </p>

            <div>
                <label class="block text-slate-400 text-xs font-medium mb-1.5">پیام نمایش داده شده به کاربران:</label>
                <input type="text" name="lock_message" value="ثبت آگهی در این بخش به دلیل تکمیل ظرفیت موقتاً متوقف شده است."
                       class="w-full bg-slate-950 border border-slate-700 rounded-2xl px-4 py-3 text-sm text-white focus:outline-none focus:border-rose-400">
            </div>

            <div class="flex items-center gap-3">
                <button type="submit" class="flex-1 bg-rose-500 hover:bg-rose-600 text-white font-bold py-3 rounded-2xl text-sm transition">
                    اعمال قفل بخش
                </button>
                <button type="button" onclick="closeLockModal()" class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-2xl text-sm transition">
                    انصراف
                </button>
            </div>
        </form>
    </div>
</div>

<script>
function openAddModal(parentId = '', type = 'FREE_GIFT') {
    document.getElementById('addParentId').value = parentId;
    document.getElementById('addType').value = type;
    document.getElementById('addCatModal').classList.remove('hidden');
}
function closeAddModal() {
    document.getElementById('addCatModal').classList.add('hidden');
}

function openEditModal(cat) {
    document.getElementById('editId').value = cat.id;
    document.getElementById('editParentId').value = cat.parent_id || '';
    document.getElementById('editNameFa').value = cat.name_fa;
    document.getElementById('editType').value = cat.type;
    document.getElementById('editIconName').value = cat.icon_name;
    document.getElementById('editDisplayOrder').value = cat.display_order;
    document.getElementById('editCatModal').classList.remove('hidden');
}
function closeEditModal() {
    document.getElementById('editCatModal').classList.add('hidden');
}

function openLockModal(catId, catName) {
    document.getElementById('lockCategoryId').value = catId;
    document.getElementById('lockCategoryName').textContent = catName;
    document.getElementById('lockCatModal').classList.remove('hidden');
}
function closeLockModal() {
    document.getElementById('lockCatModal').classList.add('hidden');
}
</script>

<?php require_once 'footer.php'; ?>
