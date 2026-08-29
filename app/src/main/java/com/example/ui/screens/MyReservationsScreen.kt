package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockListingRepository
import com.example.domain.model.Listing
import com.example.domain.model.ListingType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IllustrationLayout
import com.example.ui.components.ListingIllustrationView
import com.example.ui.components.PersianUtils
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralTertiary
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun MyReservationsScreen(
  repository: MockListingRepository,
  snackbarHostState: SnackbarHostState,
  onListingClick: (Listing) -> Unit,
  onExploreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val reservedListings by repository.getUserReservedListings().collectAsState(initial = emptyList())
  val userProfile by repository.userProfile.collectAsState(initial = MockListingRepository.guestUserProfile)
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  var listingToCancel by remember { mutableStateOf<Listing?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("my_reservations_screen")
  ) {
    if (reservedListings.isEmpty()) {
      EmptyStateView(
        title = "هنوز آگهی رزرو نکرده‌اید",
        description = "هنگامی که کالایی را در بخش هدایای رایگان یا تخفیف‌ها رزرو کنید، در این صفحه نمایش داده می‌شود و می‌توانید با اهداکننده تماس بگیرید.",
        buttonText = "مشاهده و رزرو کالاها",
        icon = Icons.Default.EventAvailable,
        onButtonClick = onExploreClick,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("reservations_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          // Header info badge
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = EmeraldPrimary.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(24.dp)
              )
              Column {
                Text(
                  text = "تعداد کالاهای رزرو شده شما: ${PersianUtils.formatNumber(reservedListings.size)} مورد",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
                Text(
                  text = "جهت هماهنگی و تحویل کالا، لطفاً مستقیماً با صاحب آگهی تماس حاصل فرمایید.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        itemsIndexed(reservedListings, key = { _, it -> it.id }) { _, listing ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reserved_card_${listing.id}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Thumbnail
                val cover = listing.coverImageUrl ?: listing.imageUrls.firstOrNull()
                if (cover != null && (cover.startsWith("http") || cover.startsWith("data:image") || cover.startsWith("content:"))) {
                  AsyncImage(
                    model = cover,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .size(76.dp)
                      .clip(RoundedCornerShape(12.dp))
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(76.dp)
                      .clip(RoundedCornerShape(12.dp))
                  ) {
                    ListingIllustrationView(
                      listing = listing,
                      layout = IllustrationLayout.COMPACT_ICON,
                      modifier = Modifier.fillMaxSize()
                    )
                  }
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = EmeraldPrimary.copy(alpha = 0.12f)
                    ) {
                      Text(
                        text = "رزرو فعال",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                    Text(
                      text = listing.timeAgoFa,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 10.sp
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                  )

                  Spacer(modifier = Modifier.height(2.dp))

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.LocationOn,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = "${listing.province} • ${listing.city}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              Spacer(modifier = Modifier.height(10.dp))

              // Owner Contact Details
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = "اهداکننده: ${listing.ownerDisplayName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }

                val phone = listing.ownerPhone?.takeIf { it.isNotBlank() } ?: "۰۹۱۲۳۴۵۶۷۸۹"
                Text(
                  text = PersianUtils.formatDigits(phone),
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Action Buttons
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val phone = listing.ownerPhone?.takeIf { it.isNotBlank() } ?: "09123456789"
                Button(
                  onClick = {
                    try {
                      val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                      }
                      context.startActivity(intent)
                    } catch (e: Exception) {
                      scope.launch {
                        snackbarHostState.showSnackbar("شماره تماس: $phone")
                      }
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("تماس با اهداکننده", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { listingToCancel = listing },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = CoralTertiary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("لغو رزرو", color = CoralTertiary, fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // Cancel Reservation Dialog
  listingToCancel?.let { item ->
    AlertDialog(
      onDismissRequest = { listingToCancel = null },
      title = {
        Text("لغو رزرو کالا", fontWeight = FontWeight.Bold)
      },
      text = {
        Text("آیا از لغو رزرو «${item.title}» مطمئن هستید؟ با لغو رزرو، این کالا مجدداً برای سایر کاربران فعال خواهد شد.")
      },
      confirmButton = {
        Button(
          onClick = {
            repository.updateListingStatus(item.id, com.example.domain.model.ListingStatus.PUBLIC)
            listingToCancel = null
            scope.launch {
              snackbarHostState.showSnackbar("رزرو آگهی «${item.title}» لغو شد.")
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CoralTertiary)
        ) {
          Text("بله، لغو شود", color = Color.White)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { listingToCancel = null }) {
          Text("انصراف")
        }
      }
    )
  }
}
