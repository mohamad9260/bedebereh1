package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.domain.model.ListingType
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SkyBlueAccent

/**
 * Clean, high-contrast, atmospheric background with subtle, luminous top ambient glow
 * tailored to each sub-tab to ensure maximum text readability, crisp card borders, and zero clutter.
 */
@Composable
fun HomeTabBackground(
  selectedType: ListingType,
  modifier: Modifier = Modifier
) {
  val surfaceColor = MaterialTheme.colorScheme.background

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(surfaceColor)
  ) {
    Crossfade(
      targetState = selectedType,
      animationSpec = tween(durationMillis = 350),
      label = "tab_ambient_glow_crossfade"
    ) { type ->
      val (glowColorStart, glowColorEnd) = when (type) {
        ListingType.FREE_GIFT -> Pair(
          EmeraldPrimary.copy(alpha = 0.08f),
          Color(0xFF10B981).copy(alpha = 0.02f)
        )
        ListingType.DISCOUNT -> Pair(
          AmberSecondary.copy(alpha = 0.09f),
          Color(0xFFF59E0B).copy(alpha = 0.02f)
        )
        ListingType.REQUEST -> Pair(
          SkyBlueAccent.copy(alpha = 0.09f),
          Color(0xFF38BDF8).copy(alpha = 0.02f)
        )
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                glowColorStart,
                glowColorEnd,
                Color.Transparent
              ),
              startY = 0f,
              endY = 500f
            )
          )
      )
    }
  }
}
