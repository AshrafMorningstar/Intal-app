package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InxernalState
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalOnline
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeaderBar(
  state: InxernalState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(InxernalCardBg)
      .border(1.dp, InxernalBorder)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("header_bar")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "INXERNAL",
          color = InxernalAccent,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "by North",
          color = InxernalTextFaint,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      // Status indicator tag
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(if (state.running) InxernalAccent.copy(alpha = 0.2f) else Color(0xFF1E2026))
          .border(
            1.dp,
            if (state.running) InxernalAccent else InxernalBorder,
            RoundedCornerShape(4.dp)
          )
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (state.running) InxernalAccent else InxernalTextDim)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (state.running) "BOT ACTIVE" else "IDLE",
            color = if (state.running) InxernalAccent else InxernalTextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.size(8.dp))

    // Status items list
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      HeaderStatusItem(
        label = "Bot",
        value = if (state.running) "running" else "idle",
        isActive = state.running
      )
      HeaderStatusItem(
        label = "Device",
        value = if (state.deviceOnline) state.deviceId else "offline",
        isActive = state.deviceOnline
      )
      HeaderStatusItem(
        label = "Engine",
        value = if (state.engineAttached) "attached" else "detached",
        isActive = state.engineAttached
      )
      HeaderStatusItem(
        label = "libg",
        value = state.libgBase,
        isActive = state.engineAttached
      )
    }
  }
}

@Composable
fun HeaderStatusItem(
  label: String,
  value: String,
  isActive: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(if (isActive) InxernalOnline else InxernalTextFaint)
    )
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = "$label:",
      color = InxernalTextDim,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = value,
      color = if (isActive) InxernalText else InxernalTextFaint,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.Monospace
    )
  }
}
