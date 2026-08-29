package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ApiResponse<T>(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "registration_token") val registrationToken: String? = null,
    @Json(name = "resend_after") val resendAfter: Int? = null,
    @Json(name = "data") val data: T? = null
)

data class OtpRequestPayload(
    @Json(name = "mobile") val mobile: String
)

data class OtpVerifyPayload(
    @Json(name = "mobile") val mobile: String,
    @Json(name = "code") val code: String
)

data class OtpResponseData(
    @Json(name = "mobile") val mobile: String? = null,
    @Json(name = "registration_token") val registrationToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Int? = null,
    @Json(name = "resend_after") val resendAfter: Int? = null
)

data class ServerCategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name_fa") val nameFa: String,
    @Json(name = "icon_name") val iconName: String? = "card_giftcard",
    @Json(name = "type") val type: String? = "FREE_GIFT",
    @Json(name = "is_locked") val isLocked: Int? = 0,
    @Json(name = "lock_message") val lockMessage: String? = null,
    @Json(name = "display_order") val displayOrder: Int? = 0
)

data class ServerBannerDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "page") val page: String,
    @Json(name = "title") val title: String,
    @Json(name = "subtitle") val subtitle: String,
    @Json(name = "badge_text") val badgeText: String? = "بده بره",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "action_url") val actionUrl: String? = null,
    @Json(name = "is_active") val isActive: Int? = 1
)

data class ServerSettingsDto(
    @Json(name = "silver_plan_price") val silverPlanPrice: Long = 49000L,
    @Json(name = "gold_plan_price") val goldPlanPrice: Long = 99000L,
    @Json(name = "diamond_plan_price") val diamondPlanPrice: Long = 149000L,
    @Json(name = "gold_early_access_hours") val goldEarlyAccessHours: Int = 2,
    @Json(name = "silver_early_access_hours") val silverEarlyAccessHours: Int = 1,
    @Json(name = "diamond_early_access_hours") val diamondEarlyAccessHours: Int = 2,
    @Json(name = "require_diamond_for_discounts") val requireDiamondForDiscounts: Boolean = true,
    @Json(name = "support_phone") val supportPhone: String = "021-88889260",
    @Json(name = "support_email") val supportEmail: String = "admin@bedebere.ir",
    @Json(name = "support_telegram") val supportTelegram: String = "@bedebere_admin",
    @Json(name = "support_hours") val supportHours: String = "پاسخگویی سریع ۲۴ ساعته",
    @Json(name = "just_free_hours") val justFreeHours: Int = 24
)

data class ServerListingDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "category_title") val categoryTitle: String?,
    @Json(name = "type") val type: String? = null,
    @Json(name = "province") val province: String? = null,
    @Json(name = "city") val city: String,
    @Json(name = "price_per_day") val pricePerDay: Double?,
    @Json(name = "deposit_amount") val depositAmount: Double?,
    @Json(name = "status") val status: String?,
    @Json(name = "approval_status") val approvalStatus: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "contact_phone") val contactPhone: String?,
    @Json(name = "owner_name") val ownerName: String?,
    @Json(name = "owner_phone") val ownerPhone: String?,
    @Json(name = "created_at") val createdAt: String?
)

data class CreateListingRequest(
    @Json(name = "user_id") val userId: Int = 1,
    @Json(name = "title") val title: String,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "type") val type: String = "FREE_GIFT",
    @Json(name = "description") val description: String,
    @Json(name = "city") val city: String,
    @Json(name = "province") val province: String = "تهران",
    @Json(name = "contact_phone") val contactPhone: String = "09120000000",
    @Json(name = "owner_name") val ownerName: String? = null,
    @Json(name = "owner_phone") val ownerPhone: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "discount_code") val discountCode: String? = null,
    @Json(name = "discount_percentage") val discountPercentage: Int? = null,
    @Json(name = "discount_amount") val discountAmount: Long? = null
)

data class AuthRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "city") val city: String = "تهران",
    @Json(name = "national_id") val nationalId: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "registration_token") val registrationToken: String? = null
)

data class SendTicketRequest(
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "subject") val subject: String,
    @Json(name = "message") val message: String
)

interface ApiService {
    @GET("get_listings.php")
    suspend fun getListings(
        @Query("city") city: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<ServerListingDto>>>

    @GET("categories.php")
    suspend fun getCategories(): Response<ApiResponse<List<ServerCategoryDto>>>

    @GET("banners.php")
    suspend fun getBanners(@Query("page") page: String? = null): Response<ApiResponse<List<ServerBannerDto>>>

    @GET("settings.php")
    suspend fun getSettings(): Response<ApiResponse<ServerSettingsDto>>

    @GET("just_free.php")
    suspend fun getJustFreeListings(@Query("limit") limit: Int = 20): Response<ApiResponse<List<ServerListingDto>>>

    @POST("otp_request.php")
    suspend fun requestOtp(
        @Body request: OtpRequestPayload
    ): Response<ApiResponse<OtpResponseData>>

    @POST("otp_verify.php")
    suspend fun verifyOtp(
        @Body request: OtpVerifyPayload
    ): Response<ApiResponse<OtpResponseData>>

    @POST("add_listing.php")
    suspend fun addListing(
        @Body request: CreateListingRequest
    ): Response<ApiResponse<Any>>

    @POST("users.php")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): Response<ApiResponse<Map<String, Any>>>

    @POST("send_ticket.php")
    suspend fun sendTicket(
        @Body request: SendTicketRequest
    ): Response<ApiResponse<Map<String, Any>>>
}

object ApiClient {
    const val BASE_URL = "https://meftah.id.ir/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
