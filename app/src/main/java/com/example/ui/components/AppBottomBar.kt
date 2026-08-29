package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.NavRoute
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AppBottomBar(
  currentRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    shadowElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Home
      BottomNavItem(
        title = "خانه",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        isSelected = currentRoute == NavRoute.HOME,
        testTag = "nav_home",
        onClick = { onNavigate(NavRoute.HOME) }
      )

      // 2. My Reservations
      BottomNavItem(
        title = "رزروهای من",
        selectedIcon = Icons.Filled.EventAvailable,
        unselectedIcon = Icons.Outlined.EventAvailable,
        isSelected = currentRoute == NavRoute.RESERVATIONS,
        testTag = "nav_reservations",
        onClick = { onNavigate(NavRoute.RESERVATIONS) }
      )

      // 3. Add Listing (Prominent Center Button)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .offset(y = (-6).dp)
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(EmeraldPrimary)
            .clickable { onNavigate(NavRoute.ADD_LISTING) }
            .testTag("nav_add_listing"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "ثبت آگهی جدید",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "ثبت آگهی",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldPrimary
        )
      }

      // 4. Favorites / Saved
      BottomNavItem(
        title = "نشان‌ها",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Filled.BookmarkBorder,
        isSelected = currentRoute == NavRoute.FAVORITES,
        testTag = "nav_favorites",
        onClick = { onNavigate(NavRoute.FAVORITES) }
      )

      // 5. Profile
      BottomNavItem(
        title = "پروفایل",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Filled.PersonOutline,
        isSelected = currentRoute == NavRoute.PROFILE,
        testTag = "nav_profile",
        onClick = { onNavigate(NavRoute.PROFILE) }
      )
    }
  }
}

@Composable
private fun BottomNavItem(
  title: String,
  selectedIcon: ImageVector,
  unselectedIcon: ImageVector,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  val tint = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp, horizontal = 12.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = if (isSelected) selectedIcon else unselectedIcon,
      contentDescription = title,
      tint = tint,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = title,
      color = tint,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}
