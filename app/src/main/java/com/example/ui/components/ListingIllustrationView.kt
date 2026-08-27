package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Listing
import com.example.domain.model.ListingType
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldenYellow
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.LimeGreenAccent
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.VioletAccent

enum class IllustrationLayout {
  WIDE_BANNER,
  SPLIT_SIDE,
  SQUARE_MAGAZINE,
  SPOTLIGHT_HERO,
  COMPACT_ICON
}

data class IllustrationTheme(
  val gradient: List<Color>,
  val primaryAccent: Color,
  val secondaryAccent: Color,
  val mainIcon: ImageVector,
  val subIcon: ImageVector,
  val sceneType: SceneType
)

enum class SceneType {
  FURNITURE_STUDY,
  BOOKS_EDUCATION,
  KIDS_WARMTH,
  TECH_MONITOR,
  FOOD_DELIVERY,
  COURSE_DEV,
  SHOPPING_DIGITAL,
  BABY_STROLLER,
  GENERIC_GIFT,
  GENERIC_DISCOUNT,
  GENERIC_REQUEST
}

@Composable
fun ListingIllustrationView(
  listing: Listing,
  layout: IllustrationLayout,
  modifier: Modifier = Modifier,
  height: Dp = 160.dp,
  showBadge: Boolean = true
) {
  val theme = getThemeForListing(listing)

  // Infinite shimmer pulse for dynamic animations
  val infiniteTransition = rememberInfiniteTransition(label = "illustration_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val shimmerOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_offset"
  )

  Box(
    modifier = modifier
      .clip(
        when (layout) {
          IllustrationLayout.WIDE_BANNER -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
          IllustrationLayout.SPLIT_SIDE -> RoundedCornerShape(14.dp)
          IllustrationLayout.SQUARE_MAGAZINE -> RoundedCornerShape(16.dp)
          IllustrationLayout.SPOTLIGHT_HERO -> RoundedCornerShape(16.dp)
          IllustrationLayout.COMPACT_ICON -> RoundedCornerShape(12.dp)
        }
      )
      .background(Brush.linearGradient(theme.gradient))
  ) {
    // Custom Background Geometric Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawVectorSceneBackground(theme, size.width, size.height, pulseScale)
    }

    // Main Center Iconic Composition
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      IllustrationContent(
        theme = theme,
        listing = listing,
        layout = layout,
        pulseScale = pulseScale
      )
    }

    // Floating Highlight Tag on Top Corner
    if (showBadge) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(8.dp)
      ) {
        when {
          listing.type == ListingType.FREE_GIFT -> {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.92f),
              shadowElevation = 2.dp
            ) {
              Text(
                text = "۱۰۰٪ رایگان 🌿",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          listing.type == ListingType.DISCOUNT && listing.discountInfo != null -> {
            val discText = when {
              listing.discountInfo.discountPercentage != null -> "${PersianUtils.formatNumber(listing.discountInfo.discountPercentage)}٪ تخفیف"
              listing.discountInfo.discountAmountToman != null -> "${PersianUtils.formatNumber(listing.discountInfo.discountAmountToman)} ت"
              else -> "کوپن ویژه"
            }
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = AmberSecondary,
              shadowElevation = 3.dp
            ) {
              Text(
                text = discText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          listing.type == ListingType.REQUEST -> {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = IndigoAccent,
              shadowElevation = 2.dp
            ) {
              Text(
                text = "درخواست یاری 🤝",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun IllustrationContent(
  theme: IllustrationTheme,
  listing: Listing,
  layout: IllustrationLayout,
  pulseScale: Float
) {
  val iconSize = when (layout) {
    IllustrationLayout.WIDE_BANNER -> 48.dp
    IllustrationLayout.SPLIT_SIDE -> 34.dp
    IllustrationLayout.SQUARE_MAGAZINE -> 44.dp
    IllustrationLayout.SPOTLIGHT_HERO -> 52.dp
    IllustrationLayout.COMPACT_ICON -> 28.dp
  }

  val containerSize = when (layout) {
    IllustrationLayout.WIDE_BANNER -> 72.dp
    IllustrationLayout.SPLIT_SIDE -> 54.dp
    IllustrationLayout.SQUARE_MAGAZINE -> 68.dp
    IllustrationLayout.SPOTLIGHT_HERO -> 80.dp
    IllustrationLayout.COMPACT_ICON -> 44.dp
  }

  Box(
    modifier = Modifier
      .size(containerSize)
      .clip(CircleShape)
      .background(Color.White.copy(alpha = 0.88f))
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = theme.mainIcon,
      contentDescription = listing.title,
      tint = theme.primaryAccent,
      modifier = Modifier.size(iconSize)
    )
  }
}

private fun DrawScope.drawVectorSceneBackground(
  theme: IllustrationTheme,
  width: Float,
  height: Float,
  pulse: Float
) {
  // Soft decorative circles
  drawCircle(
    color = Color.White.copy(alpha = 0.18f),
    radius = (height * 0.55f) * pulse,
    center = Offset(width * 0.85f, height * 0.25f)
  )

  drawCircle(
    color = theme.primaryAccent.copy(alpha = 0.12f),
    radius = height * 0.4f,
    center = Offset(width * 0.15f, height * 0.8f)
  )

  // Floating decorative geometric accents
  val strokeStyle = Stroke(width = 2.5f)

  drawRoundRect(
    color = Color.White.copy(alpha = 0.22f),
    topLeft = Offset(width * 0.1f, height * 0.2f),
    size = Size(height * 0.25f, height * 0.25f),
    cornerRadius = CornerRadius(8f, 8f),
    style = strokeStyle
  )

  drawCircle(
    color = Color.White.copy(alpha = 0.35f),
    radius = 4f,
    center = Offset(width * 0.7f, height * 0.75f)
  )
  drawCircle(
    color = Color.White.copy(alpha = 0.45f),
    radius = 6f,
    center = Offset(width * 0.3f, height * 0.35f)
  )
  drawCircle(
    color = Color.White.copy(alpha = 0.25f),
    radius = 3f,
    center = Offset(width * 0.9f, height * 0.85f)
  )
}

fun getThemeForListing(listing: Listing): IllustrationTheme {
  val title = listing.title
  val cat = listing.categoryId
  val type = listing.type

  return when {
    title.contains("میز") || cat == "fg_furniture" || cat == "rq_study" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF0D9488), Color(0xFF14B8A6), Color(0xFF5EEAD4)),
        primaryAccent = EmeraldPrimary,
        secondaryAccent = LimeGreenAccent,
        mainIcon = Icons.Default.Chair,
        subIcon = Icons.Default.AutoAwesome,
        sceneType = SceneType.FURNITURE_STUDY
      )
    }
    title.contains("کتاب") || title.contains("کنکور") || cat == "fg_books" || cat == "dc_online" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF6366F1), Color(0xFF818CF8), Color(0xFFC7D2FE)),
        primaryAccent = IndigoAccent,
        secondaryAccent = SkyBlueAccent,
        mainIcon = Icons.Default.MenuBook,
        subIcon = Icons.Default.School,
        sceneType = SceneType.BOOKS_EDUCATION
      )
    }
    title.contains("کاپشن") || title.contains("لباس") || cat == "fg_kids" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFFFECDD3)),
        primaryAccent = CoralTertiary,
        secondaryAccent = PinkAccent,
        mainIcon = Icons.Default.Toys,
        subIcon = Icons.Default.ChildCare,
        sceneType = SceneType.KIDS_WARMTH
      )
    }
    title.contains("مانیتور") || title.contains("الکترونیک") || cat == "fg_electronics" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD)),
        primaryAccent = SkyBlueAccent,
        secondaryAccent = IndigoAccent,
        mainIcon = Icons.Default.Computer,
        subIcon = Icons.Default.Devices,
        sceneType = SceneType.TECH_MONITOR
      )
    }
    title.contains("اسنپ‌فود") || title.contains("غذا") || cat == "dc_food" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFED7AA)),
        primaryAccent = OrangeAccent,
        secondaryAccent = AmberSecondary,
        mainIcon = Icons.Default.Fastfood,
        subIcon = Icons.Default.Restaurant,
        sceneType = SceneType.FOOD_DELIVERY
      )
    }
    title.contains("برنامه‌نویسی") || title.contains("آموزش") || cat == "dc_course" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFDDD6FE)),
        primaryAccent = VioletAccent,
        secondaryAccent = PinkAccent,
        mainIcon = Icons.Default.School,
        subIcon = Icons.Default.Computer,
        sceneType = SceneType.COURSE_DEV
      )
    }
    title.contains("کالسکه") || cat == "rq_kids" -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFFDB2777), Color(0xFFEC4899), Color(0xFFFBCFE8)),
        primaryAccent = PinkAccent,
        secondaryAccent = VioletAccent,
        mainIcon = Icons.Default.ChildCare,
        subIcon = Icons.Default.Toys,
        sceneType = SceneType.BABY_STROLLER
      )
    }
    type == ListingType.DISCOUNT -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFDE68A)),
        primaryAccent = AmberSecondary,
        secondaryAccent = GoldenYellow,
        mainIcon = Icons.Default.LocalOffer,
        subIcon = Icons.Default.ShoppingBag,
        sceneType = SceneType.GENERIC_DISCOUNT
      )
    }
    type == ListingType.REQUEST -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFFBFDBFE)),
        primaryAccent = SkyBlueAccent,
        secondaryAccent = IndigoAccent,
        mainIcon = Icons.Default.HelpOutline,
        subIcon = Icons.Default.AutoAwesome,
        sceneType = SceneType.GENERIC_REQUEST
      )
    }
    else -> {
      IllustrationTheme(
        gradient = listOf(Color(0xFF0D9488), Color(0xFF14B8A6), Color(0xFF99F6E4)),
        primaryAccent = EmeraldPrimary,
        secondaryAccent = AmberSecondary,
        mainIcon = Icons.Default.CardGiftcard,
        subIcon = Icons.Default.AutoAwesome,
        sceneType = SceneType.GENERIC_GIFT
      )
    }
  }
}
