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

  fun toEnglishDigits(input: String): String {
    val persianToEng = mapOf(
      '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
      '۵' to '5', '6' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
      '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
      '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
    )
    return input.map { persianToEng[it] ?: it }.joinToString("")
  }

  fun normalizeIranianMobile(input: String): String {
    val converted = toEnglishDigits(input)
    val digitsOnly = converted.filter { it.isDigit() || it == '+' }
    return when {
      digitsOnly.startsWith("+98") -> "0" + digitsOnly.removePrefix("+98")
      digitsOnly.startsWith("0098") -> "0" + digitsOnly.removePrefix("0098")
      digitsOnly.startsWith("98") && digitsOnly.length == 12 -> "0" + digitsOnly.removePrefix("98")
      else -> digitsOnly
    }
  }

  fun isValidIranianMobile(phone: String): Boolean {
    val normalized = normalizeIranianMobile(phone)
    return normalized.matches(Regex("^09\\d{9}$"))
  }

  fun isValidNationalId(nationalId: String): Boolean {
    val persianToEng = mapOf(
      '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
      '۵' to '5', '6' to '6', '۷' to '7', '۸' to '8', '۹' to '9'
    )
    val clean = nationalId.map { persianToEng[it] ?: it }.joinToString("").filter { it.isDigit() }
    if (clean.isBlank()) return true
    if (clean.length != 10) return false
    // Reject all same digits (e.g. 1111111111)
    if (clean.toSet().size == 1) return false
    val check = clean[9].digitToInt()
    val sum = (0..8).sumOf { clean[it].digitToInt() * (10 - it) }
    val rem = sum % 11
    return (rem < 2 && check == rem) || (rem >= 2 && check == 11 - rem)
  }

  fun formatMaskedPhone(phone: String): String {
    val normalized = normalizeIranianMobile(phone)
    return if (normalized.length == 11) {
      val prefix = formatDigits(normalized.substring(0, 4))
      val suffix = formatDigits(normalized.substring(7, 11))
      "\u200E$prefix ••• $suffix\u200E"
    } else if (phone.isNotBlank()) {
      "\u200E${formatDigits(phone)}\u200E"
    } else {
      "---"
    }
  }

  fun formatMaskedNationalId(nationalId: String): String {
    val persianToEng = mapOf(
      '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
      '۵' to '5', '6' to '6', '۷' to '7', '۸' to '8', '۹' to '9'
    )
    val clean = nationalId.map { persianToEng[it] ?: it }.joinToString("").filter { it.isDigit() }
    return if (clean.length == 10) {
      val prefix = formatDigits(clean.substring(0, 3))
      val suffix = formatDigits(clean.substring(8, 10))
      "\u200E$prefix ••••• $suffix\u200E"
    } else if (nationalId.isNotBlank()) {
      "\u200E${formatDigits(nationalId)}\u200E"
    } else {
      "---"
    }
  }
}
