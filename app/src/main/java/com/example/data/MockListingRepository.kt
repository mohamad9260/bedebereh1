package com.example.data

import com.example.data.network.ApiClient
import com.example.data.network.CreateListingRequest
import com.example.domain.model.DiscountInfo
import com.example.domain.model.Listing
import com.example.domain.model.ListingAccessStatus
import com.example.domain.model.ListingStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.domain.model.SystemDynamicSettings
import com.example.domain.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class ReservationResult {
  data object Success : ReservationResult()
  data class DailyLimitReached(val count: Int, val maxLimit: Int, val tier: MembershipTier) : ReservationResult()
  data class EarlyAccessLocked(
    val requiredTier: MembershipTier,
    val remainingMinutes: Int,
    val description: String
  ) : ReservationResult()
  data class Error(val message: String) : ReservationResult()
}

class MockListingRepository {
  private val repositoryScope = CoroutineScope(Dispatchers.IO)
  private val _listings = MutableStateFlow<List<Listing>>(initialListings)
  val listings: Flow<List<Listing>> = _listings.asStateFlow()

  init {
    fetchRemoteListings()
  }

  fun fetchRemoteListings() {
    repositoryScope.launch {
      try {
        val response = ApiClient.apiService.getListings()
        if (response.isSuccessful && response.body()?.status == "success") {
          val serverItems = response.body()?.data
          if (!serverItems.isNullOrEmpty()) {
            val mapped = serverItems.map { dto ->
              Listing(
                id = "srv_${dto.id}",
                type = ListingType.FREE_GIFT,
                title = dto.title,
                description = dto.description ?: "",
                categoryId = dto.categoryId,
                categoryNameFa = dto.categoryTitle ?: "عمومی",
                categoryIcon = "shopping_bag",
                ownerId = "u_remote",
                ownerDisplayName = dto.ownerName ?: "کاربر بده‌ببر",
                province = "ایران",
                city = dto.city,
                coverImageUrl = dto.imageUrl,
                status = ListingStatus.PUBLIC,
                timeAgoFa = "لحظاتی پیش",
                createdAt = System.currentTimeMillis(),
                visibilityTier = MembershipTier.FREE
              )
            }
            _listings.value = mapped + initialListings
          }
        }
      } catch (_: Exception) {
        // Fallback gracefully to offline cache/initial listings
      }
    }
  }

  private val _savedIds = MutableStateFlow<Set<String>>(setOf("1", "4"))
  val savedIds: Flow<Set<String>> = _savedIds.asStateFlow()

  private val _systemSettings = MutableStateFlow(SystemDynamicSettings())
  val systemSettings: Flow<SystemDynamicSettings> = _systemSettings.asStateFlow()

  private val _userProfile = MutableStateFlow(
    UserProfile(
      id = "u_default",
      displayName = "محمد مهربخش",
      mobileNumberMasked = "۰۹۱۲***۴۵۶۷",
      nationalIdMasked = "۰۰۱******۸",
      province = "تهران",
      city = "تهران",
      plan = MembershipTier.FREE,
      successfulOffersCount = 4,
      completedRequestsCount = 2,
      dailyReservationsCount = 1
    )
  )
  val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

  /**
   * Returns listings filtered by type, city, and query.
   * Priority rule: Listings from the user's home city appear at the top first,
   * followed by other cities / nationwide offers, sorted by newest creation time.
   */
  fun getListingsByType(type: ListingType, city: String? = null, searchQuery: String? = null): Flow<List<Listing>> {
    return combine(_listings, _userProfile) { list, profile ->
      val userCity = profile.city
      val isAllCitiesFilter = city == null || city == "همه شهرها"

      list.filter { item ->
        val isVisibleStatus = item.status != ListingStatus.COMPLETED &&
            item.status != ListingStatus.REJECTED &&
            item.status != ListingStatus.ARCHIVED
        val matchesType = item.type == type
        val matchesCity = isAllCitiesFilter || item.city == city || (item.discountInfo?.isNationwide == true)
        val matchesQuery = searchQuery.isNullOrBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true) ||
            item.categoryNameFa.contains(searchQuery, ignoreCase = true)

        isVisibleStatus && matchesType && matchesCity && matchesQuery
      }.sortedWith(
        compareBy<Listing> { item ->
          // Priority 1: User's home city (0 for home city or nationwide, 1 for other cities)
          val isHomeCity = item.city.equals(userCity, ignoreCase = true) || (item.discountInfo?.isNationwide == true)
          if (isHomeCity) 0 else 1
        }.thenByDescending { item ->
          // Priority 2: Newest first
          item.createdAt
        }
      )
    }
  }

  /**
   * Calculates the exact timestamp (epoch millis) when a listing became (or will become)
   * available/eligible for a specific membership tier.
   */
  fun getTierAvailableTimestamp(listing: Listing, tier: MembershipTier): Long {
    val settings = _systemSettings.value
    val goldEarlyHours = settings.goldEarlyAccessHours
    val silverEarlyHours = settings.silverEarlyAccessHours

    return when (listing.visibilityTier) {
      MembershipTier.FREE -> listing.createdAt
      MembershipTier.SILVER -> {
        when (tier) {
          MembershipTier.FREE -> listing.createdAt + (silverEarlyHours * 3600 * 1000L)
          MembershipTier.SILVER, MembershipTier.GOLD, MembershipTier.DIAMOND -> listing.createdAt
        }
      }
      MembershipTier.GOLD, MembershipTier.DIAMOND -> {
        when (tier) {
          MembershipTier.FREE -> listing.createdAt + (goldEarlyHours * 3600 * 1000L)
          MembershipTier.SILVER -> listing.createdAt + ((goldEarlyHours - silverEarlyHours).coerceAtLeast(0) * 3600 * 1000L)
          MembershipTier.GOLD, MembershipTier.DIAMOND -> listing.createdAt
        }
      }
    }
  }

  /**
   * Retrieves listings for the "⚡ همین الان رایگان شد" (Just Became Available) feature.
   * Only includes FREE_GIFT items that:
   * - Are approved and not reserved/completed/archived/rejected/pending
   * - Have already become available to the current user's membership tier (availableAt <= now)
   * - Became available within the configured duration window (now - availableAt <= durationMillis)
   * - Sorted by most recently available first.
   */
  fun getJustBecameAvailableListings(
    tier: MembershipTier = _userProfile.value.plan,
    cityName: String? = null
  ): Flow<List<com.example.domain.model.RecentlyAvailableItem>> {
    return combine(_listings, _systemSettings) { listings, settings ->
      val now = System.currentTimeMillis()
      val maxDurationMillis = settings.justBecameAvailableDurationHours * 3600 * 1000L

      listings
        .asSequence()
        .filter { it.type == ListingType.FREE_GIFT }
        .filter {
          !it.isReserved &&
          it.status != ListingStatus.RESERVED &&
          it.status != ListingStatus.COMPLETED &&
          it.status != ListingStatus.ARCHIVED &&
          it.status != ListingStatus.REJECTED &&
          it.status != ListingStatus.PENDING_REVIEW &&
          it.status != ListingStatus.DRAFT
        }
        .filter {
          cityName.isNullOrBlank() || cityName == "همه شهرها" || it.city.contains(cityName) || cityName.contains(it.city)
        }
        .mapNotNull { item ->
          val availableAt = getTierAvailableTimestamp(item, tier)
          if (availableAt <= now && (now - availableAt) <= maxDurationMillis) {
            com.example.domain.model.RecentlyAvailableItem(
              listing = item,
              availableAtTimestamp = availableAt,
              relativeTimeFa = com.example.ui.components.PersianUtils.formatRelativeTimeFa(availableAt)
            )
          } else {
            null
          }
        }
        .sortedByDescending { it.availableAtTimestamp }
        .toList()
    }
  }

  fun isUserOwner(listing: Listing): Boolean {
    return listing.ownerId == _userProfile.value.id || listing.id == "1"
  }

  fun getDailyReservationLimit(tier: MembershipTier = _userProfile.value.plan): Int {
    val settings = _systemSettings.value
    return when (tier) {
      MembershipTier.FREE -> settings.freeDailyReserveLimit
      MembershipTier.SILVER -> settings.silverDailyReserveLimit
      MembershipTier.GOLD -> settings.goldDailyReserveLimit
      MembershipTier.DIAMOND -> settings.diamondDailyReserveLimit
    }
  }

  fun canPostDiscount(tier: MembershipTier = _userProfile.value.plan): Boolean {
    val settings = _systemSettings.value
    if (!settings.requireDiamondForDiscounts) return true
    return tier == MembershipTier.DIAMOND
  }

  /**
   * Determines the detailed access & locking state for the current user.
   * - Public visibility: All cards are ALWAYS visible to everyone.
   * - Reservation/Selection Eligibility:
   *   - In the first X hours (e.g. 2 hours / goldEarlyAccessHours): Only Diamond and Gold can reserve.
   *   - In the 1 hour window (silverEarlyAccessHours): Diamond, Gold, and Silver can reserve.
   *   - After hour 0 (window elapsed): Public (Free and all tiers) can reserve.
   *   - Owner can always view and manage.
   */
  fun getListingAccessStatus(
    listing: Listing,
    userTier: MembershipTier = _userProfile.value.plan
  ): ListingAccessStatus {
    if (isUserOwner(listing)) {
      return ListingAccessStatus(
        isLockedForCurrentUser = false,
        requiredTierNow = null,
        remainingMinutesUntilPublic = 0,
        isEligibleToSelect = true,
        statusDescriptionFa = "آگهی متعلق به شماست"
      )
    }

    val settings = _systemSettings.value
    val goldEarlyHours = settings.goldEarlyAccessHours
    val silverEarlyHours = settings.silverEarlyAccessHours

    val maxEarlyHours = when (listing.visibilityTier) {
      MembershipTier.GOLD -> goldEarlyHours
      MembershipTier.DIAMOND -> settings.diamondEarlyAccessHours
      MembershipTier.SILVER -> silverEarlyHours
      MembershipTier.FREE -> 0
    }

    if (maxEarlyHours <= 0) {
      return ListingAccessStatus(
        isLockedForCurrentUser = false,
        requiredTierNow = null,
        remainingMinutesUntilPublic = 0,
        isEligibleToSelect = true,
        statusDescriptionFa = "عمومی و قابل انتخاب برای همه"
      )
    }

    val elapsedMillis = System.currentTimeMillis() - listing.createdAt
    val totalWindowMillis = maxEarlyHours * 3600 * 1000L
    val remainingMillis = totalWindowMillis - elapsedMillis
    val remainingMinutesUntilPublic = (remainingMillis / (60 * 1000L)).coerceAtLeast(0).toInt()

    if (remainingMinutesUntilPublic <= 0) {
      return ListingAccessStatus(
        isLockedForCurrentUser = false,
        requiredTierNow = null,
        remainingMinutesUntilPublic = 0,
        isEligibleToSelect = true,
        statusDescriptionFa = "عمومی و قابل انتخاب برای همه"
      )
    }

    val silverWindowMinutes = silverEarlyHours * 60

    // 1. First 2-Hour Phase (Gold & Diamond VIP only)
    if (remainingMinutesUntilPublic > silverWindowMinutes) {
      val isEligible = userTier == MembershipTier.DIAMOND || userTier == MembershipTier.GOLD
      return ListingAccessStatus(
        isLockedForCurrentUser = !isEligible,
        requiredTierNow = MembershipTier.GOLD,
        remainingMinutesUntilPublic = remainingMinutesUntilPublic,
        isEligibleToSelect = isEligible,
        statusDescriptionFa = "در دو ساعت اول: مخصوص پلن طلایی و الماس (${remainingMinutesUntilPublic} دقیقه تا آزادسازی عمومی)"
      )
    } else {
      // 2. 1-Hour Window Phase (Silver, Gold & Diamond)
      val isEligible = userTier == MembershipTier.DIAMOND || userTier == MembershipTier.GOLD || userTier == MembershipTier.SILVER
      return ListingAccessStatus(
        isLockedForCurrentUser = !isEligible,
        requiredTierNow = MembershipTier.SILVER,
        remainingMinutesUntilPublic = remainingMinutesUntilPublic,
        isEligibleToSelect = isEligible,
        statusDescriptionFa = "در یک ساعت قبل: مخصوص پلن نقره‌ای، طلایی و الماس (${remainingMinutesUntilPublic} دقیقه تا آزادسازی عمومی)"
      )
    }
  }

  fun toggleFavorite(id: String) {
    val current = _savedIds.value
    _savedIds.value = if (current.contains(id)) current - id else current + id
  }

  fun addListing(listing: Listing) {
    val current = _listings.value.toMutableList()
    val ownedListing = listing.copy(
      ownerId = _userProfile.value.id,
      createdAt = System.currentTimeMillis()
    )
    current.add(0, ownedListing)
    _listings.value = current

    // Increment user stats if free gift
    val profile = _userProfile.value
    if (listing.type == ListingType.FREE_GIFT) {
      _userProfile.value = profile.copy(successfulOffersCount = profile.successfulOffersCount + 1)
    }

    // Push to meftah.id.ir server
    repositoryScope.launch {
      try {
        ApiClient.apiService.addListing(
          CreateListingRequest(
            userId = 1,
            title = listing.title,
            categoryId = if (listing.categoryId.startsWith("fg_")) "TOOLS" else "TOOLS",
            description = listing.description,
            city = listing.city,
            pricePerDay = 0.0,
            contactPhone = profile.mobileNumberMasked.ifBlank { "09120000000" },
            imageUrl = listing.coverImageUrl
          )
        )
      } catch (_: Exception) {
        // Keep local
      }
    }
  }

  fun reserveListing(id: String): ReservationResult {
    val listing = _listings.value.find { it.id == id } ?: return ReservationResult.Error("آگهی یافت نشد.")
    val profile = _userProfile.value
    val limit = getDailyReservationLimit(profile.plan)

    // Check daily limit
    if (profile.dailyReservationsCount >= limit) {
      return ReservationResult.DailyLimitReached(
        count = profile.dailyReservationsCount,
        maxLimit = limit,
        tier = profile.plan
      )
    }

    // Check early access lock
    val accessStatus = getListingAccessStatus(listing, profile.plan)
    if (!accessStatus.isEligibleToSelect) {
      return ReservationResult.EarlyAccessLocked(
        requiredTier = accessStatus.requiredTierNow ?: MembershipTier.GOLD,
        remainingMinutes = accessStatus.remainingMinutesUntilPublic,
        description = accessStatus.statusDescriptionFa
      )
    }

    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(
          isReserved = true,
          status = ListingStatus.RESERVED
        )
      } else {
        item
      }
    }
    _listings.value = updated
    _userProfile.value = profile.copy(dailyReservationsCount = profile.dailyReservationsCount + 1)
    return ReservationResult.Success
  }

  fun cancelReservation(id: String) {
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(
          isReserved = false,
          status = ListingStatus.PUBLIC
        )
      } else {
        item
      }
    }
    _listings.value = updated
    val profile = _userProfile.value
    if (profile.dailyReservationsCount > 0) {
      _userProfile.value = profile.copy(dailyReservationsCount = profile.dailyReservationsCount - 1)
    }
  }

  /**
   * Action "رفت!" (Given Away / Completed):
   * Marks listing as COMPLETED so it immediately disappears from public view,
   * but remains safely archived for admin reporting & user history.
   */
  fun markAsGone(id: String) {
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(
          isReserved = false,
          status = ListingStatus.COMPLETED
        )
      } else {
        item
      }
    }
    _listings.value = updated

    val profile = _userProfile.value
    _userProfile.value = profile.copy(successfulOffersCount = profile.successfulOffersCount + 1)
  }

  fun updateListingStatus(id: String, status: ListingStatus) {
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(
          status = status,
          isReserved = status == ListingStatus.RESERVED
        )
      } else {
        item
      }
    }
    _listings.value = updated
  }

  fun upgradeMembership(tier: MembershipTier) {
    _userProfile.value = _userProfile.value.copy(
      plan = tier,
      planExpiryFa = "۱۴۰۳/۰۷/۰۱"
    )
  }

  fun updateSettings(newSettings: SystemDynamicSettings) {
    _systemSettings.value = newSettings
  }

  companion object {
    private val now = System.currentTimeMillis()

    val initialListings = listOf(
      // Free Gifts - Tehran (Home City of default user)
      Listing(
        id = "1",
        type = ListingType.FREE_GIFT,
        title = "میز تحریر چوبی دونفره بسیار تمیز و محکم",
        description = "میز تحریر چوبی تمیز و محکم به علت جابجایی منزل، به دانش‌آموز یا دانشجوی پرتلاش به صورت کاملاً رایگان اهدا می‌شود. لطفاً فقط متقاضیان واقعی هماهنگ بفرمایند.",
        categoryId = "fg_furniture",
        categoryNameFa = "مبلمان و دکوراسیون",
        categoryIcon = "chair",
        ownerId = "u101",
        ownerDisplayName = "علی رضایی",
        province = "تهران",
        city = "تهران",
        approximateLocation = "محدوده صادقیه",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۱۵ دقیقه پیش",
        createdAt = now - (15 * 60 * 1000L), // 15 mins ago -> In 2-hour Gold/Diamond window
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "2",
        type = ListingType.FREE_GIFT,
        title = "مجموعه کتب کنکور تجربی و ریاضی جامع",
        description = "بیش از ۱۰ جلد کتاب تست و درسنامه نو و بدون خط‌خوردگی برای کنکور سراسری، آماده تحویل رایگان به کنکوری‌های عزیز.",
        categoryId = "fg_books",
        categoryNameFa = "کتاب و نشریات",
        categoryIcon = "menu_book",
        ownerId = "u102",
        ownerDisplayName = "مریم حسینی",
        province = "تهران",
        city = "تهران",
        approximateLocation = "میدان انقلاب",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۷۵ دقیقه پیش",
        createdAt = now - (75 * 60 * 1000L), // 75 mins ago -> In 1-hour Silver/Gold window
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "3",
        type = ListingType.FREE_GIFT,
        title = "کاپشن زمستانه و لباس گرم کودک ۳ تا ۵ سال",
        description = "کاپشن گرم و تمیز در حد نو، جهت استفاده کودکان عزیز خانواده‌های محترم.",
        categoryId = "fg_kids",
        categoryNameFa = "کودک و اسباب‌بازی",
        categoryIcon = "toys",
        ownerId = "u103",
        ownerDisplayName = "سارا احمدی",
        province = "تهران",
        city = "تهران",
        approximateLocation = "پیروزی",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۳ ساعت پیش",
        createdAt = now - (180 * 60 * 1000L), // 3 hours ago -> Public for everyone
        visibilityTier = MembershipTier.FREE
      ),
      // Other Cities Free Gifts
      Listing(
        id = "8",
        type = ListingType.FREE_GIFT,
        title = "مانیتور ۱۹ اینچ سامسونگ سالم به همراه کابل‌ها",
        description = "مانیتور کامپیوتر کاملاً سالم و تست شده، مناسب کارهای اداری، مدرسه یا کدنویسی به عنوان مانیتور دوم. اهدایی بدون هزینه.",
        categoryId = "fg_electronics",
        categoryNameFa = "لوازم الکترونیکی",
        categoryIcon = "devices",
        ownerId = "u108",
        ownerDisplayName = "رضا باقری",
        province = "فارس",
        city = "شیراز",
        approximateLocation = "میدان آزادی",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۴۰ دقیقه پیش",
        createdAt = now - (40 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "10",
        type = ListingType.FREE_GIFT,
        title = "دوچرخه سایز ۲۰ نوجوان کاملاً سالم",
        description = "دوچرخه نوجوان دنده‌ای سالم، لاستیک‌ها نو، جهت استفاده رایگان دانش‌آموزان عزیز اهدا می‌گردد.",
        categoryId = "fg_kids",
        categoryNameFa = "کودک و اسباب‌بازی",
        categoryIcon = "toys",
        ownerId = "u110",
        ownerDisplayName = "مهدی کاظمی",
        province = "اصفهان",
        city = "اصفهان",
        approximateLocation = "چهارباغ بالا",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۸۰ دقیقه پیش",
        createdAt = now - (80 * 60 * 1000L),
        visibilityTier = MembershipTier.SILVER
      ),
      Listing(
        id = "14",
        type = ListingType.FREE_GIFT,
        title = "سرویس قابلمه ۶ پارچه تفلون نچسب",
        description = "سرویس قابلمه نچسب تمیز و مناسب استفاده دانشجویی یا جهیزیه نوپا، کاملاً سالم و اهدایی.",
        categoryId = "fg_home",
        categoryNameFa = "لوازم خانه و آشپزخانه",
        categoryIcon = "shopping_bag",
        ownerId = "u114",
        ownerDisplayName = "نسرین صادقی",
        province = "تهران",
        city = "تهران",
        approximateLocation = "ستارخان",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۱۲۵ دقیقه پیش",
        createdAt = now - (125 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "15",
        type = ListingType.FREE_GIFT,
        title = "اسکیت کفشی بچگانه سایز ۳۲ تا ۳۵ چرخ‌دار",
        description = "اسکیت تمیز و کم‌کارکرد همراه با کلاه ایمنی و زانوبند به صورت رایگان اهدا می‌شود.",
        categoryId = "fg_kids",
        categoryNameFa = "کودک و اسباب‌بازی",
        categoryIcon = "toys",
        ownerId = "u115",
        ownerDisplayName = "کامران نوری",
        province = "تهران",
        city = "تهران",
        approximateLocation = "سعادت‌آباد",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۱۰ دقیقه پیش",
        createdAt = now - (10 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      ),
      Listing(
        id = "16",
        type = ListingType.FREE_GIFT,
        title = "میز تلویزیون ام‌دی‌اف مدرن دو طبقه",
        description = "میز تلویزیون قهوه‌ای روشن در حد نو با دو کمد ریلی، اهدایی بدون هیچ‌گونه هزینه.",
        categoryId = "fg_furniture",
        categoryNameFa = "مبلمان و دکوراسیون",
        categoryIcon = "chair",
        ownerId = "u116",
        ownerDisplayName = "محسن یوسفی",
        province = "تهران",
        city = "تهران",
        approximateLocation = "تهرانپارس",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۶۸ دقیقه پیش",
        createdAt = now - (68 * 60 * 1000L),
        visibilityTier = MembershipTier.SILVER
      ),
      // Discounts
      Listing(
        id = "4",
        type = ListingType.DISCOUNT,
        title = "تخفیف ۵۰ هزار تومانی اسنپ‌فود سراسر کشور",
        description = "کد تخفیف اختصاصی ۵۰ هزار تومانی برای سفارش‌های بالای ۱۲۰ هزار تومان. ویژه سوپرمارکت و رستوران.",
        categoryId = "dc_food",
        categoryNameFa = "رستوران و کافه",
        categoryIcon = "restaurant",
        ownerId = "u104",
        ownerDisplayName = "پویا کریمی",
        province = "تهران",
        city = "تهران",
        discountInfo = DiscountInfo(
          discountCode = "BEDE-BEREH-50K",
          discountAmountToman = 50000,
          expirationDateFa = "۱۴۰۳/۰۶/۱۵",
          isExpiringSoon = true,
          isNationwide = true,
          terms = "حداقل خرید ۱۲۰ هزار تومان"
        ),
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۲۰ دقیقه پیش",
        createdAt = now - (20 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "5",
        type = ListingType.DISCOUNT,
        title = "کوپن تخفیف ۳۵٪ دوره آموزش برنامه‌نویسی و اندروید",
        description = "کوپن اختصاصی تخفیف ۳۵ درصدی برای ثبت‌نام در تمام دوره‌های تخصصی توسعه نرم‌افزار و طراحی رابط کاربری.",
        categoryId = "dc_course",
        categoryNameFa = "دوره‌های آموزشی",
        categoryIcon = "school",
        ownerId = "u105",
        ownerDisplayName = "امیرحسین عباسی",
        province = "سراسر کشور",
        city = "همه شهرها",
        discountInfo = DiscountInfo(
          discountCode = "DEV-LEARN-35",
          discountPercentage = 35,
          expirationDateFa = "۱۴۰۳/۰۶/۳۰",
          isExpiringSoon = false,
          isNationwide = true,
          terms = "بدون سقف تخفیف"
        ),
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۱ ساعت پیش",
        createdAt = now - (70 * 60 * 1000L),
        visibilityTier = MembershipTier.SILVER
      ),
      Listing(
        id = "9",
        type = ListingType.DISCOUNT,
        title = "کد تخفیف ۱۰۰ هزار تومانی خرید کتاب از دیجی‌کالا",
        description = "تخفیف عالی برای خرید انواع کتب عمومی، روانشناسی، رمان و دانشگاهی در خریدهای بالای ۳۰۰ هزار تومان.",
        categoryId = "dc_online",
        categoryNameFa = "فروشگاه‌های اینترنتی",
        categoryIcon = "shopping_bag",
        ownerId = "u109",
        ownerDisplayName = "الهام صادقی",
        province = "سراسر کشور",
        city = "همه شهرها",
        discountInfo = DiscountInfo(
          discountCode = "BOOK-LOVE-100",
          discountAmountToman = 100000,
          expirationDateFa = "۱۴۰۳/۰۶/۲۵",
          isExpiringSoon = false,
          isNationwide = true,
          terms = "خریدهای بالای ۳۰۰ هزار تومان"
        ),
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۴ ساعت پیش",
        createdAt = now - (240 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      ),
      // Requests
      Listing(
        id = "6",
        type = ListingType.REQUEST,
        title = "نیاز به میز مطالعه و صندلی برای دانش‌آموز",
        description = "برای فرزند محصلم نیازمند یک عدد میز مطالعه و صندلی ساده در محدوده شرق تهران یا کرج هستم. در صورت عدم نیاز لطفاً اطلاع دهید.",
        categoryId = "rq_study",
        categoryNameFa = "میز و لوازم مطالعه",
        categoryIcon = "menu_book",
        ownerId = "u106",
        ownerDisplayName = "حسین نوری",
        province = "تهران",
        city = "تهران",
        approximateLocation = "تهرانپارس",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۳۰ دقیقه پیش",
        createdAt = now - (30 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      ),
      Listing(
        id = "7",
        type = ListingType.REQUEST,
        title = "درخواست کالسکه یا صندلی غذای کودک در تبریز",
        description = "جهت استفاده نوزاد نیازمند کالسکه یا صندلی غذای تمیز در شهر تبریز هستیم. پیشاپیش از لطف و محبت شما سپاسگزاریم.",
        categoryId = "rq_kids",
        categoryNameFa = "لوازم تحریر و کودک",
        categoryIcon = "child_care",
        ownerId = "u107",
        ownerDisplayName = "زهرا کاظمی",
        province = "آذربایجان شرقی",
        city = "تبریز",
        approximateLocation = "آبرسان",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۳ ساعت پیش",
        createdAt = now - (190 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      )
    )
  }
}

