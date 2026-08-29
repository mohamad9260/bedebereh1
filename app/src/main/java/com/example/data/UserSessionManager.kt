package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.MembershipTier
import com.example.domain.model.UserProfile

class UserSessionManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("bedebere_user_session", Context.MODE_PRIVATE)

  fun saveUser(profile: UserProfile) {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, profile.isLoggedIn)
      .putString(KEY_USER_ID, profile.id)
      .putString(KEY_DISPLAY_NAME, profile.displayName)
      .putString(KEY_MASKED_PHONE, profile.mobileNumberMasked)
      .putString(KEY_MASKED_NATIONAL_ID, profile.nationalIdMasked)
      .putString(KEY_RAW_PHONE, profile.rawPhone)
      .putString(KEY_RAW_NATIONAL_ID, profile.rawNationalId)
      .putString(KEY_CITY, profile.city)
      .putString(KEY_PROVINCE, profile.province)
      .putString(KEY_PLAN, profile.plan.name)
      .putString(KEY_PLAN_EXPIRY, profile.planExpiryFa)
      .putInt(KEY_OFFERS_COUNT, profile.successfulOffersCount)
      .putInt(KEY_REQUESTS_COUNT, profile.completedRequestsCount)
      .putInt(KEY_DAILY_RESERVES, profile.dailyReservationsCount)
      .putString(KEY_AUTH_TOKEN, profile.authToken)
      .apply()
  }

  fun getSavedUser(): UserProfile? {
    val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    if (!isLoggedIn) return null

    val planStr = prefs.getString(KEY_PLAN, MembershipTier.FREE.name) ?: MembershipTier.FREE.name
    val plan = try {
      MembershipTier.valueOf(planStr)
    } catch (e: Exception) {
      MembershipTier.FREE
    }

    return UserProfile(
      id = prefs.getString(KEY_USER_ID, "u_saved") ?: "u_saved",
      displayName = prefs.getString(KEY_DISPLAY_NAME, "کاربر بده بره") ?: "کاربر بده بره",
      mobileNumberMasked = prefs.getString(KEY_MASKED_PHONE, "---") ?: "---",
      nationalIdMasked = prefs.getString(KEY_MASKED_NATIONAL_ID, "---") ?: "---",
      rawPhone = prefs.getString(KEY_RAW_PHONE, "") ?: "",
      rawNationalId = prefs.getString(KEY_RAW_NATIONAL_ID, "") ?: "",
      city = prefs.getString(KEY_CITY, "تهران") ?: "تهران",
      province = prefs.getString(KEY_PROVINCE, "تهران") ?: "تهران",
      plan = plan,
      planExpiryFa = prefs.getString(KEY_PLAN_EXPIRY, null),
      successfulOffersCount = prefs.getInt(KEY_OFFERS_COUNT, 0),
      completedRequestsCount = prefs.getInt(KEY_REQUESTS_COUNT, 0),
      dailyReservationsCount = prefs.getInt(KEY_DAILY_RESERVES, 0),
      isLoggedIn = true,
      authToken = prefs.getString(KEY_AUTH_TOKEN, null)
    )
  }

  fun clearSession() {
    prefs.edit().clear().apply()
  }

  fun isPhoneRegistered(phone: String): Boolean {
    val clean = phone.trim()
    val registered = prefs.getStringSet(KEY_REGISTERED_PHONES, defaultRegisteredPhones) ?: defaultRegisteredPhones
    return registered.contains(clean) || registered.contains(phone)
  }

  fun saveRegisteredPhone(phone: String, name: String) {
    val clean = phone.trim()
    val registered = (prefs.getStringSet(KEY_REGISTERED_PHONES, defaultRegisteredPhones) ?: defaultRegisteredPhones).toMutableSet()
    registered.add(clean)
    registered.add(phone)
    prefs.edit()
      .putStringSet(KEY_REGISTERED_PHONES, registered)
      .putString("user_name_$clean", name)
      .putString("user_name_$phone", name)
      .apply()
  }

  fun getRegisteredName(phone: String): String? {
    val clean = phone.trim()
    return prefs.getString("user_name_$clean", null) ?: prefs.getString("user_name_$phone", null)
  }

  fun isAdminLoggedIn(): Boolean {
    return prefs.getBoolean(KEY_ADMIN_LOGGED_IN, false)
  }

  fun setAdminLoggedIn(isAdmin: Boolean) {
    prefs.edit().putBoolean(KEY_ADMIN_LOGGED_IN, isAdmin).apply()
  }

  companion object {
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_MASKED_PHONE = "masked_phone"
    private const val KEY_MASKED_NATIONAL_ID = "masked_nat_id"
    private const val KEY_RAW_PHONE = "raw_phone"
    private const val KEY_RAW_NATIONAL_ID = "raw_nat_id"
    private const val KEY_CITY = "city"
    private const val KEY_PROVINCE = "province"
    private const val KEY_PLAN = "plan"
    private const val KEY_PLAN_EXPIRY = "plan_expiry"
    private const val KEY_OFFERS_COUNT = "offers_count"
    private const val KEY_REQUESTS_COUNT = "requests_count"
    private const val KEY_DAILY_RESERVES = "daily_reserves"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REGISTERED_PHONES = "registered_phones"
    private const val KEY_ADMIN_LOGGED_IN = "admin_logged_in"

    private val defaultRegisteredPhones = emptySet<String>()
  }
}
