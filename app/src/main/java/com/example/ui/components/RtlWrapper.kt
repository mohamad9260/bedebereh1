package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun PersianRtlLayout(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    content()
  }
}

object PersianUtils {
  private val englishToPersianDigits = mapOf(
    '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
    '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
  )

  fun formatDigits(input: String): String {
    val builder = StringBuilder()
    for (char in input) {
      builder.append(englishToPersianDigits[char] ?: char)
    }
    return builder.toString()
  }

  fun formatNumber(number: Long): String {
    val formatted = "%,d".format(number)
    return formatDigits(formatted)
  }

  fun formatNumber(number: Int): String {
    val formatted = "%,d".format(number)
    return formatDigits(formatted)
  }

  fun formatRelativeTimeFa(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val minutes = diff / (60 * 1000L)
    val hours = minutes / 60
    val days = hours / 24

    return when {
      minutes < 2 -> "همین الان"
      minutes < 60 -> "${formatNumber(minutes.toInt())} دقیقه پیش"
      hours < 24 -> "${formatNumber(hours.toInt())} ساعت پیش"
      else -> "${formatNumber(days.toInt())} روز پیش"
    }
  }
}
