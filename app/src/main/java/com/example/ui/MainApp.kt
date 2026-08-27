package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.MockListingRepository
import com.example.data.ReservationResult
import com.example.domain.model.Listing
import com.example.domain.model.MembershipTier
import com.example.ui.components.AdminModerationSheet
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.CitySelectorSheet
import com.example.ui.components.ContactAdminSheet
import com.example.ui.components.ListingDetailSheet
import com.example.ui.components.MembershipPlansDialog
import com.example.ui.components.PersianRtlLayout
import com.example.ui.navigation.NavRoute
import com.example.ui.screens.AddListingScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
  repository: MockListingRepository = remember { MockListingRepository() },
  modifier: Modifier = Modifier
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.HOME

  var selectedCityName by remember { mutableStateOf("همه شهرها") }
  var searchQuery by remember { mutableStateOf("") }
  var unreadNotificationsCount by remember { mutableIntStateOf(2) }

  var showCitySheet by remember { mutableStateOf(false) }
  var showPlansDialog by remember { mutableStateOf(false) }
  var showAdminSheet by remember { mutableStateOf(false) }
  var showContactAdminSheet by remember { mutableStateOf(false) }
  var selectedListingForDetail by remember { mutableStateOf<Listing?>(null) }

  val citySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val adminSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val contactAdminSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val allListings by repository.listings.collectAsState(initial = emptyList())
  val savedIds by repository.savedIds.collectAsState(initial = emptySet())
  val userProfile by repository.userProfile.collectAsState(initial = com.example.ui.screens.defaultUserProfile)
  val systemSettings by repository.systemSettings.collectAsState(initial = com.example.domain.model.SystemDynamicSettings())

  PersianRtlLayout {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        if (currentRoute in listOf(NavRoute.HOME, NavRoute.FAVORITES)) {
          AppTopBar(
            currentCityName = selectedCityName,
            unreadNotificationCount = unreadNotificationsCount,
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            onCityClick = { showCitySheet = true },
            onNotificationsClick = {
              unreadNotificationsCount = 0
              navController.navigate(NavRoute.NOTIFICATIONS)
            },
            onSupportClick = { showContactAdminSheet = true }
          )
        }
      },
      bottomBar = {
        AppBottomBar(
          currentRoute = currentRoute,
          onNavigate = { route ->
            navController.navigate(route) {
              popUpTo(NavRoute.HOME) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = NavRoute.HOME,
        modifier = Modifier.padding(innerPadding)
      ) {
        composable(NavRoute.HOME) {
          HomeScreen(
            repository = repository,
            currentCityName = selectedCityName,
            searchQuery = searchQuery,
            snackbarHostState = snackbarHostState,
            onNavigateToAddListing = { navController.navigate(NavRoute.ADD_LISTING) },
            onContactAdminClick = { showContactAdminSheet = true },
            onUpgradeClick = { showPlansDialog = true }
          )
        }

        composable(NavRoute.FAVORITES) {
          FavoritesScreen(
            repository = repository,
            snackbarHostState = snackbarHostState,
            onListingClick = { item -> selectedListingForDetail = item },
            onExploreClick = { navController.navigate(NavRoute.HOME) }
          )
        }

        composable(NavRoute.ADD_LISTING) {
          AddListingScreen(
            repository = repository,
            onListingCreated = {
              navController.navigate(NavRoute.HOME) {
                popUpTo(NavRoute.HOME) { inclusive = true }
              }
              scope.launch {
                snackbarHostState.showSnackbar("آگهی شما در «بده بره» با موفقیت منتشر شد 🎉")
              }
            },
            onUpgradePlanClick = { showPlansDialog = true }
          )
        }

        composable(NavRoute.NOTIFICATIONS) {
          NotificationsScreen()
        }

        composable(NavRoute.PROFILE) {
          ProfileScreen(
            repository = repository,
            onUpgradePlanClick = { showPlansDialog = true },
            onContactAdminClick = { showContactAdminSheet = true },
            onOpenAdminPanel = { showAdminSheet = true }
          )
        }
      }

      // City Selector BottomSheet
      if (showCitySheet) {
        CitySelectorSheet(
          currentCity = selectedCityName,
          onCitySelected = { city ->
            selectedCityName = city.name
          },
          onDismiss = { showCitySheet = false },
          sheetState = citySheetState
        )
      }

      // Membership Plans Dialog
      if (showPlansDialog) {
        MembershipPlansDialog(
          currentTier = userProfile.plan,
          onDismiss = { showPlansDialog = false },
          onPlanSelected = { newTier ->
            repository.upgradeMembership(newTier)
            showPlansDialog = false
            scope.launch {
              val tierName = when (newTier) {
                MembershipTier.DIAMOND -> "الماس تجاری (Diamond VIP)"
                MembershipTier.GOLD -> "طلایی (VIP)"
                MembershipTier.SILVER -> "نقره‌ای"
                MembershipTier.FREE -> "عادی (رایگان)"
              }
              snackbarHostState.showSnackbar("طرح کاربری شما به «$tierName» تغییر یافت.")
            }
          }
        )
      }

      // Admin Moderation BottomSheet
      if (showAdminSheet) {
        AdminModerationSheet(
          listings = allListings,
          systemSettings = systemSettings,
          onUpdateSettings = { updatedSettings ->
            repository.updateSettings(updatedSettings)
            scope.launch {
              snackbarHostState.showSnackbar("تنظیمات مدیریتی با موفقیت ذخیره شد.")
            }
          },
          onStatusChange = { id, status ->
            repository.updateListingStatus(id, status)
            scope.launch {
              snackbarHostState.showSnackbar("وضعیت آگهی با موفقیت بروزرسانی شد.")
            }
          },
          onDismiss = { showAdminSheet = false },
          sheetState = adminSheetState
        )
      }

      // Contact Admin Sheet
      if (showContactAdminSheet) {
        ContactAdminSheet(
          onDismiss = { showContactAdminSheet = false },
          onTicketSent = {
            showContactAdminSheet = false
            scope.launch {
              snackbarHostState.showSnackbar("پیام شما برای مدیر ارسال شد.")
            }
          },
          sheetState = contactAdminSheetState
        )
      }

      // Favorites / Detail Sheet
      selectedListingForDetail?.let { detailListing ->
        val accessStatus = remember(detailListing, userProfile.plan) {
          repository.getListingAccessStatus(detailListing, userProfile.plan)
        }

        ListingDetailSheet(
          listing = detailListing,
          isFavorite = savedIds.contains(detailListing.id),
          isOwner = repository.isUserOwner(detailListing),
          accessStatus = accessStatus,
          onUpgradeClick = {
            selectedListingForDetail = null
            showPlansDialog = true
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
            selectedListingForDetail = null
          },
          onMarkAsGone = { item ->
            repository.markAsGone(item.id)
            scope.launch {
              snackbarHostState.showSnackbar("آگهی «${item.title}» اهدا شد و از آگهی‌های عمومی برداشته شد 🎉")
            }
            selectedListingForDetail = null
          },
          onCancelReservation = { item ->
            repository.cancelReservation(item.id)
            scope.launch {
              snackbarHostState.showSnackbar("رزرو آگهی لغو شد.")
            }
            selectedListingForDetail = null
          },
          onContactAdmin = {
            selectedListingForDetail = null
            showContactAdminSheet = true
          },
          sheetState = detailSheetState
        )
      }
    }
  }
}
