-- Database schema for BedeBere CPanel Hosting
-- Character set: utf8mb4_persian_ci

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Table: admins
CREATE TABLE IF NOT EXISTS `admins` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(60) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `role` ENUM('SUPER_ADMIN', 'MODERATOR', 'FINANCE') DEFAULT 'SUPER_ADMIN',
  `last_login` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- Insert default admin (Username: mohamad.meftah@gmail.com, Password: Meftah9260)
INSERT INTO `admins` (`username`, `password_hash`, `full_name`, `role`) VALUES
('mohamad.meftah@gmail.com', '$2y$10$8u4G3K1iG7iG0L7yN5r7.e6O9J5T3Y6p1v8W2x4z9m0K1L2m3N4O5', 'محمد مفتاح (مدیر ارشد)', 'SUPER_ADMIN')
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 2. Table: users
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `phone` VARCHAR(15) NOT NULL UNIQUE,
  `full_name` VARCHAR(100) NOT NULL,
  `national_id` VARCHAR(10) DEFAULT NULL,
  `city` VARCHAR(50) DEFAULT 'تهران',
  `province` VARCHAR(50) DEFAULT 'تهران',
  `tier` ENUM('FREE', 'SILVER', 'GOLD', 'DIAMOND') DEFAULT 'FREE',
  `tier_expires_at` DATETIME DEFAULT NULL,
  `is_banned` TINYINT(1) DEFAULT 0,
  `ban_reason` TEXT DEFAULT NULL,
  `can_post_listing` TINYINT(1) DEFAULT 1,
  `wallet_balance` BIGINT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 3. Table: categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` VARCHAR(50) PRIMARY KEY,
  `parent_id` VARCHAR(50) DEFAULT NULL,
  `name_fa` VARCHAR(100) NOT NULL,
  `icon_name` VARCHAR(50) NOT NULL DEFAULT 'card_giftcard',
  `type` ENUM('FREE_GIFT', 'DISCOUNT', 'REQUEST') NOT NULL,
  `is_locked` TINYINT(1) DEFAULT 0,
  `lock_message` VARCHAR(255) DEFAULT 'ثبت آگهی در این بخش موقتاً غیرفعال است.',
  `display_order` INT DEFAULT 0,
  FOREIGN KEY (`parent_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- Insert rich default parent & child categories
INSERT INTO `categories` (`id`, `parent_id`, `name_fa`, `icon_name`, `type`, `display_order`) VALUES
-- 1. Main Parents
('cat_books', NULL, 'کتاب، جزوه و نشریات', 'menu_book', 'FREE_GIFT', 1),
('cat_home', NULL, 'لوازم خانه و آشپزخانه', 'kitchen', 'FREE_GIFT', 2),
('cat_personal', NULL, 'وسایل شخصی و پوشاک', 'checkroom', 'FREE_GIFT', 3),
('cat_tools', NULL, 'ابزارآلات و تجهیزات فنی', 'build', 'FREE_GIFT', 4),
('cat_digital', NULL, 'کالای دیجیتال و الکترونیک', 'devices', 'FREE_GIFT', 5),
('cat_kids', NULL, 'کودک و اسباب‌بازی', 'toys', 'FREE_GIFT', 6),
('cat_vehicles', NULL, 'وسایل نقلیه و یدکی', 'directions_car', 'FREE_GIFT', 7),
('cat_food', NULL, 'خوراکی و نذورات', 'restaurant', 'FREE_GIFT', 8),
('cat_discounts', NULL, 'کوپن‌ها و تخفیف‌های ویژه', 'local_offer', 'DISCOUNT', 9),
('cat_requests', NULL, 'درخواست‌های یاری و نیازمندی‌ها', 'help_outline', 'REQUEST', 10),

-- Subcategories under Books
('cat_books_school', 'cat_books', 'کتاب‌های درسی و کنکور', 'school', 'FREE_GIFT', 11),
('cat_books_uni', 'cat_books', 'کتاب‌های دانشگاهی و جزوه', 'menu_book', 'FREE_GIFT', 12),
('cat_books_novel', 'cat_books', 'رمان، داستان و شعر', 'auto_stories', 'FREE_GIFT', 13),
('cat_books_kids', 'cat_books', 'کتاب و مجله کودک', 'child_care', 'FREE_GIFT', 14),

-- Subcategories under Home
('cat_home_furniture', 'cat_home', 'مبلمان، کاناپه و میز', 'chair', 'FREE_GIFT', 21),
('cat_home_appliances', 'cat_home', 'لوازم برقی و آشپزخانه', 'kitchen', 'FREE_GIFT', 22),
('cat_home_dishes', 'cat_home', 'ظروف و وسایل پذیرایی', 'restaurant', 'FREE_GIFT', 23),
('cat_home_decor', 'cat_home', 'دکوراسیون، فرش و پرده', 'weekend', 'FREE_GIFT', 24),

-- Subcategories under Personal
('cat_clothing_adult', 'cat_personal', 'پوشاک مردانه و زنانه', 'checkroom', 'FREE_GIFT', 31),
('cat_clothing_shoes', 'cat_personal', 'کیف، کفش و کمربند', 'shopping_bag', 'FREE_GIFT', 32),
('cat_personal_accessories', 'cat_personal', 'ساعت، اکسسوری و عینک', 'watch', 'FREE_GIFT', 33),

-- Subcategories under Tools
('cat_tools_manual', 'cat_tools', 'ابزار دستی، آچار و انبر', 'handyman', 'FREE_GIFT', 41),
('cat_tools_electric', 'cat_tools', 'ابزار برقی، دریل و سنگ فرز', 'build', 'FREE_GIFT', 42),
('cat_tools_garden', 'cat_tools', 'لوازم باغبانی و گلخانه', 'yard', 'FREE_GIFT', 43),

-- Subcategories under Digital
('cat_digital_mobile', 'cat_digital', 'موبایل، تبلت و شارژر', 'smartphone', 'FREE_GIFT', 51),
('cat_digital_pc', 'cat_digital', 'کامپیوتر، مانیتور و لپ‌تاپ', 'computer', 'FREE_GIFT', 52),
('cat_digital_audio', 'cat_digital', 'هدفون، اسپیکر و صوتی', 'headphones', 'FREE_GIFT', 53),

-- Subcategories under Kids
('cat_kids_toys', 'cat_kids', 'اسباب‌بازی و سرگرمی', 'toys', 'FREE_GIFT', 61),
('cat_kids_stroller', 'cat_kids', 'کالسکه، کریر و سیسمونی', 'child_friendly', 'FREE_GIFT', 62),

-- Subcategories under Discounts
('cat_disc_food', 'cat_discounts', 'تخفیف رستوران و کافه', 'restaurant', 'DISCOUNT', 71),
('cat_disc_shop', 'cat_discounts', 'تخفیف خرید اینترنتی', 'shopping_cart', 'DISCOUNT', 72),
('cat_disc_course', 'cat_discounts', 'تخفیف دوره‌های آموزشی', 'school', 'DISCOUNT', 73)
ON DUPLICATE KEY UPDATE `name_fa`=VALUES(`name_fa`);

-- 4. Table: listings
CREATE TABLE IF NOT EXISTS `listings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `category_id` VARCHAR(50) NOT NULL,
  `type` ENUM('FREE_GIFT', 'DISCOUNT', 'REQUEST') NOT NULL,
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'RESERVED', 'EXPIRED') DEFAULT 'APPROVED',
  `rejection_reason` TEXT DEFAULT NULL,
  `city` VARCHAR(50) NOT NULL,
  `approximate_location` VARCHAR(100) DEFAULT NULL,
  `visibility_tier` ENUM('FREE', 'SILVER', 'GOLD', 'DIAMOND') DEFAULT 'FREE',
  `is_reserved` TINYINT(1) DEFAULT 0,
  `reserved_by_user_id` INT DEFAULT NULL,
  `discount_type` VARCHAR(30) DEFAULT NULL,
  `discount_percentage` INT DEFAULT NULL,
  `discount_amount_toman` BIGINT DEFAULT NULL,
  `discount_code` VARCHAR(50) DEFAULT NULL,
  `image_url` MEDIUMTEXT DEFAULT NULL,
  `views_count` INT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 5. Table: transactions
CREATE TABLE IF NOT EXISTS `transactions` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `amount_toman` BIGINT NOT NULL,
  `plan_name` VARCHAR(50) NOT NULL,
  `zarinpal_authority` VARCHAR(60) DEFAULT NULL,
  `zarinpal_ref_id` VARCHAR(60) DEFAULT NULL,
  `status` ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
  `card_pan` VARCHAR(30) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 6. Table: forbidden_words
CREATE TABLE IF NOT EXISTS `forbidden_words` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `word` VARCHAR(100) NOT NULL UNIQUE,
  `action_type` ENUM('BLOCK', 'FLAG_FOR_REVIEW') DEFAULT 'BLOCK',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- Insert sample forbidden words
INSERT INTO `forbidden_words` (`word`, `action_type`) VALUES
('قمار', 'BLOCK'),
('شرط‌بندی', 'BLOCK'),
('سلاح', 'BLOCK'),
('مواد مخدر', 'BLOCK'),
('سیگار الکترونیکی', 'FLAG_FOR_REVIEW'),
('داروهای غیرمجاز', 'BLOCK')
ON DUPLICATE KEY UPDATE `word`=`word`;

-- 7. Table: settings
CREATE TABLE IF NOT EXISTS `settings` (
  `key_name` VARCHAR(60) PRIMARY KEY,
  `key_value` TEXT NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- Insert default system settings
INSERT INTO `settings` (`key_name`, `key_value`, `description`) VALUES
('zarinpal_merchant_id', '00000000-0000-0000-0000-000000000000', 'کد درگاه زرین‌پال'),
('zarinpal_sandbox', '1', 'حالت آزمایشی زرین‌پال (1=فعال، 0=غیرفعال)'),
('silver_plan_price', '49000', 'قیمت اشتراک نقره‌ای به تومان'),
('gold_plan_price', '99000', 'قیمت اشتراک طلایی به تومان'),
('diamond_plan_price', '149000', 'قیمت اشتراک الماس به تومان'),
('gold_early_access_hours', '2', 'مدت زمان دسترسی زودهنگام پکیج طلایی به ساعت'),
('silver_early_access_hours', '1', 'مدت زمان دسترسی زودهنگام پکیج نقره‌ای به ساعت'),
('diamond_early_access_hours', '2', 'مدت زمان دسترسی زودهنگام پکیج الماس به ساعت'),
('require_diamond_for_discounts', '1', 'الزام خرید پکیج الماس برای ثبت کوپن و کد تخفیف (1=فعال، 0=غیرفعال)'),
('free_daily_reserve_limit', '3', 'سقف مجاز رزرو روزانه برای کاربران عادی'),
('silver_daily_reserve_limit', '8', 'سقف مجاز رزرو روزانه برای کاربران نقره‌ای'),
('gold_daily_reserve_limit', '15', 'سقف مجاز رزرو روزانه برای کاربران طلایی'),
('diamond_daily_reserve_limit', '25', 'سقف مجاز رزرو روزانه برای کاربران الماس'),
('site_title', 'سامانه بده بره', 'عنوان سامانه'),
('support_phone', '021-88889260', 'شماره تماس پشتیبانی'),
('support_email', 'admin@bedebere.ir', 'ایمیل پشتیبانی'),
('support_telegram', '@bedebere_admin', 'شناسه تلگرام پشتیبانی'),
('support_hours', 'پاسخگویی سریع ۲۴ ساعته', 'ساعات پاسخگویی پشتیبانی'),
('just_free_hours', '24', 'بازه زمانی نمایش در بخش همین الان رایگان شد (ساعت)')
ON DUPLICATE KEY UPDATE `key_name`=`key_name`;

-- 8. Table: banners (Dynamic page banners managed from admin)
CREATE TABLE IF NOT EXISTS `banners` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `page` VARCHAR(50) NOT NULL, -- 'home', 'free_gift', 'discount', 'request', 'vip', 'notifications', 'profile'
  `title` VARCHAR(200) NOT NULL,
  `subtitle` VARCHAR(255) NOT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `action_url` VARCHAR(255) DEFAULT NULL,
  `badge_text` VARCHAR(50) DEFAULT 'بده بره',
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

INSERT INTO `banners` (`page`, `title`, `subtitle`, `badge_text`, `image_url`) VALUES
('home', 'بده بره، مهربونی رو تکثیر کن 🌱', 'وسایلی که نیاز نداری رو به بقیه ببخش و دنیای قشنگ‌تری بساز', 'مهربانی ماندگار', 'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?w=800&auto=format&fit=crop&q=80'),
('free_gift', 'هدیه‌های بدون هزینه و کارآمد 🎁', 'کتاب، لوازم منزل، وسایل دیجیتال و هر چیزی که لازم نداری', 'بخش رایگان', 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800&auto=format&fit=crop&q=80'),
('discount', 'کوپن‌ها و بن‌های تخفیف باورنکردنی 🏷️', 'تخفیف‌های ویژه رستوران، پوشاک، دوره‌های آموزشی و فروشگاه‌ها', 'کوپن‌های ویژه', 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&auto=format&fit=crop&q=80'),
('request', 'دیوار نیاز و یاری‌رسانی 🤝', 'اگر نیازمند وسیله‌ای هستی یا می‌خوای گره‌ای باز کنی', 'یاری‌رسانی', 'https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=800&auto=format&fit=crop&q=80'),
('vip', 'اشتراک‌های ویژه VIP و دسترسی زودهنگام 💎', 'مشاهده آگهی‌ها ۲ ساعت زودتر از عموم و رزرو نامحدود', 'دسترسی سریع', 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&auto=format&fit=crop&q=80')
ON DUPLICATE KEY UPDATE `page`=`page`;

-- 9. Table: tickets (Contact admin messages)
CREATE TABLE IF NOT EXISTS `tickets` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `subject` VARCHAR(200) NOT NULL,
  `message` TEXT NOT NULL,
  `admin_reply` TEXT DEFAULT NULL,
  `status` ENUM('OPEN', 'ANSWERED', 'CLOSED') DEFAULT 'OPEN',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 10. Table: otp_codes (Mobile OTP Verification for Registration)
CREATE TABLE IF NOT EXISTS `otp_codes` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `mobile` VARCHAR(15) NOT NULL,
  `code_hash` CHAR(64) NOT NULL,
  `attempts` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `is_used` TINYINT(1) NOT NULL DEFAULT 0,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_otp_mobile_created` (`mobile`, `created_at`),
  INDEX `idx_otp_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 11. Table: registration_tokens (Temporary tokens for verified mobile registration)
CREATE TABLE IF NOT EXISTS `registration_tokens` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `mobile` VARCHAR(15) NOT NULL,
  `token_hash` CHAR(64) NOT NULL,
  `is_used` TINYINT(1) NOT NULL DEFAULT 0,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_reg_token` (`token_hash`),
  INDEX `idx_reg_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- 12. Table: otp_rate_limits (IP & Mobile Rate Limiting)
CREATE TABLE IF NOT EXISTS `otp_rate_limits` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `ip_address` VARCHAR(45) NOT NULL,
  `mobile` VARCHAR(15) DEFAULT NULL,
  `action` VARCHAR(30) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_rate_ip` (`ip_address`, `created_at`),
  INDEX `idx_rate_mobile` (`mobile`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_persian_ci;

-- Insert sample users
INSERT INTO `users` (`phone`, `full_name`, `national_id`, `city`, `tier`) VALUES
('09121112233', 'علی رضایی', '0012345678', 'تهران', 'GOLD'),
('09359876543', 'علیرضا حسینی', '0023456789', 'اصفهان', 'SILVER'),
('09193334455', 'مریم احمدی', '0034567890', 'مشهد', 'FREE'),
('09128887766', 'حمید کریمی', '0045678901', 'شیراز', 'FREE')
ON DUPLICATE KEY UPDATE `phone`=`phone`;

-- Insert sample listings
INSERT INTO `listings` (`user_id`, `title`, `description`, `category_id`, `type`, `status`, `city`, `approximate_location`, `visibility_tier`, `image_url`) VALUES
(1, 'میز تحریر چوبی کاملاً نو', 'یک عدد میز تحریر سالم و شیک مناسب برای دانش‌آموزان یا دانشجویان به همراه صندلی', 'cat_home_furniture', 'FREE_GIFT', 'APPROVED', 'تهران', 'سعادت‌آباد', 'GOLD', 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800&auto=format&fit=crop&q=80'),
(2, 'کتاب‌های جامع کنکور تجربی ۱۴۰۴', 'مجموعه کتاب‌های زیست، شیمی و فیزیک با تست‌های طبقه‌بندی شده تمیز و سیمی‌شده', 'cat_books', 'FREE_GIFT', 'APPROVED', 'اصفهان', 'چهارباغ', 'SILVER', 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800&auto=format&fit=crop&q=80'),
(3, 'کوپن ۵۰٪ تخفیف اسنپ‌فود تا سقف ۶۰ هزار تومان', 'کد تخفیف اختصاصی سفارش غذا از تمامی رستوران‌ها', 'cat_disc_food', 'DISCOUNT', 'APPROVED', 'مشهد', 'سراسری', 'FREE', 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop&q=80'),
(4, 'نیاز به کالسکه کودک برای خانواده نیازمند', 'برای فرزند یکی از بستگان نیاز به یک کالسکه ساده و سالم داریم', 'cat_requests', 'REQUEST', 'PENDING', 'شیراز', 'معالی‌آباد', 'FREE', 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=800&auto=format&fit=crop&q=80');

-- Insert sample transactions
INSERT INTO `transactions` (`user_id`, `amount_toman`, `plan_name`, `zarinpal_authority`, `zarinpal_ref_id`, `status`) VALUES
(1, 99000, 'اشتراک طلایی ۳۰ روزه', 'A00000000000000000000000000000000001', '109823412', 'SUCCESS'),
(2, 49000, 'اشتراک نقره‌ای ۳۰ روزه', 'A00000000000000000000000000000000002', '109823413', 'SUCCESS');

SET FOREIGN_KEY_CHECKS = 1;
