package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.InxernalState
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalAccentContainer
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalOnline
import com.example.ui.theme.InxernalSurfaceVariant
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint
import com.example.ui.theme.InxernalWarn

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlPanelTab(
  engine: InxernalEngine,
  state: InxernalState,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(14.dp)
      .testTag("control_panel_tab"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. MASTER BOT CONTROL CARD
    MasterBotCard(
      running = state.running,
      farmCycles = state.farmCycles,
      harvested = state.totalHarvested,
      planted = state.totalPlanted,
      onToggle = { engine.toggleBot() }
    )

    // 2. LOADER FEEDBACK BANNER
    LoaderResponsePill(response = state.lastResponse)

    // 3. AUTO-FARM CARD
    AutoFarmCard(
      engine = engine,
      state = state
    )

    // 4. QUICK ACTIONS GRID
    QuickActionsSection(
      onPlant = { engine.plantAll() },
      onHarvest = { engine.harvestAll() },
      onMarket = { engine.openMarket() },
      onReadFields = { engine.refreshFieldIDs() }
    )

    // 5. FIELD IDS READOUT
    FieldIdsCard(
      fields = state.currentFields,
      onRefresh = { engine.refreshFieldIDs() },
      onTestAdb = { engine.testADB() },
      onStatus = { engine.refreshStatus() }
    )

    // 6. ROADSIDE SHOP (SELL)
    RoadsideShopCard(
      onSell = { slot, count, price, ad, item ->
        engine.sellCrate(slot, count, price, ad, item)
      }
    )

    // 7. ANTI-BAN & STEALTH DEFENSES
    StealthDefensesCard(
      quagoBlockActive = state.quagoBlockActive,
      quagoBlockedPackets = state.quagoBlockedPackets,
      promonShieldBypassed = state.promonShieldBypassed,
      spoofActive = state.spoofActive,
      spoofProfile = state.spoofProfile,
      onToggleQuago = { engine.toggleQuagoBlock() },
      onToggleSpoof = { engine.toggleSpoofing() }
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun MasterBotCard(
  running: Boolean,
  farmCycles: Int,
  harvested: Int,
  planted: Int,
  onToggle: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("master_bot_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = "BOT CONTROL",
        color = InxernalTextDim,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (running) InxernalAccent else InxernalTextFaint)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (running) "Running" else "Idle",
              color = if (running) InxernalText else InxernalTextDim,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          if (running || farmCycles > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Cycles: $farmCycles | Harvested: $harvested | Planted: $planted",
              color = InxernalTextDim,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Button(
          onClick = onToggle,
          modifier = Modifier
            .width(140.dp)
            .height(44.dp)
            .testTag("toggle_bot_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (running) InxernalAccent else InxernalSurfaceVariant,
            contentColor = InxernalText
          ),
          shape = RoundedCornerShape(4.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (running) InxernalAccent else InxernalBorder
          )
        ) {
          Icon(
            imageVector = if (running) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (running) "Stop Bot" else "Start Bot",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
fun LoaderResponsePill(response: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(InxernalCardBg, RoundedCornerShape(4.dp))
      .border(1.dp, InxernalBorder, RoundedCornerShape(4.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("loader_response_pill")
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "loader: ",
        color = InxernalAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = response,
        color = InxernalTextDim,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        maxLines = 2
      )
    }
  }
}

@Composable
fun AutoFarmCard(
  engine: InxernalEngine,
  state: InxernalState
) {
  var waitInput by remember(state.farmWaitSeconds) {
    mutableStateOf(state.farmWaitSeconds.toString())
  }
  var cropMenuExpanded by remember { mutableStateOf(false) }
  val selectedCrop = engine.availableCrops.find { it.id == state.farmCropId }
    ?: engine.availableCrops.first()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("auto_farm_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "AUTO-FARM",
          color = InxernalTextDim,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "harvest -> plant -> wait -> repeat",
          color = InxernalTextFaint,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Wait seconds field
        OutlinedTextField(
          value = waitInput,
          onValueChange = {
            waitInput = it
            it.toIntOrNull()?.let { s -> engine.setFarmWait(s) }
          },
          label = { Text("wait (s)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
          modifier = Modifier
            .weight(1f)
            .testTag("farm_wait_input"),
          textStyle = androidx.compose.ui.text.TextStyle(
            color = InxernalText,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
          ),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InxernalAccent,
            unfocusedBorderColor = InxernalBorder,
            focusedContainerColor = InxernalSurfaceVariant,
            unfocusedContainerColor = InxernalSurfaceVariant
          )
        )

        // Crop selector dropdown
        Box(modifier = Modifier.weight(1.3f)) {
          OutlinedButton(
            onClick = { cropMenuExpanded = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("crop_selector_button"),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = InxernalSurfaceVariant)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = selectedCrop.name,
                  color = InxernalText,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "ID ${selectedCrop.id} (${selectedCrop.growthTime})",
                  color = InxernalTextDim,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = InxernalTextDim
              )
            }
          }

          DropdownMenu(
            expanded = cropMenuExpanded,
            onDismissRequest = { cropMenuExpanded = false },
            modifier = Modifier.background(InxernalCardBg)
          ) {
            engine.availableCrops.forEach { crop ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "${crop.name} (${crop.id})",
                      color = if (crop.id == selectedCrop.id) InxernalAccent else InxernalText,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = crop.growthTime,
                      color = InxernalTextDim,
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                },
                onClick = {
                  engine.setFarmCrop(crop.id)
                  cropMenuExpanded = false
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!state.farmLoop) {
          Button(
            onClick = { engine.startFarmLoop() },
            modifier = Modifier
              .width(130.dp)
              .height(38.dp)
              .testTag("engage_farm_button"),
            colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
          ) {
            Text(
              text = "Engage",
              color = InxernalText,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
              onClick = { engine.stopFarmLoop() },
              modifier = Modifier
                .width(130.dp)
                .height(38.dp)
                .testTag("disengage_farm_button"),
              colors = ButtonDefaults.buttonColors(containerColor = InxernalAccent),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "Disengage",
                color = InxernalText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(InxernalAccent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "looping",
              color = InxernalAccent,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionsSection(
  onPlant: () -> Unit,
  onHarvest: () -> Unit,
  onMarket: () -> Unit,
  onReadFields: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "QUICK ACTIONS",
      color = InxernalTextDim,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickActionCard(
        title = "Plant All",
        subtitle = "Plant current crop",
        buttonText = "Plant",
        onClick = onPlant,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = "Harvest All",
        subtitle = "Harvest ready crops",
        buttonText = "Harvest",
        onClick = onHarvest,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickActionCard(
        title = "Market",
        subtitle = "Roadside shop crate",
        buttonText = "Open",
        onClick = onMarket,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = "Field IDs",
        subtitle = "Read live entity IDs",
        buttonText = "Read",
        onClick = onReadFields,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun QuickActionCard(
  title: String,
  subtitle: String,
  buttonText: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.height(105.dp),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = title,
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = subtitle,
          color = InxernalTextDim,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          maxLines = 1
        )
      }
      Button(
        onClick = onClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(32.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
      ) {
        Text(
          text = buttonText,
          color = InxernalText,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FieldIdsCard(
  fields: List<Long>,
  onRefresh: () -> Unit,
  onTestAdb: () -> Unit,
  onStatus: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("field_ids_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "FIELD IDS (${fields.size})",
          color = InxernalTextDim,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          SmallActionButton("Refresh", onRefresh)
          SmallActionButton("Test ADB", onTestAdb)
          SmallActionButton("Status", onStatus)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (fields.isEmpty()) {
        Text(
          text = "none yet - press Read",
          color = InxernalTextDim,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        )
      } else {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          fields.forEach { id ->
            Box(
              modifier = Modifier
                .background(InxernalSurfaceVariant, RoundedCornerShape(4.dp))
                .border(1.dp, InxernalBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "$id",
                color = InxernalText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SmallActionButton(
  text: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(3.dp))
      .background(InxernalSurfaceVariant)
      .border(1.dp, InxernalBorder, RoundedCornerShape(3.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      color = InxernalTextDim,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun RoadsideShopCard(
  onSell: (slot: Int, count: Int, price: Int, ad: Boolean, item: Int) -> Unit
) {
  var slot by remember { mutableStateOf("0") }
  var count by remember { mutableStateOf("10") }
  var price by remember { mutableStateOf("1") }
  var item by remember { mutableStateOf("400001") }
  var advertise by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("roadside_shop_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = "ROADSIDE SHOP (SELL)",
        color = InxernalTextDim,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "open the roadside shop first; slot = crate index (0-based)",
        color = InxernalTextFaint,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = slot,
          onValueChange = { slot = it },
          label = { Text("slot", fontSize = 10.sp) },
          modifier = Modifier.weight(1f),
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InxernalAccent,
            unfocusedBorderColor = InxernalBorder,
            focusedContainerColor = InxernalSurfaceVariant,
            unfocusedContainerColor = InxernalSurfaceVariant
          )
        )
        OutlinedTextField(
          value = count,
          onValueChange = { count = it },
          label = { Text("count", fontSize = 10.sp) },
          modifier = Modifier.weight(1f),
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InxernalAccent,
            unfocusedBorderColor = InxernalBorder,
            focusedContainerColor = InxernalSurfaceVariant,
            unfocusedContainerColor = InxernalSurfaceVariant
          )
        )
        OutlinedTextField(
          value = price,
          onValueChange = { price = it },
          label = { Text("price", fontSize = 10.sp) },
          modifier = Modifier.weight(1f),
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InxernalAccent,
            unfocusedBorderColor = InxernalBorder,
            focusedContainerColor = InxernalSurfaceVariant,
            unfocusedContainerColor = InxernalSurfaceVariant
          )
        )
        OutlinedTextField(
          value = item,
          onValueChange = { item = it },
          label = { Text("item ID", fontSize = 10.sp) },
          modifier = Modifier.weight(1.4f),
          textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = InxernalText),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InxernalAccent,
            unfocusedBorderColor = InxernalBorder,
            focusedContainerColor = InxernalSurfaceVariant,
            unfocusedContainerColor = InxernalSurfaceVariant
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = advertise,
            onCheckedChange = { advertise = it },
            colors = CheckboxDefaults.colors(
              checkedColor = InxernalAccent,
              uncheckedColor = InxernalBorder
            )
          )
          Text(
            text = "advertise (newspaper)",
            color = InxernalTextDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Button(
          onClick = {
            val s = slot.toIntOrNull() ?: 0
            val c = count.toIntOrNull() ?: 10
            val p = price.toIntOrNull() ?: 1
            val i = item.toIntOrNull() ?: 400001
            onSell(s, c, p, advertise, i)
          },
          modifier = Modifier
            .width(100.dp)
            .height(36.dp)
            .testTag("shop_sell_button"),
          shape = RoundedCornerShape(4.dp),
          colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
          border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
        ) {
          Text(
            text = "Sell",
            color = InxernalText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
fun StealthDefensesCard(
  quagoBlockActive: Boolean,
  quagoBlockedPackets: Int,
  promonShieldBypassed: Boolean,
  spoofActive: Boolean,
  spoofProfile: String,
  onToggleQuago: () -> Unit,
  onToggleSpoof: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("stealth_defenses_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = "ANTI-BAN & DEFENSES",
        color = InxernalTextDim,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(10.dp))

      // 1. Quago
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Quago Blocker",
              color = InxernalText,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .background(InxernalAccentContainer, RoundedCornerShape(3.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "$quagoBlockedPackets dropped",
                color = InxernalAccent,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Text(
            text = "Blocks api.quago.io behavioral telemetry uploads",
            color = InxernalTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Switch(
          checked = quagoBlockActive,
          onCheckedChange = { onToggleQuago() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = InxernalText,
            checkedTrackColor = InxernalAccent,
            uncheckedThumbColor = InxernalTextDim,
            uncheckedTrackColor = InxernalSurfaceVariant
          )
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(InxernalBorder)
      )
      Spacer(modifier = Modifier.height(8.dp))

      // 2. Promon SHIELD
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Promon SHIELD Suppression",
            color = InxernalText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "JavaGuard ART hook active (anti-tamper watchdog neutral)",
            color = InxernalTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Box(
          modifier = Modifier
            .background(Color(0xFF1E2822), RoundedCornerShape(4.dp))
            .border(1.dp, InxernalOnline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(InxernalOnline)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "BYPASS",
              color = InxernalOnline,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(InxernalBorder)
      )
      Spacer(modifier = Modifier.height(8.dp))

      // 3. Device Fingerprint Spoofing
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Device Spoofer",
            color = InxernalText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = spoofProfile,
            color = InxernalTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Switch(
          checked = spoofActive,
          onCheckedChange = { onToggleSpoof() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = InxernalText,
            checkedTrackColor = InxernalAccent,
            uncheckedThumbColor = InxernalTextDim,
            uncheckedTrackColor = InxernalSurfaceVariant
          )
        )
      }
    }
  }
}
