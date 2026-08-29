package com.example.data

import android.content.Context
import com.example.data.network.ApiClient
import com.example.data.network.AuthRequest
import com.example.data.network.CreateListingRequest
import com.example.data.network.OtpRequestPayload
import com.example.data.network.OtpVerifyPayload
import com.example.data.network.SendTicketRequest
import com.example.domain.model.AdminContactInfo
import com.example.domain.model.AppNotification
import com.example.domain.model.Category
import com.example.domain.model.DiscountInfo
import com.example.domain.model.Listing
import com.example.domain.model.ListingAccessStatus
import com.example.domain.model.ListingStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.domain.model.PageBanner
import com.example.domain.model.RecentlyAvailableItem
import com.example.domain.model.SystemDynamicSettings
import com.example.domain.model.UserProfile
import com.example.ui.components.PersianUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class MockListingRepository(context: Context? = null) {
  private val repositoryScope = CoroutineScope(Dispatchers.IO)
  private val sessionManager: UserSessionManager? = context?.let { UserSessionManager(it.applicationContext) }

  private val _listings = MutableStateFlow<List<Listing>>(initialListings)
  val listings: Flow<List<Listing>> = _listings.asStateFlow()

  private val _categories = MutableStateFlow<List<Category>>(CategoryData.categories)
  val categories: Flow<List<Category>> = _categories.asStateFlow()

  private val _banners = MutableStateFlow<Map<String, PageBanner>>(defaultBanners)
  val banners: Flow<Map<String, PageBanner>> = _banners.asStateFlow()

  private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
  val savedIds: Flow<Set<String>> = _savedIds.asStateFlow()

  private val _systemSettings = MutableStateFlow(SystemDynamicSettings())
  val systemSettings: Flow<SystemDynamicSettings> = _systemSettings.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(initialNotifications)
  val notifications: Flow<List<AppNotification>> = _notifications.asStateFlow()

  // Guest or restored session
  private val _userProfile = MutableStateFlow(
    sessionManager?.getSavedUser() ?: guestUserProfile
  )
  val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

  init {
    fetchAllRemoteData()
  }

  fun attachContext(context: Context) {
    val manager = UserSessionManager(context.applicationContext)
    val saved = manager.getSavedUser()
    if (saved != null && !_userProfile.value.isLoggedIn) {
      _userProfile.value = saved
    }
  }

  fun fetchAllRemoteData() {
    fetchRemoteListings()
    fetchRemoteCategories()
    fetchRemoteBanners()
    fetchRemoteSettings()
  }

  fun fetchRemoteListings() {
    repositoryScope.launch {
      try {
        val response = ApiClient.apiService.getListings()
        if (response.isSuccessful && response.body()?.status == "success") {
          val serverItems = response.body()?.data
          if (!serverItems.isNullOrEmpty()) {
            val mapped = serverItems.map { dto ->
              val type = when (dto.type?.uppercase()) {
                "DISCOUNT" -> ListingType.DISCOUNT
                "REQUEST" -> ListingType.REQUEST
                else -> ListingType.FREE_GIFT
              }
              val currentProfile = _userProfile.value
              val isOwnedByMe = (dto.ownerPhone != null && dto.ownerPhone == currentProfile.rawPhone && currentProfile.rawPhone.isNotBlank()) ||
                  (dto.ownerName != null && dto.ownerName == currentProfile.displayName && currentProfile.isLoggedIn)

              val mappedStatus = when {
                dto.approvalStatus == "APPROVED" || dto.status == "APPROVED" || dto.status == "PUBLIC" -> ListingStatus.PUBLIC
                dto.approvalStatus == "REJECTED" || dto.status == "REJECTED" -> ListingStatus.REJECTED
                dto.status == "RESERVED" -> ListingStatus.RESERVED
                else -> ListingStatus.PENDING_REVIEW
              }

              val itemProvince = if (!dto.province.isNullOrBlank()) dto.province else "تهران"

              Listing(
                id = "srv_${dto.id}",
                type = type,
                title = dto.title,
                description = dto.description ?: "",
                categoryId = dto.categoryId,
                categoryNameFa = dto.categoryTitle ?: "عمومی",
                categoryIcon = getIconForCategory(dto.categoryId),
                ownerId = if (isOwnedByMe) currentProfile.id else "srv_user_${dto.id}",
                ownerDisplayName = dto.ownerName ?: "کاربر بده بره",
                ownerPhone = dto.ownerPhone ?: dto.contactPhone,
                province = itemProvince,
                city = dto.city.ifBlank { "تهران" },
                coverImageUrl = dto.imageUrl,
                status = mappedStatus,
                createdAt = System.currentTimeMillis() - 3600000L,
                visibilityTier = MembershipTier.FREE
              )
            }
            // Merge with local items
            val combined = (mapped + _listings.value).distinctBy { it.id }
            _listings.value = combined
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("ListingRepo", "Error fetching remote listings: ${e.localizedMessage}")
      }
    }
  }

  fun fetchRemoteCategories() {
    repositoryScope.launch {
      try {
        val response = ApiClient.apiService.getCategories()
        if (response.isSuccessful && response.body()?.status == "success") {
          val serverCategories = response.body()?.data
          if (!serverCategories.isNullOrEmpty()) {
            val mapped = serverCategories.map { dto ->
              Category(
                id = dto.id,
                titleFa = dto.nameFa,
                type = if (dto.type == "DISCOUNT") ListingType.DISCOUNT else ListingType.FREE_GIFT,
                iconName = dto.iconName ?: "card_giftcard",
                isLocked = (dto.isLocked ?: 0) == 1,
                lockMessage = dto.lockMessage,
                displayOrder = dto.displayOrder ?: 0
              )
            }
            _categories.value = mapped
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("ListingRepo", "Error fetching remote categories: ${e.localizedMessage}")
      }
    }
  }

  fun fetchRemoteBanners() {
    repositoryScope.launch {
      try {
        val response = ApiClient.apiService.getBanners()
        if (response.isSuccessful && response.body()?.status == "success") {
          val bannerList = response.body()?.data
          if (!bannerList.isNullOrEmpty()) {
            val map = mutableMapOf<String, PageBanner>()
            bannerList.forEach { dto ->
              val banner = PageBanner(
                page = dto.page,
                title = dto.title,
                subtitle = dto.subtitle,
                badgeText = dto.badgeText ?: "بده بره",
                imageUrl = dto.imageUrl,
                actionUrl = dto.actionUrl,
                isActive = (dto.isActive ?: 1) == 1
              )
              map[dto.page] = banner
              if (dto.page == "free_gift") map["home_free_gift"] = banner
              if (dto.page == "discount") map["home_discount"] = banner
              if (dto.page == "request") map["home_request"] = banner
              if (dto.page == "home") map["main"] = banner
            }
            _banners.value = _banners.value + map
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("ListingRepo", "Error fetching banners: ${e.localizedMessage}")
      }
    }
  }

  fun fetchRemoteSettings() {
    repositoryScope.launch {
      try {
        val response = ApiClient.apiService.getSettings()
        if (response.isSuccessful && response.body()?.status == "success") {
          val dto = response.body()?.data
          if (dto != null) {
            _systemSettings.value = SystemDynamicSettings(
              silverPlanPriceToman = dto.silverPlanPrice,
              goldPlanPriceToman = dto.goldPlanPrice,
              diamondPlanPriceToman = dto.diamondPlanPrice,
              goldEarlyAccessHours = dto.goldEarlyAccessHours,
              silverEarlyAccessHours = dto.silverEarlyAccessHours,
              diamondEarlyAccessHours = dto.diamondEarlyAccessHours,
              requireDiamondForDiscounts = dto.requireDiamondForDiscounts,
              justBecameAvailableDurationHours = dto.justFreeHours,
              contactInfo = AdminContactInfo(
                supportPhone = dto.supportPhone,
                supportEmail = dto.supportEmail,
                supportTelegram = dto.supportTelegram,
                supportHours = dto.supportHours
              )
            )
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("ListingRepo", "Error fetching settings: ${e.localizedMessage}")
      }
    }
  }

  private fun extractErrorMessage(response: retrofit2.Response<*>): String? {
    return try {
      val errBody = response.errorBody()?.string()
      if (!errBody.isNullOrBlank()) {
        val json = org.json.JSONObject(errBody)
        json.optString("message").takeIf { it.isNotBlank() }
      } else null
    } catch (e: Exception) {
      null
    }
  }

  suspend fun requestRegistrationOtp(phone: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanPhone = PersianUtils.normalizeIranianMobile(phone)
    if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
      return@withContext Pair(false, "شماره موبایل وارد شده معتبر نیست. لطفاً شماره ۱۱ رقمی را به صورت ۰۹۱۲۳۴۵۶۷۸۹ وارد کنید.")
    }

    try {
      val response = ApiClient.apiService.requestOtp(OtpRequestPayload(mobile = cleanPhone))
      val body = response.body()
      if (response.isSuccessful && body?.status == "success") {
        return@withContext Pair(true, body.message ?: "کد تأیید ارسال شد.")
      } else {
        val errMsg = body?.message ?: extractErrorMessage(response) ?: "ارسال کد تأیید با خطا مواجه شد."
        return@withContext Pair(false, errMsg)
      }
    } catch (e: Exception) {
      android.util.Log.e("ListingRepo", "OTP Request error: ${e.localizedMessage}")
      return@withContext Pair(false, "خطا در برقراری ارتباط با سرور پیامک: ${e.localizedMessage ?: "اتصال اینترنت را بررسی کنید."}")
    }
  }

  suspend fun verifyRegistrationOtp(phone: String, code: String): Pair<Boolean, Pair<String?, String?>> = withContext(Dispatchers.IO) {
    val cleanPhone = PersianUtils.normalizeIranianMobile(phone)
    val cleanCode = PersianUtils.toEnglishDigits(code.trim())

    if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
      return@withContext Pair(false, Pair(null, "شماره موبایل نامعتبر است."))
    }
    if (cleanCode.length < 4 || cleanCode.length > 8) {
      return@withContext Pair(false, Pair(null, "کد تأیید ۵ رقمی را به صورت کامل وارد کنید."))
    }

    try {
      val response = ApiClient.apiService.verifyOtp(OtpVerifyPayload(mobile = cleanPhone, code = cleanCode))
      val body = response.body()
      if (response.isSuccessful && body?.status == "success") {
        val regToken = body.registrationToken ?: body.data?.registrationToken
        if (!regToken.isNullOrBlank()) {
          return@withContext Pair(true, Pair(regToken, null))
        } else {
          return@withContext Pair(false, Pair(null, "توکن تأیید شماره دریافت نشد."))
        }
      } else {
        val errMsg = body?.message ?: extractErrorMessage(response) ?: "کد وارد شده صحیح نمی‌باشد یا منقضی شده است."
        return@withContext Pair(false, Pair(null, errMsg))
      }
    } catch (e: Exception) {
      android.util.Log.e("ListingRepo", "OTP Verify error: ${e.localizedMessage}")
      return@withContext Pair(false, Pair(null, "خطا در ارتباط با سرور: ${e.localizedMessage ?: "اتصال اینترنت را بررسی کنید."}"))
    }
  }

  suspend fun login(phone: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanPhone = PersianUtils.normalizeIranianMobile(phone)
    if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
      return@withContext Pair(false, "شماره موبایل وارد شده معتبر نیست. لطفاً شماره ۱۱ رقمی را به صورت ۰۹۱۲۳۴۵۶۷۸۹ وارد کنید.")
    }

    try {
      val req = AuthRequest(
        phone = cleanPhone,
        fullName = "",
        city = "تهران",
        nationalId = null,
        action = "login"
      )
      val response = ApiClient.apiService.authenticate(req)
      val body = response.body()

      if (response.isSuccessful && body?.status == "success") {
        val rawPhone = cleanPhone
        val maskedPhone = PersianUtils.formatMaskedPhone(rawPhone)

        val profileName = (body.data?.get("full_name") as? String)?.takeIf { it.isNotBlank() }
          ?: sessionManager?.getRegisteredName(cleanPhone)
          ?: "کاربر گرامی ($maskedPhone)"
        val userCity = (body.data?.get("city") as? String)?.takeIf { it.isNotBlank() } ?: "تهران"
        val rawNat = (body.data?.get("national_id") as? String) ?: ""
        val maskedNat = if (rawNat.isNotBlank()) PersianUtils.formatMaskedNationalId(rawNat) else "---"
        val token = body.token ?: "auth_token_${System.currentTimeMillis()}"

        val restoredProfile = UserProfile(
          id = "u_${rawPhone.hashCode()}",
          displayName = profileName,
          mobileNumberMasked = maskedPhone,
          nationalIdMasked = maskedNat,
          rawPhone = rawPhone,
          rawNationalId = rawNat,
          province = "تهران",
          city = userCity,
          plan = MembershipTier.FREE,
          successfulOffersCount = 1,
          completedRequestsCount = 0,
          dailyReservationsCount = 0,
          isLoggedIn = true,
          authToken = token
        )

        _userProfile.value = restoredProfile
        sessionManager?.saveUser(restoredProfile)
        sessionManager?.saveRegisteredPhone(cleanPhone, profileName)
        fetchRemoteListings()
        return@withContext Pair(true, null)
      } else {
        val errMsg = body?.message ?: extractErrorMessage(response) ?: "ورود به حساب ناموفق بود."
        return@withContext Pair(false, errMsg)
      }
    } catch (e: Exception) {
      // Offline fallback only if user previously had a registered session locally
      val savedName = sessionManager?.getRegisteredName(cleanPhone)
      if (savedName != null) {
        val rawPhone = cleanPhone
        val maskedPhone = PersianUtils.formatMaskedPhone(rawPhone)
        val localProfile = UserProfile(
          id = "u_${rawPhone.hashCode()}",
          displayName = savedName,
          mobileNumberMasked = maskedPhone,
          nationalIdMasked = "---",
          rawPhone = rawPhone,
          rawNationalId = "",
          province = "تهران",
          city = "تهران",
          plan = MembershipTier.FREE,
          successfulOffersCount = 0,
          completedRequestsCount = 0,
          dailyReservationsCount = 0,
          isLoggedIn = true,
          authToken = "offline_token"
        )
        _userProfile.value = localProfile
        sessionManager?.saveUser(localProfile)
        return@withContext Pair(true, null)
      }
      return@withContext Pair(false, "خطا در برقراری ارتباط با سرور: ${e.localizedMessage}")
    }
  }

  suspend fun register(
    name: String,
    phone: String,
    city: String,
    nationalId: String?,
    registrationToken: String
  ): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
    val cleanName = name.trim()
    val cleanPhone = PersianUtils.normalizeIranianMobile(phone)
    val cleanCity = city.trim().ifBlank { "تهران" }
    val cleanNat = nationalId?.trim() ?: ""

    if (cleanName.isBlank()) {
      return@withContext Pair(false, "لطفاً نام و نام خانوادگی خود را وارد کنید.")
    }
    if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
      return@withContext Pair(false, "شماره موبایل وارد شده معتبر نیست (مثال: ۰۹۱۲۳۴۵۶۷۸۹).")
    }
    if (cleanNat.isNotBlank() && !PersianUtils.isValidNationalId(cleanNat)) {
      return@withContext Pair(false, "کد ملی ۱۰ رقمی وارد شده نامعتبر است.")
    }
    if (registrationToken.isBlank()) {
      return@withContext Pair(false, "تأیید پیامکی شماره موبایل الزامی است.")
    }

    try {
      val req = AuthRequest(
        phone = cleanPhone,
        fullName = cleanName,
        city = cleanCity,
        nationalId = cleanNat.ifBlank { null },
        action = "register",
        registrationToken = registrationToken
      )
      val response = ApiClient.apiService.authenticate(req)
      val body = response.body()

      if (response.isSuccessful && body?.status == "success") {
        val token = body.token ?: "auth_token_${System.currentTimeMillis()}"

        val maskedPhone = PersianUtils.formatMaskedPhone(cleanPhone)
        val maskedNat = if (cleanNat.isNotBlank()) PersianUtils.formatMaskedNationalId(cleanNat) else "---"

        val newProfile = UserProfile(
          id = "u_${cleanPhone.hashCode()}",
          displayName = cleanName,
          mobileNumberMasked = maskedPhone,
          nationalIdMasked = maskedNat,
          rawPhone = cleanPhone,
          rawNationalId = cleanNat,
          province = "تهران",
          city = cleanCity,
          plan = MembershipTier.FREE,
          successfulOffersCount = 0,
          completedRequestsCount = 0,
          dailyReservationsCount = 0,
          isLoggedIn = true,
          authToken = token
        )

        _userProfile.value = newProfile
        sessionManager?.saveUser(newProfile)
        sessionManager?.saveRegisteredPhone(cleanPhone, cleanName)
        fetchRemoteListings()
        addNotification(
          title = "خوش آمدید به بده‌بره",
          message = "ثبت‌نام شما با موفقیت انجام شد. از این پس می‌توانید آگهی ثبت کرده و وسایل را هدیه بگیرید.",
          type = "approved"
        )
        return@withContext Pair(true, null)
      } else {
        val errMsg = body?.message ?: extractErrorMessage(response) ?: "ثبت‌نام با خطا مواجه شد."
        return@withContext Pair(false, errMsg)
      }
    } catch (e: Exception) {
      return@withContext Pair(false, "خطا در ارتباط با سرور: ${e.localizedMessage}")
    }
  }

  fun logout() {
    sessionManager?.clearSession()
    _userProfile.value = guestUserProfile
  }

  fun addListing(listing: Listing) {
    val profile = _userProfile.value
    val current = _listings.value.toMutableList()

    // Listings start in PENDING_REVIEW unless Diamond VIP tier
    val initialStatus = if (profile.plan == MembershipTier.DIAMOND) ListingStatus.PUBLIC else ListingStatus.PENDING_REVIEW

    val ownedListing = listing.copy(
      id = "loc_${System.currentTimeMillis()}",
      ownerId = profile.id,
      ownerDisplayName = if (profile.isLoggedIn) profile.displayName else "شما (کاربر مهمان)",
      ownerPhone = profile.rawPhone.ifBlank { listing.ownerPhone },
      status = initialStatus,
      createdAt = System.currentTimeMillis()
    )
    current.add(0, ownedListing)
    _listings.value = current

    if (listing.type == ListingType.FREE_GIFT && profile.isLoggedIn) {
      val updatedProfile = profile.copy(successfulOffersCount = profile.successfulOffersCount + 1)
      _userProfile.value = updatedProfile
      sessionManager?.saveUser(updatedProfile)
    }

    addNotification(
      title = "آگهی شما ثبت شد",
      message = if (initialStatus == ListingStatus.PUBLIC)
        "آگهی «${listing.title}» با موفقیت منتشر گردید."
      else
        "آگهی «${listing.title}» ثبت شد و پس از بررسی تیم مدیریت منتشر خواهد شد.",
      type = if (initialStatus == ListingStatus.PUBLIC) "approved" else "info"
    )

    repositoryScope.launch {
      try {
        val req = CreateListingRequest(
          userId = if (profile.id.isNotBlank()) profile.id.hashCode() else 1,
          title = listing.title,
          categoryId = listing.categoryId,
          type = listing.type.name,
          description = listing.description,
          city = listing.city,
          province = listing.province,
          contactPhone = listing.ownerPhone?.takeIf { it.isNotBlank() } ?: profile.rawPhone.ifBlank { "09120000000" },
          imageUrl = listing.coverImageUrl,
          discountCode = listing.discountInfo?.discountCode,
          discountPercentage = listing.discountInfo?.discountPercentage,
          discountAmount = listing.discountInfo?.discountAmountToman,
          ownerName = listing.ownerDisplayName.takeIf { it.isNotBlank() } ?: profile.displayName,
          ownerPhone = listing.ownerPhone?.takeIf { it.isNotBlank() } ?: profile.rawPhone.ifBlank { "09120000000" }
        )
        ApiClient.apiService.addListing(req)
      } catch (e: Exception) {
        android.util.Log.e("ListingRepo", "Exception pushing listing to server: ${e.localizedMessage}")
      }
    }
  }

  fun isUserOwner(listing: Listing): Boolean {
    val profile = _userProfile.value
    if (listing.ownerId == "u_current" || listing.ownerId == profile.id) return true
    if (profile.isLoggedIn && profile.rawPhone.isNotBlank() && listing.ownerPhone == profile.rawPhone) return true
    if (profile.isLoggedIn && profile.displayName.isNotBlank() && listing.ownerDisplayName == profile.displayName && profile.displayName != "کاربر بده بره") return true
    return false
  }

  fun deleteListing(id: String) {
    _listings.value = _listings.value.filterNot { it.id == id }
  }

  fun updateListingStatus(id: String, status: ListingStatus) {
    val targetItem = _listings.value.find { it.id == id }
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(status = status, isReserved = status == ListingStatus.RESERVED)
      } else {
        item
      }
    }
    _listings.value = updated

    targetItem?.let { item ->
      when (status) {
        ListingStatus.PUBLIC -> addNotification(
          title = "آگهی شما تایید و منتشر شد",
          message = "آگهی «${item.title}» توسط مدیر تایید شد و هم‌اکنون برای همه کاربران قابل مشاهده است.",
          type = "approved"
        )
        ListingStatus.REJECTED -> addNotification(
          title = "آگهی تایید نشد",
          message = "متاسفانه آگهی «${item.title}» به دلیل مغایرت با قوانین سامانه منتشر نشد.",
          type = "rejected"
        )
        ListingStatus.RESERVED -> addNotification(
          title = "آگهی رزرو شد",
          message = "آگهی «${item.title}» به حالت رزرو شده تغییر یافت.",
          type = "reserved"
        )
        else -> {}
      }
    }
  }

  fun addNotification(title: String, message: String, type: String = "info") {
    val newNotif = AppNotification(
      id = "notif_${System.currentTimeMillis()}_${(100..999).random()}",
      title = title,
      message = message,
      timeAgo = "هم‌اکنون",
      type = type,
      isUnread = true,
      timestamp = System.currentTimeMillis()
    )
    _notifications.value = listOf(newNotif) + _notifications.value
  }

  fun markAllNotificationsRead() {
    _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
  }

  fun deleteNotification(id: String) {
    _notifications.value = _notifications.value.filterNot { it.id == id }
  }

  fun updateBannerStatus(page: String, isActive: Boolean) {
    val current = _banners.value.toMutableMap()
    val existing = current[page]
    if (existing != null) {
      current[page] = existing.copy(isActive = isActive)
      _banners.value = current
    }
  }

  fun getListingsByType(type: ListingType, city: String? = null, searchQuery: String? = null): Flow<List<Listing>> {
    return combine(_listings, _userProfile) { list, profile ->
      val userCity = profile.city
      val isAllCitiesFilter = city == null || city == "همه شهرها"

      list.filter { item ->
        // Only public or reserved items visible in main feed
        val isVisibleStatus = item.status == ListingStatus.PUBLIC || item.status == ListingStatus.RESERVED
        val matchesType = item.type == type
        val matchesCity = isAllCitiesFilter || item.city == city || (item.discountInfo?.isNationwide == true)
        val matchesQuery = searchQuery.isNullOrBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true) ||
            item.categoryNameFa.contains(searchQuery, ignoreCase = true)

        isVisibleStatus && matchesType && matchesCity && matchesQuery
      }.sortedByDescending { item ->
        item.createdAt
      }
    }
  }

  fun getUserOwnedListings(): Flow<List<Listing>> {
    return combine(_listings, _userProfile) { list, profile ->
      list.filter { isUserOwner(it) }.sortedByDescending { it.createdAt }
    }
  }

  fun getUserReservedListings(): Flow<List<Listing>> {
    return combine(_listings, _userProfile) { list, profile ->
      list.filter { item ->
        item.isReserved && (
          item.reservedByUserId == profile.id ||
          item.reservedByUserId == "u_current" ||
          (profile.rawPhone.isNotBlank() && item.reservedByPhone == profile.rawPhone) ||
          item.reservedByPhone == profile.mobileNumberMasked
        )
      }.sortedByDescending { it.createdAt }
    }
  }

  fun toggleFavorite(id: String) {
    val current = _savedIds.value
    _savedIds.value = if (current.contains(id)) current - id else current + id
  }

  fun reserveListing(id: String): ReservationResult {
    val listing = _listings.value.find { it.id == id } ?: return ReservationResult.Error("آگهی یافت نشد.")
    val profile = _userProfile.value
    val limit = getDailyReservationLimit(profile.plan)

    if (profile.dailyReservationsCount >= limit) {
      return ReservationResult.DailyLimitReached(
        count = profile.dailyReservationsCount,
        maxLimit = limit,
        tier = profile.plan
      )
    }

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
          status = ListingStatus.RESERVED,
          reservedByUserId = profile.id,
          reservedByPhone = profile.rawPhone.ifBlank { profile.mobileNumberMasked }
        )
      } else {
        item
      }
    }
    _listings.value = updated
    val newProfile = profile.copy(dailyReservationsCount = profile.dailyReservationsCount + 1)
    _userProfile.value = newProfile
    sessionManager?.saveUser(newProfile)

    addNotification(
      title = "رزرو با موفقیت ثبت شد",
      message = "شما آگهی «${listing.title}» را رزرو کردید. شماره تماس اهداکننده در جزئیات آگهی قابل مشاهده است.",
      type = "reserved"
    )
    return ReservationResult.Success
  }

  fun cancelReservation(id: String) {
    val listing = _listings.value.find { it.id == id }
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(
          isReserved = false,
          status = ListingStatus.PUBLIC,
          reservedByUserId = null,
          reservedByPhone = null
        )
      } else {
        item
      }
    }
    _listings.value = updated
    val profile = _userProfile.value
    if (profile.dailyReservationsCount > 0) {
      val newProfile = profile.copy(dailyReservationsCount = profile.dailyReservationsCount - 1)
      _userProfile.value = newProfile
      sessionManager?.saveUser(newProfile)
    }
    listing?.let {
      addNotification(
        title = "لغو رزرو",
        message = "رزرو آگهی «${it.title}» با موفقیت لغو شد و به حالت فعال برگشت.",
        type = "info"
      )
    }
  }

  fun markAsGone(id: String) {
    val listing = _listings.value.find { it.id == id }
    val updated = _listings.value.map { item ->
      if (item.id == id) {
        item.copy(isReserved = false, status = ListingStatus.COMPLETED)
      } else {
        item
      }
    }
    _listings.value = updated
    val profile = _userProfile.value
    if (profile.isLoggedIn) {
      val newProfile = profile.copy(successfulOffersCount = profile.successfulOffersCount + 1)
      _userProfile.value = newProfile
      sessionManager?.saveUser(newProfile)
    }
    listing?.let {
      addNotification(
        title = "اتمام اهدا و بسته شدن آگهی",
        message = "آگهی «${it.title}» به عنوان اهدا شده ثبت گردید. با تشکر از مهربانی شما!",
        type = "approved"
      )
    }
  }

  fun upgradeMembership(tier: MembershipTier) {
    val current = _userProfile.value
    val updated = current.copy(
      plan = tier,
      planExpiryFa = "۱۴۰۴/۰۶/۰۱"
    )
    _userProfile.value = updated
    sessionManager?.saveUser(updated)
    addNotification(
      title = "ارتقای اشتراک به ${tier.titleFa}",
      message = "طرح کاربری شما به «${tier.titleFa}» ارتقا یافت. از دسترسی زودهنگام و سقف رزرو روزانه بالاتر لذت ببرید!",
      type = "approved"
    )
  }

  fun updateSettings(newSettings: SystemDynamicSettings) {
    _systemSettings.value = newSettings
  }

  fun sendTicketToAdmin(subject: String, message: String) {
    repositoryScope.launch {
      try {
        val req = SendTicketRequest(
          phone = _userProfile.value.rawPhone.ifBlank { "09120000000" },
          subject = subject,
          message = message
        )
        ApiClient.apiService.sendTicket(req)
      } catch (_: Exception) {}
    }
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
        statusDescriptionFa = "عمومی و آزاد برای همه"
      )
    }

    val silverWindowMinutes = silverEarlyHours * 60

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

  fun getJustBecameAvailableListings(
    tier: MembershipTier = _userProfile.value.plan,
    cityName: String? = null
  ): Flow<List<RecentlyAvailableItem>> {
    return combine(_listings, _systemSettings) { list, settings ->
      val now = System.currentTimeMillis()
      val maxDurationMillis = settings.justBecameAvailableDurationHours * 3600 * 1000L
      val isAllCities = cityName == null || cityName == "همه شهرها"

      list
        .filter { item ->
          item.type == ListingType.FREE_GIFT &&
              item.status == ListingStatus.PUBLIC &&
              (item.visibilityTier == MembershipTier.GOLD || item.visibilityTier == MembershipTier.SILVER) &&
              (isAllCities || item.city == cityName)
        }
        .mapNotNull { item ->
          val availableAt = getTierAvailableTimestamp(item, tier)
          if (availableAt <= now && (now - availableAt) <= maxDurationMillis) {
            RecentlyAvailableItem(
              listing = item,
              availableAtTimestamp = availableAt,
              relativeTimeFa = PersianUtils.formatRelativeTimeFa(availableAt)
            )
          } else {
            null
          }
        }
        .sortedByDescending { it.availableAtTimestamp }
        .toList()
    }
  }

  companion object {
    private val now = System.currentTimeMillis()

    val guestUserProfile = UserProfile(
      id = "u_guest",
      displayName = "کاربر مهمان",
      mobileNumberMasked = "---",
      nationalIdMasked = "---",
      rawPhone = "",
      rawNationalId = "",
      province = "تهران",
      city = "تهران",
      plan = MembershipTier.FREE,
      successfulOffersCount = 0,
      completedRequestsCount = 0,
      dailyReservationsCount = 0,
      isLoggedIn = false,
      authToken = null
    )

    fun getIconForCategory(categoryId: String): String {
      return when {
        categoryId.contains("book", ignoreCase = true) -> "menu_book"
        categoryId.contains("furn", ignoreCase = true) -> "chair"
        categoryId.contains("home", ignoreCase = true) -> "kitchen"
        categoryId.contains("digit", ignoreCase = true) -> "devices"
        categoryId.contains("cloth", ignoreCase = true) -> "checkroom"
        categoryId.contains("kid", ignoreCase = true) || categoryId.contains("toy", ignoreCase = true) -> "toys"
        categoryId.contains("tool", ignoreCase = true) -> "build"
        categoryId.contains("car", ignoreCase = true) || categoryId.contains("veh", ignoreCase = true) -> "directions_car"
        categoryId.contains("food", ignoreCase = true) -> "restaurant"
        categoryId.contains("disc", ignoreCase = true) -> "local_offer"
        categoryId.contains("req", ignoreCase = true) -> "help_outline"
        else -> "card_giftcard"
      }
    }

    val defaultBanners = mapOf(
      "home" to PageBanner(
        page = "home",
        title = "بده بره، مهربونی رو تکثیر کن",
        subtitle = "وسایلی که نیاز نداری رو هدیه بده یا از دیگران هدیه بگیر",
        badgeText = "بده بره",
        isActive = true
      ),
      "home_free_gift" to PageBanner(
        page = "home_free_gift",
        title = "هدایای کاملاً رایگان هموطنان",
        subtitle = "هر آنچه که نیاز ندارید را با عشق هدیه دهید",
        badgeText = "مهربانی رایگان",
        isActive = true
      ),
      "home_discount" to PageBanner(
        page = "home_discount",
        title = "تخفیف‌ها و کوپن‌های اختصاصی",
        subtitle = "بهترین کدهای تخفیف فروشگاهی و آموزشی",
        badgeText = "تخفیف ویژه",
        isActive = true
      ),
      "home_request" to PageBanner(
        page = "home_request",
        title = "نیازمندی‌ها و درخواست‌های یاری",
        subtitle = "نیازهای خود را بیان کنید تا دیگران همراهتان شوند",
        badgeText = "همیاری",
        isActive = true
      )
    )

    val initialListings = listOf(
      Listing(
        id = "1",
        type = ListingType.FREE_GIFT,
        title = "میز تحریر چوبی دونفره بسیار تمیز و محکم",
        description = "میز تحریر چوبی تمیز و محکم به علت جابجایی منزل، به دانش‌آموز یا دانشجوی پرتلاش به صورت کاملاً رایگان اهدا می‌شود. لطفاً فقط متقاضیان واقعی هماهنگ بفرمایند.",
        categoryId = "cat_home_furniture",
        categoryNameFa = "مبلمان، کاناپه و میز",
        categoryIcon = "chair",
        ownerId = "u101",
        ownerDisplayName = "علی رضایی",
        ownerPhone = "09121112233",
        province = "تهران",
        city = "تهران",
        approximateLocation = "محدوده صادقیه",
        coverImageUrl = "vector:furniture",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۱۵ دقیقه پیش",
        createdAt = now - (15 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "2",
        type = ListingType.FREE_GIFT,
        title = "مجموعه کتب کنکور تجربی و ریاضی جامع",
        description = "بیش از ۱۰ جلد کتاب تست و درسنامه نو و بدون خط‌خوردگی برای کنکور سراسری، آماده تحویل رایگان به کنکوری‌های عزیز.",
        categoryId = "cat_books_school",
        categoryNameFa = "کتاب‌های درسی و کنکور",
        categoryIcon = "school",
        ownerId = "u102",
        ownerDisplayName = "مریم حسینی",
        ownerPhone = "09122223344",
        province = "تهران",
        city = "تهران",
        approximateLocation = "میدان انقلاب",
        coverImageUrl = "vector:books",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۷۵ دقیقه پیش",
        createdAt = now - (75 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
      Listing(
        id = "3",
        type = ListingType.FREE_GIFT,
        title = "کاپشن زمستانه و لباس گرم کودک ۳ تا ۵ سال",
        description = "کاپشن گرم و تمیز در حد نو، جهت استفاده کودکان عزیز خانواده‌های محترم.",
        categoryId = "cat_clothing_adult",
        categoryNameFa = "پوشاک مردانه و زنانه",
        categoryIcon = "checkroom",
        ownerId = "u103",
        ownerDisplayName = "سارا احمدی",
        ownerPhone = "09123334455",
        province = "تهران",
        city = "تهران",
        approximateLocation = "پیروزی",
        coverImageUrl = "vector:kids",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۳ ساعت پیش",
        createdAt = now - (180 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      ),
      Listing(
        id = "8",
        type = ListingType.FREE_GIFT,
        title = "مانیتور ۱۹ اینچ سامسونگ سالم به همراه کابل‌ها",
        description = "مانیتور کامپیوتر کاملاً سالم و تست شده، مناسب کارهای اداری، مدرسه یا کدنویسی به عنوان مانیتور دوم. اهدایی بدون هزینه.",
        categoryId = "cat_digital_pc",
        categoryNameFa = "کامپیوتر، مانیتور و لپ‌تاپ",
        categoryIcon = "computer",
        ownerId = "u108",
        ownerDisplayName = "رضا باقری",
        ownerPhone = "09171112233",
        province = "فارس",
        city = "شیراز",
        approximateLocation = "میدان آزادی",
        coverImageUrl = "vector:digital",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۴۰ دقیقه پیش",
        createdAt = now - (40 * 60 * 1000L),
        visibilityTier = MembershipTier.GOLD
      ),
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
        ownerPhone = "09124445566",
        province = "تهران",
        city = "تهران",
        coverImageUrl = "vector:food",
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
        categoryNameFa = "دوره‌های آموزشی و وبینار",
        categoryIcon = "school",
        ownerId = "u105",
        ownerDisplayName = "امیرحسین عباسی",
        ownerPhone = "09125556677",
        province = "سراسر کشور",
        city = "همه شهرها",
        coverImageUrl = "vector:discount",
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
        id = "6",
        type = ListingType.REQUEST,
        title = "نیاز به میز مطالعه و صندلی برای دانش‌آموز",
        description = "برای فرزند محصلم نیازمند یک عدد میز مطالعه و صندلی ساده در محدوده شرق تهران یا کرج هستم. در صورت عدم نیاز لطفاً اطلاع دهید.",
        categoryId = "rq_study",
        categoryNameFa = "میز و لوازم مطالعه",
        categoryIcon = "menu_book",
        ownerId = "u106",
        ownerDisplayName = "حسین نوری",
        ownerPhone = "09126667788",
        province = "تهران",
        city = "تهران",
        approximateLocation = "تهرانپارس",
        coverImageUrl = "vector:furniture",
        status = ListingStatus.PUBLIC,
        timeAgoFa = "۳۰ دقیقه پیش",
        createdAt = now - (30 * 60 * 1000L),
        visibilityTier = MembershipTier.FREE
      )
    )

    val initialNotifications = listOf(
      AppNotification(
        id = "notif_1",
        title = "آگهی شما تایید و منتشر شد",
        message = "آگهی «میز تحریر چوبی دونفره بسیار سالم» توسط مدیر تایید شد و اکنون در دسترس همگان قرار دارد.",
        timeAgo = "۱۰ دقیقه پیش",
        type = "approved",
        isUnread = true,
        timestamp = now - (10 * 60 * 1000L)
      ),
      AppNotification(
        id = "notif_2",
        title = "درخواست رزرو جدید",
        message = "کاربری برای دریافت وسیله شما درخواست رزرو ثبت نموده است. لطفاً جهت هماهنگی بررسی فرمایید.",
        timeAgo = "۱ ساعت پیش",
        type = "reserved",
        isUnread = true,
        timestamp = now - (60 * 60 * 1000L)
      ),
      AppNotification(
        id = "notif_3",
        title = "دسترسی زودهنگام به تخفیف‌های جدید",
        message = "آگهی‌های دارای اشتراک طلایی برای شما قابل مشاهده هستند.",
        timeAgo = "دیروز",
        type = "info",
        isUnread = false,
        timestamp = now - (24 * 60 * 60 * 1000L)
      )
    )
  }
}
