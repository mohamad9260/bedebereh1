package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Listing
import com.example.domain.model.ListingAccessStatus
import com.example.domain.model.ListingType
import com.example.domain.model.MembershipTier
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LimeGreenAccent
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.VioletAccent

enum class CardPresentationStyle {
  FULL_WIDTH_HERO,
  SIDE_BY_SIDE_SPLIT,
  SQUARE_MAGAZINE,
  SPOTLIGHT_GRADIENT
}

@Composable
fun ListingCard(
  listing: Listing,
  isFavorite: Boolean,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onItemClick: (Listing) -> Unit,
  onFavoriteClick: (String) -> Unit,
  onShareClick: (Listing) -> Unit,
  onReserveClick: (Listing) -> Unit,
  modifier: Modifier = Modifier,
  cardIndex: Int = 0
) {
  // Determine layout style based on listing id hash / index to create varied feed layouts
  val style = remember(listing.id, cardIndex) {
    val hash = Math.abs((listing.id.hashCode() + cardIndex * 31) % 4)
    when (hash) {
      0 -> CardPresentationStyle.FULL_WIDTH_HERO
      1 -> CardPresentationStyle.SIDE_BY_SIDE_SPLIT
      2 -> CardPresentationStyle.SQUARE_MAGAZINE
      else -> CardPresentationStyle.SPOTLIGHT_GRADIENT
    }
  }

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "card_press_scale"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .scale(scale)
      .testTag("listing_card_${listing.id}")
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = { onItemClick(listing) }
      )
  ) {
    when (style) {
      CardPresentationStyle.FULL_WIDTH_HERO -> {
        FullWidthHeroCard(
          listing = listing,
          isFavorite = isFavorite,
          accessStatus = accessStatus,
          onFavoriteClick = onFavoriteClick,
          onShareClick = onShareClick,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
      CardPresentationStyle.SIDE_BY_SIDE_SPLIT -> {
        SideBySideSplitCard(
          listing = listing,
          isFavorite = isFavorite,
          accessStatus = accessStatus,
          onFavoriteClick = onFavoriteClick,
          onShareClick = onShareClick,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
      CardPresentationStyle.SQUARE_MAGAZINE -> {
        SquareMagazineCard(
          listing = listing,
          isFavorite = isFavorite,
          accessStatus = accessStatus,
          onFavoriteClick = onFavoriteClick,
          onShareClick = onShareClick,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
      CardPresentationStyle.SPOTLIGHT_GRADIENT -> {
        SpotlightGradientCard(
          listing = listing,
          isFavorite = isFavorite,
          accessStatus = accessStatus,
          onFavoriteClick = onFavoriteClick,
          onShareClick = onShareClick,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
    }
  }
}

// 1. Full-Width Hero Image Banner Card
@Composable
private fun FullWidthHeroCard(
  listing: Listing,
  isFavorite: Boolean,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onFavoriteClick: (String) -> Unit,
  onShareClick: (Listing) -> Unit,
  onReserveClick: (Listing) -> Unit,
  onItemClick: (Listing) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Full Width Top Graphic Banner
      Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.WIDE_BANNER,
          modifier = Modifier.fillMaxSize()
        )

        // Overlay Action Icons on top of Image
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Category pill with translucent backdrop
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.55f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = getCategoryVector(listing.categoryIcon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = listing.categoryNameFa,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
              )
            }
          }

          // Favorite & Share with Circular glassmorphism buttons
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AnimatedFavoriteButton(
              isFavorite = isFavorite,
              onClick = { onFavoriteClick(listing.id) },
              containerColor = Color.White.copy(alpha = 0.95f)
            )
            Surface(
              shape = CircleShape,
              color = Color.White.copy(alpha = 0.95f),
              modifier = Modifier.size(34.dp).clickable { onShareClick(listing) }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "اشتراک‌گذاری",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      // Content Section below Banner
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Badges Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (listing.visibilityTier != MembershipTier.FREE) {
              TierBadge(tier = listing.visibilityTier)
            }
            if (listing.isReserved) {
              ListingStateBadge(status = listing.status)
            }
          }

          Text(
            text = listing.timeAgoFa,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
          text = listing.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Description
        Text(
          text = listing.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 20.sp,
          fontSize = 13.sp
        )

        // Discount box if any
        if (listing.type == ListingType.DISCOUNT && listing.discountInfo != null) {
          Spacer(modifier = Modifier.height(10.dp))
          DiscountHighlightBar(discountInfo = listing.discountInfo)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Footer with Location & Primary Action
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          LocationLabel(city = listing.city, location = listing.approximateLocation)
          ListingActionButton(
            listing = listing,
            accessStatus = accessStatus,
            onReserveClick = onReserveClick,
            onItemClick = onItemClick
          )
        }
      }
    }
  }
}

// 2. Side-by-Side Split Card (Image on Right, Text on Left)
@Composable
private fun SideBySideSplitCard(
  listing: Listing,
  isFavorite: Boolean,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onFavoriteClick: (String) -> Unit,
  onShareClick: (Listing) -> Unit,
  onReserveClick: (Listing) -> Unit,
  onItemClick: (Listing) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Content details (Takes remaining width)
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when (listing.type) {
              ListingType.FREE_GIFT -> EmeraldPrimary.copy(alpha = 0.12f)
              ListingType.DISCOUNT -> AmberSecondary.copy(alpha = 0.12f)
              ListingType.REQUEST -> SkyBlueAccent.copy(alpha = 0.12f)
            }
          ) {
            Text(
              text = listing.categoryNameFa,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when (listing.type) {
                ListingType.FREE_GIFT -> EmeraldPrimary
                ListingType.DISCOUNT -> AmberSecondary
                ListingType.REQUEST -> SkyBlueAccent
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          AnimatedFavoriteButton(
            isFavorite = isFavorite,
            onClick = { onFavoriteClick(listing.id) }
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = listing.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = listing.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          LocationLabel(city = listing.city, location = null)
          ListingActionButton(
            listing = listing,
            accessStatus = accessStatus,
            onReserveClick = onReserveClick,
            onItemClick = onItemClick,
            compact = true
          )
        }
      }

      // Compact Image Thumbnail on Right (in Persian RTL layout)
      Box(
        modifier = Modifier
          .size(105.dp)
          .clip(RoundedCornerShape(14.dp))
      ) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.SPLIT_SIDE,
          modifier = Modifier.fillMaxSize(),
          showBadge = false
        )
      }
    }
  }
}

// 3. Square Magazine Card Layout
@Composable
private fun SquareMagazineCard(
  listing: Listing,
  isFavorite: Boolean,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onFavoriteClick: (String) -> Unit,
  onShareClick: (Listing) -> Unit,
  onReserveClick: (Listing) -> Unit,
  onItemClick: (Listing) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Author header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
          Column {
            Text(
              text = listing.ownerDisplayName,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = listing.timeAgoFa,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          AnimatedFavoriteButton(
            isFavorite = isFavorite,
            onClick = { onFavoriteClick(listing.id) }
          )
          IconButton(
            onClick = { onShareClick(listing) },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "اشتراک",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Graphic Square/Portrait Image Scene
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(16.dp))
      ) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.SQUARE_MAGAZINE,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = listing.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = listing.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LocationLabel(city = listing.city, location = listing.approximateLocation)
        ListingActionButton(
          listing = listing,
          accessStatus = accessStatus,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
    }
  }
}

// 4. Spotlight Gradient Card (Rich Soft-Tinted Background)
@Composable
private fun SpotlightGradientCard(
  listing: Listing,
  isFavorite: Boolean,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onFavoriteClick: (String) -> Unit,
  onShareClick: (Listing) -> Unit,
  onReserveClick: (Listing) -> Unit,
  onItemClick: (Listing) -> Unit
) {
  val theme = remember(listing.id) { getThemeForListing(listing) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(20.dp)
      ),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(theme.gradient)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = theme.mainIcon,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = listing.categoryNameFa,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = theme.primaryAccent
            )
            Text(
              text = listing.timeAgoFa,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (listing.visibilityTier != MembershipTier.FREE) {
            TierBadge(tier = listing.visibilityTier)
          }
          AnimatedFavoriteButton(
            isFavorite = isFavorite,
            onClick = { onFavoriteClick(listing.id) }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Compact Visual Spotlight Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
          .clip(RoundedCornerShape(14.dp))
      ) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.SPOTLIGHT_HERO,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = listing.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 22.sp
      )

      if (listing.type == ListingType.DISCOUNT && listing.discountInfo != null) {
        Spacer(modifier = Modifier.height(8.dp))
        DiscountHighlightBar(discountInfo = listing.discountInfo)
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LocationLabel(city = listing.city, location = listing.approximateLocation)
        ListingActionButton(
          listing = listing,
          accessStatus = accessStatus,
          onReserveClick = onReserveClick,
          onItemClick = onItemClick
        )
      }
    }
  }
}

// Micro-Interaction Animated Favorite Button
@Composable
fun AnimatedFavoriteButton(
  isFavorite: Boolean,
  onClick: () -> Unit,
  containerColor: Color = Color.Transparent
) {
  val scale by animateFloatAsState(
    targetValue = if (isFavorite) 1.25f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    label = "fav_scale"
  )

  val iconColor by animateColorAsState(
    targetValue = if (isFavorite) CoralTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
    animationSpec = tween(200),
    label = "fav_color"
  )

  Surface(
    shape = CircleShape,
    color = containerColor,
    modifier = Modifier.size(34.dp).clickable(onClick = onClick)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
        contentDescription = "نشان کردن",
        tint = iconColor,
        modifier = Modifier
          .size(20.dp)
          .scale(scale)
      )
    }
  }
}

@Composable
private fun DiscountHighlightBar(discountInfo: com.example.domain.model.DiscountInfo) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(AmberSecondary.copy(alpha = 0.12f))
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.LocalOffer,
        contentDescription = null,
        tint = AmberSecondary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (discountInfo.discountPercentage != null) {
          "${PersianUtils.formatNumber(discountInfo.discountPercentage)}٪ تخفیف اختصاصی"
        } else if (discountInfo.discountAmountToman != null) {
          "${PersianUtils.formatNumber(discountInfo.discountAmountToman)} تومان تخفیف"
        } else {
          "کوپن اختصاصی"
        },
        style = MaterialTheme.typography.labelMedium,
        color = AmberSecondary,
        fontWeight = FontWeight.Bold
      )
    }

    if (discountInfo.discountCode != null) {
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = AmberSecondary
      ) {
        Text(
          text = discountInfo.discountCode,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }
    }
  }
}

@Composable
private fun LocationLabel(city: String, location: String?) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = Icons.Default.LocationOn,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(15.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = buildString {
        append(city)
        if (!location.isNullOrBlank()) {
          append(" • ")
          append(location)
        }
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun ListingActionButton(
  listing: Listing,
  accessStatus: ListingAccessStatus = ListingAccessStatus(),
  onReserveClick: (Listing) -> Unit,
  onItemClick: (Listing) -> Unit,
  compact: Boolean = false
) {
  val btnHeight = if (compact) 32.dp else 36.dp
  val fontSize = if (compact) 11.sp else 12.sp

  when (listing.type) {
    ListingType.FREE_GIFT -> {
      if (listing.isReserved) {
        OutlinedButton(
          onClick = { },
          enabled = false,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(btnHeight)
        ) {
          Text("رزرو شده", fontSize = fontSize)
        }
      } else if (accessStatus.isLockedForCurrentUser) {
        OutlinedButton(
          onClick = { onItemClick(listing) },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AmberSecondary
          ),
          modifier = Modifier
            .height(btnHeight)
            .testTag("reserve_locked_btn_${listing.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = AmberSecondary,
            modifier = Modifier.size(if (compact) 11.dp else 13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = if (accessStatus.requiredTierNow == MembershipTier.GOLD) "مخصوص طلایی" else "مخصوص نقره‌ای",
            fontSize = fontSize,
            color = AmberSecondary,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        Button(
          onClick = { onReserveClick(listing) },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.height(btnHeight).testTag("reserve_btn_${listing.id}")
        ) {
          Text("رزرو هدیه", fontSize = fontSize, color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
    ListingType.DISCOUNT -> {
      FilledTonalButton(
        onClick = { onItemClick(listing) },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = AmberSecondary.copy(alpha = 0.15f),
          contentColor = AmberSecondary
        ),
        modifier = Modifier.height(btnHeight)
      ) {
        Text("مشاهده کوپن", fontSize = fontSize, fontWeight = FontWeight.Bold)
      }
    }
    ListingType.REQUEST -> {
      Button(
        onClick = { onReserveClick(listing) },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueAccent),
        modifier = Modifier.height(btnHeight)
      ) {
        Text("پاسخ به نیاز", fontSize = fontSize, color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  }
}

fun getCategoryVector(iconName: String): ImageVector {
  return when (iconName) {
    "chair" -> Icons.Default.Chair
    "menu_book" -> Icons.Default.MenuBook
    "checkroom" -> Icons.Default.Checkroom
    "devices" -> Icons.Default.Devices
    "toys" -> Icons.Default.Toys
    "restaurant" -> Icons.Default.Restaurant
    "shopping_bag" -> Icons.Default.ShoppingBag
    "school" -> Icons.Default.School
    "child_care" -> Icons.Default.ChildCare
    else -> Icons.Default.CardGiftcard
  }
}
