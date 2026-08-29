package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.IranLocationsData
import com.example.data.MockListingRepository
import com.example.domain.model.Category
import com.example.domain.model.DiscountInfo
import com.example.domain.model.Listing
import com.example.domain.model.ListingStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.ui.components.PersianUtils
import com.example.ui.components.getCategoryVector
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.VioletAccent
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.util.UUID

data class VectorOption(
  val id: String,
  val icon: ImageVector
)

data class GradientPreset(
  val id: String,
  val nameFa: String,
  val colors: List<Color>,
  val accentColor: Color
)

val GRADIENT_PALETTES = listOf(
  GradientPreset("emerald", "زمردی", listOf(Color(0xFF0D9488), Color(0xFF10B981)), EmeraldPrimary),
  GradientPreset("ocean", "اقیانوسی", listOf(Color(0xFF1E88E5), Color(0xFF00ACC1)), SkyBlueAccent),
  GradientPreset("sunset", "غروب پرتقالی", listOf(Color(0xFFF97316), Color(0xFFF43F5E)), OrangeAccent),
  GradientPreset("royal_purple", "بنفش ارغوانی", listOf(Color(0xFF7E22CE), Color(0xFFA855F7)), VioletAccent),
  GradientPreset("indigo_night", "نیلی کهکشانی", listOf(Color(0xFF3730A3), Color(0xFF6366F1)), IndigoAccent),
  GradientPreset("rose_gold", "صورتی گلبهی", listOf(Color(0xFFDB2777), Color(0xFFFB7185)), PinkAccent),
  GradientPreset("amber_gold", "طلایی کهربایی", listOf(Color(0xFFD97706), Color(0xFFFBBF24)), AmberSecondary),
  GradientPreset("slate_dark", "طوسی ذغالی", listOf(Color(0xFF334155), Color(0xFF64748B)), Color(0xFF94A3B8))
)

val PRESET_VECTORS = listOf(
  VectorOption("vector:gift", Icons.Default.CardGiftcard),
  VectorOption("vector:furniture", Icons.Default.Chair),
  VectorOption("vector:books", Icons.Default.MenuBook),
  VectorOption("vector:digital", Icons.Default.Computer),
  VectorOption("vector:kids", Icons.Default.Toys),
  VectorOption("vector:clothes", Icons.Default.Checkroom),
  VectorOption("vector:kitchen", Icons.Default.Kitchen),
  VectorOption("vector:car", Icons.Default.DirectionsCar),
  VectorOption("vector:food", Icons.Default.Fastfood),
  VectorOption("vector:discount", Icons.Default.LocalOffer),
  VectorOption("vector:tools", Icons.Default.Build),
  VectorOption("vector:health", Icons.Default.MedicalServices),
  VectorOption("vector:school", Icons.Default.School),
  VectorOption("vector:childcare", Icons.Default.ChildCare)
)

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
  val serverCategories by repository.categories.collectAsState(initial = emptyList())
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
      allCategories = serverCategories,
      userProfile = userProfile,
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ListingFormStep(
  type: ListingType,
  allCategories: List<Category>,
  userProfile: com.example.domain.model.UserProfile?,
  onBack: () -> Unit,
  onSubmit: (Listing) -> Unit,
  modifier: Modifier = Modifier
) {
  val matchingCategories = remember(type, allCategories) {
    val filtered = allCategories.filter { it.type == type }
    if (filtered.isNotEmpty()) filtered else allCategories
  }
  var selectedCategory by remember { mutableStateOf<Category?>(null) }

  // Multi-Image Support
  var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
  var selectedGradient by remember { mutableStateOf(GRADIENT_PALETTES.first()) }
  var imageInputTab by remember { mutableIntStateOf(1) } // 0: Presets, 1: Gallery/URL
  var customUrlInput by remember { mutableStateOf("") }

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Gallery multi-selection launcher
  val galleryMultiLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      coroutineScope.launch {
        val dataUrls = uris.map { ImageUtils.uriToDataUrl(context, it) }
        selectedImages = (selectedImages + dataUrls).distinct()
      }
    }
  }

  // Single gallery launcher (for adding one)
  val gallerySingleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    uri?.let { pickedUri ->
      coroutineScope.launch {
        val dataUrl = ImageUtils.uriToDataUrl(context, pickedUri)
        if (!selectedImages.contains(dataUrl)) {
          selectedImages = selectedImages + dataUrl
        }
      }
    }
  }

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  
  // Province & City Selection from IranLocationsData
  var selectedProvince by remember { mutableStateOf("تهران") }
  var selectedCity by remember { mutableStateOf("تهران") }
  var provinceDropdownExpanded by remember { mutableStateOf(false) }
  var cityDropdownExpanded by remember { mutableStateOf(false) }
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

    // ==========================================
    // SECTION 1: MULTI-IMAGE SELECTION & PREVIEW
    // ==========================================
    Text(
      text = "تصاویر آگهی (حداقل یک تصویر الزامی است) *",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = if (selectedImages.isEmpty()) CoralTertiary else MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = "می‌توانید چند عکس انتخاب کنید. اولین تصویر به عنوان تصویر اصلی و کاور آگهی نمایش داده می‌شود:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Multi-Image Preview Strip
    if (selectedImages.isNotEmpty()) {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        itemsIndexed(selectedImages) { index, imgUrl ->
          val isCover = index == 0
          Card(
            modifier = Modifier
              .size(width = 110.dp, height = 110.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(
                width = if (isCover) 2.5.dp else 1.dp,
                color = if (isCover) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
              ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              if (imgUrl.startsWith("http") || imgUrl.startsWith("data:image") || imgUrl.startsWith("content") || imgUrl.startsWith("file")) {
                AsyncImage(
                  model = imgUrl,
                  contentDescription = "تصویر ${index + 1}",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                val preset = PRESET_VECTORS.find { it.id == imgUrl } ?: PRESET_VECTORS.first()
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(selectedGradient.colors)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                  )
                }
              }

              // Cover badge on the first image
              if (isCover) {
                Surface(
                  shape = RoundedCornerShape(bottomEnd = 10.dp),
                  color = EmeraldPrimary,
                  modifier = Modifier.align(Alignment.TopStart)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                  ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Text("کاور اصلی", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }

              // Delete button
              IconButton(
                onClick = {
                  selectedImages = selectedImages.toMutableList().also { it.removeAt(index) }
                },
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(4.dp)
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.65f))
              ) {
                Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }
        }

        // Add More Images Button
        item {
          OutlinedButton(
            onClick = { galleryMultiLauncher.launch("image/*") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .size(width = 110.dp, height = 110.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text("افزودن عکس", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    // Image Picker Options Box
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          width = 1.dp,
          color = if (selectedImages.isEmpty()) CoralTertiary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
          shape = RoundedCornerShape(16.dp)
        ),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        TabRow(
          selectedTabIndex = imageInputTab,
          containerColor = Color.Transparent,
          modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
          Tab(
            selected = imageInputTab == 1,
            onClick = { imageInputTab = 1 },
            text = { Text("انتخاب از گالری گوشی / لینک", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
            icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp)) }
          )
          Tab(
            selected = imageInputTab == 0,
            onClick = { imageInputTab = 0 },
            text = { Text("آیکون‌های آماده بده‌بره", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
            icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (imageInputTab == 1) {
          // Upload from Gallery (Multiple images) & URL
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { galleryMultiLauncher.launch("image/*") },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
              Spacer(modifier = Modifier.width(8.dp))
              Text("انتخاب یک یا چند عکس از گالری", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = customUrlInput,
                onValueChange = { customUrlInput = it },
                label = { Text("یا وارد کردن لینک مستقیم تصویر (URL)") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              )
              Button(
                onClick = {
                  if (customUrlInput.isNotBlank()) {
                    selectedImages = selectedImages + customUrlInput.trim()
                    customUrlInput = ""
                  }
                },
                enabled = customUrlInput.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("افزودن")
              }
            }
          }
        } else {
          // Presets / Gradients
          Column {
            Text(
              text = "انتخاب تم رنگی گرادیانت:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              GRADIENT_PALETTES.forEach { grad ->
                val isGradSelected = selectedGradient.id == grad.id
                Box(
                  modifier = Modifier
                    .size(width = 68.dp, height = 34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(grad.colors))
                    .border(
                      width = if (isGradSelected) 2.5.dp else 1.dp,
                      color = if (isGradSelected) Color.White else Color.Transparent,
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { selectedGradient = grad },
                  contentAlignment = Alignment.Center
                ) {
                  if (isGradSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "انتخاب آیکون وکتور موضوعی:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              PRESET_VECTORS.forEach { vectorOpt ->
                val isSelected = selectedImages.contains(vectorOpt.id)
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) selectedGradient.accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) selectedGradient.accentColor else MaterialTheme.colorScheme.outlineVariant
                  ),
                  modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      if (!isSelected) {
                        selectedImages = selectedImages + vectorOpt.id
                      } else {
                        selectedImages = selectedImages.filterNot { it == vectorOpt.id }
                      }
                    }
                ) {
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = vectorOpt.icon,
                      contentDescription = null,
                      tint = if (isSelected) selectedGradient.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(26.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // SECTION 2: CATEGORY SELECTION (NO DEFAULT)
    // ==========================================
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "انتخاب دسته‌بندی (الزامی) *",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = if (selectedCategory == null) CoralTertiary else MaterialTheme.colorScheme.onSurface
      )
      if (selectedCategory == null) {
        Text(
          text = "لطفاً یکی را انتخاب کنید",
          style = MaterialTheme.typography.bodySmall,
          color = CoralTertiary,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Grouping by Parent / Child
    val parentCategories = remember(matchingCategories) {
      matchingCategories.filter { it.parentId == null }
    }

    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      parentCategories.forEach { parentCat ->
        val isSelected = selectedCategory?.id == parentCat.id || selectedCategory?.parentId == parentCat.id
        FilterChip(
          selected = isSelected,
          onClick = { selectedCategory = parentCat },
          label = {
            Text(text = parentCat.titleFa, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
          },
          leadingIcon = {
            Icon(
              imageVector = getCategoryVector(parentCat.iconName),
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.15f),
            selectedLabelColor = EmeraldPrimary,
            selectedLeadingIconColor = EmeraldPrimary
          )
        )
      }
    }

    // Subcategories if a parent with children is chosen
    val selectedParent = parentCategories.find { it.id == selectedCategory?.id || it.id == selectedCategory?.parentId }
    if (selectedParent != null) {
      val subCategories = remember(selectedParent, matchingCategories) {
        matchingCategories.filter { it.parentId == selectedParent.id }
      }
      if (subCategories.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "زیرمجموعه «${selectedParent.titleFa}»:",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          subCategories.forEach { subCat ->
            val isSubSelected = selectedCategory?.id == subCat.id
            FilterChip(
              selected = isSubSelected,
              onClick = { selectedCategory = subCat },
              label = { Text(subCat.titleFa, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // SECTION 3: TITLE & DESCRIPTION
    // ==========================================
    Text(
      text = "مشخصات و توضیحات آگهی *",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("عنوان آگهی (مثال: میز تحریر چوبی تمیز، کتاب کنکور)") },
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

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("توضیحات کامل آگهی و مشخصات کالا") },
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

    Spacer(modifier = Modifier.height(20.dp))

    // ==========================================
    // SECTION 4: PROVINCE & CITY SELECTION
    // ==========================================
    Text(
      text = "استان و شهر (انتخاب از لیست رسمی کشور) *",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Province Dropdown
      ExposedDropdownMenuBox(
        expanded = provinceDropdownExpanded,
        onExpandedChange = { provinceDropdownExpanded = !provinceDropdownExpanded },
        modifier = Modifier.weight(1f)
      ) {
        OutlinedTextField(
          value = selectedProvince,
          onValueChange = {},
          readOnly = true,
          label = { Text("استان") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceDropdownExpanded) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            focusedLabelColor = accentColor
          ),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("dropdown_province")
        )

        ExposedDropdownMenu(
          expanded = provinceDropdownExpanded,
          onDismissRequest = { provinceDropdownExpanded = false }
        ) {
          IranLocationsData.provinces.forEach { prov ->
            DropdownMenuItem(
              text = { Text(prov, fontWeight = if (prov == selectedProvince) FontWeight.Bold else FontWeight.Normal) },
              onClick = {
                selectedProvince = prov
                // Set first city of the selected province
                val cities = IranLocationsData.getCitiesForProvince(prov)
                selectedCity = cities.firstOrNull() ?: prov
                provinceDropdownExpanded = false
              }
            )
          }
        }
      }

      // 2. City Dropdown
      ExposedDropdownMenuBox(
        expanded = cityDropdownExpanded,
        onExpandedChange = { cityDropdownExpanded = !cityDropdownExpanded },
        modifier = Modifier.weight(1f)
      ) {
        val availableCities = remember(selectedProvince) {
          IranLocationsData.getCitiesForProvince(selectedProvince)
        }

        OutlinedTextField(
          value = selectedCity,
          onValueChange = {},
          readOnly = true,
          label = { Text("شهر") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded) },
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            focusedLabelColor = accentColor
          ),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("dropdown_city")
        )

        ExposedDropdownMenu(
          expanded = cityDropdownExpanded,
          onDismissRequest = { cityDropdownExpanded = false }
        ) {
          availableCities.forEach { cName ->
            DropdownMenuItem(
              text = { Text(cName, fontWeight = if (cName == selectedCity) FontWeight.Bold else FontWeight.Normal) },
              onClick = {
                selectedCity = cName
                cityDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Approximate Location / Neighborhood input
    OutlinedTextField(
      value = approximateLocation,
      onValueChange = { approximateLocation = it },
      label = { Text("محله یا منطقه (اختیاری - مثال: میدان ونک، پیروزی)") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentColor,
        focusedLabelColor = accentColor
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("form_input_location")
    )

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

    Spacer(modifier = Modifier.height(24.dp))

    // Form Validity Check: Category, Image/Vector, Text
    val isCategorySelected = selectedCategory != null
    val isImageSelected = selectedImages.isNotEmpty()
    val isTextValid = title.isNotBlank() && description.isNotBlank() && selectedCity.isNotBlank()
    val isFormValid = isCategorySelected && isImageSelected && isTextValid

    Button(
      onClick = {
        val cat = selectedCategory!!
        val isUserLoggedIn = userProfile?.isLoggedIn == true
        val ownerPhone = if (isUserLoggedIn) (userProfile?.rawPhone?.ifBlank { userProfile?.mobileNumberMasked } ?: "09120000000") else "09120000000"
        val ownerName = if (isUserLoggedIn) (userProfile?.displayName ?: "کاربر بده بره") else "کاربر مهمان"
        
        val newListing = Listing(
          id = UUID.randomUUID().toString().take(6),
          type = type,
          title = title.trim(),
          description = description.trim(),
          categoryId = cat.id,
          categoryNameFa = cat.titleFa,
          categoryIcon = cat.iconName,
          ownerId = userProfile?.id ?: "u_current",
          ownerDisplayName = ownerName,
          ownerPhone = ownerPhone,
          province = selectedProvince,
          city = selectedCity.trim(),
          approximateLocation = approximateLocation.ifBlank { null },
          imageUrls = selectedImages,
          coverImageUrl = selectedImages.firstOrNull(),
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
          status = ListingStatus.PENDING_REVIEW,
          timeAgoFa = "چند لحظه پیش",
          visibilityTier = userProfile?.plan ?: MembershipTier.FREE
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
        text = "ثبت و ارسال آگهی",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.White
      )
    }

    if (!isFormValid) {
      Spacer(modifier = Modifier.height(8.dp))
      val errorMsg = when {
        !isImageSelected -> "⚠️ لطفاً حداقل یک تصویر برای آگهی انتخاب یا آپلود کنید."
        !isCategorySelected -> "⚠️ لطفاً دسته‌بندی آگهی را انتخاب فرمایید."
        title.isBlank() -> "⚠️ لطفاً عنوان آگهی را بنویسید."
        description.isBlank() -> "⚠️ لطفاً توضیحات آگهی را وارد کنید."
        else -> ""
      }
      if (errorMsg.isNotBlank()) {
        Text(
          text = errorMsg,
          color = CoralTertiary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.align(Alignment.CenterHorizontally)
        )
      }
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
