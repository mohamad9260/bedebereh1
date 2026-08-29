<?php
require_once 'config.php';
requireAdminLogin();

$currentPage = basename($_SERVER['PHP_SELF']);
$adminName = $_SESSION['admin_name'] ?? 'مدیر سامانه';
$adminRole = $_SESSION['admin_role'] ?? 'SUPER_ADMIN';

// Quick stats for badges
$db = getDB();
$pendingListingsCount = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'PENDING'")->fetchColumn();
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= $pageTitle ?? 'پنل مدیریت بده بره' ?></title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            50: '#f0fdfa',
                            100: '#ccfbf1',
                            500: '#0d9488',
                            600: '#0f766e',
                            700: '#115e59',
                            900: '#134e4a',
                        },
                        amberCustom: '#f59e0b',
                        coralCustom: '#f43f5e'
                    }
                }
            }
        }
    </script>
    <!-- Vazirmatn Font CDN -->
    <link href="https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css" rel="stylesheet" type="text/css" />
    <!-- FontAwesome 6 Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Chart.js CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Vazirmatn', sans-serif; }
    </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen flex">

    <!-- Sidebar Navigation -->
    <aside class="w-72 bg-slate-900 border-l border-slate-800 flex flex-col shrink-0 min-h-screen sticky top-0">
        
        <!-- Brand Header -->
        <div class="p-6 border-b border-slate-800 flex items-center gap-3">
            <div class="w-12 h-12 bg-gradient-to-tr from-teal-500 to-emerald-400 rounded-2xl flex items-center justify-center shadow-lg shadow-teal-500/20">
                <i class="fa-solid fa-hand-holding-heart text-2xl text-white"></i>
            </div>
            <div>
                <h2 class="font-black text-lg text-white">بـده بـره</h2>
                <span class="text-xs text-teal-400 font-semibold bg-teal-950 px-2 py-0.5 rounded-md border border-teal-800">پنل مدیریت تحت وب</span>
            </div>
        </div>

        <!-- Navigation Links -->
        <nav class="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
            
            <!-- Dashboard -->
            <a href="dashboard.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'dashboard.php' || $currentPage === 'admin.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-chart-pie w-5 text-center text-base"></i>
                    <span>داشبورد و آمار کل</span>
                </div>
            </a>

            <!-- Listings Moderation -->
            <a href="listings.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'listings.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-layer-group w-5 text-center text-base"></i>
                    <span>مدیریت و تایید آگهی‌ها</span>
                </div>
                <?php if ($pendingListingsCount > 0): ?>
                    <span class="bg-rose-500 text-white text-xs font-bold px-2 py-0.5 rounded-full animate-pulse"><?= persianNumber($pendingListingsCount) ?></span>
                <?php endif; ?>
            </a>

            <!-- Users Management & Banning -->
            <a href="users.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'users.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-users w-5 text-center text-base"></i>
                    <span>کاربران و مسدودسازی</span>
                </div>
            </a>

            <!-- Forbidden Content Blacklist Engine -->
            <a href="content_control.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'content_control.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-shield-halved w-5 text-center text-base text-rose-400"></i>
                    <span>کنترل کلمات ممنوعه</span>
                </div>
            </a>

            <!-- Categories Management & Lockdown -->
            <a href="categories.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'categories.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-lock w-5 text-center text-base text-amber-400"></i>
                    <span>دسته‌بندی‌ها و قفل بخش‌ها</span>
                </div>
            </a>

            <!-- Just Became Free Manager -->
            <a href="just_free.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'just_free.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-bolt-lightning w-5 text-center text-base text-amber-400"></i>
                    <span>همین الان رایگان شد</span>
                </div>
            </a>

            <!-- Page Banners Customizer -->
            <a href="banners.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'banners.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-image w-5 text-center text-base text-teal-400"></i>
                    <span>مدیریت بنر صفحات اپ</span>
                </div>
            </a>

            <!-- User Tickets & Feedback -->
            <a href="tickets.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'tickets.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-envelope-open-text w-5 text-center text-base text-cyan-400"></i>
                    <span>پیام‌ها و تیکت‌های کاربران</span>
                </div>
            </a>

            <!-- Financial Reports & ZarinPal -->
            <a href="financial.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'financial.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-receipt w-5 text-center text-base text-emerald-400"></i>
                    <span>گزارش مالی و زرین‌پال</span>
                </div>
            </a>

            <!-- Settings -->
            <a href="settings.php" class="flex items-center justify-between px-4 py-3 rounded-2xl text-sm font-medium transition <?= ($currentPage === 'settings.php') ? 'bg-teal-500/15 text-teal-400 border border-teal-500/30' : 'text-slate-400 hover:bg-slate-800/80 hover:text-white' ?>">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-sliders w-5 text-center text-base"></i>
                    <span>تنظیمات درگاه و سامانه</span>
                </div>
            </a>

        </nav>

        <!-- Admin Profile Footer -->
        <div class="p-4 border-t border-slate-800 bg-slate-900/60">
            <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-teal-400 font-bold">
                        <i class="fa-solid fa-user-gear"></i>
                    </div>
                    <div>
                        <div class="text-sm font-bold text-white"><?= htmlspecialchars($adminName) ?></div>
                        <div class="text-xs text-slate-400"><?= $adminRole === 'SUPER_ADMIN' ? 'مدیر ارشد' : 'ناظر محتوا' ?></div>
                    </div>
                </div>
                <a href="logout.php" title="خروج از پنل" class="text-slate-400 hover:text-rose-400 p-2 rounded-xl hover:bg-slate-800 transition">
                    <i class="fa-solid fa-arrow-right-from-bracket text-lg"></i>
                </a>
            </div>
        </div>

    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 flex flex-col min-w-0 bg-slate-950 overflow-x-hidden">
        
        <!-- Top Navigation Bar -->
        <header class="h-20 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-8 flex items-center justify-between sticky top-0 z-20">
            <div>
                <h1 class="text-xl font-bold text-white"><?= $pageTitle ?? 'داشبورد مدیریت' ?></h1>
                <p class="text-xs text-slate-400 mt-0.5">وضعیت سرور: <span class="text-emerald-400 font-semibold">● آنلاین و پایدار (cPanel/MySQL)</span></p>
            </div>
            
            <div class="flex items-center gap-4">
                <a href="listings.php?status=PENDING" class="bg-slate-800 hover:bg-slate-700 text-slate-300 px-4 py-2 rounded-xl text-xs font-semibold flex items-center gap-2 border border-slate-700 transition">
                    <i class="fa-solid fa-clock-rotate-left text-amber-400"></i>
                    <span>صف تایید: <?= persianNumber($pendingListingsCount) ?> آگهی</span>
                </a>
                <a href="settings.php" class="w-10 h-10 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 flex items-center justify-center border border-slate-700 transition">
                    <i class="fa-solid fa-gear"></i>
                </a>
            </div>
        </header>

        <!-- Page Body Container -->
        <div class="p-8 space-y-8 flex-1">
