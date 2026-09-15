package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InxernalEngine
import com.example.model.BridgeMode
import com.example.model.InxernalState
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalOnline
import com.example.ui.theme.InxernalSurfaceVariant
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint

@Composable
fun SettingsTab(
  engine: InxernalEngine,
  state: InxernalState,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  var hostInput by remember(state.host) { mutableStateOf(state.host) }
  var portInput by remember(state.port) { mutableStateOf(state.port.toString()) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(14.dp)
      .testTag("settings_tab"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "ENGINE & BRIDGE SETTINGS",
      color = InxernalTextDim,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )

    // 1. Bridge Mode Selection Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("bridge_mode_card"),
      colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
      shape = RoundedCornerShape(6.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "Execution Mode",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Choose how Inxernal communicates with the game process",
          color = InxernalTextDim,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mode 1: Autonomous In-App Engine
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = state.bridgeMode == BridgeMode.AUTONOMOUS_ENGINE,
            onClick = { engine.setBridgeMode(BridgeMode.AUTONOMOUS_ENGINE) },
            colors = RadioButtonDefaults.colors(selectedColor = InxernalAccent)
          )
          Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
              text = "Autonomous In-App Engine (Standalone Android)",
              color = InxernalText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Runs directly on Android without requiring a desktop host. Simulates native engine calls, cycles, Quago interceptor, and anti-ban.",
              color = InxernalTextDim,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mode 2: External Socket Bridge
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = state.bridgeMode == BridgeMode.SOCKET_BRIDGE,
            onClick = { engine.setBridgeMode(BridgeMode.SOCKET_BRIDGE) },
            colors = RadioButtonDefaults.colors(selectedColor = InxernalAccent)
          )
          Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
              text = "External Loader TCP Bridge (loader.py)",
              color = InxernalText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Connects via TCP socket to loader.py running on LDPlayer host PC or local loopback daemon.",
              color = InxernalTextDim,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 2. TCP Connection Settings Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tcp_settings_card"),
      colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
      shape = RoundedCornerShape(6.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "TCP Server Target (loader.py)",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Default Inxernal control server: 127.0.0.1:31350",
          color = InxernalTextDim,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("host / ip", fontSize = 10.sp) },
            modifier = Modifier.weight(2f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
            singleLine = true,
            colors = devTextFieldColors()
          )

          OutlinedTextField(
            value = portInput,
            onValueChange = { portInput = it },
            label = { Text("port", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = devTextFieldColors()
          )

          Button(
            onClick = {
              val p = portInput.toIntOrNull() ?: 31350
              engine.setHostAndPort(hostInput.trim(), p)
            },
            modifier = Modifier
              .height(50.dp)
              .testTag("save_target_button"),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
          ) {
            Text(
              text = "Apply",
              color = InxernalText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 3. Environment & Target Status
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("env_status_card"),
      colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
      shape = RoundedCornerShape(6.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "Device & Target Diagnostics",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        DiagnosticRow("Target App", "com.supercell.hayday (ARM64-v8a)")
        DiagnosticRow("Engine Seam", "libnxrth.so (C++20 Frida Gadget)")
        DiagnosticRow("Anti-Tamper", "Promon SHIELD (JavaGuard bypassed)")
        DiagnosticRow("Behavioral", "Quago Telemetry (blocking api.quago.io)")
        DiagnosticRow("Spoofed Model", "SM-S928B (Samsung Galaxy S24 Ultra)")
        DiagnosticRow("Architecture", System.getProperty("os.arch") ?: "aarch64")
      }
    }

    // 4. Usage Tips
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("usage_tips_card"),
      colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
      border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
      shape = RoundedCornerShape(6.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "HOW TO OPERATE INXERNAL",
          color = InxernalAccent,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "1. Auto-Farm: Set wait time to match crop growth (e.g. 120s for Wheat). Click 'Engage' or 'Start Bot'. Inxernal harvests mature fields, seeds new crops, and applies human-jitter intervals.\n\n" +
            "2. Roadside Shop: Open roadside shop in game, configure crate slot, count, price (1 coin), toggle paper ad, and click 'Sell'.\n\n" +
            "3. Developer Mode: Live memory reads/writes, AOB pattern scanning, heap value scanner, and ARM64 code caves/NOP slides.\n\n" +
            "4. Terminal: Run any 'nxrth>' command with full history and clipboard export.",
          color = InxernalTextDim,
          fontSize = 11.sp,
          lineHeight = 16.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = InxernalTextDim,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(5.dp)
          .clip(CircleShape)
          .background(InxernalOnline)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = value,
        color = InxernalText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
