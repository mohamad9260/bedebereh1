package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockListingRepository
import com.example.data.ReservationResult
import com.example.domain.model.Listing
import com.example.domain.model.ListingType
import com.example.domain.model.PageBanner
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HomeSubTabsBar
import com.example.ui.components.HomeTabBackground
import com.example.ui.components.JustBecameAvailableSection
import com.example.ui.components.ListingCard
import com.example.ui.components.ListingDetailSheet
import com.example.ui.components.PersianUtils
import com.example.ui.components.RecentlyAvailableSheet
import com.example.ui.components.getCategoryVector
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SkyBlueAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  repository: MockListingRepository,
  currentCityName: String,
  searchQuery: String,
  snackbarHostState: SnackbarHostState,
  onNavigateToAddListing: () -> Unit,
  onContactAdminClick: () -> Unit = {},
  onUpgradeClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(ListingType.FREE_GIFT) }
  var selectedCategoryId by remember { mutableStateOf<String?>(null) }
  var selectedListingForDetail by remember { mutableStateOf<Listing?>(null) }
  val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var showRecentlyAvailableSheet by remember { mutableStateOf(false) }
  val recentlySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val userProfile by repository.userProfile.collectAsState(initial = MockListingRepository.guestUserProfile)
  val serverCategories by repository.categories.collectAsState(initial = emptyList())
  val serverBanners by repository.banners.collectAsState(initial = emptyMap())

  val listingsFlow = remember(selectedTab, currentCityName, searchQuery) {
    repository.getListingsByType(
      type = selectedTab,
      city = currentCityName,
      searchQuery = searchQuery
    )
  }

  val listings by listingsFlow.collectAsState(initial = emptyList())
  val savedIds by repository.savedIds.collectAsState(initial = emptySet())

  val justBecameAvailableFlow = remember(userProfile.plan, currentCityName) {
    repository.getJustBecameAvailableListings(
      tier = userProfile.plan,
      cityName = currentCityName
    )
  }
  val justBecameAvailableItems by justBecameAvailableFlow.collectAsState(initial = emptyList())

  val categories = remember(selectedTab, serverCategories) {
    val filtered = serverCategories.filter { it.type == selectedTab }
    if (filtered.isNotEmpty()) filtered else serverCategories
  }

  val filteredListings = remember(listings, selectedCategoryId) {
    if (selectedCategoryId == null) listings
    else listings.filter { it.categoryId == selectedCategoryId }
  }

  val activeAccentColor = when (selectedTab) {
    ListingType.FREE_GIFT -> EmeraldPrimary
    ListingType.DISCOUNT -> AmberSecondary
    ListingType.REQUEST -> Color(0xFF0284C7)
  }

  // Active banner for current tab
  val activeBanner = remember(selectedTab, serverBanners) {
    val b = when (selectedTab) {
      ListingType.FREE_GIFT -> serverBanners["free_gift"] ?: serverBanners["home_free_gift"] ?: serverBanners["home"]
      ListingType.DISCOUNT -> serverBanners["discount"] ?: serverBanners["home_discount"] ?: serverBanners["home"]
      ListingType.REQUEST -> serverBanners["request"] ?: serverBanners["home_request"] ?: serverBanners["home"]
    }
    if (b != null && !b.isActive) null else b
  }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Dynamic 3-Section Interior Background Matching User-Uploaded Images
    HomeTabBackground(selectedType = selectedTab)

    // 2. Foreground Content
    Column(modifier = Modifier.fillMaxSize()) {
      // 3 Primary Home Tabs
      HomeSubTabsBar(
        selectedType = selectedTab,
        onTabSelected = { newTab ->
          selectedTab = newTab
          selectedCategoryId = null
        }
      )

      // Category Filter Chips
      if (categories.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = selectedCategoryId == null,
            onClick = { selectedCategoryId = null },
            label = {
              Text(
                text = "همه (${PersianUtils.formatNumber(listings.size)})",
                fontSize = 12.5.sp,
                fontWeight = if (selectedCategoryId == null) FontWeight.Bold else FontWeight.Medium
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = activeAccentColor,
              selectedLabelColor = Color.White,
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = selectedCategoryId == null,
              borderColor = MaterialTheme.colorScheme.outlineVariant,
              selectedBorderColor = activeAccentColor,
              borderWidth = 1.dp,
              selectedBorderWidth = 1.dp
            ),
            modifier = Modifier.testTag("category_chip_all")
          )

          categories.forEach { category ->
            val count = listings.count { it.categoryId == category.id }
            val isSelected = selectedCategoryId == category.id
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedCategoryId = if (isSelected) null else category.id
              },
              label = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = getCategoryVector(category.iconName),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                  )
                  Text(
                    text = "${category.titleFa} (${PersianUtils.formatNumber(count)})",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = activeAccentColor,
                selectedLabelColor = Color.White,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = activeAccentColor,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp
              ),
              modifier = Modifier.testTag("category_chip_${category.id}")
            )
          }
        }
      }

      // Main Listings Feed
      if (filteredListings.isEmpty()) {
        EmptyStateView(
          title = when (selectedTab) {
            ListingType.FREE_GIFT -> "هیچ هدیه رایگانی یافت نشد"
            ListingType.DISCOUNT -> "هیچ کوپن یا تخفیفی یافت نشد"
            ListingType.REQUEST -> "هیچ درخواستی ثبت نشده است"
          },
          description = "با تغییر فیلتر شهر یا دسته‌بندی دوباره جستجو کنید، یا اولین آگهی را ثبت نمایید.",
          onButtonClick = onNavigateToAddListing,
          modifier = Modifier.weight(1f)
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("listings_feed"),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Dynamic Banner Managed via Admin Panel (Hidden if disabled by admin)
          if (activeBanner != null) {
            item(key = "hero_branding_banner") {
              HeroBrandingCard(selectedTab = selectedTab, customBanner = activeBanner)
            }
          }

          // Dedicated "⚡ همین الان رایگان شد" (Just Became Available) Section on Free Gifts tab
          if (selectedTab == ListingType.FREE_GIFT && justBecameAvailableItems.isNotEmpty()) {
            item(key = "just_became_available_section") {
              JustBecameAvailableSection(
                items = justBecameAvailableItems,
                userTier = userProfile.plan,
                onListingClick = { item ->
                  selectedListingForDetail = item
                },
                onViewAllClick = {
                  showRecentlyAvailableSheet = true
                }
              )
            }
          }

          itemsIndexed(
            items = filteredListings,
            key = { _, it -> it.id }
          ) { index, listing ->
            val accessStatus = remember(listing, userProfile.plan) {
              repository.getListingAccessStatus(listing, userProfile.plan)
            }

            ListingCard(
              listing = listing,
              cardIndex = index,
              isFavorite = savedIds.contains(listing.id),
              accessStatus = accessStatus,
              onItemClick = { item ->
                selectedListingForDetail = item
              },
              onFavoriteClick = { id ->
                repository.toggleFavorite(id)
                scope.launch {
                  val isSaved = !savedIds.contains(id)
                  snackbarHostState.showSnackbar(
                    if (isSaved) "به نشان‌شده‌ها اضافه شد" else "از نشان‌شده‌ها حذف شد"
                  )
                }
              },
              onShareClick = { item ->
                shareListing(context, item)
              },
              onReserveClick = { item ->
                when (val res = repository.reserveListing(item.id)) {
                  is ReservationResult.Success -> {
                    scope.launch {
                      snackbarHostState.showSnackbar("درخواست رزرو برای «${item.title}» با موفقیت ثبت شد 🎉")
                    }
                  }
                  is ReservationResult.DailyLimitReached -> {
                    scope.launch {
                      snackbarHostState.showSnackbar(
                        "سقف مجاز روزانه (${res.maxLimit} رزرو) برای طرح ${res.tier.titleFa} تکمیل شده است. برای ارتقای سقف پکیج را ارتقا دهید."
                      )
                    }
                  }
                  is ReservationResult.EarlyAccessLocked -> {
                    scope.launch {
                      snackbarHostState.showSnackbar(
                        "این آگهی در بازه اختصاصی پکیج ${res.requiredTier.titleFa} است (${res.remainingMinutes} دقیقه باقی‌مانده)."
                      )
                    }
                  }
                  is ReservationResult.Error -> {
                    scope.launch {
                      snackbarHostState.showSnackbar(res.message)
                    }
                  }
                }
              }
            )
          }
          item {
            Spacer(modifier = Modifier.height(80.dp))
          }
        }
      }
    }
  }

  // Detail Modal BottomSheet
  selectedListingForDetail?.let { detailListing ->
    val detailAccessStatus = remember(detailListing, userProfile.plan) {
      repository.getListingAccessStatus(detailListing, userProfile.plan)
    }

    ListingDetailSheet(
      listing = detailListing,
      isFavorite = savedIds.contains(detailListing.id),
      isOwner = repository.isUserOwner(detailListing),
      isReserver = (detailListing.reservedByUserId == userProfile.id || (userProfile.rawPhone.isNotBlank() && detailListing.reservedByPhone == userProfile.rawPhone)),
      accessStatus = detailAccessStatus,
      onUpgradeClick = {
        selectedListingForDetail = null
        onUpgradeClick()
      },
      onDismiss = { selectedListingForDetail = null },
      onFavoriteClick = { id ->
        repository.toggleFavorite(id)
        scope.launch {
          val isSaved = !savedIds.contains(id)
          snackbarHostState.showSnackbar(
            if (isSaved) "به نشان‌شده‌ها اضافه شد" else "از نشان‌شده‌ها حذف شد"
          )
        }
      },
      onReserveClick = { item ->
        when (val res = repository.reserveListing(item.id)) {
          is ReservationResult.Success -> {
            scope.launch {
              snackbarHostState.showSnackbar("درخواست رزرو برای «${item.title}» با موفقیت ثبت شد 🎉")
            }
          }
          is ReservationResult.DailyLimitReached -> {
            scope.launch {
              snackbarHostState.showSnackbar(
                "سقف مجاز روزانه (${res.maxLimit} رزرو) برای طرح ${res.tier.titleFa} تکمیل شده است. برای افزایش سقف، طرح خود را ارتقا دهید."
              )
            }
          }
          is ReservationResult.EarlyAccessLocked -> {
            scope.launch {
              snackbarHostState.showSnackbar(
                "این آگهی در بازه دسترسی زودهنگام پکیج ${res.requiredTier.titleFa} است (${res.remainingMinutes} دقیقه باقی‌مانده)."
              )
            }
          }
          is ReservationResult.Error -> {
            scope.launch {
              snackbarHostState.showSnackbar(res.message)
            }
          }
        }
        selectedListingForDetail = null
      },
      onMarkAsGone = { item ->
        repository.markAsGone(item.id)
        scope.launch {
          snackbarHostState.showSnackbar("آگهی «${item.title}» با موفقیت اهدا شد و از آگهی‌های عمومی برداشته شد 🎉")
        }
        selectedListingForDetail = null
      },
      onCancelReservation = { item ->
        repository.cancelReservation(item.id)
        scope.launch {
          snackbarHostState.showSnackbar("رزرو آگهی لغو شد و به حالت عمومی بازگشت.")
        }
        selectedListingForDetail = null
      },
      onContactAdmin = {
        selectedListingForDetail = null
        onContactAdminClick()
      },
      sheetState = detailSheetState
    )
  }

  // "⚡ همین الان رایگان شد" (View All) Modal BottomSheet
  if (showRecentlyAvailableSheet) {
    RecentlyAvailableSheet(
      items = justBecameAvailableItems,
      userTier = userProfile.plan,
      sheetState = recentlySheetState,
      onDismiss = { showRecentlyAvailableSheet = false },
      onListingClick = { item ->
        selectedListingForDetail = item
      }
    )
  }
}

@Composable
private fun HeroBrandingCard(
  selectedTab: ListingType,
  customBanner: PageBanner? = null
) {
  val defaultInfo = when (selectedTab) {
    ListingType.FREE_GIFT -> Quadruple(
      Brush.horizontalGradient(listOf(Color(0xFF00796B), Color(0xFF004D40))),
      Icons.Default.CardGiftcard,
      "بده بره؛ مهربونی رو تکثیر کن! 🌿",
      "وسایل بدون استفاده را رایگان اهدا کنید یا دریافت نمایید."
    )
    ListingType.DISCOUNT -> Quadruple(
      Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFF92400E))),
      Icons.Default.LocalOffer,
      "تخفیف‌ها و کوپن‌های اختصاصی 🎟️",
      "کدهای تخفیف معتبر رستوران، خرید اینترنتی، آموزش و سفر."
    )
    ListingType.REQUEST -> Quadruple(
      Brush.horizontalGradient(listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))),
      Icons.Default.HelpOutline,
      "درخواست‌های نیازمندی و یاری 🤝",
      "نیازهای درسی، منزل یا ضروری خود را ثبت کنید تا دیگران اهدا کنند."
    )
  }

  val bannerTitle = customBanner?.title ?: defaultInfo.third
  val bannerSubtitle = customBanner?.subtitle ?: defaultInfo.fourth
  val bannerImageUrl = customBanner?.imageUrl

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      if (!bannerImageUrl.isNullOrBlank()) {
        AsyncImage(
          model = bannerImageUrl,
          contentDescription = bannerTitle,
          contentScale = ContentScale.FillWidth,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
        )
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(defaultInfo.first)
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = defaultInfo.second,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = bannerTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (!bannerSubtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = bannerSubtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 11.sp
                )
              }
            }

            customBanner?.badgeText?.let { badge ->
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(start = 8.dp)
              ) {
                Text(
                  text = badge,
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun shareListing(context: Context, listing: Listing) {
  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(
      Intent.EXTRA_TEXT,
      buildString {
        append("آگهی در اپلیکیشن «بده بره»:\n")
        append("📌 ${listing.title}\n")
        append("📍 شهر: ${listing.city}\n")
        append("${listing.description}\n\n")
        append("مشاهده رایگان در اپلیکیشن بده بره")
      }
    )
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری آگهی")
  context.startActivity(shareIntent)
}
