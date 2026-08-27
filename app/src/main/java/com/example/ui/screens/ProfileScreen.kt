package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockListingRepository
import com.example.domain.model.MembershipTier
import com.example.domain.model.UserProfile
import com.example.ui.components.PersianUtils
import com.example.ui.components.TierBadge
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ProfileScreen(
  repository: MockListingRepository,
  onUpgradePlanClick: () -> Unit,
  onContactAdminClick: () -> Unit = {},
  onOpenAdminPanel: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val userProfile by repository.userProfile.collectAsState(initial = defaultUserProfile)
  val systemSettings by repository.systemSettings.collectAsState(initial = com.example.domain.model.SystemDynamicSettings())
  var showRegistryDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("profile_screen")
  ) {
    // Profile Header Card
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
              text = userProfile.mobileNumberMasked,
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
              text = "کد ملی: ${userProfile.nationalIdMasked}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

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
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuItem(
          title = "پنل نظارت و تنظیمات مدیریتی پکیج‌ها",
          icon = Icons.Default.AdminPanelSettings,
          onClick = onOpenAdminPanel,
          testTag = "menu_admin_panel"
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ProfileMenuItem(
          title = "ارتباط و پشتیبانی با مدیر سامانه",
          icon = Icons.Default.SupportAgent,
          onClick = onContactAdminClick,
          testTag = "menu_contact_admin"
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ProfileMenuItem(
          title = "کلید امنیتی رجیستری و لایسنس اپلیکیشن",
          icon = Icons.Default.Key,
          onClick = { showRegistryDialog = true },
          testTag = "menu_registry_key"
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ProfileMenuItem(
          title = "راهنمای استفاده و قوانین بده بره",
          icon = Icons.Default.MenuBook,
          onClick = { },
          testTag = "menu_guide"
        )
      }
    }

    Spacer(modifier = Modifier.height(80.dp))
  }

  // Registry Key Dialog
  if (showRegistryDialog) {
    AlertDialog(
      onDismissRequest = { showRegistryDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("کلید رجیستری و امضای اپ", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("کلید امضای رسمی برنامه (Keystore) با موفقیت تولید و محافظت شد.")
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(10.dp)
          ) {
            Column {
              Text("رمز عبور کلید (Registry Password):", style = MaterialTheme.typography.labelSmall)
              Text("Meftah9260", fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary, fontSize = 16.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("مسیر فایل دانلودی در هاست سی‌پنل:", style = MaterialTheme.typography.labelSmall)
              Text("/downloads/bedebere-release.keystore", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showRegistryDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("تایید و بستن")
        }
      }
    )
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

val defaultUserProfile = UserProfile(
  id = "u_default",
  displayName = "محمد مهربخش",
  mobileNumberMasked = "۰۹۱۲***۴۵۶۷",
  nationalIdMasked = "۰۰۱******۸",
  province = "تهران",
  city = "تهران",
  plan = MembershipTier.GOLD,
  successfulOffersCount = 4,
  completedRequestsCount = 2
)
