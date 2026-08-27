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

-- Insert default admin (Username: admin, Password: admin123456_ChangeMe!)
-- Password hash generated using password_hash('admin123456_ChangeMe!', PASSWORD_BCRYPT)
INSERT INTO `admins` (`username`, `password_hash`, `full_name`, `role`) VALUES
('admin', '$2y$10$tZ2zW4iKk.1V6Qn36gUuMeE7Kx9Yk6Z0zS3l3u0eP5p7qQ3yX7n1u', 'مدیر ارشد سامانه', 'SUPER_ADMIN')
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
  `image_url` VARCHAR(255) DEFAULT NULL,
  `views_count` INT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN_KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN_KEY (`category_id`) REFERENCES `categories`(`id`)
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
  FOREIGN_KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
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
('site_title', 'سامانه بده بره', 'عنوان سامانه')
ON DUPLICATE KEY UPDATE `key_name`=`key_name`;

-- Insert sample users
INSERT INTO `users` (`phone`, `full_name`, `national_id`, `city`, `tier`) VALUES
('09121112233', 'سارا رضایی', '0012345678', 'تهران', 'GOLD'),
('09359876543', 'علیرضا حسینی', '0023456789', 'اصفهان', 'SILVER'),
('09193334455', 'مریم احمدی', '0034567890', 'مشهد', 'FREE'),
('09128887766', 'حمید کریمی', '0045678901', 'شیراز', 'FREE')
ON DUPLICATE KEY UPDATE `phone`=`phone`;

-- Insert sample listings
INSERT INTO `listings` (`user_id`, `title`, `description`, `category_id`, `type`, `status`, `city`, `approximate_location`, `visibility_tier`) VALUES
(1, 'میز تحریر چوبی کاملاً نو', 'یک عدد میز تحریر سالم و شیک مناسب برای دانش‌آموزان یا دانشجویان به همراه صندلی', 'fg_furniture', 'FREE_GIFT', 'APPROVED', 'تهران', 'سعادت‌آباد', 'GOLD'),
(2, 'کتاب‌های جامع کنکور تجربی ۱۴۰۴', 'مجموعه کتاب‌های زیست، شیمی و فیزیک با تست‌های طبقه‌بندی شده تمیز و سیمی‌شده', 'fg_books', 'FREE_GIFT', 'APPROVED', 'اصفهان', 'چهارباغ', 'SILVER'),
(3, 'کوپن ۵۰٪ تخفیف اسنپ‌فود تا سقف ۶۰ هزار تومان', 'کد تخفیف اختصاصی سفارش غذا از تمامی رستوران‌ها', 'dc_food', 'DISCOUNT', 'APPROVED', 'مشهد', 'سراسری', 'FREE'),
(4, 'نیاز به کالسکه کودک برای خانواده نیازمند', 'برای فرزند یکی از بستگان نیاز به یک کالسکه ساده و سالم داریم', 'rq_kids', 'REQUEST', 'PENDING', 'شیراز', 'معالی‌آباد', 'FREE');

-- Insert sample transactions
INSERT INTO `transactions` (`user_id`, `amount_toman`, `plan_name`, `zarinpal_authority`, `zarinpal_ref_id`, `status`) VALUES
(1, 99000, 'اشتراک طلایی ۳۰ روزه', 'A00000000000000000000000000000000001', '109823412', 'SUCCESS'),
(2, 49000, 'اشتراک نقره‌ای ۳۰ روزه', 'A00000000000000000000000000000000002', '109823413', 'SUCCESS');

SET FOREIGN_KEY_CHECKS = 1;
