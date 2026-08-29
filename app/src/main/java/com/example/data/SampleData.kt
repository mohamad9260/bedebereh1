package com.example.data

import com.example.domain.model.Category
import com.example.domain.model.City
import com.example.domain.model.ListingType

object IranLocationsData {
  val ALL_CITIES_OPTION = City(name = "همه شهرها", province = "تمام ایران")

  val provincesWithCities: Map<String, List<String>> = mapOf(
    "تهران" to listOf("تهران", "شهریار", "اسلامشهر", "ورامین", "ری", "پردیس", "دماوند", "رباط‌کریم", "پاکدشت", "قدس", "ملارد", "فیروزکوه"),
    "اصفهان" to listOf("اصفهان", "کاشان", "خمینی‌شهر", "نجف‌آباد", "شاهین‌شهر", "فولادشهر", "مبارکه", "شهرضا", "گلپایگان", "نائین"),
    "خراسان رضوی" to listOf("مشهد", "نیشابور", "سبزوار", "تربت حیدریه", "قوچان", "کاشمر", "گناباد", "تربت جام", "چناران"),
    "فارس" to listOf("شیراز", "مرودشت", "جهرم", "فسا", "کازرون", "لار", "داراب", "آباده", "اقلید", "نورآباد"),
    "آذربایجان شرقی" to listOf("تبریز", "مراغه", "مرند", "میانه", "اهر", "بناب", "سراب", "شبستر", "آذرشهر"),
    "البرز" to listOf("کرج", "فردیس", "کمال‌شهر", "نظرآباد", "محمدشهر", "هشتگرد", "اشتهارد", "طالقان"),
    "خوزستان" to listOf("اهواز", "دزفول", "آبادان", "خرمشهر", "ماهشهر", "شوشتر", "اندیمشک", "بهبهان", "ایذه", "مسجد سلیمان"),
    "قم" to listOf("قم", "قنوات", "جعفریه", "کهک", "دستجرد"),
    "گیلان" to listOf("رشت", "بندر انزلی", "لاهیجان", "لنگرود", "تالش", "فومن", "آستارا", "صومعه‌سرا", "رودسر"),
    "مازندران" to listOf("ساری", "بابل", "آمل", "قائم‌شهر", "بابلسر", "چالوس", "تنکابن", "نوشهر", "رامسر", "محمودآباد"),
    "کرمانشاه" to listOf("کرمانشاه", "اسلام‌آباد غرب", "کنگاور", "جوانرود", "سنقر", "هرسین", "صحنه", "پاوه"),
    "آذربایجان غربی" to listOf("ارومیه", "خوی", "بوکان", "مهاباد", "میاندوآب", "سلماس", "پیرانشهر", "نقده"),
    "یزد" to listOf("یزد", "میبد", "اردکان", "بافق", "مهریز", "ابرکوه", "تفت"),
    "کرمان" to listOf("کرمان", "سیرجان", "رفسنجان", "جیرفت", "بم", "زرند", "کهنوج"),
    "مرکزی" to listOf("اراک", "ساوه", "خمین", "محلات", "دلیجان", "تفرش", "شازند"),
    "هرمزگان" to listOf("بندرعباس", "قشم", "کیش", "میناب", "بندرلنگه", "حاجی‌آباد", "جاسک"),
    "همدان" to listOf("همدان", "ملایر", "نهاوند", "تویسرکان", "کبودرآهنگ", "اسدآباد", "بهار"),
    "قزوین" to listOf("قزوین", "الوند", "تاکستان", "محمدیه", "بوئین‌زهرا", "آبیک"),
    "کردستان" to listOf("سنندج", "سقز", "مریوان", "بانه", "قروه", "کامیاران", "بیجار"),
    "لرستان" to listOf("خرم‌آباد", "بروجرد", "دورود", "کوهدشت", "الیگودرز", "نورآباد", "پلدختر"),
    "بوشهر" to listOf("بوشهر", "برازجان", "گناوه", "کنگان", "عسلویه", "خورموج", "دیلم"),
    "زنجان" to listOf("زنجان", "ابهر", "خرمدره", "قیدار", "طارم"),
    "سیستان و بلوچستان" to listOf("زاهدان", "چابهار", "زابل", "ایرانشهر", "سراوان", "خاش"),
    "گلستان" to listOf("گرگان", "گنبد کاووس", "علی‌آباد کتول", "بندر ترکمن", "کلاله", "آق‌قلا"),
    "اردبیل" to listOf("اردبیل", "پارس‌آباد", "مشگین‌شهر", "خلخال", "گرمی"),
    "سمنان" to listOf("سمنان", "شاهرود", "دامغان", "گرمسار", "مهدی‌شهر"),
    "ایلام" to listOf("ایلام", "دهلران", "ایوان", "آبدانان", "مهران"),
    "چهارمحال و بختیاری" to listOf("شهرکرد", "بروجن", "لردگان", "فارسان", "سامان"),
    "خراسان جنوبی" to listOf("بیرجند", "قائن", "فردوس", "طبس", "نهبندان"),
    "خراسان شمالی" to listOf("بجنورد", "شیروان", "اسفراین", "آشخانه", "جاجرم"),
    "کهگیلویه و بویراحمد" to listOf("یاسوج", "دوگنبدان (گچساران)", "دهدشت", "سی‌سخت", "چرام")
  )

  val provinces: List<String> by lazy {
    provincesWithCities.keys.toList()
  }

  fun getCitiesForProvince(province: String): List<String> {
    return provincesWithCities[province] ?: listOf(province)
  }

  val allCities: List<City> by lazy {
    val list = mutableListOf(ALL_CITIES_OPTION)
    provincesWithCities.forEach { (province, cities) ->
      cities.forEach { city ->
        list.add(City(name = city, province = province))
      }
    }
    list
  }
}

object CategoryData {
  val categories: List<Category> = listOf(
    // 1. کتاب و آموزش
    Category("cat_books", "کتاب، جزوه و نشریات", ListingType.FREE_GIFT, "menu_book"),
    Category("cat_books_school", "کتاب‌های درسی و کنکور", ListingType.FREE_GIFT, "school", "cat_books"),
    Category("cat_books_uni", "کتاب‌های دانشگاهی و تخصصی", ListingType.FREE_GIFT, "menu_book", "cat_books"),
    Category("cat_books_novel", "رمان، داستان و ادبیات", ListingType.FREE_GIFT, "auto_stories", "cat_books"),
    Category("cat_books_kids", "کتاب و مجله کودک", ListingType.FREE_GIFT, "child_care", "cat_books"),

    // 2. لوازم خانه و آشپزخانه
    Category("cat_home", "لوازم خانه و آشپزخانه", ListingType.FREE_GIFT, "kitchen"),
    Category("cat_home_furniture", "مبلمان، کاناپه و میز", ListingType.FREE_GIFT, "chair", "cat_home"),
    Category("cat_home_appliances", "لوازم برقی و آشپزخانه", ListingType.FREE_GIFT, "kitchen", "cat_home"),
    Category("cat_home_dishes", "ظروف و وسایل پذیرایی", ListingType.FREE_GIFT, "restaurant", "cat_home"),
    Category("cat_home_decor", "دکوراسیون، فرش و روشنایی", ListingType.FREE_GIFT, "weekend", "cat_home"),

    // 3. وسایل شخصی و پوشاک
    Category("cat_personal", "وسایل شخصی و پوشاک", ListingType.FREE_GIFT, "checkroom"),
    Category("cat_clothing_adult", "پوشاک مردانه و زنانه", ListingType.FREE_GIFT, "checkroom", "cat_personal"),
    Category("cat_clothing_shoes", "کیف، کفش و کمربند", ListingType.FREE_GIFT, "shopping_bag", "cat_personal"),
    Category("cat_personal_accessories", "ساعت، عینک و اکسسوری", ListingType.FREE_GIFT, "watch", "cat_personal"),

    // 4. ابزارآلات و تجهیزات فنی
    Category("cat_tools", "ابزارآلات و تجهیزات فنی", ListingType.FREE_GIFT, "build"),
    Category("cat_tools_manual", "ابزار دستی، آچار و انبر", ListingType.FREE_GIFT, "handyman", "cat_tools"),
    Category("cat_tools_electric", "ابزار برقی، دریل و سنگ فرز", ListingType.FREE_GIFT, "build", "cat_tools"),
    Category("cat_tools_garden", "لوازم باغبانی و کشاورزی", ListingType.FREE_GIFT, "yard", "cat_tools"),

    // 5. کالای دیجیتال و الکترونیک
    Category("cat_digital", "کالای دیجیتال و الکترونیک", ListingType.FREE_GIFT, "devices"),
    Category("cat_digital_mobile", "موبایل، تبلت و لوازم جانبی", ListingType.FREE_GIFT, "smartphone", "cat_digital"),
    Category("cat_digital_pc", "کامپیوتر، مانیتور و لپ‌تاپ", ListingType.FREE_GIFT, "computer", "cat_digital"),
    Category("cat_digital_audio", "هدفون، اسپیکر و صوتی", ListingType.FREE_GIFT, "headphones", "cat_digital"),

    // 6. کودک و اسباب‌بازی
    Category("cat_kids", "کودک و اسباب‌بازی", ListingType.FREE_GIFT, "toys"),
    Category("cat_kids_toys", "اسباب‌بازی و سرگرمی", ListingType.FREE_GIFT, "toys", "cat_kids"),
    Category("cat_kids_stroller", "کالسکه، کریر و سیسمونی", ListingType.FREE_GIFT, "child_friendly", "cat_kids"),

    // 7. وسایل نقلیه و یدکی
    Category("cat_vehicles", "وسایل نقلیه و یدکی", ListingType.FREE_GIFT, "directions_car"),
    Category("cat_vehicles_bicycle", "دوچرخه، اسکیت و اسکوتر", ListingType.FREE_GIFT, "directions_bike", "cat_vehicles"),
    Category("cat_vehicles_car_parts", "قطعات و لوازم جانبی خودرو", ListingType.FREE_GIFT, "directions_car", "cat_vehicles"),

    // 8. خوراکی و نذورات
    Category("cat_food", "خوراکی و نذورات", ListingType.FREE_GIFT, "restaurant"),
    Category("cat_food_charity", "بسته‌های ارزاق و نذورات", ListingType.FREE_GIFT, "card_giftcard", "cat_food"),

    // 9. کوپن‌ها و تخفیف‌ها
    Category("cat_discounts", "کوپن‌ها و تخفیف‌ها", ListingType.DISCOUNT, "local_offer"),
    Category("dc_online", "فروشگاه‌های اینترنتی", ListingType.DISCOUNT, "shopping_bag", "cat_discounts"),
    Category("dc_food", "رستوران و کافه", ListingType.DISCOUNT, "restaurant", "cat_discounts"),
    Category("dc_travel", "سفر و تاکسی اینترنتی", ListingType.DISCOUNT, "flight", "cat_discounts"),
    Category("dc_course", "دوره‌های آموزشی و وبینار", ListingType.DISCOUNT, "school", "cat_discounts"),

    // 10. درخواست‌ها و نیازمندی‌ها
    Category("cat_requests", "درخواست‌های یاری و نیازمندی‌ها", ListingType.REQUEST, "help_outline"),
    Category("rq_study", "میز و لوازم مطالعه", ListingType.REQUEST, "menu_book", "cat_requests"),
    Category("rq_home", "وسایل ضروری منزل", ListingType.REQUEST, "home", "cat_requests"),
    Category("rq_kids", "لوازم تحریر و کودک", ListingType.REQUEST, "child_care", "cat_requests"),
    Category("rq_medical", "تجهیزات درمانی و بهداشتی", ListingType.REQUEST, "healing", "cat_requests"),
    Category("rq_tools", "ابزار کار و فنی", ListingType.REQUEST, "build", "cat_requests")
  )

  fun getForType(type: ListingType, parentOnly: Boolean = false): List<Category> {
    return categories.filter {
      it.type == type && (!parentOnly || it.parentId == null)
    }
  }

  fun getChildren(parentId: String): List<Category> {
    return categories.filter { it.parentId == parentId }
  }

  fun getParentCategories(): List<Category> {
    return categories.filter { it.parentId == null }
  }
}
