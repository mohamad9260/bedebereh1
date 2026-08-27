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
  val diamondPlanPriceToman: Long = 149_000L
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
  val isFavorite: Boolean = false,
  val visibilityTier: MembershipTier = MembershipTier.FREE
)

data class Category(
  val id: String,
  val titleFa: String,
  val type: ListingType,
  val iconName: String,
  val parentId: String? = null
)

data class UserProfile(
  val id: String,
  val displayName: String,
  val mobileNumberMasked: String,
  val nationalIdMasked: String,
  val province: String,
  val city: String,
  val plan: MembershipTier = MembershipTier.FREE,
  val planExpiryFa: String? = null,
  val successfulOffersCount: Int = 0,
  val completedRequestsCount: Int = 0,
  val dailyReservationsCount: Int = 0
)

data class City(
  val name: String,
  val province: String
)
