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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldPrimary

/**
 * Redesigned Distinctive Brand Logo for "بده بره" (Bede Bere)
 * Represents the cycle of kindness, giving without expectation, and vibrant mutual support.
 */
@Composable
fun BedeBereLogo(
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
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
        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = EmeraldPrimary.copy(alpha = 0.4f))
        .clip(RoundedCornerShape(14.dp))
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFF00B4D8),
              Color(0xFF0D9488),
              Color(0xFF064E3B)
            ),
            center = Offset.Zero,
            radius = 180f
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(size * 0.78f)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Infinity / Heart Caring Arms Loop (Cycle of Kindness)
        val infinityPath = Path().apply {
          moveTo(w * 0.5f, h * 0.72f)
          cubicTo(w * 0.22f, h * 0.95f, w * 0.05f, h * 0.65f, w * 0.2f, h * 0.42f)
          cubicTo(w * 0.32f, h * 0.25f, w * 0.45f, h * 0.52f, w * 0.5f, h * 0.62f)
          cubicTo(w * 0.55f, h * 0.52f, w * 0.68f, h * 0.25f, w * 0.8f, h * 0.42f)
          cubicTo(w * 0.95f, h * 0.65f, w * 0.78f, h * 0.95f, w * 0.5f, h * 0.72f)
          close()
        }
        drawPath(
          path = infinityPath,
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
          ),
          style = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Center Radiant Gift Box Floating
        val boxW = w * 0.46f
        val boxH = h * 0.36f
        val boxLeft = (w - boxW) / 2f
        val boxTop = h * 0.32f

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFE6FFFA))
          ),
          topLeft = Offset(boxLeft, boxTop),
          size = Size(boxW, boxH),
          cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Box Lid
        val lidW = w * 0.52f
        val lidH = h * 0.12f
        val lidLeft = (w - lidW) / 2f
        val lidTop = h * 0.24f

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFF99F6E4))
          ),
          topLeft = Offset(lidLeft, lidTop),
          size = Size(lidW, lidH),
          cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )

        // Vertical Amber Ribbon on Box
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
          ),
          topLeft = Offset((w - w * 0.12f) / 2f, lidTop),
          size = Size(w * 0.12f, boxH + lidH)
        )

        // Golden Bow Top Ribbon Knot
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B)),
            center = Offset(w * 0.5f, lidTop),
            radius = w * 0.1f
          ),
          radius = w * 0.09f,
          center = Offset(w * 0.5f, lidTop)
        )

        // 3. Sparkling North Star at Top Right
        val starX = w * 0.84f
        val starY = h * 0.18f
        val s = w * 0.14f
        val starPath = Path().apply {
          moveTo(starX, starY - s)
          quadraticTo(starX, starY, starX + s, starY)
          quadraticTo(starX, starY, starX, starY + s)
          quadraticTo(starX, starY, starX - s, starY)
          quadraticTo(starX, starY, starX, starY - s)
          close()
        }
        drawPath(starPath, Color(0xFFFFFBEB), style = Fill)
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
