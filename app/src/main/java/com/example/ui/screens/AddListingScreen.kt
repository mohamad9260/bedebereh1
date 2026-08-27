package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryData
import com.example.data.MockListingRepository
import com.example.domain.model.DiscountInfo
import com.example.domain.model.Listing
import com.example.domain.model.ListingStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import java.util.UUID

@Composable
fun AddListingScreen(
  repository: MockListingRepository,
  onListingCreated: () -> Unit,
  onUpgradePlanClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedType by remember { mutableStateOf<ListingType?>(null) }
  val userProfile by repository.userProfile.collectAsState(initial = null)
  val systemSettings by repository.systemSettings.collectAsState(initial = null)
  var showDiamondRequiredDialog by remember { mutableStateOf(false) }

  if (selectedType == null) {
    TypeSelectionStep(
      onTypeSelected = { type ->
        if (type == ListingType.DISCOUNT && !repository.canPostDiscount()) {
          showDiamondRequiredDialog = true
        } else {
          selectedType = type
        }
      },
      isDiamondRequiredForDiscounts = systemSettings?.requireDiamondForDiscounts == true,
      userPlan = userProfile?.plan ?: MembershipTier.FREE,
      modifier = modifier
    )
  } else {
    ListingFormStep(
      type = selectedType!!,
      onBack = { selectedType = null },
      onSubmit = { listing ->
        repository.addListing(listing)
        onListingCreated()
      },
      modifier = modifier
    )
  }

  // Diamond Plan Required Alert
  if (showDiamondRequiredDialog) {
    AlertDialog(
      onDismissRequest = { showDiamondRequiredDialog = false },
      icon = {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF00B4D8).copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Diamond,
            contentDescription = null,
            tint = Color(0xFF00B4D8),
            modifier = Modifier.size(30.dp)
          )
        }
      },
      title = {
        Text(
          text = "نیازمند پکیج الماس (VIP تجاری)",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleLarge
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "بر اساس سیاست‌های مدیریتی سامانه «بده بره»، ثبت کوپن‌ها، بن‌های خرید و کدهای تخفیف فروشگاهی تنها برای دارندگان اشتراک الماس امکان‌پذیر است.",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF00B4D8).copy(alpha = 0.08f))
              .padding(10.dp)
          ) {
            Text(
              text = "💎 با خرید اشتراک الماس، علاوه بر ثبت نامحدود تخفیف‌ها، از ۲ ساعت دسترسی زودهنگام به هدایا و سقف ۲۵ رزرو در روز نیز بهره‌مند شوید.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF0077B6),
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showDiamondRequiredDialog = false
            onUpgradePlanClick()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
        ) {
          Text("ارتقا به پکیج الماس", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        Button(
          onClick = { showDiamondRequiredDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    )
  }
}

@Composable
private fun TypeSelectionStep(
  onTypeSelected: (ListingType) -> Unit,
  isDiamondRequiredForDiscounts: Boolean,
  userPlan: MembershipTier,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(20.dp)
      .testTag("add_listing_screen"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "ثبت آگهی در «بده بره»",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "نوع آگهی مورد نظر خود را برای اشتراک‌گذاری انتخاب کنید",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 1. Free Gift
    ListingTypeOptionCard(
      title = "هدیه رایگان (اهدای وسیله)",
      description = "اهدای وسایل بدون استفاده منزل، کتاب، مبلمان، پوشاک یا لوازم الکترونیکی به صورت رایگان به هموطنان.",
      icon = Icons.Default.CardGiftcard,
      accentColor = EmeraldPrimary,
      testTag = "option_free_gift",
      onClick = { onTypeSelected(ListingType.FREE_GIFT) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Discount / Coupon
    ListingTypeOptionCard(
      title = "تخفیف یا کوپن",
      description = "اشتراک‌گذاری کد تخفیف، کارت تخفیف، بن خرید یا پیشنهادهای ویژه با تخفیف درصدی و ریالی.",
      icon = Icons.Default.LocalOffer,
      accentColor = AmberSecondary,
      badgeText = if (isDiamondRequiredForDiscounts && userPlan != MembershipTier.DIAMOND) "نیازمند پکیج الماس" else null,
      badgeColor = Color(0xFF00B4D8),
      testTag = "option_discount",
      onClick = { onTypeSelected(ListingType.DISCOUNT) }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Request
    ListingTypeOptionCard(
      title = "درخواست وسیله یا نیازمندی",
      description = "ثبت نیاز به کتاب، میز، لوازم ضروری یا تحصیلی به صورت رایگان تا دیگران بتوانند به شما کمک کنند.",
      icon = Icons.Default.HelpOutline,
      accentColor = MaterialTheme.colorScheme.primary,
      testTag = "option_request",
      onClick = { onTypeSelected(ListingType.REQUEST) }
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Friendly Note
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        .padding(16.dp)
    ) {
      Text(
        text = "💡 در بده بره تمامی هدایا ۱۰۰٪ رایگان بوده و هرگونه خرید و فروش ممنوع می‌باشد. با اهدای وسایلتان، مهربانی را تکثیر کنید.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 22.sp
      )
    }

    Spacer(modifier = Modifier.height(60.dp))
  }
}

@Composable
private fun ListingFormStep(
  type: ListingType,
  onBack: () -> Unit,
  onSubmit: (Listing) -> Unit,
  modifier: Modifier = Modifier
) {
  val categories = remember(type) { CategoryData.getForType(type) }
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var province by remember { mutableStateOf("تهران") }
  var city by remember { mutableStateOf("تهران") }
  var approximateLocation by remember { mutableStateOf("") }

  // Discount specific
  var discountCode by remember { mutableStateOf("") }
  var discountPercentText by remember { mutableStateOf("") }
  var discountAmountText by remember { mutableStateOf("") }
  var discountExpiry by remember { mutableStateOf("۱۴۰۳/۰۶/۳۰") }
  var discountTerms by remember { mutableStateOf("") }
  var isNationwide by remember { mutableStateOf(true) }

  val accentColor = when (type) {
    ListingType.FREE_GIFT -> EmeraldPrimary
    ListingType.DISCOUNT -> AmberSecondary
    ListingType.REQUEST -> MaterialTheme.colorScheme.primary
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("listing_form_screen")
  ) {
    // Top Row with Back Button & Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = "بازگشت",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = when (type) {
          ListingType.FREE_GIFT -> "ثبت اهدای هدیه رایگان"
          ListingType.DISCOUNT -> "اشتراک‌گذاری کوپن تخفیف"
          ListingType.REQUEST -> "ثبت درخواست نیازمندی"
        },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Category Selector
    Text(
      text = "انتخاب دسته‌بندی",
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { category ->
        FilterChip(
          selected = selectedCategory?.id == category.id,
          onClick = { selectedCategory = category },
          label = { Text(category.titleFa, fontSize = 12.sp) },
          shape = RoundedCornerShape(20.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor.copy(alpha = 0.15f),
            selectedLabelColor = accentColor
          ),
          modifier = Modifier.testTag("form_category_${category.id}")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Title Input
    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("عنوان آگهی (مثال: میز مطالعه، کد تخفیف دیجی‌کالا)") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentColor,
        focusedLabelColor = accentColor
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("form_input_title")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Description Input
    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("توضیحات کامل آگهی و مشخصات") },
      minLines = 3,
      maxLines = 5,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentColor,
        focusedLabelColor = accentColor
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("form_input_desc")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Location inputs
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedTextField(
        value = city,
        onValueChange = { city = it },
        label = { Text("شهر") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = accentColor,
          focusedLabelColor = accentColor
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("form_input_city")
      )

      OutlinedTextField(
        value = approximateLocation,
        onValueChange = { approximateLocation = it },
        label = { Text("محدوده یا محله") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = accentColor,
          focusedLabelColor = accentColor
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("form_input_location")
      )
    }

    // Extra fields for Discount
    if (type == ListingType.DISCOUNT) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "اطلاعات کوپن تخفیف",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = AmberSecondary
      )
      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = discountCode,
        onValueChange = { discountCode = it.uppercase() },
        label = { Text("کد تخفیف (مثال: OFF50, BEDE20)") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AmberSecondary,
          focusedLabelColor = AmberSecondary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("form_input_coupon_code")
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = discountPercentText,
          onValueChange = { discountPercentText = it },
          label = { Text("درصد تخفیف (٪)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AmberSecondary,
            focusedLabelColor = AmberSecondary
          ),
          modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
          value = discountExpiry,
          onValueChange = { discountExpiry = it },
          label = { Text("تاریخ انقضا") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AmberSecondary,
            focusedLabelColor = AmberSecondary
          ),
          modifier = Modifier.weight(1f)
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Submit Button
    val isFormValid = title.isNotBlank() && description.isNotBlank() && city.isNotBlank()

    Button(
      onClick = {
        val newListing = Listing(
          id = UUID.randomUUID().toString().take(6),
          type = type,
          title = title.trim(),
          description = description.trim(),
          categoryId = selectedCategory?.id ?: "general",
          categoryNameFa = selectedCategory?.titleFa ?: "عمومی",
          categoryIcon = selectedCategory?.iconName ?: "card_giftcard",
          ownerId = "u_current",
          ownerDisplayName = "شما (کاربر تایید شده)",
          province = province,
          city = city.trim(),
          approximateLocation = approximateLocation.ifBlank { null },
          discountInfo = if (type == ListingType.DISCOUNT) {
            DiscountInfo(
              discountCode = discountCode.ifBlank { null },
              discountPercentage = discountPercentText.toIntOrNull(),
              discountAmountToman = discountAmountText.toLongOrNull(),
              expirationDateFa = discountExpiry.ifBlank { null },
              isExpiringSoon = false,
              isNationwide = isNationwide,
              terms = discountTerms.ifBlank { null }
            )
          } else null,
          status = ListingStatus.PUBLIC,
          timeAgoFa = "چند لحظه پیش",
          visibilityTier = MembershipTier.FREE
        )
        onSubmit(newListing)
      },
      enabled = isFormValid,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = accentColor),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("submit_listing_btn")
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "ثبت و انتشار آگهی",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.White
      )
    }

    Spacer(modifier = Modifier.height(80.dp))
  }
}

@Composable
private fun ListingTypeOptionCard(
  title: String,
  description: String,
  icon: ImageVector,
  accentColor: Color,
  badgeText: String? = null,
  badgeColor: Color = accentColor,
  testTag: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (badgeText != null) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronLeft,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
