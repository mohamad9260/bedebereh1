package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary

/**
 * Custom High-Craft Logo Component for "بده بره" (Bede Bere)
 * Features an elegant 3D gift box with golden ribbon bows and radiant sparkle stars.
 */
@Composable
fun BedeBereLogo(
  modifier: Modifier = Modifier,
  size: Dp = 42.dp,
  showText: Boolean = false,
  textColor: Color = MaterialTheme.colorScheme.onSurface
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .size(size)
        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = EmeraldPrimary.copy(alpha = 0.35f))
        .clip(RoundedCornerShape(12.dp))
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFF00897B),
              Color(0xFF004D40)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(size * 0.75f)) {
        val w = this.size.width
        val h = this.size.height

        // Gift Box Bottom Base
        val boxTop = h * 0.42f
        val boxHeight = h * 0.52f
        val boxWidth = w * 0.82f
        val boxLeft = (w - boxWidth) / 2f

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              Color(0xFFE0F2F1)
            )
          ),
          topLeft = Offset(boxLeft, boxTop),
          size = Size(boxWidth, boxHeight),
          cornerRadius = CornerRadius(6f, 6f)
        )

        // Gift Box Lid
        val lidTop = h * 0.32f
        val lidHeight = h * 0.16f
        val lidWidth = w * 0.94f
        val lidLeft = (w - lidWidth) / 2f

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              Color(0xFFB2DFDB)
            )
          ),
          topLeft = Offset(lidLeft, lidTop),
          size = Size(lidWidth, lidHeight),
          cornerRadius = CornerRadius(4f, 4f)
        )

        // Vertical Golden Ribbon
        val ribbonWidth = w * 0.16f
        val ribbonLeft = (w - ribbonWidth) / 2f
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFFD54F),
              Color(0xFFFFB300),
              Color(0xFFFFA000)
            )
          ),
          topLeft = Offset(ribbonLeft, lidTop),
          size = Size(ribbonWidth, boxTop + boxHeight - lidTop)
        )

        // Ribbon Bow Left Loop
        val bowLeft = Path().apply {
          moveTo(w * 0.5f, lidTop)
          cubicTo(
            w * 0.4f, lidTop - h * 0.28f,
            w * 0.12f, lidTop - h * 0.24f,
            w * 0.15f, lidTop - h * 0.04f
          )
          cubicTo(
            w * 0.2f, lidTop + h * 0.06f,
            w * 0.42f, lidTop + h * 0.02f,
            w * 0.5f, lidTop
          )
          close()
        }
        drawPath(
          path = bowLeft,
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300))
          )
        )

        // Ribbon Bow Right Loop
        val bowRight = Path().apply {
          moveTo(w * 0.5f, lidTop)
          cubicTo(
            w * 0.6f, lidTop - h * 0.28f,
            w * 0.88f, lidTop - h * 0.24f,
            w * 0.85f, lidTop - h * 0.04f
          )
          cubicTo(
            w * 0.8f, lidTop + h * 0.06f,
            w * 0.58f, lidTop + h * 0.02f,
            w * 0.5f, lidTop
          )
          close()
        }
        drawPath(
          path = bowRight,
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
          )
        )

        // Central Knot
        drawCircle(
          color = Color(0xFFFF8F00),
          radius = w * 0.08f,
          center = Offset(w * 0.5f, lidTop)
        )

        // Sparkle Star at Top Right
        val starCenter = Offset(w * 0.88f, h * 0.18f)
        val starSize = w * 0.12f
        val starPath = Path().apply {
          moveTo(starCenter.x, starCenter.y - starSize)
          quadraticTo(starCenter.x, starCenter.y, starCenter.x + starSize, starCenter.y)
          quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y + starSize)
          quadraticTo(starCenter.x, starCenter.y, starCenter.x - starSize, starCenter.y)
          quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y - starSize)
          close()
        }
        drawPath(starPath, Color(0xFFFFF9C4), style = Fill)
      }
    }

    if (showText) {
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "بده",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = EmeraldPrimary,
            letterSpacing = (-0.5).sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "بِره",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = AmberSecondary,
            letterSpacing = (-0.5).sp
          )
        }
        Text(
          text = "سامانه اهدای وسایل و تخفیف‌ها",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 9.sp
        )
      }
    }
  }
}
