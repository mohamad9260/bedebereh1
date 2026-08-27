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
    @Json(name = "count") val count: Int? = null,
    @Json(name = "data") val data: T? = null
)

data class ServerListingDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "category_id") val categoryId: String,
    @Json(name = "category_title") val categoryTitle: String?,
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
    @Json(name = "description") val description: String,
    @Json(name = "city") val city: String,
    @Json(name = "price_per_day") val pricePerDay: Double = 0.0,
    @Json(name = "deposit_amount") val depositAmount: Double = 0.0,
    @Json(name = "contact_phone") val contactPhone: String,
    @Json(name = "image_url") val imageUrl: String? = null
)

data class AuthRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "full_name") val fullName: String
)

interface ApiService {
    @GET("get_listings.php")
    suspend fun getListings(
        @Query("city") city: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<ServerListingDto>>>

    @POST("add_listing.php")
    suspend fun addListing(
        @Body request: CreateListingRequest
    ): Response<ApiResponse<Any>>

    @POST("auth.php")
    suspend fun authenticate(
        @Body request: AuthRequest
    ): Response<ApiResponse<Map<String, Any>>>
}

object ApiClient {
    const val BASE_URL = "https://meftah.id.ir/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
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
