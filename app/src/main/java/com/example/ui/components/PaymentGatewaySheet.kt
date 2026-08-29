package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MembershipTier
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewaySheet(
  tierToPurchase: MembershipTier,
  onPaymentSuccess: (MembershipTier) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var cardNumber by remember { mutableStateOf("") }
  var cvv2 by remember { mutableStateOf("") }
  var expMonth by remember { mutableStateOf("") }
  var expYear by remember { mutableStateOf("") }
  var otpCode by remember { mutableStateOf("") }
  var captchaInput by remember { mutableStateOf("") }
  var generatedCaptcha by remember { mutableIntStateOf(5823) }

  var otpTimer by remember { mutableIntStateOf(0) }
  var isOtpRequested by remember { mutableStateOf(false) }
  var isProcessingPayment by remember { mutableStateOf(false) }
  var paymentCompleted by remember { mutableStateOf(false) }
  var trackingCode by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val priceToman = when (tierToPurchase) {
    MembershipTier.SILVER -> 49_000L
    MembershipTier.GOLD -> 99_000L
    MembershipTier.DIAMOND -> 149_000L
    MembershipTier.FREE -> 0L
  }

  // OTP Countdown timer
  LaunchedEffect(otpTimer) {
    if (otpTimer > 0) {
      delay(1000L)
      otpTimer--
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("payment_gateway_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
      if (!paymentCompleted) {
        // Gateway Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF0077B6).copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF0077B6),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "درگاه پرداخت امن الکترونیک (شاپرک)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "اتصال به شبکه پرداخت کارتی شتاب",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "بستن", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Merchant and Amount Info Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("پذیرنده:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("سامانه اجتماعی بده‌بره", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("بسته انتخابی:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(tierToPurchase.titleFa, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("مبلغ قابل پرداخت:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text(
                text = "${PersianUtils.formatNumber(priceToman)} تومان",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Number
        OutlinedTextField(
          value = cardNumber,
          onValueChange = { if (it.length <= 16) cardNumber = it.filter { c -> c.isDigit() } },
          label = { Text("شماره کارت ۱۶ رقمی شتاب") },
          placeholder = { Text("6037997512345678") },
          leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = EmeraldPrimary) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // CVV2 and Expiry
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = cvv2,
            onValueChange = { if (it.length <= 4) cvv2 = it.filter { c -> c.isDigit() } },
            label = { Text("CVV2") },
            placeholder = { Text("3 یا 4 رقم") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = expMonth,
            onValueChange = { if (it.length <= 2) expMonth = it.filter { c -> c.isDigit() } },
            label = { Text("ماه (MM)") },
            placeholder = { Text("08") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = expYear,
            onValueChange = { if (it.length <= 2) expYear = it.filter { c -> c.isDigit() } },
            label = { Text("سال (YY)") },
            placeholder = { Text("06") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Password (رمز پویا)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 8) otpCode = it.filter { c -> c.isDigit() } },
            label = { Text("رمز اینترنتی (پویا)") },
            placeholder = { Text("کد پیامک شده") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )

          Button(
            onClick = {
              isOtpRequested = true
              otpTimer = 120
              otpCode = "79412"
            },
            enabled = otpTimer == 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
            modifier = Modifier.height(54.dp)
          ) {
            Text(
              text = if (otpTimer > 0) "${PersianUtils.formatNumber(otpTimer)} ثانیه" else "دریافت رمز پویا",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Captcha Code
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = captchaInput,
            onValueChange = { captchaInput = it },
            label = { Text("کد امنیتی") },
            placeholder = { Text("ارقام تصویر") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )

          Box(
            modifier = Modifier
              .height(54.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = PersianUtils.formatDigits(generatedCaptcha.toString()),
                letterSpacing = 4.sp,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(
                onClick = { generatedCaptcha = (1000..9999).random() },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "تغییر کد", modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        errorMessage?.let { err ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(err, color = CoralTertiary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Payment Button
        Button(
          onClick = {
            if (cardNumber.length < 16) {
              errorMessage = "لطفاً شماره کارت ۱۶ رقمی را کامل وارد نمایید."
              return@Button
            }
            if (cvv2.length < 3) {
              errorMessage = "لطفاً کد CVV2 را وارد نمایید."
              return@Button
            }
            if (otpCode.isBlank()) {
              errorMessage = "لطفاً رمز پویا را دریافت و وارد نمایید."
              return@Button
            }

            isProcessingPayment = true
            errorMessage = null
            scope.launch {
              delay(1500L)
              isProcessingPayment = false
              trackingCode = "TR-${(10000000..99999999).random()}"
              paymentCompleted = true
            }
          },
          enabled = !isProcessingPayment,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("submit_payment_btn")
        ) {
          if (isProcessingPayment) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("در حال پردازش و برقراری ارتباط با شاپرک...", color = Color.White)
          } else {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("پرداخت امن (${PersianUtils.formatNumber(priceToman)} تومان)", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      } else {
        // Success Receipt
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(38.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "پرداخت با موفقیت انجام شد 🎉",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "اشتراک شما بلافاصله ارتقا یافت و فعال شد.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("طرح خریداری شده:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tierToPurchase.titleFa, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("مبلغ تراکنش:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${PersianUtils.formatNumber(priceToman)} تومان", fontWeight = FontWeight.Bold)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("کد پیگیری شاپرک:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(trackingCode, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("شماره کارت پرداخت‌کننده:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("•••• ${cardNumber.takeLast(4)}", fontWeight = FontWeight.Medium)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("وضعیت تراکنش:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("موفق (تایید شده)", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              onPaymentSuccess(tierToPurchase)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
          ) {
            Text("تکمیل و بازگشت به برنامه", fontWeight = FontWeight.Bold, color = Color.White)
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}
