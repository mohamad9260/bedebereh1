package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.MembershipTier
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary

@Composable
fun MembershipPlansDialog(
  currentTier: MembershipTier,
  onDismiss: () -> Unit,
  onPlanSelected: (MembershipTier) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTier by remember { mutableStateOf(currentTier) }
  var isProcessing by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("membership_plans_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
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
                .background(AmberSecondary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = null,
                tint = AmberSecondary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "طرح‌های اشتراک بده بره",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "دسترسی زودهنگام به هدایا و تخفیف‌ها",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Diamond Tier Option (VIP Commercial & Discount Partner)
        PlanOptionCard(
          title = "اشتراک الماس (VIP تجاری و فروشگاهی)",
          price = "۱۴۹,۰۰۰ تومان / ماهانه",
          perks = listOf(
            "امکان ثبت و درج نامحدود کوپن‌ها و کدهای تخفیف تجاری",
            "۲ ساعت دسترسی زودهنگام به تمام هدایای رایگان",
            "سقف رزرو روزانه ویژه: تا ۲۵ کارت در روز",
            "نشان درخشان الماس در پروفایل و آگهی‌ها",
            "پشتیبانی اختصاصی و اولویت آنی در تایید آگهی‌ها"
          ),
          isSelected = selectedTier == MembershipTier.DIAMOND,
          isCurrent = currentTier == MembershipTier.DIAMOND,
          badgeColor = Color(0xFF00B4D8),
          onClick = { selectedTier = MembershipTier.DIAMOND },
          testTag = "plan_diamond_card"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gold Tier Option
        PlanOptionCard(
          title = "اشتراک طلایی (VIP)",
          price = "۹۹,۰۰۰ تومان / ماهانه",
          perks = listOf(
            "۲ ساعت دسترسی زودهنگام به تمام هدایای رایگان",
            "۲ ساعت اولویت در مشاهده و دریافت کدهای تخفیف ویژه",
            "سقف رزرو روزانه: تا ۱۵ کارت در روز",
            "نشان طلایی کاربری در پروفایل و آگهی‌ها",
            "ارسال نامحدود درخواست‌های نیازمندی"
          ),
          isSelected = selectedTier == MembershipTier.GOLD,
          isCurrent = currentTier == MembershipTier.GOLD,
          badgeColor = AmberSecondary,
          onClick = { selectedTier = MembershipTier.GOLD },
          testTag = "plan_gold_card"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Silver Tier Option
        PlanOptionCard(
          title = "اشتراک نقره‌ای",
          price = "۴۹,۰۰۰ تومان / ماهانه",
          perks = listOf(
            "۱ ساعت دسترسی زودهنگام به هدایا و تخفیف‌ها",
            "سقف رزرو روزانه: تا ۸ کارت در روز",
            "نشان نقره‌ای کاربری در پروفایل",
            "ثبت آگهی با اولویت تایید مدیریت"
          ),
          isSelected = selectedTier == MembershipTier.SILVER,
          isCurrent = currentTier == MembershipTier.SILVER,
          badgeColor = EmeraldPrimary,
          onClick = { selectedTier = MembershipTier.SILVER },
          testTag = "plan_silver_card"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Free Tier Option
        PlanOptionCard(
          title = "طرح عادی (رایگان)",
          price = "رایگان برای همیشه",
          perks = listOf(
            "سقف رزرو روزانه: حداکثر ۳ کارت در روز",
            "مشاهده و دریافت تمام هدایا پس از انقضای بازه اولویت",
            "ثبت نامحدود هدایای اهدایی برای دیگران",
            "امکان استفاده از کدهای تخفیف عمومی"
          ),
          isSelected = selectedTier == MembershipTier.FREE,
          isCurrent = currentTier == MembershipTier.FREE,
          badgeColor = MaterialTheme.colorScheme.outline,
          onClick = { selectedTier = MembershipTier.FREE },
          testTag = "plan_free_card"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Button
        Button(
          onClick = {
            isProcessing = true
            onPlanSelected(selectedTier)
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = when (selectedTier) {
              MembershipTier.DIAMOND -> Color(0xFF00B4D8)
              MembershipTier.GOLD -> AmberSecondary
              MembershipTier.SILVER -> EmeraldPrimary
              MembershipTier.FREE -> MaterialTheme.colorScheme.primary
            }
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("confirm_plan_btn")
        ) {
          Text(
            text = when {
              selectedTier == currentTier -> "طرح فعلی شما"
              selectedTier == MembershipTier.FREE -> "تغییر به طرح عادی"
              else -> "پرداخت و ارتقای فوری طرح"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun PlanOptionCard(
  title: String,
  price: String,
  perks: List<String>,
  isSelected: Boolean,
  isCurrent: Boolean,
  badgeColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(16.dp)
      )
      .clickable(onClick = onClick)
      .testTag(testTag),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) badgeColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(22.dp)
              .clip(CircleShape)
              .background(if (isSelected) badgeColor else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (isCurrent) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(EmeraldPrimary.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "طرح فعال",
              style = MaterialTheme.typography.labelSmall,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = price,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = badgeColor
      )

      Spacer(modifier = Modifier.height(8.dp))

      perks.forEach { perk ->
        Row(
          modifier = Modifier.padding(vertical = 2.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier
              .size(14.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = perk,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}
