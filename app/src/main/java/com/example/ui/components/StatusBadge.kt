package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF00C6FF), Color(0xFF0072FF), Color(0xFF7F00FF))
            )
          )
          .border(1.dp, Color(0xFFB3E5FC), RoundedCornerShape(12.dp))
          .padding(horizontal = 9.dp, vertical = 4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.background(Color.Transparent)
        ) {
          Icon(
            imageVector = Icons.Default.Diamond,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "الماس VIP ✨",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }
    MembershipTier.GOLD -> {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.linearGradient(
              listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFF8F00))
            )
          )
          .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
          .padding(horizontal = 9.dp, vertical = 4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.background(Color.Transparent)
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFF3E2723),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "دسترسی طلایی ⭐",
            color = Color(0xFF3E2723),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }
    MembershipTier.SILVER -> {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.linearGradient(
              listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE))
            )
          )
          .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(12.dp))
          .padding(horizontal = 9.dp, vertical = 4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.background(Color.Transparent)
        ) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = Color(0xFF263238),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "دسترسی نقره‌ای 🥈",
            color = Color(0xFF263238),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
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
