package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.MockListingRepository
import com.example.data.ReservationResult
import com.example.domain.model.Listing
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ListingCard
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
  repository: MockListingRepository,
  snackbarHostState: SnackbarHostState,
  onListingClick: (Listing) -> Unit,
  onExploreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allListings by repository.listings.collectAsState(initial = emptyList())
  val savedIds by repository.savedIds.collectAsState(initial = emptySet())
  val userProfile by repository.userProfile.collectAsState(initial = MockListingRepository.guestUserProfile)
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val favoriteListings = allListings.filter { savedIds.contains(it.id) }

  Column(modifier = modifier.fillMaxSize()) {
    if (favoriteListings.isEmpty()) {
      EmptyStateView(
        title = "هنوز آگهی نشان نکرده‌اید",
        description = "آگهی‌ها، کوپن‌های تخفیف و هدایای مورد علاقه خود را با زدن علامت نشان ذخیره کنید.",
        buttonText = "مشاهده آگهی‌های پیشنهادی",
        icon = Icons.Default.BookmarkBorder,
        onButtonClick = onExploreClick,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("favorites_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        itemsIndexed(favoriteListings, key = { _, it -> it.id }) { index, listing ->
          val accessStatus = remember(listing, userProfile.plan) {
            repository.getListingAccessStatus(listing, userProfile.plan)
          }

          ListingCard(
            listing = listing,
            cardIndex = index,
            isFavorite = true,
            accessStatus = accessStatus,
            onItemClick = onListingClick,
            onFavoriteClick = { id ->
              repository.toggleFavorite(id)
              scope.launch {
                snackbarHostState.showSnackbar("از نشان‌شده‌ها حذف شد")
              }
            },
            onShareClick = { /* share */ },
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
