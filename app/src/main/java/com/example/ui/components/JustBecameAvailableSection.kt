package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Listing
import com.example.domain.model.MembershipTier
import com.example.domain.model.RecentlyAvailableItem
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LimeGreenAccent

/**
 * Compact horizontal scrolling section displaying listings that just became available
 * to the current user's membership tier ("⚡ همین الان رایگان شد").
 */
@Composable
fun JustBecameAvailableSection(
  items: List<RecentlyAvailableItem>,
  userTier: MembershipTier,
  onListingClick: (Listing) -> Unit,
  onViewAllClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .testTag("just_became_available_section")
  ) {
    // Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
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
            .background(
              Brush.linearGradient(
                colors = listOf(AmberSecondary, EmeraldPrimary)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "⚡ همین الان رایگان شد",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AmberSecondary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "تازه آزاد شده",
                color = AmberSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "هدایای اهدایی که به تازگی در دسترس پلن ${userTier.titleFa} شما قرار گرفته‌اند",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      // "مشاهده همه" Action
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onViewAllClick)
          .padding(horizontal = 6.dp, vertical = 4.dp)
          .testTag("just_available_view_all_btn"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Text(
          text = "مشاهده همه",
          color = EmeraldPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Horizontal Scrolling Row
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(items, key = { it.listing.id }) { item ->
        JustBecameAvailableCard(
          item = item,
          onClick = { onListingClick(item.listing) }
        )
      }
    }
  }
}

/**
 * Compact, fast, fresh card for recently available items.
 */
@Composable
fun JustBecameAvailableCard(
  item: RecentlyAvailableItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listing = item.listing

  Card(
    modifier = modifier
      .width(172.dp)
      .height(224.dp)
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("just_available_card_${listing.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.dp,
      pressedElevation = 3.dp
    )
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Image / Graphic Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(112.dp)
      ) {
        ListingIllustrationView(
          listing = listing,
          layout = IllustrationLayout.WIDE_BANNER,
          height = 112.dp,
          showBadge = false,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay at bottom of image for contrast
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                startY = 40f
              )
            )
        )

        // Top Start (Right in RTL): "جدید" Badge
        Surface(
          shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 14.dp),
          color = EmeraldPrimary,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(top = 0.dp, start = 0.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Icon(
              imageVector = Icons.Default.NewReleases,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(10.dp)
            )
            Text(
              text = "جدید",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Bottom End: Relative time pill
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color.Black.copy(alpha = 0.75f),
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              tint = AmberSecondary,
              modifier = Modifier.size(11.dp)
            )
            Text(
              text = item.relativeTimeFa,
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Card Content Body
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Title
        Text(
          text = listing.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Location & Category / Free tag
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = listing.city,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldPrimary.copy(alpha = 0.14f)
            ) {
              Text(
                text = "رایگان",
                color = EmeraldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Modal BottomSheet for "مشاهده همه" recently available listings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyAvailableSheet(
  items: List<RecentlyAvailableItem>,
  userTier: MembershipTier,
  sheetState: SheetState,
  onDismiss: () -> Unit,
  onListingClick: (Listing) -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
        .testTag("recently_available_bottom_sheet")
    ) {
      // Sheet Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
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
              .background(AmberSecondary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = AmberSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
          Column {
            Text(
              text = "⚡ همین الان رایگان شد (${PersianUtils.formatNumber(items.size)} آگهی)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "هدایای اهدایی به تازگی آزاد شده برای شما (${userTier.titleFa})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.5.sp
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "بستن")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        itemsIndexed(items, key = { _, it -> it.listing.id }) { index, item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                onDismiss()
                onListingClick(item.listing)
              }
              .testTag("recently_sheet_item_${item.listing.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(RoundedCornerShape(10.dp))
              ) {
                ListingIllustrationView(
                  listing = item.listing,
                  layout = IllustrationLayout.COMPACT_ICON,
                  height = 68.dp,
                  showBadge = false,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "⚡ آزادسازی: ${item.relativeTimeFa}",
                      color = EmeraldPrimary,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }

                  Text(
                    text = item.listing.city,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = item.listing.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = item.listing.description,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
  }
}
