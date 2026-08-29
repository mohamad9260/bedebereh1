package com.example.domain.model

enum class ListingType(val titleFa: String, val identifier: String) {
  FREE_GIFT("هدایای رایگان", "free_offers"),
  DISCOUNT("تخفیف‌ها و کوپن‌ها", "discounts"),
  REQUEST("درخواست‌ها", "requests");

  companion object {
    fun fromIdentifier(id: String): ListingType {
      return entries.find { it.identifier == id } ?: FREE_GIFT
    }
  }
}

enum class ListingStatus(val labelFa: String) {
  DRAFT("پیش‌نویس"),
  PENDING_REVIEW("در انتظار تایید"),
  APPROVED("تایید شده"),
  SCHEDULED("زمان‌بندی شده"),
  GOLD_EARLY_ACCESS("دسترسی طلایی"),
  SILVER_EARLY_ACCESS("دسترسی نقره‌ای"),
  PUBLIC("عمومی"),
  RESERVED("رزرو شده"),
  COMPLETED("تحویل شده"),
  REJECTED("رد شده"),
  ARCHIVED("بایگانی شده")
}

enum class MembershipTier(
  val titleFa: String,
  val earlyAccessHours: Int,
  val defaultDailyReservationLimit: Int
) {
  FREE("عادی (رایگان)", 0, 3),
  SILVER("نقره‌ای", 1, 8),
  GOLD("طلایی", 2, 15),
  DIAMOND("الماس (VIP تجاری)", 2, 25)
}

data class PageBanner(
  val page: String,
  val title: String,
  val subtitle: String,
  val badgeText: String = "بده بره",
  val imageUrl: String? = null,
  val actionUrl: String? = null,
  val isActive: Boolean = true
)

data class AdminContactInfo(
  val supportPhone: String = "021-88889260",
  val supportEmail: String = "admin@bedebere.ir",
  val supportTelegram: String = "@bedebere_admin",
  val supportHours: String = "پاسخگویی سریع ۲۴ ساعته"
)

data class SystemDynamicSettings(
  val goldEarlyAccessHours: Int = 2,
  val silverEarlyAccessHours: Int = 1,
  val diamondEarlyAccessHours: Int = 2,
  val justBecameAvailableDurationHours: Int = 24,
  val requireDiamondForDiscounts: Boolean = true,
  val isDiamondFeeEnabled: Boolean = true,
  val freeDailyReserveLimit: Int = 3,
  val silverDailyReserveLimit: Int = 8,
  val goldDailyReserveLimit: Int = 15,
  val diamondDailyReserveLimit: Int = 25,
  val silverPlanPriceToman: Long = 49_000L,
  val goldPlanPriceToman: Long = 99_000L,
  val diamondPlanPriceToman: Long = 149_000L,
  val contactInfo: AdminContactInfo = AdminContactInfo()
)

data class RecentlyAvailableItem(
  val listing: Listing,
  val availableAtTimestamp: Long,
  val relativeTimeFa: String
)

data class ListingAccessStatus(
  val isLockedForCurrentUser: Boolean = false,
  val requiredTierNow: MembershipTier? = null,
  val remainingMinutesUntilPublic: Int = 0,
  val isEligibleToSelect: Boolean = true,
  val statusDescriptionFa: String = ""
)

data class DiscountInfo(
  val discountCode: String? = null,
  val discountPercentage: Int? = null,
  val discountAmountToman: Long? = null,
  val expirationDateFa: String? = null,
  val isExpiringSoon: Boolean = false,
  val isExpired: Boolean = false,
  val isNationwide: Boolean = true,
  val terms: String? = null
)

data class Listing(
  val id: String,
  val type: ListingType,
  val title: String,
  val description: String,
  val categoryId: String,
  val categoryNameFa: String,
  val categoryIcon: String,
  val ownerId: String,
  val ownerDisplayName: String,
  val ownerPhone: String? = null,
  val province: String,
  val city: String,
  val approximateLocation: String? = null,
  val imageUrls: List<String> = emptyList(),
  val coverImageUrl: String? = null,
  val status: ListingStatus = ListingStatus.PUBLIC,
  val discountInfo: DiscountInfo? = null,
  val timeAgoFa: String = "چند لحظه پیش",
  val createdAt: Long = System.currentTimeMillis(),
  val isReserved: Boolean = false,
  val reservedByUserId: String? = null,
  val reservedByPhone: String? = null,
  val rejectionReason: String? = null,
  val isFavorite: Boolean = false,
  val visibilityTier: MembershipTier = MembershipTier.FREE
)

data class Category(
  val id: String,
  val titleFa: String,
  val type: ListingType,
  val iconName: String,
  val parentId: String? = null,
  val isLocked: Boolean = false,
  val lockMessage: String? = null,
  val displayOrder: Int = 0
)

data class UserProfile(
  val id: String,
  val displayName: String,
  val mobileNumberMasked: String,
  val nationalIdMasked: String,
  val rawPhone: String = "",
  val rawNationalId: String = "",
  val province: String,
  val city: String,
  val plan: MembershipTier = MembershipTier.FREE,
  val planExpiryFa: String? = null,
  val successfulOffersCount: Int = 0,
  val completedRequestsCount: Int = 0,
  val dailyReservationsCount: Int = 0,
  val isBanned: Boolean = false,
  val banReason: String? = null,
  val canPostListing: Boolean = true,
  val isLoggedIn: Boolean = false,
  val authToken: String? = null
)

data class City(
  val name: String,
  val province: String
)

data class AppNotification(
  val id: String,
  val title: String,
  val message: String,
  val timeAgo: String = "هم‌اکنون",
  val type: String = "info", // "approved", "rejected", "reserved", "system"
  val isUnread: Boolean = true,
  val timestamp: Long = System.currentTimeMillis()
)
