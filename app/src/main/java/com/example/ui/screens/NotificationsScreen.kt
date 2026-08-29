package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.model.AppNotification
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent

@Composable
fun NotificationsScreen(
  repository: MockListingRepository,
  onBack: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val notifications by repository.notifications.collectAsState(initial = emptyList())
  val hasUnread = notifications.any { it.isUnread }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("notifications_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "بازگشت",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
          }
          Text(
            text = "پیام‌ها و اعلانات",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (hasUnread) {
          TextButton(
            onClick = { repository.markAllNotificationsRead() },
            modifier = Modifier.testTag("mark_all_read_btn")
          ) {
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = EmeraldPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "خوانده شدن همه",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary
            )
          }
        }
      }
    }

    if (notifications.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsNone,
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "هیچ پیامی وجود ندارد",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "تمام رویدادهای تایید آگهی، رزرو و پیام‌های مهم در این قسمت نمایش داده می‌شوند.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    } else {
      items(notifications, key = { it.id }) { notif ->
        val icon = when (notif.type) {
          "approved" -> Icons.Default.CheckCircle
          "rejected" -> Icons.Default.Close
          "reserved" -> Icons.Default.Lock
          "vip" -> Icons.Default.Star
          else -> Icons.Default.Notifications
        }
        val iconColor = when (notif.type) {
          "approved" -> EmeraldPrimary
          "rejected" -> CoralTertiary
          "reserved" -> IndigoAccent
          "vip" -> AmberSecondary
          else -> AmberSecondary
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${notif.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (notif.isUnread)
              iconColor.copy(alpha = 0.07f)
            else MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (notif.isUnread) 2.dp else 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.14f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (notif.isUnread) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                    )
                  }
                }
                Text(
                  text = notif.timeAgo,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  fontSize = 10.sp
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = notif.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
              )
            }

            IconButton(
              onClick = { repository.deleteNotification(notif.id) },
              modifier = Modifier
                .size(32.dp)
                .padding(start = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "حذف اعلان",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
