package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary

data class AppNotification(
  val id: String,
  val title: String,
  val message: String,
  val timeAgo: String,
  val icon: ImageVector,
  val isUnread: Boolean = false
)

@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
  val sampleNotifications = listOf(
    AppNotification(
      id = "n1",
      title = "آگهی شما تایید و منتشر شد",
      message = "آگهی «میز تحریر چوبی دونفره بسیار سالم» توسط مدیر تایید شد و اکنون در دسترس همگان قرار دارد.",
      timeAgo = "۱۰ دقیقه پیش",
      icon = Icons.Default.CheckCircle,
      isUnread = true
    ),
    AppNotification(
      id = "n2",
      title = "درخواست رزرو جدید",
      message = "کاربری برای دریافت وسیله شما درخواست رزرو ثبت نموده است. لطفاً جهت هماهنگی بررسی فرمایید.",
      timeAgo = "۱ ساعت پیش",
      icon = Icons.Default.Lock,
      isUnread = true
    ),
    AppNotification(
      id = "n3",
      title = "دسترسی زودهنگام به تخفیف‌های جدید",
      message = "آگهی‌های دارای اشتراک طلایی برای شما قابل مشاهده هستند.",
      timeAgo = "دیروز",
      icon = Icons.Default.Star,
      isUnread = false
    )
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("notifications_list"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        text = "پیام‌ها و اعلانات",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }

    items(sampleNotifications, key = { it.id }) { notif ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (notif.isUnread)
            EmeraldPrimary.copy(alpha = 0.05f)
          else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
              .background(EmeraldPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = notif.icon,
              contentDescription = null,
              tint = EmeraldPrimary,
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
              Text(
                text = notif.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = notif.timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = notif.message,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
