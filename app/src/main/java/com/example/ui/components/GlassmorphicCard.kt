package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBg
import com.example.ui.theme.GlassOutline

@Composable
fun GlassmorphicCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 16.dp,
  borderWidth: Dp = 1.dp,
  glowColor: Color = Color.Transparent,
  glowRadius: Dp = 0.dp,
  content: @Composable BoxScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)
  
  Box(
    modifier = modifier
      .then(
        if (glowColor != Color.Transparent && glowRadius > 0.dp) {
          Modifier.shadow(
            elevation = glowRadius,
            shape = shape,
            clip = false,
            ambientColor = glowColor,
            spotColor = glowColor
          )
        } else {
          Modifier
        }
      )
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            GlassBg,
            Color(0x05FFFFFF)
          )
        )
      )
      .border(
        width = borderWidth,
        brush = Brush.linearGradient(
          colors = listOf(
            GlassOutline,
            Color(0x05FFFFFF),
            GlassOutline.copy(alpha = 0.1f)
          )
        ),
        shape = shape
      )
  ) {
    content()
  }
}
