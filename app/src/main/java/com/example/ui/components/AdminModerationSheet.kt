package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.domain.model.Listing
import com.example.domain.model.ListingStatus
import com.example.domain.model.PageBanner
import com.example.domain.model.SystemDynamicSettings
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationSheet(
  listings: List<Listing>,
  systemSettings: SystemDynamicSettings = SystemDynamicSettings(),
  banners: Map<String, PageBanner> = emptyMap(),
  onUpdateBannerStatus: (String, Boolean) -> Unit = { _, _ -> },
  onUpdateSettings: (SystemDynamicSettings) -> Unit = {},
  onStatusChange: (String, ListingStatus) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  var selectedAdminTab by remember { mutableIntStateOf(0) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("admin_moderation_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AdminPanelSettings,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "پنل مدیریت و نظارت (Admin)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "نظارت آگهی‌ها، بنرها و تنظیمات اولویت پکیج‌ها",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "بستن",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab switcher
      TabRow(
        selectedTabIndex = selectedAdminTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        contentColor = EmeraldPrimary,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
      ) {
        Tab(
          selected = selectedAdminTab == 0,
          onClick = { selectedAdminTab = 0 },
          text = {
            Text(
              text = "نظارت آگهی‌ها (${PersianUtils.formatNumber(listings.size)})",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        )
        Tab(
          selected = selectedAdminTab == 1,
          onClick = { selectedAdminTab = 1 },
          text = {
            Text(
              text = "مدیریت بنرها",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        )
        Tab(
          selected = selectedAdminTab == 2,
          onClick = { selectedAdminTab = 2 },
          text = {
            Text(
              text = "تنظیمات پکیج‌ها",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      when (selectedAdminTab) {
        0 -> {
          // Listings moderation list
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(listings, key = { it.id }) { item ->
              ModerationItemCard(
                listing = item,
                onApprove = { onStatusChange(item.id, ListingStatus.PUBLIC) },
                onReserve = { onStatusChange(item.id, ListingStatus.RESERVED) },
                onReject = { onStatusChange(item.id, ListingStatus.REJECTED) }
              )
            }
          }
        }
        1 -> {
          // Banners Management
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            val bannerEntries = listOf(
              Triple("home_free_gift", "بنر صفحه هدایای رایگان", "هدایای کاملاً رایگان هموطنان"),
              Triple("home_discount", "بنر صفحه تخفیف‌ها", "تخفیف‌ها و کوپن‌های اختصاصی"),
              Triple("home_request", "بنر صفحه نیازمندی‌ها", "درخواست‌های نیازمندی و یاری"),
              Triple("home", "بنر صفحه اصلی (عمومی)", "بده بره؛ مهربونی رو تکثیر کن")
            )

            items(bannerEntries) { (pageKey, title, sub) ->
              val currentBanner = banners[pageKey]
              val isBannerActive = currentBanner?.isActive ?: true

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Image, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                      Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }

                  Switch(
                    checked = isBannerActive,
                    onCheckedChange = { checked ->
                      onUpdateBannerStatus(pageKey, checked)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                  )
                }
              }
            }
          }
        }
        2 -> {
          // Dynamic System Settings Panel
          LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AmberSecondary.copy(alpha = 0.08f))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "⏱️ زمان‌بندی اولویت دسترسی زودهنگام پکیج‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberSecondary
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "آگهی‌ها در ساعات اولیه فقط توسط دارندگان پکیج‌های مجاز قابل انتخاب و رزرو هستند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )
                  Spacer(modifier = Modifier.height(10.dp))

                  SettingStepperRow(
                    title = "زمان دسترسی زودهنگام طلایی / الماس:",
                    unit = "ساعت قبل از عموم",
                    value = systemSettings.goldEarlyAccessHours,
                    minValue = 1,
                    maxValue = 24,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(goldEarlyAccessHours = newVal, diamondEarlyAccessHours = newVal))
                    }
                  )

                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                  SettingStepperRow(
                    title = "زمان دسترسی زودهنگام نقره‌ای:",
                    unit = "ساعت قبل از عموم",
                    value = systemSettings.silverEarlyAccessHours,
                    minValue = 1,
                    maxValue = systemSettings.goldEarlyAccessHours - 1,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(silverEarlyAccessHours = newVal))
                    }
                  )

                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                  SettingStepperRow(
                    title = "بازه نمایش «همین الان رایگان شد»:",
                    unit = "ساعت پس از آزادسازی",
                    value = systemSettings.justBecameAvailableDurationHours,
                    minValue = 1,
                    maxValue = 72,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(justBecameAvailableDurationHours = newVal))
                    }
                  )
                }
              }
            }

            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "🎯 سقف مجاز رزرو روزانه بر اساس پکیج‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                  )
                  Spacer(modifier = Modifier.height(10.dp))

                  SettingStepperRow(
                    title = "سقف روزانه پلن عادی (رایگان):",
                    unit = "کارت در روز",
                    value = systemSettings.freeDailyReserveLimit,
                    minValue = 1,
                    maxValue = 10,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(freeDailyReserveLimit = newVal))
                    }
                  )

                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                  SettingStepperRow(
                    title = "سقف روزانه پلن نقره‌ای:",
                    unit = "کارت در روز",
                    value = systemSettings.silverDailyReserveLimit,
                    minValue = 2,
                    maxValue = 20,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(silverDailyReserveLimit = newVal))
                    }
                  )

                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                  SettingStepperRow(
                    title = "سقف روزانه پلن طلایی:",
                    unit = "کارت در روز",
                    value = systemSettings.goldDailyReserveLimit,
                    minValue = 5,
                    maxValue = 50,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(goldDailyReserveLimit = newVal))
                    }
                  )

                  HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                  SettingStepperRow(
                    title = "سقف روزانه پلن الماس VIP:",
                    unit = "کارت در روز",
                    value = systemSettings.diamondDailyReserveLimit,
                    minValue = 10,
                    maxValue = 200,
                    onValueChange = { newVal ->
                      onUpdateSettings(systemSettings.copy(diamondDailyReserveLimit = newVal))
                    }
                  )
                }
              }
            }

            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoAccent.copy(alpha = 0.08f))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = "💎 قوانین پکیج الماس و کدهای تخفیف",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IndigoAccent
                  )
                  Spacer(modifier = Modifier.height(10.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "الزام پکیج الماس برای ثبت کوپن تخفیف",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = "فقط کسب‌وکارها و کاربران پکیج الماس می‌توانند آگهی کوپن/تخفیف ثبت کنند",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                      )
                    }
                    Switch(
                      checked = systemSettings.requireDiamondForDiscounts,
                      onCheckedChange = { checked ->
                        onUpdateSettings(systemSettings.copy(requireDiamondForDiscounts = checked))
                      },
                      colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingStepperRow(
  title: String,
  unit: String,
  value: Int,
  minValue: Int,
  maxValue: Int,
  onValueChange: (Int) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "${PersianUtils.formatNumber(value)} $unit",
        style = MaterialTheme.typography.labelMedium,
        color = EmeraldPrimary,
        fontWeight = FontWeight.Bold
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .size(32.dp)
          .clickable(enabled = value > minValue) {
            onValueChange(value - 1)
          }
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "کاهش",
            modifier = Modifier.size(16.dp),
            tint = if (value > minValue) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
        }
      }

      Text(
        text = PersianUtils.formatNumber(value),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .size(32.dp)
          .clickable(enabled = value < maxValue) {
            onValueChange(value + 1)
          }
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "افزایش",
            modifier = Modifier.size(16.dp),
            tint = if (value < maxValue) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
          )
        }
      }
    }
  }
}

@Composable
private fun ModerationItemCard(
  listing: Listing,
  onApprove: () -> Unit,
  onReserve: () -> Unit,
  onReject: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = listing.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )
        ListingStateBadge(status = listing.status)
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "ثبت‌کننده: ${listing.ownerDisplayName} (${PersianUtils.formatMaskedPhone(listing.ownerPhone ?: "")}) • ${listing.city}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onApprove,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("انتشار آگهی", fontSize = 11.sp, color = Color.White)
        }

        OutlinedButton(
          onClick = onReserve,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("رزرو", fontSize = 11.sp)
        }

        OutlinedButton(
          onClick = onReject,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.weight(1f).height(36.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("رد", fontSize = 11.sp)
        }
      }
    }
  }
}
