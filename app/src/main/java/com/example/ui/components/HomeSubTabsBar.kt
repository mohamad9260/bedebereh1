package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ListingType
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SkyBlueAccent

@Composable
fun HomeSubTabsBar(
  selectedType: ListingType,
  onTabSelected: (ListingType) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HomeTabPill(
          title = "هدایای رایگان",
          icon = Icons.Default.CardGiftcard,
          isSelected = selectedType == ListingType.FREE_GIFT,
          activeColor = EmeraldPrimary,
          testTag = "tab_free_offers",
          onClick = { onTabSelected(ListingType.FREE_GIFT) },
          modifier = Modifier.weight(1f)
        )

        HomeTabPill(
          title = "تخفیف‌ها",
          icon = Icons.Default.LocalOffer,
          isSelected = selectedType == ListingType.DISCOUNT,
          activeColor = AmberSecondary,
          testTag = "tab_discounts",
          onClick = { onTabSelected(ListingType.DISCOUNT) },
          modifier = Modifier.weight(1f)
        )

        HomeTabPill(
          title = "درخواست‌ها",
          icon = Icons.Default.HelpOutline,
          isSelected = selectedType == ListingType.REQUEST,
          activeColor = SkyBlueAccent,
          testTag = "tab_requests",
          onClick = { onTabSelected(ListingType.REQUEST) },
          modifier = Modifier.weight(1f)
        )
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        thickness = 1.dp
      )
    }
  }
}

@Composable
private fun HomeTabPill(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  activeColor: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor by animateColorAsState(
    targetValue = if (isSelected) activeColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    label = "tab_bg"
  )
  val contentColor by animateColorAsState(
    targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
    label = "tab_text"
  )
  val scale by animateFloatAsState(
    targetValue = if (isSelected) 1.01f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "tab_scale"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(14.dp))
      .then(
        if (isSelected) {
          Modifier.border(1.5.dp, activeColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
        } else {
          Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        }
      )
      .background(bgColor)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp, horizontal = 4.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(19.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        color = contentColor,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
        fontSize = 12.5.sp,
        maxLines = 1
      )
    }
  }
}

