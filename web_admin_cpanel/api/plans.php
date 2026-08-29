<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../config.php';

try {
    $plans = [
        [
            'tier' => 'FREE',
            'title' => 'عضویت عادی (همیار رایگان)',
            'price_toman' => 0,
            'duration_days' => 365,
            'early_access_hours' => 0,
            'daily_reserve_limit' => (int)getSetting('free_daily_reserve_limit', '3'),
            'description' => 'مشاهده و دریافت رایگان وسایل اهدایی در زمان انتشار عمومی، سقف ۳ رزرو روزانه'
        ],
        [
            'tier' => 'SILVER',
            'title' => 'عضویت نقره‌ای',
            'price_toman' => (int)getSetting('silver_plan_price', '49000'),
            'duration_days' => 30,
            'early_access_hours' => (int)getSetting('silver_early_access_hours', '1'),
            'daily_reserve_limit' => (int)getSetting('silver_daily_reserve_limit', '8'),
            'description' => '۱ ساعت دسترسی زودهنگام به تمام آگهی‌های اهدایی نو و داغ، سقف ۸ رزرو روزانه'
        ],
        [
            'tier' => 'GOLD',
            'title' => 'عضویت طلایی (VIP)',
            'price_toman' => (int)getSetting('gold_plan_price', '99000'),
            'duration_days' => 30,
            'early_access_hours' => (int)getSetting('gold_early_access_hours', '2'),
            'daily_reserve_limit' => (int)getSetting('gold_daily_reserve_limit', '15'),
            'description' => '۲ ساعت دسترسی زودهنگام اولویت‌دار به تمامی هدایا و لوازم، سقف ۱۵ رزرو روزانه'
        ],
        [
            'tier' => 'DIAMOND',
            'title' => 'عضویت الماس (کسب‌وکارها و فروشگاه‌ها)',
            'price_toman' => (int)getSetting('diamond_plan_price', '149000'),
            'duration_days' => 30,
            'early_access_hours' => (int)getSetting('diamond_early_access_hours', '2'),
            'daily_reserve_limit' => (int)getSetting('diamond_daily_reserve_limit', '25'),
            'description' => 'ثبت نامحدود کوپن‌ها و کدهای تخفیف اختصاصی، نمایش نشان ویژه الماس، ۲۵ رزرو روزانه'
        ]
    ];

    jsonResponse('success', 'پلن‌های عضویت با موفقیت دریافت شد.', $plans);
} catch (Exception $e) {
    jsonError('خطا در دریافت پلن‌ها: ' . $e->getMessage(), 'SERVER_ERROR', 500);
}
