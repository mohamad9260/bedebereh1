<?php
/**
 * ==============================================================================
 * وب‌سایت بده بره | صفحه اصلی و لندینگ پیج معرفی و دانلود اپلیکیشن
 * BedeBere Official Landing Page & App Showcase
 * ==============================================================================
 */

// Load basic configuration if available (graceful standalone fallback)
$apkDownloadUrl = 'bedebere.apk';
$appVersion = '1.0.0';
$appSize = '۲۳.۸ مگابایت';

if (file_exists(__DIR__ . '/config.php')) {
    @require_once __DIR__ . '/config.php';
    if (defined('APP_DOWNLOAD_URL')) {
        $apkDownloadUrl = APP_DOWNLOAD_URL;
    }
    if (defined('APP_VERSION_NAME')) {
        $appVersion = APP_VERSION_NAME;
    }
    if (defined('APP_SIZE_MB')) {
        $appSize = APP_SIZE_MB . ' مگابایت';
    }
}
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl" class="scroll-smooth">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>بده بره | هدیه بده، تخفیف بده، فرصت پیدا کن</title>
    
    <!-- SEO & Social Meta Tags -->
    <meta name="description" content="بده بره؛ پلتفرمی برای ارتباط میان کسانی که می‌خواهند هدیه بدهند یا تخفیف ارائه کنند و کسانی که به دنبال یک فرصت خوب هستند. چیزی که برای تو اضافه است، شاید برای دیگری یک فرصت باشد.">
    <meta name="keywords" content="بده بره, هدیه رایگان, تخفیف ویژه, بخشش, اشتراک گذاری, اپلیکیشن هدیه, اقتصاد چرخشی, فرصت خوب">
    <meta name="author" content="تیم بده بره">
    <meta name="robots" content="index, follow">
    <link rel="canonical" href="./">

    <!-- Open Graph / Facebook -->
    <meta property="og:type" content="website">
    <meta property="og:url" content="./">
    <meta property="og:title" content="بده بره | هدیه بده، تخفیف بده، فرصت پیدا کن">
    <meta property="og:description" content="چیزی که برای تو اضافه است، شاید برای دیگری یک فرصت باشد. دانلود مستقیم اپلیکیشن بده بره.">
    <meta property="og:locale" content="fa_IR">
    <meta property="og:site_name" content="بده بره">

    <!-- Twitter Card -->
    <meta name="twitter:card" content="summary_large_image">
    <meta name="twitter:title" content="بده بره | هدیه بده، تخفیف بده، فرصت پیدا کن">
    <meta name="twitter:description" content="پلتفرم اجتماعی برای بخشیدن و دریافت کردن فرصت‌ها و هدیه‌ها">

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
                            200: '#99f6e4',
                            300: '#5eead4',
                            400: '#2dd4bf',
                            500: '#14b8a6',
                            600: '#0d9488',
                            700: '#0f766e',
                            800: '#115e59',
                            900: '#134e4a',
                        },
                        violetBrand: {
                            50: '#faf5ff',
                            100: '#f3e8ff',
                            500: '#a855f7',
                            600: '#9333ea',
                            700: '#7e22ce',
                        },
                        pinkBrand: {
                            500: '#ec4899',
                            600: '#db2777',
                        },
                        coralBrand: '#fb7185',
                        amberBrand: '#f59e0b',
                    },
                    fontFamily: {
                        sans: ['Vazirmatn', 'Tahoma', 'sans-serif'],
                    },
                    animation: {
                        'float-slow': 'float 6s ease-in-out infinite',
                        'float-delayed': 'float 6s ease-in-out 3s infinite',
                        'pulse-gentle': 'pulseGentle 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
                        'sparkle': 'sparkle 2s ease-in-out infinite',
                    },
                    keyframes: {
                        float: {
                            '0%, 100%': { transform: 'translateY(0px)' },
                            '50%': { transform: 'translateY(-12px)' },
                        },
                        pulseGentle: {
                            '0%, 100%': { opacity: '1', transform: 'scale(1)' },
                            '50%': { opacity: '0.88', transform: 'scale(1.03)' },
                        },
                        sparkle: {
                            '0%, 100%': { opacity: '0.3', transform: 'scale(0.8)' },
                            '50%': { opacity: '1', transform: 'scale(1.2)' },
                        }
                    }
                }
            }
        }
    </script>

    <!-- Persian Font: Vazirmatn CDN -->
    <link href="https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css" rel="stylesheet" type="text/css" />

    <!-- FontAwesome 6 Icons CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        body {
            font-family: 'Vazirmatn', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #fafbfc;
            color: #1e293b;
            overflow-x: hidden;
        }

        /* Glassmorphism & Mesh Backgrounds */
        .glass-card {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(14px);
            -webkit-backdrop-filter: blur(14px);
            border: 1px solid rgba(255, 255, 255, 0.6);
        }

        .glass-nav {
            background: rgba(255, 255, 255, 0.9);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-bottom: 1px solid rgba(226, 232, 240, 0.8);
        }

        .gradient-text-teal {
            background: linear-gradient(135deg, #0d9488 0%, #06b6d4 50%, #6366f1 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .gradient-text-warm {
            background: linear-gradient(135deg, #ec4899 0%, #f97316 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .hero-blob-1 {
            background: radial-gradient(circle, rgba(45, 212, 191, 0.28) 0%, rgba(204, 251, 241, 0.05) 70%);
        }

        .hero-blob-2 {
            background: radial-gradient(circle, rgba(168, 85, 247, 0.22) 0%, rgba(243, 232, 255, 0.05) 70%);
        }

        .hero-blob-3 {
            background: radial-gradient(circle, rgba(251, 146, 60, 0.2) 0%, rgba(254, 243, 199, 0.05) 70%);
        }

        /* Accessibility: reduce motion */
        @media (prefers-reduced-motion: reduce) {
            *, ::before, ::after {
                animation-duration: 0.01ms !important;
                animation-iteration-count: 1 !important;
                transition-duration: 0.01ms !important;
                scroll-behavior: auto !important;
            }
        }

        /* Custom Scrollbar */
        ::-webkit-scrollbar {
            width: 8px;
        }
        ::-webkit-scrollbar-track {
            background: #f1f5f9;
        }
        ::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 4px;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: #94a3b8;
        }
    </style>

    <!-- JSON-LD Structured Data for SEO -->
    <script type="application/ld+json">
    {
      "@context": "https://schema.org",
      "@type": "MobileApplication",
      "name": "بده بره",
      "operatingSystem": "ANDROID",
      "applicationCategory": "SocialNetworkingApplication",
      "description": "پلتفرم اجتماعی برای بخشیدن، هدیه دادن و پیدا کردن تخفیف‌ها و فرصت‌های خوب میان مردم.",
      "offers": {
        "@type": "Offer",
        "price": "0",
        "priceCurrency": "IRR"
      }
    }
    </script>
</head>
<body class="antialiased selection:bg-brand-500 selection:text-white">

    <!-- ========================================================================= -->
    <!-- NAVBAR -->
    <!-- ========================================================================= -->
    <header class="fixed top-0 left-0 right-0 z-50 transition-all duration-300 glass-nav shadow-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex items-center justify-between h-20">
                
                <!-- Logo & Brand Name -->
                <a href="#" class="flex items-center gap-3.5 group">
                    <div class="w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 via-brand-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-brand-500/25 group-hover:scale-105 transition-transform duration-300">
                        <i class="fa-solid fa-hand-holding-heart text-2xl text-white"></i>
                    </div>
                    <div>
                        <div class="flex items-center gap-2">
                            <span class="text-2xl font-black text-slate-800 tracking-tight">بده بره</span>
                            <span class="text-[10px] font-bold bg-brand-50 text-brand-700 border border-brand-200 px-2 py-0.5 rounded-full">اجتماعی</span>
                        </div>
                        <span class="text-xs text-slate-500 hidden sm:block">پلتفرم هدیه، تخفیف و فرصت‌ها</span>
                    </div>
                </a>

                <!-- Navigation Anchors (Desktop) -->
                <nav class="hidden md:flex items-center gap-8 text-sm font-medium text-slate-600">
                    <a href="#what-is-it" class="hover:text-brand-600 transition">بده بره یعنی چی؟</a>
                    <a href="#how-it-works" class="hover:text-brand-600 transition">چطور کار می‌کنه؟</a>
                    <a href="#for-everyone" class="hover:text-brand-600 transition">برای همه</a>
                    <a href="#categories" class="hover:text-brand-600 transition">دسته‌بندی‌ها</a>
                    <a href="#trust" class="hover:text-brand-600 transition">امنیت و اعتماد</a>
                </nav>

                <!-- Header CTA Button -->
                <div class="flex items-center gap-3">
                    <a href="#download" class="inline-flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-700 hover:to-brand-600 text-white text-sm font-bold shadow-md shadow-brand-600/20 hover:shadow-lg hover:shadow-brand-600/30 hover:-translate-y-0.5 active:translate-y-0 transition-all duration-200">
                        <i class="fa-brands fa-android text-lg"></i>
                        <span>دانلود اپلیکیشن</span>
                    </a>
                </div>

            </div>
        </div>
    </header>

    <div class="h-20"></div> <!-- Navbar Spacer -->

    <!-- ========================================================================= -->
    <!-- 1. HERO SECTION (اولین برخورد) -->
    <!-- ========================================================================= -->
    <section class="relative pt-12 pb-20 lg:pt-20 lg:pb-32 overflow-hidden">
        
        <!-- Ambient Glow Elements -->
        <div class="absolute top-10 right-1/4 w-96 h-96 hero-blob-1 rounded-full filter blur-3xl pointer-events-none -z-10"></div>
        <div class="absolute top-40 left-10 w-96 h-96 hero-blob-2 rounded-full filter blur-3xl pointer-events-none -z-10"></div>
        <div class="absolute bottom-10 right-10 w-80 h-80 hero-blob-3 rounded-full filter blur-3xl pointer-events-none -z-10"></div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
                
                <!-- Left/Main Text Column -->
                <div class="lg:col-span-7 space-y-8 text-center lg:text-right">
                    
                    <!-- Friendly Badge -->
                    <div class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-gradient-to-r from-brand-50 via-purple-50 to-pink-50 border border-brand-200/80 shadow-sm text-xs font-bold text-slate-700">
                        <span class="flex h-2.5 w-2.5 relative">
                            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
                            <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-brand-500"></span>
                        </span>
                        <span>یک حرکت اجتماعی شاد و پرانرژی برای هدیه دادن و دریافت فرصت‌ها</span>
                    </div>

                    <!-- Main Hero Heading -->
                    <h1 class="text-3xl sm:text-4xl lg:text-5xl xl:text-6xl font-black text-slate-900 leading-[1.3] lg:leading-[1.25] tracking-tight">
                        گاهی چیزی که برای تو <span class="gradient-text-teal">ساده</span> است،
                        <br class="hidden sm:inline" />
                        برای یک نفر <span class="gradient-text-warm">یک دنیا ارزش</span> دارد.
                    </h1>

                    <!-- Subtitle & Explanatory Text -->
                    <div class="space-y-3 max-w-2xl mx-auto lg:mx-0">
                        <p class="text-lg sm:text-xl font-bold text-slate-700">
                            بده بره؛ جایی برای بخشیدن، پیدا کردن و سهیم شدن در فرصت‌های خوب.
                        </p>
                        <p class="text-sm sm:text-base text-slate-600 leading-relaxed">
                            آدم‌هایی که می‌خواهند چیزی را هدیه کنند یا با تخفیف در اختیار دیگران بگذارند، به آدم‌هایی می‌رسند که به آن نیاز دارند. بدون پیچیدگی، با صمیمیت و حس خوب.
                        </p>
                    </div>

                    <!-- Hero Action CTAs -->
                    <div class="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4 pt-2">
                        
                        <!-- Primary Download CTA -->
                        <a href="<?= htmlspecialchars($apkDownloadUrl) ?>" download class="w-full sm:w-auto inline-flex items-center justify-center gap-3.5 px-8 py-4 rounded-2xl bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 hover:from-brand-700 hover:to-cyan-600 text-white font-extrabold text-base shadow-xl shadow-brand-500/30 hover:shadow-2xl hover:shadow-brand-500/40 hover:-translate-y-1 transition-all duration-200 group">
                            <i class="fa-brands fa-android text-2xl group-hover:scale-110 transition-transform"></i>
                            <div class="text-right">
                                <span class="block text-xs font-medium text-brand-100">دانلود مستقیم اندروید</span>
                                <span class="block text-base font-black">دانلود اپلیکیشن بده بره</span>
                            </div>
                            <i class="fa-solid fa-arrow-down-long text-lg mr-2 animate-bounce"></i>
                        </a>

                        <!-- Secondary How It Works CTA -->
                        <a href="#how-it-works" class="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-6 py-4 rounded-2xl bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 font-bold text-sm shadow-sm hover:border-slate-300 hover:text-brand-700 transition-all duration-200">
                            <i class="fa-regular fa-circle-question text-brand-600 text-lg"></i>
                            <span>بده بره چطور کار می‌کند؟</span>
                        </a>

                    </div>

                    <!-- Trust Micro Metrics -->
                    <div class="pt-4 flex flex-wrap items-center justify-center lg:justify-start gap-6 text-xs text-slate-500 border-t border-slate-100">
                        <div class="flex items-center gap-2">
                            <i class="fa-solid fa-circle-check text-emerald-500"></i>
                            <span>۱۰۰٪ رایگان و بدون کارمزد</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <i class="fa-solid fa-shield-halved text-brand-600"></i>
                            <span>تأیید پیامکی شماره کاربران</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <i class="fa-solid fa-heart text-pink-500"></i>
                            <span>حس خوب بخشش و همبستگی</span>
                        </div>
                    </div>

                </div>

                <!-- Right/Hero Visual Column -->
                <div class="lg:col-span-5 relative">
                    
                    <!-- Decorative Background Card -->
                    <div class="relative mx-auto max-w-md lg:max-w-none">
                        
                        <!-- Main Visual Showcase Box -->
                        <div class="relative bg-gradient-to-b from-white/90 to-brand-50/60 rounded-3xl p-6 sm:p-8 border border-white shadow-2xl shadow-slate-200/80 backdrop-blur-xl">
                            
                            <!-- Floating Badges in Visual -->
                            <div class="absolute -top-5 -right-4 bg-white rounded-2xl p-3 shadow-lg border border-slate-100 flex items-center gap-3 animate-float-slow">
                                <div class="w-10 h-10 rounded-xl bg-pink-100 text-pink-600 flex items-center justify-center text-lg">
                                    <i class="fa-solid fa-gift"></i>
                                </div>
                                <div class="text-right">
                                    <span class="text-[11px] font-bold text-slate-800 block">یک هدیه پیدا شد!</span>
                                    <span class="text-[10px] text-slate-400">کتاب‌های کنکور رایگان</span>
                                </div>
                            </div>

                            <div class="absolute -bottom-5 -left-4 bg-white rounded-2xl p-3 shadow-lg border border-slate-100 flex items-center gap-3 animate-float-delayed">
                                <div class="w-10 h-10 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center text-lg">
                                    <i class="fa-solid fa-tag"></i>
                                </div>
                                <div class="text-right">
                                    <span class="text-[11px] font-bold text-slate-800 block">تخفیف ویژه ۶۰٪</span>
                                    <span class="text-[10px] text-emerald-600 font-semibold">توسط همشهری مهربان</span>
                                </div>
                            </div>

                            <!-- Visual Graphic Canvas: Diverse Smiling Youth Sharing Opportunities -->
                            <div class="w-full bg-gradient-to-tr from-teal-500/10 via-purple-500/10 to-pink-500/10 rounded-2xl p-6 relative overflow-hidden border border-brand-100">
                                
                                <!-- Decorative Sparkles -->
                                <div class="absolute top-4 left-6 text-amber-400 text-sm animate-sparkle"><i class="fa-solid fa-sparkles"></i></div>
                                <div class="absolute bottom-6 right-8 text-pink-400 text-xs animate-sparkle"><i class="fa-solid fa-heart"></i></div>
                                <div class="absolute top-1/2 right-4 text-cyan-400 text-sm animate-sparkle"><i class="fa-solid fa-star"></i></div>

                                <!-- Central Illustration Component (Modern Vector Art) -->
                                <div class="py-4 space-y-6">
                                    
                                    <!-- Person 1: Giving a gift -->
                                    <div class="flex items-center gap-4 bg-white/95 rounded-2xl p-4 shadow-sm border border-brand-100/60 transform hover:scale-[1.02] transition-transform">
                                        <div class="w-12 h-12 rounded-full bg-gradient-to-tr from-teal-500 to-emerald-400 flex items-center justify-center text-white text-xl font-black shadow-md shadow-teal-500/20 shrink-0">
                                            <span>سارا</span>
                                        </div>
                                        <div class="flex-1 text-right">
                                            <div class="flex items-center justify-between">
                                                <span class="text-xs font-bold text-slate-800">«دوچرخه نوجوان که دیگه استفاده نمیشه»</span>
                                                <span class="text-[10px] bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-md font-bold">هدیه رایگان</span>
                                            </div>
                                            <p class="text-[11px] text-slate-500 mt-1">«دوست دارم برسه دست کسی که عاشق دوچرخه‌سواریه!»</p>
                                        </div>
                                    </div>

                                    <!-- Connecting Energy Beam -->
                                    <div class="flex items-center justify-center gap-2">
                                        <div class="h-0.5 flex-1 bg-gradient-to-l from-brand-400 to-transparent"></div>
                                        <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-brand-500 to-purple-500 flex items-center justify-center text-white text-xs shadow-md">
                                            <i class="fa-solid fa-arrow-down"></i>
                                        </div>
                                        <div class="h-0.5 flex-1 bg-gradient-to-r from-purple-400 to-transparent"></div>
                                    </div>

                                    <!-- Person 2: Joyfully receiving & connecting -->
                                    <div class="flex items-center gap-4 bg-white/95 rounded-2xl p-4 shadow-sm border border-purple-100/60 transform hover:scale-[1.02] transition-transform">
                                        <div class="w-12 h-12 rounded-full bg-gradient-to-tr from-purple-500 to-pink-500 flex items-center justify-center text-white text-xl font-black shadow-md shadow-purple-500/20 shrink-0">
                                            <span>علی</span>
                                        </div>
                                        <div class="flex-1 text-right">
                                            <div class="flex items-center justify-between">
                                                <span class="text-xs font-bold text-slate-800">«رزرو شد و با لبخند تحویل گرفته شد»</span>
                                                <span class="text-[10px] bg-purple-100 text-purple-700 px-2 py-0.5 rounded-md font-bold">اتفاق خوب</span>
                                            </div>
                                            <p class="text-[11px] text-slate-500 mt-1">«بهترین هدیه برای برادر کوچکم شد. ممنون از بده بره!»</p>
                                        </div>
                                    </div>

                                </div>

                                <!-- Bottom Status Bar in Graphic -->
                                <div class="mt-4 pt-3 border-t border-slate-200/60 flex items-center justify-between text-[11px] text-slate-600 font-medium">
                                    <span class="flex items-center gap-1.5 text-brand-700 font-bold">
                                        <i class="fa-solid fa-bolt text-amber-500"></i>
                                        بده بره؛ این دو نفر را به هم وصل کرد
                                    </span>
                                    <span class="text-slate-400">امروز در تهران</span>
                                </div>

                            </div>

                        </div>

                    </div>

                </div>

            </div>
        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 2. «بده بره یعنی چی؟» (CONCEPT & 3 CARDS) -->
    <!-- ========================================================================= -->
    <section id="what-is-it" class="py-20 bg-white relative">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <!-- Section Header -->
            <div class="text-center max-w-3xl mx-auto mb-16 space-y-4">
                <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-50 text-brand-700 text-xs font-extrabold border border-brand-200">
                    <i class="fa-solid fa-sparkles text-amber-500"></i>
                    <span>ایده اصلی سامانه</span>
                </div>
                <h2 class="text-3xl sm:text-4xl font-black text-slate-900">
                    بده بره یعنی چی؟
                </h2>
                <p class="text-base sm:text-lg text-slate-600 leading-relaxed">
                    بده بره یک پل ارتباطی صمیمی و امن است؛ بین کسی که چیزی برای بخشیدن دارد و کسی که به دنبال آن فرصت یا کالاست.
                </p>
            </div>

            <!-- 3 Feature Cards Grid -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                
                <!-- Card 1: Give Gift -->
                <div class="group relative bg-gradient-to-b from-teal-50/50 to-white p-8 rounded-3xl border border-teal-100 hover:border-teal-300 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
                    <div class="w-16 h-16 rounded-2xl bg-gradient-to-tr from-teal-500 to-emerald-400 text-white flex items-center justify-center text-2xl shadow-lg shadow-teal-500/25 mb-6 group-hover:rotate-6 transition-transform">
                        <i class="fa-solid fa-gift"></i>
                    </div>
                    <div class="inline-block text-xs font-bold text-teal-700 bg-teal-100/70 px-2.5 py-1 rounded-lg mb-3">
                        سخاوت و مهربانی
                    </div>
                    <h3 class="text-2xl font-black text-slate-900 mb-3">
                        🎁 هدیه بده
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        چیزی در خانه داری که دیگر به کارت نمی‌آید اما کاملاً سالم و قابل استفاده است؟ لباس، کتاب، اسباب‌بازی، میز یا گجت‌ها را رایگان به دیگری ببخش و خانه‌ات را سبک‌تر و دلت را شادتر کن.
                    </p>
                    <div class="mt-6 pt-4 border-t border-teal-100/60 flex items-center text-xs font-bold text-teal-700">
                        <span>ایجاد حس خوشایند بخشش</span>
                        <i class="fa-solid fa-arrow-left mr-auto group-hover:-translate-x-1 transition-transform"></i>
                    </div>
                </div>

                <!-- Card 2: Offer Discount -->
                <div class="group relative bg-gradient-to-b from-purple-50/50 to-white p-8 rounded-3xl border border-purple-100 hover:border-purple-300 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
                    <div class="w-16 h-16 rounded-2xl bg-gradient-to-tr from-purple-600 to-pink-500 text-white flex items-center justify-center text-2xl shadow-lg shadow-purple-500/25 mb-6 group-hover:rotate-6 transition-transform">
                        <i class="fa-solid fa-tags"></i>
                    </div>
                    <div class="inline-block text-xs font-bold text-purple-700 bg-purple-100/70 px-2.5 py-1 rounded-lg mb-3">
                        فرصت‌های استثنایی
                    </div>
                    <h3 class="text-2xl font-black text-slate-900 mb-3">
                        🏷️ تخفیف بده
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        کسب‌وکار، مهارت یا خدمتی داری که می‌خواهی با شرایط ویژه، بلیت تخفیف‌دار یا قیمت استثنایی در اختیار جامعه قرار دهی؟ تخفیف‌هایت را معرفی کن و مشتریان و دوستان جدید جذب کن.
                    </p>
                    <div class="mt-6 pt-4 border-t border-purple-100/60 flex items-center text-xs font-bold text-purple-700">
                        <span>سهیم شدن در کسب درآمد منصفانه</span>
                        <i class="fa-solid fa-arrow-left mr-auto group-hover:-translate-x-1 transition-transform"></i>
                    </div>
                </div>

                <!-- Card 3: Receive & Discover -->
                <div class="group relative bg-gradient-to-b from-pink-50/50 to-white p-8 rounded-3xl border border-pink-100 hover:border-pink-300 shadow-sm hover:shadow-xl hover:-translate-y-2 transition-all duration-300">
                    <div class="w-16 h-16 rounded-2xl bg-gradient-to-tr from-pink-500 to-rose-400 text-white flex items-center justify-center text-2xl shadow-lg shadow-pink-500/25 mb-6 group-hover:rotate-6 transition-transform">
                        <i class="fa-solid fa-heart-circle-check"></i>
                    </div>
                    <div class="inline-block text-xs font-bold text-pink-700 bg-pink-100/70 px-2.5 py-1 rounded-lg mb-3">
                        کشف نیازها
                    </div>
                    <h3 class="text-2xl font-black text-slate-900 mb-3">
                        💚 دریافت کن
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        دنبال یک هدیه خوب، کتاب درسی، وسیله کاربردی یا تخفیف ویژه در شهرت هستی؟ بین صدها آگهی روزانه جستجو کن، هدیه موردنظرت را رزرو کن و با هماهنگی مستقیم صاحبش تحویل بگیر.
                    </p>
                    <div class="mt-6 pt-4 border-t border-pink-100/60 flex items-center text-xs font-bold text-pink-700">
                        <span>دسترسی به فرصت‌های عالی</span>
                        <i class="fa-solid fa-arrow-left mr-auto group-hover:-translate-x-1 transition-transform"></i>
                    </div>
                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 3. «یک چیز ساده می‌تواند یک اتفاق خوب بسازد» (STORY SECTION) -->
    <!-- ========================================================================= -->
    <section class="py-20 bg-gradient-to-b from-slate-900 via-slate-950 to-slate-900 text-white relative overflow-hidden">
        
        <!-- Background Lighting -->
        <div class="absolute -top-24 -right-24 w-96 h-96 bg-teal-500/10 rounded-full filter blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-96 h-96 bg-purple-500/10 rounded-full filter blur-3xl pointer-events-none"></div>

        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
                
                <!-- Story Visual Box -->
                <div class="lg:col-span-6 space-y-6">
                    <div class="bg-gradient-to-tr from-slate-800/80 to-slate-900/90 border border-slate-700/80 rounded-3xl p-8 shadow-2xl relative backdrop-blur-xl">
                        
                        <div class="flex items-center gap-4 mb-6">
                            <div class="w-14 h-14 rounded-2xl bg-gradient-to-tr from-amber-400 to-orange-500 flex items-center justify-center text-2xl text-slate-950 font-black shadow-lg">
                                <i class="fa-solid fa-hands-holding-circle"></i>
                            </div>
                            <div>
                                <span class="text-xs text-amber-400 font-bold tracking-wide block">داستان یک همدلی ساده</span>
                                <h4 class="text-xl font-black text-white">تغییر نگاه به وسایل بلااستفاده</h4>
                            </div>
                        </div>

                        <!-- Quote / Core message -->
                        <div class="bg-slate-950/60 rounded-2xl p-6 border border-slate-800 relative">
                            <i class="fa-solid fa-quote-right text-3xl text-slate-700 absolute top-4 left-4 opacity-40"></i>
                            <p class="text-base sm:text-lg text-slate-200 leading-relaxed font-medium">
                                «چیزی که سال‌ها در انبار یا کمد خانه‌ات خاک می‌خورد، می‌تواند آرزوی یک کودک، راهگشای یک دانشجو، یا شروع یک انگیزه تازه برای زندگی یک انسان دیگر باشد.»
                            </p>
                        </div>

                        <!-- 3 Micro Points -->
                        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-6">
                            <div class="bg-slate-800/40 rounded-xl p-3.5 border border-slate-700/50 text-center">
                                <span class="text-2xl block mb-1">🌱</span>
                                <span class="text-xs font-bold text-slate-300 block">حفظ محیط زیست</span>
                                <span class="text-[10px] text-slate-400">کاهش دورریز و مصرف‌گرایی</span>
                            </div>
                            <div class="bg-slate-800/40 rounded-xl p-3.5 border border-slate-700/50 text-center">
                                <span class="text-2xl block mb-1">🤝</span>
                                <span class="text-xs font-bold text-slate-300 block">دوستی‌های واقعی</span>
                                <span class="text-[10px] text-slate-400">آشنایی با آدم‌های نیک‌اندیش</span>
                            </div>
                            <div class="bg-slate-800/40 rounded-xl p-3.5 border border-slate-700/50 text-center">
                                <span class="text-2xl block mb-1">✨</span>
                                <span class="text-xs font-bold text-slate-300 block">برکت زندگی</span>
                                <span class="text-[10px] text-slate-400">شادی عمیق درونی</span>
                            </div>
                        </div>

                    </div>
                </div>

                <!-- Story Text Column -->
                <div class="lg:col-span-6 space-y-6 text-center lg:text-right">
                    
                    <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-teal-500/10 border border-teal-500/30 text-teal-400 text-xs font-bold">
                        <i class="fa-solid fa-heart-pulse"></i>
                        <span>فرهنگ زیبای بخشیدن و به اشتراک گذاشتن</span>
                    </div>

                    <h2 class="text-3xl sm:text-4xl lg:text-5xl font-black text-white leading-tight">
                        خوبی وقتی می‌چرخه، <span class="text-transparent bg-clip-text bg-gradient-to-r from-teal-400 via-cyan-300 to-purple-400">بزرگتر میشه.</span>
                    </h2>

                    <p class="text-slate-300 text-base sm:text-lg leading-relaxed">
                        شاید چیزی برای تو دیگر استفاده‌ای نداشته باشد؛ اما همان چیز می‌تواند برای شخص دیگری دقیقاً همان چیزی باشد که ماه‌ها به دنبالش بوده است.
                    </p>

                    <p class="text-slate-400 text-sm leading-relaxed">
                        در «بده بره» همه چیز بر پایه کرامت انسانی، احترام متقابل و لذت بخشیدن بنا شده است. بدون هیچ واسطه و هزینه‌ای، وسایلی که دوست داری هدیه کنی را در چند ثانیه آگهی کن و لذت دیدن لبخند دیگری را تجربه کن.
                    </p>

                    <div class="pt-4">
                        <a href="#download" class="inline-flex items-center gap-3 px-6 py-3.5 rounded-2xl bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-slate-950 font-black text-sm shadow-lg shadow-teal-500/25 transition">
                            <i class="fa-solid fa-circle-arrow-down"></i>
                            <span>همین حالا اپلیکیشن را نصب کنید</span>
                        </a>
                    </div>

                </div>

            </div>
        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 4. HOW IT WORKS (نحوه کارکرد در ۴ مرحله ساده) -->
    <!-- ========================================================================= -->
    <section id="how-it-works" class="py-24 bg-slate-50 relative">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <!-- Section Title -->
            <div class="text-center max-w-3xl mx-auto mb-20 space-y-4">
                <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-100/80 text-brand-800 text-xs font-black border border-brand-200">
                    <i class="fa-solid fa-route text-brand-600"></i>
                    <span>مسیر ساده و سریع</span>
                </div>
                <h2 class="text-3xl sm:text-4xl font-black text-slate-900">
                    بده بره چطور کار می‌کند؟
                </h2>
                <p class="text-base text-slate-600">
                    در ۴ قدم بسیار ساده، هدیه بدهید یا فرصت‌های دلخواهتان را پیدا کنید.
                </p>
            </div>

            <!-- 4 Steps Grid with Visual Connection Path -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 relative">
                
                <!-- Step 01 -->
                <div class="bg-white p-8 rounded-3xl border border-slate-200/80 shadow-sm hover:shadow-xl hover:border-brand-300 transition-all duration-300 relative group">
                    <div class="flex items-center justify-between mb-6">
                        <span class="text-4xl font-black text-slate-200 group-hover:text-brand-500 transition-colors">01</span>
                        <div class="w-14 h-14 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                            <i class="fa-solid fa-mobile-screen-button"></i>
                        </div>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-3">
                        اپ را نصب کن
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        اپلیکیشن اندروید بده بره را رایگان دانلود کن و در کمتر از یک دقیقه با شماره موبایلت عضو شو.
                    </p>
                </div>

                <!-- Step 02 -->
                <div class="bg-white p-8 rounded-3xl border border-slate-200/80 shadow-sm hover:shadow-xl hover:border-brand-300 transition-all duration-300 relative group">
                    <div class="flex items-center justify-between mb-6">
                        <span class="text-4xl font-black text-slate-200 group-hover:text-amber-500 transition-colors">02</span>
                        <div class="w-14 h-14 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                            <i class="fa-solid fa-camera-retro"></i>
                        </div>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-3">
                        هدیه یا تخفیف را ثبت کن
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        از وسیله، کالا یا خدمت خودت عکس بگیر و همراه توضیحات کوتاه در دسته‌بندی مناسب آگهی کن.
                    </p>
                </div>

                <!-- Step 03 -->
                <div class="bg-white p-8 rounded-3xl border border-slate-200/80 shadow-sm hover:shadow-xl hover:border-brand-300 transition-all duration-300 relative group">
                    <div class="flex items-center justify-between mb-6">
                        <span class="text-4xl font-black text-slate-200 group-hover:text-purple-500 transition-colors">03</span>
                        <div class="w-14 h-14 rounded-2xl bg-purple-50 text-purple-600 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                            <i class="fa-solid fa-handshake"></i>
                        </div>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-3">
                        آدم مناسب پیدایش می‌کند
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        فردی که به آن نیاز دارد آگهی را می‌بیند و با زدن دکمه رزرو، هماهنگی تحویل را با شما شروع می‌کند.
                    </p>
                </div>

                <!-- Step 04 -->
                <div class="bg-white p-8 rounded-3xl border border-slate-200/80 shadow-sm hover:shadow-xl hover:border-brand-300 transition-all duration-300 relative group">
                    <div class="flex items-center justify-between mb-6">
                        <span class="text-4xl font-black text-slate-200 group-hover:text-pink-500 transition-colors">04</span>
                        <div class="w-14 h-14 rounded-2xl bg-pink-50 text-pink-600 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
                            <i class="fa-solid fa-face-smile-beam"></i>
                        </div>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-3">
                        یک اتفاق خوب شکل می‌گیرد
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        کالا با یک لبخند تحویل داده می‌شود و شادی و برکت بین دو همشهری و دوست جدید به جریان می‌افتد.
                    </p>
                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 5. بخش دوطرفه «برای همه» (FOR EVERYONE) -->
    <!-- ========================================================================= -->
    <section id="for-everyone" class="py-20 bg-white relative">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div class="text-center max-w-2xl mx-auto mb-14 space-y-3">
                <span class="text-xs font-black text-brand-600 bg-brand-50 border border-brand-200 px-3 py-1 rounded-full">تعاملی و دوطرفه</span>
                <h2 class="text-3xl font-black text-slate-900">بده بره برای چه کسانی است؟</h2>
                <p class="text-sm text-slate-600">فرقی نمی‌کند می‌خواهی هدیه بدهی یا چیزی دریافت کنی؛ اینجا برای هر دو طرف پر از برکت و فرصت است.</p>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-11 gap-6 items-center">
                
                <!-- Side 1: Givers -->
                <div class="lg:col-span-5 bg-gradient-to-br from-teal-500 to-emerald-600 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden">
                    <div class="absolute -right-8 -bottom-8 w-40 h-40 bg-white/10 rounded-full"></div>
                    <div class="flex items-center gap-3 mb-4">
                        <div class="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center text-xl">
                            <i class="fa-solid fa-hand-holding-heart"></i>
                        </div>
                        <h3 class="text-2xl font-black">اگر چیزی برای بخشیدن داری...</h3>
                    </div>
                    <p class="text-teal-50 text-sm leading-relaxed mb-6">
                        «هدیه کن، تخفیف بده و فرصت ایجاد کن. وسایلی که در کنج کمد خاک می‌خورند یا مهارتی که داری را به یک لبخند رضایت‌بخش تبدیل کن.»
                    </p>
                    <ul class="space-y-2.5 text-xs text-teal-100 font-medium">
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-emerald-200"></i> ثبت رایگان و بی‌دردسر آگهی هدیه</li>
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-emerald-200"></i> امکان تعیین شروط تحویل و انتخاب تحویل‌گیرنده</li>
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-emerald-200"></i> رهایی از وسایل اضافه بدون آسیب به محیط زیست</li>
                    </ul>
                </div>

                <!-- Center Connector Icon -->
                <div class="lg:col-span-1 flex items-center justify-center">
                    <div class="w-14 h-14 rounded-full bg-slate-900 text-white flex items-center justify-center text-xl shadow-lg border-4 border-white">
                        <i class="fa-solid fa-people-arrows"></i>
                    </div>
                </div>

                <!-- Side 2: Seekers -->
                <div class="lg:col-span-5 bg-gradient-to-br from-purple-600 to-pink-600 rounded-3xl p-8 text-white shadow-xl relative overflow-hidden">
                    <div class="absolute -left-8 -bottom-8 w-40 h-40 bg-white/10 rounded-full"></div>
                    <div class="flex items-center gap-3 mb-4">
                        <div class="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center text-xl">
                            <i class="fa-solid fa-magnifying-glass-location"></i>
                        </div>
                        <h3 class="text-2xl font-black">اگر دنبال چیزی هستی...</h3>
                    </div>
                    <p class="text-purple-50 text-sm leading-relaxed mb-6">
                        «شاید چیزی که می‌خواهی همین حالا نزد همشهری دیگری باشد که با تمام وجود می‌خواهد آن را به تو برساند.»
                    </p>
                    <ul class="space-y-2.5 text-xs text-purple-100 font-medium">
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-pink-200"></i> دسترسی به هزاران وسیله و کتاب رایگان</li>
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-pink-200"></i> تخفیف‌های ویژه خدمات، آموزش و محصولات</li>
                        <li class="flex items-center gap-2"><i class="fa-solid fa-check text-pink-200"></i> رزرو سریع و بی‌واسطه در سراسر کشور</li>
                    </ul>
                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 6. CATEGORIES SHOWCASE (دسته‌بندی‌های متنوع) -->
    <!-- ========================================================================= -->
    <section id="categories" class="py-20 bg-slate-50 relative">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div class="text-center max-w-3xl mx-auto mb-16 space-y-3">
                <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-200/80 text-slate-800 text-xs font-bold">
                    <i class="fa-solid fa-shapes text-brand-600"></i>
                    <span>تنوع بی‌پایان فرصت‌ها</span>
                </div>
                <h2 class="text-3xl sm:text-4xl font-black text-slate-900">
                    در بده بره چه چیزهایی پیدا می‌شود؟
                </h2>
                <p class="text-sm sm:text-base text-slate-600">
                    از وسایل منزل و پوشاک تا کتاب، تخفیف‌های طلایی و دوره‌های آموزشی.
                </p>
            </div>

            <!-- Categories Grid -->
            <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-5">
                
                <!-- 1. Free Gifts -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-teal-50 text-teal-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-gift"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">هدایای رایگان</h4>
                    <p class="text-xs text-slate-500">کالاهای اهدایی و بخشیدنی</p>
                </div>

                <!-- 2. Discounts -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-tags"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">تخفیف‌های ویژه</h4>
                    <p class="text-xs text-slate-500">کوپن و آفر طلایی مشاغل</p>
                </div>

                <!-- 3. Clothing -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-purple-50 text-purple-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-shirt"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">پوشاک و مد</h4>
                    <p class="text-xs text-slate-500">لباس، کفش، کاپشن و کودک</p>
                </div>

                <!-- 4. Books -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-book-open"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">کتاب و آموزش</h4>
                    <p class="text-xs text-slate-500">کنکور، رمان و لوازم تحریر</p>
                </div>

                <!-- 5. Furniture & Home -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-couch"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">لوازم خانه</h4>
                    <p class="text-xs text-slate-500">میز، صندلی، ظروف و دکور</p>
                </div>

                <!-- 6. Digital & Gadgets -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-cyan-50 text-cyan-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-laptop"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">کالای دیجیتال</h4>
                    <p class="text-xs text-slate-500">کابل، گجت، هندزفری و قطعات</p>
                </div>

                <!-- 7. Services & Skills -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-graduation-cap"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">خدمات و مهارت‌ها</h4>
                    <p class="text-xs text-slate-500">تدریس، مشاوره و تعمیرات</p>
                </div>

                <!-- 8. Opportunities & Social -->
                <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-brand-400 hover:-translate-y-1 transition-all duration-200 text-center">
                    <div class="w-14 h-14 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center text-2xl mx-auto mb-3">
                        <i class="fa-solid fa-handshake-angle"></i>
                    </div>
                    <h4 class="font-black text-slate-800 text-base mb-1">مشارکت اجتماعی</h4>
                    <p class="text-xs text-slate-500">هم‌فکری، کار داوطلبانه و مهر</p>
                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 7. SOCIAL VIBE & COMMUNITY (حس همبستگی اجتماعی) -->
    <!-- ========================================================================= -->
    <section class="py-20 bg-gradient-to-r from-brand-600 via-teal-600 to-purple-600 text-white relative overflow-hidden">
        
        <!-- Ambient Shapes -->
        <div class="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full filter blur-3xl pointer-events-none"></div>
        <div class="absolute bottom-0 left-0 w-96 h-96 bg-pink-500/20 rounded-full filter blur-3xl pointer-events-none"></div>

        <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
            
            <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/20 backdrop-blur-md text-white text-xs font-bold">
                <i class="fa-solid fa-sparkles text-amber-300"></i>
                <span>جامعه بزرگ و پرانرژی بده بره</span>
            </div>

            <h2 class="text-3xl sm:text-4xl lg:text-5xl font-black leading-tight">
                «شاید چیزی که می‌بخشی، روز کسی را بسازد.»
            </h2>

            <p class="text-base sm:text-lg text-teal-100 max-w-2xl mx-auto leading-relaxed">
                هر روز ده‌ها ارتباط صمیمی و دوستانه بین مردم شهرهای مختلف شکل می‌گیرد. بده بره فراتر از یک اپلیکیشن، یک حس مشترک از مهربانی و انسان‌دوستی است.
            </p>

            <div class="pt-4 flex flex-wrap items-center justify-center gap-4">
                <a href="#download" class="px-8 py-4 rounded-2xl bg-white text-slate-900 hover:bg-slate-100 font-extrabold text-sm shadow-xl hover:-translate-y-0.5 transition flex items-center gap-2">
                    <i class="fa-brands fa-android text-brand-600 text-xl"></i>
                    <span>پیوستن به جمع ما (دانلود اپ)</span>
                </a>
            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 8. TRUST & SAFETY (اعتماد و امنیت واقعی) -->
    <!-- ========================================================================= -->
    <section id="trust" class="py-20 bg-white relative">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div class="text-center max-w-3xl mx-auto mb-16 space-y-3">
                <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold border border-emerald-200">
                    <i class="fa-solid fa-shield-check"></i>
                    <span>بستری امن و مطمئن</span>
                </div>
                <h2 class="text-3xl sm:text-4xl font-black text-slate-900">
                    برای یک تجربه خوب، اعتماد مهم است.
                </h2>
                <p class="text-sm sm:text-base text-slate-600">
                    ما سازوکارهای متعددی را برای امنیت، آرامش خاطر و تعامل مطمئن میان کاربران طراحی کرده‌ایم.
                </p>
            </div>

            <!-- 3 Trust Pillars -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                
                <!-- Pillar 1: Privacy & Security -->
                <div class="bg-slate-50 p-8 rounded-3xl border border-slate-200/80 hover:border-emerald-300 transition">
                    <div class="w-14 h-14 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center text-2xl mb-6">
                        <i class="fa-solid fa-lock"></i>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-2">
                        🔒 امنیت و حفظ حریم خصوصی
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        اطلاعات هویتی و شماره تماس شما نزد سیستم محفوظ است. شماره‌های همراه در نمایش عمومی برای جلوگیری از تماس‌های ناخواسته ماسک می‌شوند.
                    </p>
                </div>

                <!-- Pillar 2: Verified Users via OTP -->
                <div class="bg-slate-50 p-8 rounded-3xl border border-slate-200/80 hover:border-teal-300 transition">
                    <div class="w-14 h-14 rounded-2xl bg-teal-100 text-teal-700 flex items-center justify-center text-2xl mb-6">
                        <i class="fa-solid fa-user-check"></i>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-2">
                        ✓ کاربران تأییدشده پیامکی
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        عضویت کلیه کاربران با احراز شماره همراه از طریق پیامک (SMS OTP) انجام می‌شود تا از فعالیت ربات‌ها و ثبت‌نام‌های نامعتبر جلوگیری شود.
                    </p>
                </div>

                <!-- Pillar 3: Content Moderation -->
                <div class="bg-slate-50 p-8 rounded-3xl border border-slate-200/80 hover:border-blue-300 transition">
                    <div class="w-14 h-14 rounded-2xl bg-blue-100 text-blue-700 flex items-center justify-center text-2xl mb-6">
                        <i class="fa-solid fa-shield-halved"></i>
                    </div>
                    <h3 class="text-xl font-black text-slate-900 mb-2">
                        🛡️ نظارت و مدیریت محتوا
                    </h3>
                    <p class="text-slate-600 text-sm leading-relaxed">
                        محتوای آگهی‌ها تحت سازوکارهای مدیریتی و نظارتی دقیق قرار دارد تا محیطی پاکیزه، محترمانه و قابل اعتماد برای همه همشهریان فراهم باشد.
                    </p>
                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 9. APP PREVIEW (پیش‌نمایش اپلیکیشن در جیب شما) -->
    <!-- ========================================================================= -->
    <section class="py-24 bg-gradient-to-b from-slate-900 via-slate-950 to-slate-900 text-white relative overflow-hidden">
        
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
                
                <!-- Left: Text & Features -->
                <div class="lg:col-span-6 space-y-6 text-center lg:text-right">
                    
                    <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-500/10 border border-brand-500/30 text-brand-400 text-xs font-bold">
                        <i class="fa-solid fa-mobile-screen"></i>
                        <span>نسخه بومی اندروید با Jetpack Compose</span>
                    </div>

                    <h2 class="text-3xl sm:text-4xl lg:text-5xl font-black leading-tight text-white">
                        بده بره را <span class="gradient-text-teal">توی جیبت</span> داشته باش.
                    </h2>

                    <p class="text-slate-300 text-base sm:text-lg leading-relaxed">
                        برای دیدن فرصت‌ها، پیدا کردن هدیه‌ها، باخبر شدن از بخش‌های «همین الان رایگان شد» و ساختن اتفاق‌های خوب، اپلیکیشن بده بره را نصب کن.
                    </p>

                    <!-- Feature List -->
                    <div class="space-y-3 pt-2 text-right">
                        <div class="flex items-center gap-3 bg-slate-800/50 p-3.5 rounded-2xl border border-slate-700/60">
                            <div class="w-8 h-8 rounded-xl bg-teal-500/20 text-teal-400 flex items-center justify-center shrink-0">
                                <i class="fa-solid fa-bolt text-sm"></i>
                            </div>
                            <span class="text-xs sm:text-sm text-slate-200">مشاهده لحظه‌ای هدایای تازه ثبت‌شده با تایمر دسترسی سریع</span>
                        </div>
                        <div class="flex items-center gap-3 bg-slate-800/50 p-3.5 rounded-2xl border border-slate-700/60">
                            <div class="w-8 h-8 rounded-xl bg-purple-500/20 text-purple-400 flex items-center justify-center shrink-0">
                                <i class="fa-solid fa-bell text-sm"></i>
                            </div>
                            <span class="text-xs sm:text-sm text-slate-200">اعلان‌های هوشمند هنگام تایید آگهی یا رزرو کالا</span>
                        </div>
                        <div class="flex items-center gap-3 bg-slate-800/50 p-3.5 rounded-2xl border border-slate-700/60">
                            <div class="w-8 h-8 rounded-xl bg-pink-500/20 text-pink-400 flex items-center justify-center shrink-0">
                                <i class="fa-solid fa-crown text-sm"></i>
                            </div>
                            <span class="text-xs sm:text-sm text-slate-200">پکیج‌های طلایی و الماس برای فعالان و کسب‌وکارهای تخفیف‌دهنده</span>
                        </div>
                    </div>

                    <div class="pt-4">
                        <a href="<?= htmlspecialchars($apkDownloadUrl) ?>" download class="inline-flex items-center gap-3 px-8 py-4 rounded-2xl bg-gradient-to-r from-brand-500 to-cyan-400 hover:from-brand-600 hover:to-cyan-500 text-slate-950 font-black text-base shadow-xl shadow-brand-500/20 hover:-translate-y-1 transition">
                            <i class="fa-brands fa-android text-2xl"></i>
                            <span>دانلود اپلیکیشن Android (نسخه <?= htmlspecialchars($appVersion) ?>)</span>
                        </a>
                    </div>

                </div>

                <!-- Right: Sleek Smartphone Mockups -->
                <div class="lg:col-span-6 flex items-center justify-center">
                    <div class="relative w-full max-w-sm sm:max-w-md">
                        
                        <!-- Phone Frame -->
                        <div class="bg-slate-900 border-4 border-slate-700 rounded-[44px] p-3 shadow-2xl shadow-cyan-500/10 relative">
                            
                            <!-- Phone Camera Notch / Dynamic Island -->
                            <div class="w-28 h-5 bg-slate-800 rounded-full mx-auto mb-3 flex items-center justify-center gap-2">
                                <div class="w-2 h-2 rounded-full bg-slate-900"></div>
                                <div class="w-2.5 h-2.5 rounded-full bg-slate-950"></div>
                            </div>

                            <!-- Mockup Screen UI -->
                            <div class="bg-slate-950 rounded-[34px] p-4 text-right space-y-4 overflow-hidden border border-slate-800">
                                
                                <!-- App Header Inside Screen -->
                                <div class="flex items-center justify-between border-b border-slate-800 pb-3">
                                    <div class="flex items-center gap-2">
                                        <div class="w-8 h-8 rounded-xl bg-gradient-to-tr from-brand-600 to-brand-400 flex items-center justify-center text-white text-xs">
                                            <i class="fa-solid fa-hand-holding-heart"></i>
                                        </div>
                                        <div>
                                            <span class="text-xs font-black text-white block">بده بره</span>
                                            <span class="text-[9px] text-slate-400">تهران، همه محله‌ها</span>
                                        </div>
                                    </div>
                                    <span class="text-[10px] bg-brand-500/20 text-brand-300 px-2 py-0.5 rounded-full font-bold">نسخه اصلی</span>
                                </div>

                                <!-- Screen Banner Inside Mockup -->
                                <div class="bg-gradient-to-r from-teal-600 to-cyan-600 rounded-2xl p-3.5 text-white shadow-md">
                                    <span class="text-[10px] font-bold bg-white/20 px-2 py-0.5 rounded-md inline-block mb-1">همین الان رایگان شد ⚡</span>
                                    <h5 class="text-xs font-black">میز تحریر چوبی و صندلی ارگونومیک</h5>
                                    <p class="text-[10px] text-teal-100 mt-0.5">آماده تحویل در پاسداران</p>
                                </div>

                                <!-- Mini Card 1 -->
                                <div class="bg-slate-900/90 rounded-xl p-3 border border-slate-800 flex items-center justify-between">
                                    <div class="space-y-0.5">
                                        <span class="text-xs font-bold text-white block">مجموعه کتاب‌های کنکور تجربی</span>
                                        <span class="text-[10px] text-emerald-400 font-bold">کاملاً رایگان (هدیه)</span>
                                    </div>
                                    <span class="text-[10px] bg-slate-800 text-slate-300 px-2.5 py-1 rounded-lg">رزرو هدیه</span>
                                </div>

                                <!-- Mini Card 2 -->
                                <div class="bg-slate-900/90 rounded-xl p-3 border border-slate-800 flex items-center justify-between">
                                    <div class="space-y-0.5">
                                        <span class="text-xs font-bold text-white block">۵۰٪ تخفیف دوره آموزش برنامه‌نویسی</span>
                                        <span class="text-[10px] text-amber-400 font-bold">تخفیف طلایی</span>
                                    </div>
                                    <span class="text-[10px] bg-amber-500/20 text-amber-300 px-2.5 py-1 rounded-lg">دریافت کوپن</span>
                                </div>

                                <!-- Bottom Navigation Bar in Phone -->
                                <div class="pt-2 border-t border-slate-800/80 flex items-center justify-around text-[10px] text-slate-400">
                                    <span class="text-teal-400 flex flex-col items-center"><i class="fa-solid fa-house mb-0.5"></i> خانه</span>
                                    <span class="flex flex-col items-center"><i class="fa-solid fa-magnifying-glass mb-0.5"></i> جستجو</span>
                                    <span class="flex flex-col items-center"><i class="fa-solid fa-plus-circle text-teal-400 text-sm mb-0.5"></i> ثبت</span>
                                    <span class="flex flex-col items-center"><i class="fa-solid fa-bell mb-0.5"></i> اعلان‌ها</span>
                                    <span class="flex flex-col items-center"><i class="fa-solid fa-user mb-0.5"></i> پروفایل</span>
                                </div>

                            </div>

                        </div>

                    </div>
                </div>

            </div>
        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 10. BIG DOWNLOAD CTA (دعوت نهایی به دانلود) -->
    <!-- ========================================================================= -->
    <section id="download" class="py-24 bg-gradient-to-b from-white to-brand-50/50 relative">
        <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div class="bg-gradient-to-tr from-brand-700 via-brand-600 to-cyan-600 rounded-[36px] p-8 sm:p-14 text-white text-center shadow-2xl shadow-brand-600/30 relative overflow-hidden">
                
                <!-- Floating Light Orbs -->
                <div class="absolute -top-20 -left-20 w-64 h-64 bg-white/10 rounded-full filter blur-2xl"></div>
                <div class="absolute -bottom-20 -right-20 w-64 h-64 bg-pink-500/20 rounded-full filter blur-2xl"></div>

                <div class="relative z-10 space-y-6 max-w-3xl mx-auto">
                    
                    <div class="w-16 h-16 rounded-2xl bg-white text-brand-600 flex items-center justify-center text-3xl mx-auto shadow-lg shadow-black/10">
                        <i class="fa-solid fa-hand-holding-heart"></i>
                    </div>

                    <h2 class="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight">
                        یک اتفاق خوب می‌تواند از همین‌جا شروع شود.
                    </h2>

                    <p class="text-base sm:text-lg text-teal-100 leading-relaxed font-medium">
                        اپلیکیشن بده بره را نصب کن و به جمع آدم‌هایی بپیوند که دوست دارند چیزی را با دیگری قسمت کنند.
                    </p>

                    <!-- Big CTA Download Button -->
                    <div class="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
                        <a href="<?= htmlspecialchars($apkDownloadUrl) ?>" download class="w-full sm:w-auto inline-flex items-center justify-center gap-4 px-10 py-5 rounded-2xl bg-white hover:bg-slate-100 text-slate-900 font-black text-lg shadow-2xl hover:shadow-white/20 hover:-translate-y-1 active:translate-y-0 transition-all duration-200 group">
                            <i class="fa-brands fa-android text-3xl text-brand-600 group-hover:scale-110 transition-transform"></i>
                            <div class="text-right">
                                <span class="block text-xs font-bold text-slate-500">فایل نصب مستقیم APK</span>
                                <span class="block text-xl font-black">دانلود اپلیکیشن بده بره</span>
                            </div>
                            <i class="fa-solid fa-cloud-arrow-down text-2xl text-brand-600 mr-2"></i>
                        </a>
                    </div>

                    <!-- Meta specs -->
                    <div class="flex flex-wrap items-center justify-center gap-6 text-xs text-teal-100 font-medium pt-2">
                        <span>نسخه <?= htmlspecialchars($appVersion) ?></span>
                        <span>•</span>
                        <span>حجم: <?= htmlspecialchars($appSize) ?></span>
                        <span>•</span>
                        <span>اندروید ۸.۰ به بالا</span>
                        <span>•</span>
                        <span>کاملاً رایگان</span>
                    </div>

                </div>

            </div>

        </div>
    </section>

    <!-- ========================================================================= -->
    <!-- 11. FOOTER (فوتر مینیمال و پاکیزه) -->
    <!-- ========================================================================= -->
    <footer class="bg-slate-950 text-slate-400 py-14 border-t border-slate-900 text-sm">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            
            <div class="grid grid-cols-1 md:grid-cols-12 gap-8 items-center pb-10 border-b border-slate-900">
                
                <!-- Logo & Short Bio -->
                <div class="md:col-span-5 space-y-3 text-center md:text-right">
                    <div class="flex items-center justify-center md:justify-start gap-3">
                        <div class="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center text-white text-lg font-black">
                            <i class="fa-solid fa-hand-holding-heart"></i>
                        </div>
                        <span class="text-xl font-black text-white">بـده بـره</span>
                    </div>
                    <p class="text-xs text-slate-500 leading-relaxed max-w-sm mx-auto md:mx-0">
                        پلتفرمی اجتماعی برای ایجاد فرصت‌های خوب، هدیه دادن وسایل سالم و باارزش و استفاده از تخفیف‌های ویژه همشهریان مهربان.
                    </p>
                </div>

                <!-- Footer Links -->
                <div class="md:col-span-7 flex flex-wrap items-center justify-center md:justify-end gap-6 text-xs font-semibold">
                    <button onclick="openModal('about-modal')" class="hover:text-white transition">درباره بده بره</button>
                    <button onclick="openModal('terms-modal')" class="hover:text-white transition">قوانین و شرایط</button>
                    <button onclick="openModal('privacy-modal')" class="hover:text-white transition">حریم خصوصی</button>
                    <button onclick="openModal('contact-modal')" class="hover:text-white transition">ارتباط و پشتیبانی</button>
                    <a href="<?= htmlspecialchars($apkDownloadUrl) ?>" download class="text-teal-400 hover:text-teal-300 font-bold transition">دانلود APK</a>
                </div>

            </div>

            <!-- Bottom Copyright -->
            <div class="pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-600 text-center sm:text-right">
                <p>© ۱۴۰۵ تمامی حقوق متعلق به سامانه اجتماعی «بده بره» است.</p>
                <p class="flex items-center gap-1.5 text-slate-500">
                    ساخته‌شده با <i class="fa-solid fa-heart text-rose-500 text-xs"></i> برای لبخند مردم ایران
                </p>
            </div>

        </div>
    </footer>

    <!-- ========================================================================= -->
    <!-- MODALS FOR FOOTER POLICIES (No page leave) -->
    <!-- ========================================================================= -->
    
    <!-- About Modal -->
    <div id="about-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 max-w-lg w-full text-right text-slate-300 space-y-4 shadow-2xl">
            <div class="flex items-center justify-between border-b border-slate-800 pb-4">
                <h3 class="text-lg font-black text-white">درباره پلتفرم بده بره</h3>
                <button onclick="closeModal('about-modal')" class="text-slate-400 hover:text-white text-xl"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <p class="text-sm leading-relaxed">
                «بده بره» با هدف گسترش فرهنگ بخشش و بازچرخانی کالاهای سالم و ایجاد فرصت‌های تخفیف میان شهروندان طراحی شده است. ما بر این باوریم که هر کالایی که دیگر برای شما کاربردی ندارد، می‌تواند برای شخص دیگری سرشار از ارزش و خوشحالی باشد.
            </p>
            <button onclick="closeModal('about-modal')" class="w-full py-2.5 rounded-xl bg-teal-600 text-white font-bold text-xs hover:bg-teal-500 transition">بستن</button>
        </div>
    </div>

    <!-- Terms Modal -->
    <div id="terms-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 max-w-lg w-full text-right text-slate-300 space-y-4 shadow-2xl">
            <div class="flex items-center justify-between border-b border-slate-800 pb-4">
                <h3 class="text-lg font-black text-white">قوانین و شرایط استفاده</h3>
                <button onclick="closeModal('terms-modal')" class="text-slate-400 hover:text-white text-xl"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <ul class="text-xs space-y-2 text-slate-400 list-disc list-inside leading-relaxed">
                <li>ثبت اقلام غیرمجاز، دارویی، اسلحه و مواد خطرناک اکیداً ممنوع است.</li>
                <li>کلیه اقلام اهدایی باید کاملاً رایگان و بدون هیچ‌گونه دریافت وجه واگذار شوند.</li>
                <li>رعایت ادب و کرامت انسانی در تعاملات میان کاربران الزامی است.</li>
                <li>مسئولیت صحت اطلاعات آگهی‌ها بر عهده ثبت‌کننده آن است.</li>
            </ul>
            <button onclick="closeModal('terms-modal')" class="w-full py-2.5 rounded-xl bg-teal-600 text-white font-bold text-xs hover:bg-teal-500 transition">متوجه شدم</button>
        </div>
    </div>

    <!-- Privacy Modal -->
    <div id="privacy-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 max-w-lg w-full text-right text-slate-300 space-y-4 shadow-2xl">
            <div class="flex items-center justify-between border-b border-slate-800 pb-4">
                <h3 class="text-lg font-black text-white">حریم خصوصی کاربران</h3>
                <button onclick="closeModal('privacy-modal')" class="text-slate-400 hover:text-white text-xl"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <p class="text-xs leading-relaxed text-slate-400">
                شماره موبایل و اطلاعات هویتی شما صرفاً جهت احراز هویت و امنیت حساب کاربری استفاده می‌شود و در اختیار هیچ سازمان یا شخص ثالثی قرار نخواهد گرفت. در نمایش عمومی آگهی‌ها، شماره تماس‌ها ماسک می‌شوند.
            </p>
            <button onclick="closeModal('privacy-modal')" class="w-full py-2.5 rounded-xl bg-teal-600 text-white font-bold text-xs hover:bg-teal-500 transition">بستن</button>
        </div>
    </div>

    <!-- Contact Modal -->
    <div id="contact-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 max-w-lg w-full text-right text-slate-300 space-y-4 shadow-2xl">
            <div class="flex items-center justify-between border-b border-slate-800 pb-4">
                <h3 class="text-lg font-black text-white">ارتباط و پشتیبانی</h3>
                <button onclick="closeModal('contact-modal')" class="text-slate-400 hover:text-white text-xl"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <p class="text-xs text-slate-400 leading-relaxed">
                برای ثبت پیشنهادات، انتقادات یا گزارش تخلفات می‌توانید از طریق بخش تیکت‌ها در داخل اپلیکیشن بده بره اقدام فرمایید.
            </p>
            <div class="bg-slate-950 p-4 rounded-xl border border-slate-800 text-xs space-y-2">
                <div class="flex items-center justify-between">
                    <span class="text-slate-500">پشتیبانی درون‌برنامه‌ای:</span>
                    <span class="text-teal-400 font-bold">بخش تیکت‌های اپلیکیشن</span>
                </div>
                <div class="flex items-center justify-between">
                    <span class="text-slate-500">سیستم عامل:</span>
                    <span class="text-slate-300">Android 8.0+</span>
                </div>
            </div>
            <button onclick="closeModal('contact-modal')" class="w-full py-2.5 rounded-xl bg-teal-600 text-white font-bold text-xs hover:bg-teal-500 transition">بستن</button>
        </div>
    </div>

    <script>
        function openModal(id) {
            const el = document.getElementById(id);
            if (el) {
                el.classList.remove('hidden');
                el.classList.add('flex');
            }
        }
        function closeModal(id) {
            const el = document.getElementById(id);
            if (el) {
                el.classList.add('hidden');
                el.classList.remove('flex');
            }
        }
    </script>

</body>
</html>
