package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactAdminSheet(
  onDismiss: () -> Unit,
  sheetState: SheetState,
  onTicketSent: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var ticketSubject by remember { mutableStateOf("") }
  var ticketMessage by remember { mutableStateOf("") }
  var sentSuccessfully by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("contact_admin_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SupportAgent,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Column {
            Text(
              text = "ارتباط با مدیر سامانه بده بره",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "پاسخگویی سریع ۲۴ ساعته و ثبت نظرات",
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

      Spacer(modifier = Modifier.height(18.dp))

      // Direct Contact Channels
      Text(
        text = "راه‌های ارتباط مستقیم",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ContactChannelCard(
          icon = Icons.Default.Call,
          title = "تماس تلفنی",
          subtitle = "۰۲۱-۸۸۸۸۹۲۶۰",
          onClick = { dialSupport(context, "02188889260") },
          modifier = Modifier.weight(1f)
        )

        ContactChannelCard(
          icon = Icons.Default.Email,
          title = "ایمیل پشتیبانی",
          subtitle = "admin@bedebere.ir",
          onClick = { sendEmail(context, "admin@bedebere.ir") },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(16.dp))

      // Quick Ticket Form
      Text(
        text = "ارسال پیام مستقیم به مدیریت",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "پیشنهاد، گزارش تخلف یا درخواست خود را برای مدیر ارسال کنید:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      if (sentSuccessfully) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EmeraldPrimary.copy(alpha = 0.12f))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(16.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "پیام شما با موفقیت برای مدیر ارسال شد ✅",
              fontWeight = FontWeight.Bold,
              color = EmeraldPrimary,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "کارشناسان در اسرع وقت پاسخ را در بخش اعلانات ارسال می‌کنند.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        OutlinedTextField(
          value = ticketSubject,
          onValueChange = { ticketSubject = it },
          label = { Text("موضوع پیام") },
          placeholder = { Text("مثال: پیشنهاد دسته‌بندی جدید یا گزارش آگهی") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = ticketMessage,
          onValueChange = { ticketMessage = it },
          label = { Text("متن پیام شما") },
          placeholder = { Text("پیام کامل خود را بنویسید...") },
          minLines = 3,
          maxLines = 5,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            if (ticketMessage.isNotBlank()) {
              sentSuccessfully = true
              onTicketSent(ticketMessage)
            }
          },
          enabled = ticketMessage.isNotBlank(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("send_ticket_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("ارسال پیام به مدیر", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
private fun ContactChannelCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(12.dp)
  ) {
    Column {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = EmeraldPrimary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp
      )
    }
  }
}

private fun dialSupport(context: Context, number: String) {
  try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$number")
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}

private fun sendEmail(context: Context, email: String) {
  try {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:$email")
      putExtra(Intent.EXTRA_SUBJECT, "پشتیبانی بده بره")
    }
    context.startActivity(intent)
  } catch (_: Exception) {}
}
