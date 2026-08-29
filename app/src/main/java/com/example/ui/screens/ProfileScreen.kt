package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IranLocationsData
import com.example.data.MockListingRepository
import com.example.domain.model.Listing
import com.example.domain.model.ListingStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.domain.model.UserProfile
import com.example.ui.components.PersianUtils
import com.example.ui.components.TierBadge
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  repository: MockListingRepository,
  onUpgradePlanClick: () -> Unit,
  onContactAdminClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val userProfile by repository.userProfile.collectAsState(initial = MockListingRepository.guestUserProfile)
  val systemSettings by repository.systemSettings.collectAsState(initial = com.example.domain.model.SystemDynamicSettings())
  val myOwnedListings by repository.getUserOwnedListings().collectAsState(initial = emptyList())
  val scope = rememberCoroutineScope()

  var showLogoutConfirmDialog by remember { mutableStateOf(false) }
  var showAuthDialog by remember { mutableStateOf(false) }

  // Form states for login/register
  var authPhone by remember { mutableStateOf("") }
  var authName by remember { mutableStateOf("") }
  var authProvince by remember { mutableStateOf("تهران") }
  var authCity by remember { mutableStateOf("تهران") }
  var authProvinceDropdownExpanded by remember { mutableStateOf(false) }
  var authCityDropdownExpanded by remember { mutableStateOf(false) }
  var authNationalId by remember { mutableStateOf("") }
  var authOtpCode by remember { mutableStateOf("") }
  var authRulesAccepted by remember { mutableStateOf(false) }
  var registrationToken by remember { mutableStateOf<String?>(null) }
  var registerStep by remember { mutableIntStateOf(1) } // 1: Phone input, 2: OTP verification, 3: Profile info
  var otpCountdown by remember { mutableIntStateOf(0) }
  var isAuthenticating by remember { mutableStateOf(false) }
  var authErrorMessage by remember { mutableStateOf<String?>(null) }
  var authSuccessMessage by remember { mutableStateOf<String?>(null) }
  var authTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

  LaunchedEffect(otpCountdown) {
    if (otpCountdown > 0) {
      kotlinx.coroutines.delay(1000L)
      otpCountdown -= 1
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("profile_screen")
  ) {
    if (userProfile.isLoggedIn) {
      // 1. LOGGED-IN USER PROFILE HEADER
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Avatar
          Box(
            modifier = Modifier
              .size(76.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(44.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Display Name
          Text(
            text = userProfile.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(4.dp))

          // Location & Plan
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "${userProfile.province}، ${userProfile.city}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("•", color = MaterialTheme.colorScheme.outline)
            TierBadge(tier = userProfile.plan)
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Privacy info notes (Masked phone & National ID)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = PersianUtils.formatMaskedPhone(userProfile.rawPhone.ifBlank { userProfile.mobileNumberMasked }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "کد ملی: ${PersianUtils.formatMaskedNationalId(userProfile.rawNationalId.ifBlank { userProfile.nationalIdMasked })}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Logout button inside profile
          OutlinedButton(
            onClick = { showLogoutConfirmDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralTertiary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.ExitToApp,
              contentDescription = null,
              tint = CoralTertiary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("خروج از حساب کاربری", fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      // 2. GUEST / LOGGED-OUT CARD
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Login,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "ورود یا ثبت‌نام در بده بره",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "برای ثبت آگهی، رزرو هدایا و مدیریت فعالیت‌ها، وارد حساب کاربری خود شوید.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              authErrorMessage = null
              showAuthDialog = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ورود / ثبت‌نام سریع", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. User Owned Listings Section (آگهی‌های من)
    if (userProfile.isLoggedIn) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "آگهی‌های ثبت شده شما",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "${PersianUtils.formatNumber(myOwnedListings.size)} آگهی",
              style = MaterialTheme.typography.labelMedium,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (myOwnedListings.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "شما هنوز آگهی‌ای ثبت نکرده‌اید. با فشردن دکمه «+ ثبت آگهی» اولین هدیه را ثبت کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              myOwnedListings.forEach { listing ->
                MyListingItemRow(
                  listing = listing,
                  onDelete = { repository.deleteListing(listing.id) },
                  onMarkAsGone = { repository.markAsGone(listing.id) }
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Activity Stats Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        StatItem(
          title = "هدایای اهدا شده",
          count = userProfile.successfulOffersCount,
          icon = Icons.Default.CardGiftcard,
          accentColor = EmeraldPrimary
        )
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
        )
        StatItem(
          title = "درخواست‌های موفق",
          count = userProfile.completedRequestsCount,
          icon = Icons.Default.CheckCircle,
          accentColor = AmberSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Daily Reservation Limit Status Card
    val dailyLimit = repository.getDailyReservationLimit(userProfile.plan)
    val usedReservations = userProfile.dailyReservationsCount
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bookmark,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "سهمیه رزرو کارت‌های امروز",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Text(
            text = "${PersianUtils.formatNumber(usedReservations)} از ${PersianUtils.formatNumber(dailyLimit)} کارت",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (usedReservations >= dailyLimit) MaterialTheme.colorScheme.error else EmeraldPrimary
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        val progress = (usedReservations.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction = progress)
              .fillMaxHeight()
              .clip(RoundedCornerShape(4.dp))
              .background(if (usedReservations >= dailyLimit) MaterialTheme.colorScheme.error else EmeraldPrimary)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = when (userProfile.plan) {
            MembershipTier.FREE -> "سقف پلن عادی: حداکثر ${PersianUtils.formatNumber(systemSettings.freeDailyReserveLimit)} رزرو در روز. برای افزایش سقف، طرح خود را ارتقا دهید."
            MembershipTier.SILVER -> "سقف پلن نقره‌ای: حداکثر ${PersianUtils.formatNumber(systemSettings.silverDailyReserveLimit)} رزرو در روز."
            MembershipTier.GOLD -> "سقف پلن طلایی: حداکثر ${PersianUtils.formatNumber(systemSettings.goldDailyReserveLimit)} رزرو در روز."
            MembershipTier.DIAMOND -> "سقف پلن الماس: حداکثر ${PersianUtils.formatNumber(systemSettings.diamondDailyReserveLimit)} رزرو در روز."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Free Listing Quota & Countdown Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CardGiftcard,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "سهمیه ثبت آگهی رایگان",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (userProfile.canPostListing) EmeraldPrimary.copy(alpha = 0.12f) else CoralTertiary.copy(alpha = 0.12f)
          ) {
            Text(
              text = if (userProfile.canPostListing) "آماده ثبت آگهی" else "در انتظار بازنشانی",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (userProfile.canPostListing) EmeraldPrimary else CoralTertiary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EmeraldPrimary.copy(alpha = 0.07f))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "زمان باز شدن سهمیه بعدی:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (userProfile.canPostListing) "هم‌اکنون فعال است ✅" else "هر ۲۴ ساعت (ساعت ۰۰:۰۰ بامداد)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (userProfile.canPostListing) EmeraldPrimary else AmberSecondary
              )
            }
            Text(
              text = "کاربران می‌توانند به صورت روزانه از سهمیه رایگان ثبت هدایا استفاده نمایند. دارندگان اشتراک‌های نقره‌ای، طلایی و الماس از سهمیه بیشتر برخوردارند.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Membership Plan Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Diamond,
              contentDescription = null,
              tint = AmberSecondary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "طرح‌های اشتراک بده بره",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          TierBadge(tier = userProfile.plan)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "طرح طلایی (${PersianUtils.formatNumber(systemSettings.goldEarlyAccessHours)} ساعت اولویت) و نقره‌ای (${PersianUtils.formatNumber(systemSettings.silverEarlyAccessHours)} ساعت اولویت) برای مشاهده و رزرو زودهنگام آگهی‌ها، و طرح الماس (VIP تجاری) برای ثبت کدهای تخفیف و بن‌های خرید فروشگاهی.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onUpgradePlanClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("upgrade_plan_btn")
        ) {
          Text(
            text = "مشاهده و ارتقای طرح اشتراک",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Quick User Actions & Support
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuItem(
          title = "پشتیبانی، ارتباط و ارسال پیام",
          icon = Icons.Default.SupportAgent,
          onClick = onContactAdminClick,
          testTag = "contact_support_item"
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }

  // Logout Confirm Dialog
  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      title = { Text("خروج از حساب کاربری", fontWeight = FontWeight.Bold) },
      text = { Text("آیا مطمئن هستید که می‌خواهید از حساب کاربری خود خارج شوید؟") },
      confirmButton = {
        Button(
          onClick = {
            repository.logout()
            showLogoutConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralTertiary)
        ) {
          Text("بله، خروج", color = Color.White)
        }
      },
      dismissButton = {
        Button(
          onClick = { showLogoutConfirmDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    )
  }

  // Auth Dialog (Separate Login & Register with SMS.ir OTP for Registration)
  if (showAuthDialog) {
    AlertDialog(
      onDismissRequest = {
        if (!isAuthenticating) {
          showAuthDialog = false
          authErrorMessage = null
          authSuccessMessage = null
        }
      },
      title = {
        Text(
          text = if (authTab == 0) "ورود به حساب کاربری" else "ثبت‌نام کاربر جدید",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Tab selection between Login and Register
          TabRow(
            selectedTabIndex = authTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Tab(
              selected = authTab == 0,
              onClick = {
                authTab = 0
                authErrorMessage = null
                authSuccessMessage = null
                registerStep = 1
                authOtpCode = ""
                registrationToken = null
              },
              text = { Text("ورود", fontWeight = FontWeight.Bold) }
            )
            Tab(
              selected = authTab == 1,
              onClick = {
                authTab = 1
                authErrorMessage = null
                authSuccessMessage = null
              },
              text = { Text("ثبت‌نام جدید", fontWeight = FontWeight.Bold) }
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          if (authTab == 0) {
            // ==================== TAB 0: LOGIN ====================
            Text(
              text = "برای ورود به حساب کاربری، لطفاً شماره موبایلی را که قبلاً با آن ثبت‌نام کرده‌اید وارد کنید:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 20.sp
            )

            OutlinedTextField(
              value = authPhone,
              onValueChange = {
                authPhone = it
                authErrorMessage = null
              },
              label = { Text("شماره موبایل (مثال: 09123456789)") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            )
          } else {
            // ==================== TAB 1: REGISTRATION VIA OTP ====================
            when (registerStep) {
              1 -> {
                // Step 1: Mobile input for OTP
                Text(
                  text = "برای ثبت‌نام در سامانه بده‌بره، ابتدا شماره موبایل خود را جهت دریافت کد تأیید پیامکی وارد نمایید:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 20.sp
                )

                OutlinedTextField(
                  value = authPhone,
                  onValueChange = {
                    authPhone = it
                    authErrorMessage = null
                  },
                  label = { Text("شماره موبایل (مثال: 09123456789)") },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                )
              }

              2 -> {
                // Step 2: OTP Code input & Resend
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.08f))
                    .padding(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = "کد تأیید به شماره زیر ارسال شد:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Text(
                        text = PersianUtils.formatDigits(authPhone),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    }
                    OutlinedButton(
                      onClick = {
                        registerStep = 1
                        authOtpCode = ""
                        authErrorMessage = null
                      },
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                      Text("ویرایش شماره", fontSize = 12.sp)
                    }
                  }
                }

                OutlinedTextField(
                  value = authOtpCode,
                  onValueChange = {
                    if (it.length <= 6) {
                      authOtpCode = it
                      authErrorMessage = null
                    }
                  },
                  label = { Text("کد تأیید ۵ رقمی پیامک شده") },
                  singleLine = true,
                  textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  if (otpCountdown > 0) {
                    Text(
                      text = "ارسال مجدد تا ${PersianUtils.formatDigits(otpCountdown.toString())} ثانیه دیگر",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  } else {
                    Row(
                      modifier = Modifier.clickable {
                        isAuthenticating = true
                        authErrorMessage = null
                        scope.launch {
                          val res = repository.requestRegistrationOtp(authPhone)
                          isAuthenticating = false
                          if (res.first) {
                            otpCountdown = 60
                            authSuccessMessage = "کد تأیید مجدداً پیامک شد."
                          } else {
                            authErrorMessage = res.second
                          }
                        }
                      },
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("ارسال مجدد کد تأیید", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              3 -> {
                // Step 3: Complete profile info
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.12f))
                    .padding(10.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "شماره ${PersianUtils.formatDigits(authPhone)} با موفقیت تأیید شد ✅",
                      style = MaterialTheme.typography.bodySmall,
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Text(
                  text = "اطلاعات تکمیلی حساب کاربری خود را وارد فرمایید:",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                  value = authName,
                  onValueChange = {
                    authName = it
                    authErrorMessage = null
                  },
                  label = { Text("نام و نام خانوادگی *") },
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                )

                // Province & City Selection
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Province Dropdown
                  ExposedDropdownMenuBox(
                    expanded = authProvinceDropdownExpanded,
                    onExpandedChange = { authProvinceDropdownExpanded = !authProvinceDropdownExpanded },
                    modifier = Modifier.weight(1f)
                  ) {
                    OutlinedTextField(
                      value = authProvince,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text("استان") },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authProvinceDropdownExpanded) },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                      expanded = authProvinceDropdownExpanded,
                      onDismissRequest = { authProvinceDropdownExpanded = false }
                    ) {
                      IranLocationsData.provinces.forEach { prov ->
                        DropdownMenuItem(
                          text = { Text(prov, fontWeight = if (prov == authProvince) FontWeight.Bold else FontWeight.Normal) },
                          onClick = {
                            authProvince = prov
                            val cities = IranLocationsData.getCitiesForProvince(prov)
                            authCity = cities.firstOrNull() ?: prov
                            authProvinceDropdownExpanded = false
                          }
                        )
                      }
                    }
                  }

                  // City Dropdown
                  ExposedDropdownMenuBox(
                    expanded = authCityDropdownExpanded,
                    onExpandedChange = { authCityDropdownExpanded = !authCityDropdownExpanded },
                    modifier = Modifier.weight(1f)
                  ) {
                    val availableCities = remember(authProvince) {
                      IranLocationsData.getCitiesForProvince(authProvince)
                    }

                    OutlinedTextField(
                      value = authCity,
                      onValueChange = {},
                      readOnly = true,
                      label = { Text("شهر") },
                      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authCityDropdownExpanded) },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                      expanded = authCityDropdownExpanded,
                      onDismissRequest = { authCityDropdownExpanded = false }
                    ) {
                      availableCities.forEach { cName ->
                        DropdownMenuItem(
                          text = { Text(cName, fontWeight = if (cName == authCity) FontWeight.Bold else FontWeight.Normal) },
                          onClick = {
                            authCity = cName
                            authCityDropdownExpanded = false
                          }
                        )
                      }
                    }
                  }
                }

                OutlinedTextField(
                  value = authNationalId,
                  onValueChange = {
                    authNationalId = it
                    authErrorMessage = null
                  },
                  label = { Text("کد ملی ۱۰ رقمی (اختیاری)") },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Display Terms and Conditions once upon registration
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "قوانین و مقررات سامانه بده‌بره",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }

                    Text(
                      text = "۱. تمام هدایای ثبت شده در این سامانه ۱۰۰٪ رایگان بوده و هرگونه معامله مالی ممنوع است.\n۲. ثبت آگهی‌های مغایر با قوانین کشور، سلاح، مواد مخدر یا کالاهای قاچاق اکیداً ممنوع بوده و منجر به مسدودی دائم حساب خواهد شد.\n۳. رزرو اقلام بر اساس اولویت ثبت و سهمیه روزانه انجام می‌پذیرد.\n۴. حفظ احترام، امانتداری و حسن نیت در تحویل کالا الزامی است.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      lineHeight = 20.sp,
                      fontSize = 11.sp
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable { authRulesAccepted = !authRulesAccepted },
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Checkbox(
                        checked = authRulesAccepted,
                        onCheckedChange = { authRulesAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                      )
                      Text(
                        text = "قوانین و مقررات را خوانده و می‌پذیرم *",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (authRulesAccepted) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                      )
                    }
                  }
                }
              }
            }
          }

          authSuccessMessage?.let { msg ->
            Text(text = msg, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
          }

          authErrorMessage?.let { err ->
            Text(text = err, color = CoralTertiary, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            authErrorMessage = null
            authSuccessMessage = null
            val cleanPhone = PersianUtils.normalizeIranianMobile(authPhone)

            if (authTab == 0) {
              // Standard Login
              if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
                authErrorMessage = "شماره موبایل وارد شده معتبر نیست. لطفاً شماره ۱۱ رقمی را به صورت ۰۹۱۲۳۴۵۶۷۸۹ وارد کنید."
                return@Button
              }

              isAuthenticating = true
              scope.launch {
                val result = repository.login(authPhone)
                isAuthenticating = false
                if (result.first) {
                  showAuthDialog = false
                } else {
                  authErrorMessage = result.second ?: "حساب کاربری با این شماره یافت نشد. در صورت عدم ثبت‌نام، لطفاً از بخش «ثبت‌نام جدید» اقدام فرمایید."
                }
              }
            } else {
              // Registration OTP Steps
              when (registerStep) {
                1 -> {
                  if (!PersianUtils.isValidIranianMobile(cleanPhone)) {
                    authErrorMessage = "شماره موبایل وارد شده معتبر نیست (مثال: ۰۹۱۲۳۴۵۶۷۸۹)."
                    return@Button
                  }
                  isAuthenticating = true
                  scope.launch {
                    val result = repository.requestRegistrationOtp(authPhone)
                    isAuthenticating = false
                    if (result.first) {
                      registerStep = 2
                      otpCountdown = 60
                      authSuccessMessage = result.second ?: "کد تأیید برای شماره شما ارسال شد."
                    } else {
                      authErrorMessage = result.second ?: "ارسال کد با خطا مواجه شد."
                    }
                  }
                }

                2 -> {
                  val code = PersianUtils.toEnglishDigits(authOtpCode.trim())
                  if (code.length < 4) {
                    authErrorMessage = "لطفاً کد تأیید ۵ رقمی را به صورت کامل وارد کنید."
                    return@Button
                  }
                  isAuthenticating = true
                  scope.launch {
                    val result = repository.verifyRegistrationOtp(authPhone, code)
                    isAuthenticating = false
                    if (result.first) {
                      registrationToken = result.second.first
                      registerStep = 3
                      authSuccessMessage = null
                    } else {
                      authErrorMessage = result.second.second ?: "کد وارد شده اشتباه یا منقضی شده است."
                    }
                  }
                }

                3 -> {
                  if (authName.isBlank()) {
                    authErrorMessage = "لطفاً نام و نام خانوادگی خود را وارد کنید."
                    return@Button
                  }
                  if (!authRulesAccepted) {
                    authErrorMessage = "برای تکمیل ثبت‌نام، لطفاً تیک پذیرش قوانین و مقررات را بزنید."
                    return@Button
                  }
                  if (authNationalId.isNotBlank() && !PersianUtils.isValidNationalId(authNationalId)) {
                    authErrorMessage = "کد ملی ۱۰ رقمی وارد شده نامعتبر است."
                    return@Button
                  }
                  val token = registrationToken
                  if (token.isNullOrBlank()) {
                    authErrorMessage = "اعتبار سنجی پیامکی شماره منقضی شده است. لطفاً مجدداً کد دریافت کنید."
                    registerStep = 1
                    return@Button
                  }

                  isAuthenticating = true
                  scope.launch {
                    val result = repository.register(
                      name = authName,
                      phone = authPhone,
                      city = authCity,
                      nationalId = authNationalId,
                      registrationToken = token
                    )
                    isAuthenticating = false
                    if (result.first) {
                      showAuthDialog = false
                    } else {
                      authErrorMessage = result.second ?: "ثبت‌نام با خطا مواجه شد."
                    }
                  }
                }
              }
            }
          },
          enabled = !isAuthenticating && (authPhone.isNotBlank() || authTab == 1 && registerStep == 3),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          if (isAuthenticating) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
          } else {
            val label = when {
              authTab == 0 -> "ورود به حساب"
              registerStep == 1 -> "دریافت کد تأیید پیامکی"
              registerStep == 2 -> "تأیید کد و ادامه"
              else -> "تکمیل ثبت‌نام و ورود"
            }
            Text(label, fontWeight = FontWeight.Bold)
          }
        }
      },
      dismissButton = {
        Button(
          onClick = {
            showAuthDialog = false
            authErrorMessage = null
            authSuccessMessage = null
            registerStep = 1
            authOtpCode = ""
            registrationToken = null
          },
          enabled = !isAuthenticating,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    )
  }
}

@Composable
private fun MyListingItemRow(
  listing: Listing,
  onDelete: () -> Unit,
  onMarkAsGone: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = listing.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        // Status Tag
        val (statusText, statusColor) = when (listing.status) {
          ListingStatus.PUBLIC -> "منتشر شده ✅" to EmeraldPrimary
          ListingStatus.PENDING_REVIEW -> "در انتظار بررسی ⏳" to AmberSecondary
          ListingStatus.RESERVED -> "رزرو شده 🔒" to Color(0xFF0284C7)
          ListingStatus.COMPLETED -> "اهدا شده 🎉" to Color.Gray
          ListingStatus.REJECTED -> "رد شده ❌" to CoralTertiary
          else -> "پیش‌نویس" to Color.Gray
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = listing.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (listing.status != ListingStatus.COMPLETED) {
          OutlinedButton(
            onClick = onMarkAsGone,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text("ثبت اهدا شد", fontSize = 11.sp, color = EmeraldPrimary)
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CoralTertiary, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
private fun StatItem(
  title: String,
  count: Int,
  icon: ImageVector,
  accentColor: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = PersianUtils.formatNumber(count),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun ProfileMenuItem(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EmeraldPrimary,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Icon(
      imageVector = Icons.Default.ChevronLeft,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
