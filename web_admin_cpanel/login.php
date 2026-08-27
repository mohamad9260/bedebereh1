<?php
require_once 'config.php';

$error = '';
$success = '';

if (!empty($_SESSION['admin_logged_in'])) {
    header('Location: index.php');
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = trim($_POST['username'] ?? '');
    $password = trim($_POST['password'] ?? '');
    $csrf = $_POST['csrf_token'] ?? '';

    if (!verifyCsrfToken($csrf)) {
        $error = 'اعتبارسنجی نشست منقضی شده است. مجدداً تلاش کنید.';
    } elseif (empty($username) || empty($password)) {
        $error = 'لطفاً نام کاربری و کلمه عبور را وارد نمایید.';
    } else {
        $db = getDB();
        $stmt = $db->prepare("SELECT * FROM admins WHERE username = ? LIMIT 1");
        $stmt->execute([$username]);
        $admin = $stmt->fetch();

        if ($admin && password_verify($password, $admin['password_hash'])) {
            $_SESSION['admin_logged_in'] = true;
            $_SESSION['admin_id'] = $admin['id'];
            $_SESSION['admin_username'] = $admin['username'];
            $_SESSION['admin_name'] = $admin['full_name'];
            $_SESSION['admin_role'] = $admin['role'];

            // Update last login
            $up = $db->prepare("UPDATE admins SET last_login = NOW() WHERE id = ?");
            $up->execute([$admin['id']]);

            header('Location: index.php');
            exit;
        } else {
            $error = 'نام کاربری یا رمز عبور اشتباه است.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ورود به پنل مدیریت سامانه بده بره</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css" rel="stylesheet" type="text/css" />
    <!-- FontAwesome Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body { font-family: 'Vazirmatn', sans-serif; }
    </style>
</head>
<body class="bg-slate-900 min-h-screen flex items-center justify-center p-4">

<div class="max-w-md w-full bg-slate-800/90 border border-slate-700 rounded-3xl p-8 shadow-2xl backdrop-blur-xl">
    
    <!-- Logo & Header -->
    <div class="text-center mb-8">
        <div class="w-20 h-20 bg-gradient-to-tr from-teal-500 to-emerald-400 rounded-3xl mx-auto flex items-center justify-center shadow-lg shadow-teal-500/30 mb-4">
            <i class="fa-solid fa-hand-holding-heart text-3xl text-white"></i>
        </div>
        <h1 class="text-2xl font-black text-white tracking-tight">پنل مدیریت «بده بره»</h1>
        <p class="text-slate-400 text-sm mt-1">مدیریت آگهی‌ها، کاربران، تراکنش‌ها و درگاه بانکی</p>
    </div>

    <?php if (!empty($error)): ?>
        <div class="bg-rose-500/10 border border-rose-500/30 text-rose-400 p-4 rounded-2xl mb-6 text-sm flex items-center gap-3">
            <i class="fa-solid fa-circle-exclamation text-lg"></i>
            <span><?= htmlspecialchars($error) ?></span>
        </div>
    <?php endif; ?>

    <form method="POST" action="login.php" class="space-y-5">
        <input type="hidden" name="csrf_token" value="<?= generateCsrfToken() ?>">

        <div>
            <label class="block text-slate-300 text-sm font-medium mb-2" for="username">نام کاربری مدیر</label>
            <div class="relative">
                <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none text-slate-500">
                    <i class="fa-solid fa-user-shield"></i>
                </div>
                <input type="text" id="username" name="username" required
                       placeholder="مثال: admin"
                       class="w-full bg-slate-900/80 border border-slate-700 rounded-2xl pr-11 pl-4 py-3.5 text-white placeholder-slate-500 focus:outline-none focus:border-teal-400 focus:ring-2 focus:ring-teal-400/20 transition duration-200">
            </div>
        </div>

        <div>
            <label class="block text-slate-300 text-sm font-medium mb-2" for="password">کلمه عبور</label>
            <div class="relative">
                <div class="absolute inset-y-0 right-0 pr-4 flex items-center pointer-events-none text-slate-500">
                    <i class="fa-solid fa-lock"></i>
                </div>
                <input type="password" id="password" name="password" required
                       placeholder="••••••••••••"
                       class="w-full bg-slate-900/80 border border-slate-700 rounded-2xl pr-11 pl-4 py-3.5 text-white placeholder-slate-500 focus:outline-none focus:border-teal-400 focus:ring-2 focus:ring-teal-400/20 transition duration-200">
            </div>
        </div>

        <button type="submit"
                class="w-full bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-white font-bold py-4 rounded-2xl shadow-lg shadow-teal-500/25 transition duration-200 flex items-center justify-center gap-2 mt-2">
            <i class="fa-solid fa-right-to-bracket"></i>
            <span>ورود امن به پنل مدیریت</span>
        </button>
    </form>

    <div class="mt-8 pt-6 border-t border-slate-700/60 text-center text-xs text-slate-500">
        اطلاعات پیش‌فرض دیتابیس: نام‌کاربری <code class="text-teal-400">admin</code> و رمزعبور <code class="text-teal-400">admin123456_ChangeMe!</code>
    </div>

</div>

</body>
</html>
