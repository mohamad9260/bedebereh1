package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ListingStatus
import com.example.domain.model.MembershipTier
import com.example.ui.theme.BadgeExpiringBg
import com.example.ui.theme.BadgeExpiringText
import com.example.ui.theme.BadgeGoldBg
import com.example.ui.theme.BadgeGoldText
import com.example.ui.theme.BadgeNewBg
import com.example.ui.theme.BadgeNewText
import com.example.ui.theme.BadgeReservedBg
import com.example.ui.theme.BadgeReservedText
import com.example.ui.theme.BadgeSilverBg
import com.example.ui.theme.BadgeSilverText

@Composable
fun StatusBadge(
  text: String,
  bgColor: Color,
  textColor: Color,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = text,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall
      )
    }
  }
}

@Composable
fun TierBadge(tier: MembershipTier, modifier: Modifier = Modifier) {
  when (tier) {
    MembershipTier.DIAMOND -> {
      StatusBadge(
        text = "اشتراک الماس",
        bgColor = Color(0xFFE0F7FA),
        textColor = Color(0xFF0077B6),
        icon = Icons.Default.Diamond,
        modifier = modifier
      )
    }
    MembershipTier.GOLD -> {
      StatusBadge(
        text = "دسترسی طلایی",
        bgColor = BadgeGoldBg,
        textColor = BadgeGoldText,
        icon = Icons.Default.Star,
        modifier = modifier
      )
    }
    MembershipTier.SILVER -> {
      StatusBadge(
        text = "دسترسی نقره‌ای",
        bgColor = BadgeSilverBg,
        textColor = BadgeSilverText,
        icon = Icons.Default.Bookmark,
        modifier = modifier
      )
    }
    MembershipTier.FREE -> {
      StatusBadge(
        text = "طرح عادی",
        bgColor = MaterialTheme.colorScheme.surfaceVariant,
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
      )
    }
  }
}

@Composable
fun ListingStateBadge(status: ListingStatus, isExpiring: Boolean = false, modifier: Modifier = Modifier) {
  when {
    status == ListingStatus.RESERVED -> {
      StatusBadge(
        text = "رزرو شده",
        bgColor = BadgeReservedBg,
        textColor = BadgeReservedText,
        icon = Icons.Default.Lock,
        modifier = modifier
      )
    }
    isExpiring -> {
      StatusBadge(
        text = "در حال انقضا",
        bgColor = BadgeExpiringBg,
        textColor = BadgeExpiringText,
        icon = Icons.Default.Timer,
        modifier = modifier
      )
    }
    status == ListingStatus.PUBLIC -> {
      StatusBadge(
        text = "فعال",
        bgColor = BadgeNewBg,
        textColor = BadgeNewText,
        modifier = modifier
      )
    }
    else -> {
      StatusBadge(
        text = status.labelFa,
        bgColor = MaterialTheme.colorScheme.surfaceVariant,
        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
      )
    }
  }
}
