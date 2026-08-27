package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AppTopBar(
  currentCityName: String,
  unreadNotificationCount: Int,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  onCityClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onSupportClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var isSearchExpanded by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App Logo & Brand Identity
        BedeBereLogo(
          size = 38.dp,
          showText = true
        )

        // Right Action Bar: City Pill, Support, Search, Notifications
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Location selector pill button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
              .clickable(onClick = onCityClick)
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("topbar_city_btn")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "موقعیت",
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = currentCityName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          // Search Toggle Button
          IconButton(
            onClick = {
              isSearchExpanded = !isSearchExpanded
              if (!isSearchExpanded) onSearchQueryChanged("")
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("topbar_search_toggle")
          ) {
            Icon(
              imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
              contentDescription = "جستجو",
              tint = if (isSearchExpanded) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }

          // Contact Admin / Support Agent Icon
          IconButton(
            onClick = onSupportClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("topbar_support_btn")
          ) {
            Icon(
              imageVector = Icons.Default.SupportAgent,
              contentDescription = "ارتباط با مدیر",
              tint = EmeraldPrimary,
              modifier = Modifier.size(22.dp)
            )
          }

          // Notifications Button
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("topbar_notifications_btn")
          ) {
            BadgedBox(
              badge = {
                if (unreadNotificationCount > 0) {
                  Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                  ) {
                    Text(
                      text = PersianUtils.formatNumber(unreadNotificationCount),
                      fontSize = 10.sp
                    )
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "اعلان‌ها",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // Expandable Search Field
      AnimatedVisibility(
        visible = isSearchExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
              Text("جستجو در آگهی‌ها، کوپن‌ها و نیازمندی‌ها…", fontSize = 13.sp)
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChanged("") }) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "پاک کردن",
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              focusedIndicatorColor = EmeraldPrimary,
              unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("topbar_search_input")
          )
        }
      }
    }
  }
}

