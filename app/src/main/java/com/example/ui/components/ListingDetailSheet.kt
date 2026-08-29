package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Listing
import com.example.domain.model.ListingAccessStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailSheet(
  listing: Listing,
  isFavorite: Boolean,
  isOwner: Boolean = false,
  isReserver: Boolean = false,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onDismiss: () -> Unit,
  onFavoriteClick: (String) -> Unit,
  onReserveClick: (Listing) -> Unit,
  onMarkAsGone: (Listing) -> Unit = {},
  onCancelReservation: (Listing) -> Unit = {},
  onContactAdmin: () -> Unit = {},
  onUpgradeClick: (() -> Unit)? = null,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var copiedSuccess by remember { mutableStateOf(false) }
  val targetPhone = listing.ownerPhone?.takeIf { it.isNotBlank() } ?: "09121234567"

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("listing_detail_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Top Bar in sheet
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                when (listing.type) {
                  ListingType.FREE_GIFT -> EmeraldPrimary.copy(alpha = 0.15f)
                  ListingType.DISCOUNT -> AmberSecondary.copy(alpha = 0.15f)
                  ListingType.REQUEST -> MaterialTheme.colorScheme.primaryContainer
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getCategoryVector(listing.categoryIcon),
              contentDescription = listing.categoryNameFa,
              tint = when (listing.type) {
                ListingType.FREE_GIFT -> EmeraldPrimary
                ListingType.DISCOUNT -> AmberSecondary
                ListingType.REQUEST -> MaterialTheme.colorScheme.primary
              },
              modifier = Modifier.size(22.dp)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = listing.categoryNameFa,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (isOwner) {
                Text(
                  text = "آگهی شما",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldPrimary,
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = listing.timeAgoFa,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { onFavoriteClick(listing.id) },
            modifier = Modifier.testTag("detail_favorite_btn")
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "نشان کردن",
              tint = if (isFavorite) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { shareDetail(context, listing) },
            modifier = Modifier.testTag("detail_share_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "اشتراک‌گذاری",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "بستن",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Hero Graphic Banner for Detail
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(16.dp))
      ) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.WIDE_BANNER,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Badges
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (listing.visibilityTier != MembershipTier.FREE) {
          TierBadge(tier = listing.visibilityTier)
        }
        if (listing.isReserved) {
          ListingStateBadge(status = listing.status)
        }
        if (listing.discountInfo?.isExpiringSoon == true) {
          ListingStateBadge(status = listing.status, isExpiring = true)
        }
        if (accessStatus.isLockedForCurrentUser) {
          StatusBadge(
            text = "🔒 ${PersianUtils.formatNumber(accessStatus.remainingMinutesUntilPublic)} دقیقه تا آزادسازی عمومی",
            bgColor = AmberSecondary.copy(alpha = 0.15f),
            textColor = AmberSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Title
      Text(
        text = listing.title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 32.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Location card
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.LocationOn,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = buildString {
            append("استان ")
            append(listing.province)
            append("، شهر ")
            append(listing.city)
            if (!listing.approximateLocation.isNullOrBlank()) {
              append(" (")
              append(listing.approximateLocation)
              append(")")
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Discount Voucher Box (if discount)
      if (listing.type == ListingType.DISCOUNT && listing.discountInfo != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberSecondary.copy(alpha = 0.08f))
            .border(1.5.dp, AmberSecondary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(16.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "کد تخفیف اختصاصی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AmberSecondary
              )
              if (listing.discountInfo.expirationDateFa != null) {
                Text(
                  text = "انقضا: ${listing.discountInfo.expirationDateFa}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (listing.discountInfo.discountCode != null) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surface)
                  .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = listing.discountInfo.discountCode,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = EmeraldPrimary,
                  letterSpacing = 2.sp
                )

                FilledTonalButton(
                  onClick = {
                    copyToClipboard(context, listing.discountInfo.discountCode)
                    copiedSuccess = true
                  },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("copy_coupon_btn")
                ) {
                  Icon(
                    imageVector = if (copiedSuccess) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (copiedSuccess) "کپی شد!" else "کپی کد", fontSize = 12.sp)
                }
              }
            }

            if (!listing.discountInfo.terms.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "شرایط: ${listing.discountInfo.terms}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Description text
      Text(
        text = "توضیحات آگهی",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = listing.description,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 24.sp
      )

      Spacer(modifier = Modifier.height(20.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(16.dp))

      // User Trust & Security Info
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(EmeraldPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = if (isOwner) "${listing.ownerDisplayName} (شما)" else listing.ownerDisplayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = "احراز هویت شده",
              tint = EmeraldPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
          Text(
            text = "کاربر تایید شده با شماره همراه و کد ملی",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      // Early Access VIP Notification Box for General/Non-eligible users
      if (!isOwner && accessStatus.isLockedForCurrentUser) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberSecondary.copy(alpha = 0.12f))
            .border(1.dp, AmberSecondary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = AmberSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (accessStatus.requiredTierNow == MembershipTier.GOLD)
                  "دسترسی زودهنگام پکیج طلایی و الماس"
                else
                  "دسترسی زودهنگام پکیج نقره‌ای و بالاتر",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AmberSecondary
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "این کارت برای عموم نمایش داده می‌شود، اما انتخاب و رزرو آن تا ${PersianUtils.formatNumber(accessStatus.remainingMinutesUntilPublic)} دقیقه دیگر تنها برای اعضای دارای پکیج مجاز است.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 20.sp
            )

            if (onUpgradeClick != null) {
              Spacer(modifier = Modifier.height(10.dp))
              FilledTonalButton(
                onClick = onUpgradeClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = AmberSecondary,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Diamond,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ارتقای اشتراک جهت انتخاب و رزرو آنی",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Buttons
      if (isOwner) {
        // Owner specific actions
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              onMarkAsGone(listing)
              onDismiss()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("owner_mark_as_gone_btn")
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "«رفت!» (هدیه داده شد - حذف از آگهی‌ها)",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 15.sp
            )
          }

          if (listing.isReserved) {
            OutlinedButton(
              onClick = { onCancelReservation(listing) },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("owner_cancel_reservation_btn")
            ) {
              Text("لغو رزرو و بازگرداندن به حالت عمومی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      } else {
        // Normal User actions
        when (listing.type) {
          ListingType.FREE_GIFT -> {
            if (listing.isReserved) {
              if (isReserver) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "این هدیه برای شما رزرو شده است. جهت هماهنگی با اهداکننده تماس بگیرید.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                      )
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Button(
                      onClick = { dialPhone(context, targetPhone) },
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                      modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("detail_call_reserver_btn")
                    ) {
                      Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("تماس: $targetPhone", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                      onClick = {
                        onCancelReservation(listing)
                        onDismiss()
                      },
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier
                        .weight(0.8f)
                        .height(50.dp)
                        .testTag("detail_cancel_reservation_btn")
                    ) {
                      Text("لغو رزرو", fontWeight = FontWeight.Bold, color = CoralTertiary)
                    }
                  }
                }
              } else {
                OutlinedButton(
                  onClick = {},
                  enabled = false,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                ) {
                  Text("این هدیه توسط کاربر دیگری رزرو شده است (شماره تماس مخفی است)", fontWeight = FontWeight.Bold)
                }
              }
            } else if (accessStatus.isLockedForCurrentUser) {
              // Locked for non-eligible tiers (Visible to public, but not selectable yet)
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = {
                    if (onUpgradeClick != null) onUpgradeClick() else onReserveClick(listing)
                  },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AmberSecondary
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("detail_reserve_locked_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AmberSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "🔒 قفل انتخاب (مخصوص ${if (accessStatus.requiredTierNow == MembershipTier.GOLD) "اعضای طلایی و الماس" else "اعضای نقره‌ای و بالاتر"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }

                OutlinedButton(
                  onClick = { dialPhone(context, targetPhone) },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("detail_call_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("تماس تلفنی با اهداکننده", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = { onReserveClick(listing) },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  modifier = Modifier
                    .weight(1.3f)
                    .height(50.dp)
                    .testTag("detail_reserve_btn")
                ) {
                  Text(
                    text = "درخواست رزرو رایگان",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                OutlinedButton(
                  onClick = { dialPhone(context, targetPhone) },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("detail_call_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("تماس تلفنی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
          ListingType.DISCOUNT -> {
            Button(
              onClick = {
                if (listing.discountInfo?.discountCode != null) {
                  copyToClipboard(context, listing.discountInfo.discountCode)
                  copiedSuccess = true
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("detail_use_coupon_btn")
            ) {
              Icon(
                imageVector = if (copiedSuccess) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (copiedSuccess) "کد تخفیف کپی شد!" else "کپی کد تخفیف و استفاده",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
          ListingType.REQUEST -> {
            Button(
              onClick = { dialPhone(context, "09129876543") },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("detail_help_request_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "تماس جهت اهدای وسیله به این کاربر",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }
      }

      // Contact Admin link
      Spacer(modifier = Modifier.height(14.dp))
      OutlinedButton(
        onClick = onContactAdmin,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
      ) {
        Text("گزارش آگهی یا ارتباط با مدیر سامانه", fontSize = 12.sp)
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

private fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("Coupon Code", text)
  clipboard.setPrimaryClip(clip)
}

private fun dialPhone(context: Context, phoneNumber: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun shareDetail(context: Context, listing: Listing) {
  val sendIntent: Intent = Intent().apply {
    action = Intent.ACTION_SEND
    putExtra(
      Intent.EXTRA_TEXT,
      buildString {
        append("آگهی در اپلیکیشن «بده بره»:\n")
        append("📌 ${listing.title}\n")
        append("📍 شهر: ${listing.city}\n")
        append("${listing.description}\n\n")
        append("اپلیکیشن بده بره - اهدای وسایل و تخفیف‌های طلایی")
      }
    )
    type = "text/plain"
  }
  val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری آگهی")
  context.startActivity(shareIntent)
}
