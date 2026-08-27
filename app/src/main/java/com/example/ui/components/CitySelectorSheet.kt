package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IranLocationsData
import com.example.domain.model.City
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectorSheet(
  currentCity: String,
  onCitySelected: (City) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredCities = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      IranLocationsData.allCities
    } else {
      IranLocationsData.allCities.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
            it.province.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("city_selector_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(horizontal = 16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationCity,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "انتخاب استان و شهر",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "بستن",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("جستجوی نام شهر یا استان…", fontSize = 14.sp) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "جستجو",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("city_search_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
          focusedIndicatorColor = EmeraldPrimary,
          unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
        )
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Quick shortcut chip for "All Cities"
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(
            if (currentCity == "همه شهرها") EmeraldPrimary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
          .clickable {
            onCitySelected(IranLocationsData.ALL_CITIES_OPTION)
            onDismiss()
          }
          .padding(14.dp)
          .testTag("select_all_cities"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "همه شهرها (سراسر کشور)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = if (currentCity == "همه شهرها") EmeraldPrimary else MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "نمایش تمام هدایا و تخفیف‌های کل ایران",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        if (currentCity == "همه شهرها") {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = EmeraldPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // List of Cities
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(top = 8.dp)
      ) {
        items(filteredCities) { city ->
          if (city.name != "همه شهرها") {
            val isSelected = currentCity == city.name
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onCitySelected(city)
                  onDismiss()
                }
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .testTag("city_item_${city.name}"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = city.name,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  style = MaterialTheme.typography.bodyLarge,
                  color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "استان ${city.province}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            HorizontalDivider(
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              modifier = Modifier.padding(horizontal = 8.dp)
            )
          }
        }
      }
    }
  }
}
