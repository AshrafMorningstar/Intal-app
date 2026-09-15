package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InxernalEngine
import com.example.model.InxernalState
import com.example.model.MemoryDumpResult
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalSurfaceVariant
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint
import com.example.ui.theme.InxernalWarn

@Composable
fun DeveloperTab(
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
      .testTag("developer_tab"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "DEVELOPER MODE",
      color = InxernalTextDim,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = "Low-level Frida & native engine primitives (libg.so / ARM64)",
      color = InxernalTextFaint,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )

    // 1. MEMORY SECTION
    DeveloperMemoryCard(
      onRead = { type, off, len -> engine.readMemory(type, off, len) },
      onWrite = { type, off, valStr -> engine.writeMemory(type, off, valStr) },
      onDump = { off, len -> engine.dumpMemory(off, len) },
      onScanAob = { pat -> engine.patternScan(pat) },
      onReadAbs = { addr, len -> engine.readAbs(addr, len) },
      onWriteAbs = { addr, hex -> engine.writeAbs(addr, hex) }
    )

    // Memory Dump Result View if available
    state.latestDump?.let { dump ->
      MemoryDumpViewerCard(dump)
    }

    // 2. VALUE SCANNER SECTION
    DeveloperValueScannerCard(
      matchesCount = state.scanMatches,
      matchesList = state.activeScanResults,
      onScan = { type, valStr -> engine.valueScan(type, valStr) },
      onNarrow = { type, valStr -> engine.valueNarrow(type, valStr) },
      onWrite = { valStr, idx -> engine.valueWrite(valStr, idx) },
      onReset = { engine.valueReset() }
    )

    // 3. ARM64 PATCHING & CODE CAVES
    DeveloperArm64Card(
      onAllocCave = { size -> engine.allocCave(size) },
      onNop = { off, count -> engine.nopPatch(off, count) },
      onFarJump = { off, tgt -> engine.farJump(off, tgt) },
      onBranch = { off, tgt, link -> engine.branch(off, tgt, link) },
      onGotHook = { got -> engine.gotHook(got) }
    )

    // 4. COMMAND HOOKS & CAPTURE
    DeveloperHooksCard(
      onHook = { off -> engine.hookFn(off) },
      onCmdHook = { engine.cmdHook() },
      onCmdLog = { engine.cmdLog() },
      onArgHook = { off -> engine.argHook(off) },
      onArgLog = { engine.argLog() },
      onCapture = { sec -> engine.capture(sec) }
    )

    // 5. FIELD ENUMERATION & REVERSE ENGINEERING
    DeveloperReCard(
      onFields = { engine.refreshFieldIDs() },
      onFindFields = { engine.findFields() },
      onFindMgr = { engine.findMgr() },
      onMgrDiag = { engine.mgrDiag() },
      onFdump = { engine.fdump() },
      onFieldsDiag = { engine.fieldsDiag() },
      onObjDump = { addr -> engine.objDump(addr) },
      onDumpSo = { path -> engine.dumpSo(path) },
      onExport = { name -> engine.getExport(name) }
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun DeveloperMemoryCard(
  onRead: (type: String, offset: String, length: String) -> Unit,
  onWrite: (type: String, offset: String, value: String) -> Unit,
  onDump: (offset: String, length: String) -> Unit,
  onScanAob: (pattern: String) -> Unit,
  onReadAbs: (addr: String, length: String) -> Unit,
  onWriteAbs: (addr: String, hex: String) -> Unit
) {
  var expanded by remember { mutableStateOf(true) }

  val readTypes = listOf("int", "float", "double", "long", "ptr", "str", "bytes")
  var selectedReadType by remember { mutableStateOf("int") }
  var readTypeDropdown by remember { mutableStateOf(false) }
  var rOffset by remember { mutableStateOf("0x004F2B80") }
  var rLength by remember { mutableStateOf("256") }

  val writeTypes = listOf("int", "float", "double", "long", "bytes")
  var selectedWriteType by remember { mutableStateOf("int") }
  var writeTypeDropdown by remember { mutableStateOf(false) }
  var wOffset by remember { mutableStateOf("0x004F2B80") }
  var wValue by remember { mutableStateOf("42") }

  var dOffset by remember { mutableStateOf("0x004F2B80") }
  var dLength by remember { mutableStateOf("64") }

  var aobPattern by remember { mutableStateOf("1F 20 03 D5") }

  var raAddr by remember { mutableStateOf("0x7f8ba10000") }
  var raLen by remember { mutableStateOf("16") }

  var waAddr by remember { mutableStateOf("0x7f8ba10000") }
  var waHex by remember { mutableStateOf("1F 20 03 D5") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dev_memory_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Memory (libg.so offsets)",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = InxernalTextDim
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Read memory row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box {
              OutlinedButton(
                onClick = { readTypeDropdown = true },
                modifier = Modifier
                  .width(80.dp)
                  .height(44.dp),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
              ) {
                Text(selectedReadType, fontSize = 11.sp, color = InxernalText)
              }
              DropdownMenu(
                expanded = readTypeDropdown,
                onDismissRequest = { readTypeDropdown = false },
                modifier = Modifier.background(InxernalCardBg)
              ) {
                readTypes.forEach { t ->
                  DropdownMenuItem(
                    text = { Text(t, fontSize = 11.sp, color = InxernalText) },
                    onClick = {
                      selectedReadType = t
                      readTypeDropdown = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = rOffset,
              onValueChange = { rOffset = it },
              label = { Text("offset (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.5f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            OutlinedTextField(
              value = rLength,
              onValueChange = { rLength = it },
              label = { Text("len", fontSize = 9.sp) },
              modifier = Modifier.weight(1f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            DevRunButton("read") { onRead(selectedReadType, rOffset, rLength) }
          }

          // Write memory row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box {
              OutlinedButton(
                onClick = { writeTypeDropdown = true },
                modifier = Modifier
                  .width(80.dp)
                  .height(44.dp),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
              ) {
                Text(selectedWriteType, fontSize = 11.sp, color = InxernalText)
              }
              DropdownMenu(
                expanded = writeTypeDropdown,
                onDismissRequest = { writeTypeDropdown = false },
                modifier = Modifier.background(InxernalCardBg)
              ) {
                writeTypes.forEach { t ->
                  DropdownMenuItem(
                    text = { Text(t, fontSize = 11.sp, color = InxernalText) },
                    onClick = {
                      selectedWriteType = t
                      writeTypeDropdown = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = wOffset,
              onValueChange = { wOffset = it },
              label = { Text("offset (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            OutlinedTextField(
              value = wValue,
              onValueChange = { wValue = it },
              label = { Text("value", fontSize = 9.sp) },
              modifier = Modifier.weight(1.3f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            DevRunButton("write") { onWrite(selectedWriteType, wOffset, wValue) }
          }

          // Dump memory row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = dOffset,
              onValueChange = { dOffset = it },
              label = { Text("dump offset", fontSize = 9.sp) },
              modifier = Modifier.weight(1.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            OutlinedTextField(
              value = dLength,
              onValueChange = { dLength = it },
              label = { Text("length", fontSize = 9.sp) },
              modifier = Modifier.weight(1f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            DevRunButton("dump") { onDump(dOffset, dLength) }
          }

          // AOB Pattern Scan row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = aobPattern,
              onValueChange = { aobPattern = it },
              label = { Text("AOB pattern  e.g. 1F 20 03 D5", fontSize = 9.sp) },
              modifier = Modifier.weight(2.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            DevRunButton("scan") { onScanAob(aobPattern) }
          }

          // Absolute read/write (rabs, wabs)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = raAddr,
              onValueChange = { raAddr = it },
              label = { Text("abs addr (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = raLen,
              onValueChange = { raLen = it },
              label = { Text("len", fontSize = 9.sp) },
              modifier = Modifier.weight(1f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("rabs") { onReadAbs(raAddr, raLen) }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = waAddr,
              onValueChange = { waAddr = it },
              label = { Text("abs addr (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.4f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = waHex,
              onValueChange = { waHex = it },
              label = { Text("hex bytes", fontSize = 9.sp) },
              modifier = Modifier.weight(1.4f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("wabs") { onWriteAbs(waAddr, waHex) }
          }
        }
      }
    }
  }
}

@Composable
fun MemoryDumpViewerCard(dump: MemoryDumpResult) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0E10)),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalAccent.copy(alpha = 0.5f)),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "HEX DUMP: ${dump.offset} (${dump.length} bytes)",
          color = InxernalAccent,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "ARM64 Little-Endian",
          color = InxernalTextDim,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      dump.lines.forEach { line ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = line.address,
            color = InxernalTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = line.hexBytes,
            color = InxernalText,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = line.asciiText,
            color = InxernalAccent,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
fun DeveloperValueScannerCard(
  matchesCount: Int,
  matchesList: List<com.example.model.ScanMatch>,
  onScan: (type: String, value: String) -> Unit,
  onNarrow: (type: String, value: String) -> Unit,
  onWrite: (value: String, index: String) -> Unit,
  onReset: () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val valTypes = listOf("int", "float", "double", "short", "long")
  var selectedType by remember { mutableStateOf("int") }
  var typeDropdown by remember { mutableStateOf(false) }

  var scanValue by remember { mutableStateOf("100") }
  var writeVal by remember { mutableStateOf("999") }
  var writeIdx by remember { mutableStateOf("0") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dev_value_scanner_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Value Scanner (heap)",
            color = InxernalText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          if (matchesCount >= 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "[$matchesCount matches]",
              color = InxernalAccent,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = InxernalTextDim
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box {
              OutlinedButton(
                onClick = { typeDropdown = true },
                modifier = Modifier
                  .width(80.dp)
                  .height(44.dp),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
              ) {
                Text(selectedType, fontSize = 11.sp, color = InxernalText)
              }
              DropdownMenu(
                expanded = typeDropdown,
                onDismissRequest = { typeDropdown = false },
                modifier = Modifier.background(InxernalCardBg)
              ) {
                valTypes.forEach { t ->
                  DropdownMenuItem(
                    text = { Text(t, fontSize = 11.sp, color = InxernalText) },
                    onClick = {
                      selectedType = t
                      typeDropdown = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = scanValue,
              onValueChange = { scanValue = it },
              label = { Text("value", fontSize = 9.sp) },
              modifier = Modifier.weight(1.5f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )

            DevRunButton("scan") { onScan(selectedType, scanValue) }
            DevRunButton("narrow") { onNarrow(selectedType, scanValue) }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = writeVal,
              onValueChange = { writeVal = it },
              label = { Text("new value", fontSize = 9.sp) },
              modifier = Modifier.weight(1.5f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = writeIdx,
              onValueChange = { writeIdx = it },
              label = { Text("index (opt)", fontSize = 9.sp) },
              modifier = Modifier.weight(1f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("write") { onWrite(writeVal, writeIdx) }
            DevRunButton("reset") { onReset() }
          }

          if (matchesList.isNotEmpty()) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1014), RoundedCornerShape(4.dp))
                .border(1.dp, InxernalBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
            ) {
              Text(
                text = "Resolved Heap Addresses:",
                color = InxernalTextDim,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
              matchesList.take(6).forEach { m ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "#${m.index}  ${m.address}",
                    color = InxernalText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "val: ${m.value}",
                    color = InxernalAccent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun DeveloperArm64Card(
  onAllocCave: (size: String) -> Unit,
  onNop: (off: String, count: String) -> Unit,
  onFarJump: (off: String, target: String) -> Unit,
  onBranch: (off: String, target: String, link: Boolean) -> Unit,
  onGotHook: (gotOffset: String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  var caveSize by remember { mutableStateOf("256") }
  var nopOff by remember { mutableStateOf("0x004F2B80") }
  var nopCount by remember { mutableStateOf("4") }

  var fjOff by remember { mutableStateOf("0x004F2B80") }
  var fjTgt by remember { mutableStateOf("0x7f8baf0000") }

  var brOff by remember { mutableStateOf("0x004F2B80") }
  var brTgt by remember { mutableStateOf("0x7f8ba30000") }
  var brLink by remember { mutableStateOf(false) }

  var gotOff by remember { mutableStateOf("0x005E1020") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dev_arm64_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ARM64 Patching & Code Caves",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = InxernalTextDim
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Cave allocator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = caveSize,
              onValueChange = { caveSize = it },
              label = { Text("cave size (bytes)", fontSize = 9.sp) },
              modifier = Modifier.weight(2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("cave") { onAllocCave(caveSize) }
          }

          // NOP patch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = nopOff,
              onValueChange = { nopOff = it },
              label = { Text("offset (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = nopCount,
              onValueChange = { nopCount = it },
              label = { Text("byte count", fontSize = 9.sp) },
              modifier = Modifier.weight(1f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("nop") { onNop(nopOff, nopCount) }
          }

          // FarJump
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = fjOff,
              onValueChange = { fjOff = it },
              label = { Text("offset", fontSize = 9.sp) },
              modifier = Modifier.weight(1.4f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = fjTgt,
              onValueChange = { fjTgt = it },
              label = { Text("abs target", fontSize = 9.sp) },
              modifier = Modifier.weight(1.4f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("farjump") { onFarJump(fjOff, fjTgt) }
          }

          // Branch BL
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = brOff,
              onValueChange = { brOff = it },
              label = { Text("offset", fontSize = 9.sp) },
              modifier = Modifier.weight(1.3f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            OutlinedTextField(
              value = brTgt,
              onValueChange = { brTgt = it },
              label = { Text("target", fontSize = 9.sp) },
              modifier = Modifier.weight(1.3f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                checked = brLink,
                onCheckedChange = { brLink = it },
                colors = CheckboxDefaults.colors(checkedColor = InxernalAccent)
              )
              Text("BL", fontSize = 10.sp, color = InxernalText)
            }
            DevRunButton("branch") { onBranch(brOff, brTgt, brLink) }
          }

          // GOT hook
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = gotOff,
              onValueChange = { gotOff = it },
              label = { Text("GOT offset", fontSize = 9.sp) },
              modifier = Modifier.weight(2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("gothook") { onGotHook(gotOff) }
          }
        }
      }
    }
  }
}

@Composable
fun DeveloperHooksCard(
  onHook: (off: String) -> Unit,
  onCmdHook: () -> Unit,
  onCmdLog: () -> Unit,
  onArgHook: (off: String) -> Unit,
  onArgLog: () -> Unit,
  onCapture: (sec: String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  var hookOff by remember { mutableStateOf("0x004F2B80") }
  var argOff by remember { mutableStateOf("0x004F2B80") }
  var capSec by remember { mutableStateOf("5") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dev_hooks_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Command Hooks & Capture",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = InxernalTextDim
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = hookOff,
              onValueChange = { hookOff = it },
              label = { Text("offset (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(1.5f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("hook") { onHook(hookOff) }
            DevRunButton("cmdhook") { onCmdHook() }
            DevRunButton("cmdlog") { onCmdLog() }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = argOff,
              onValueChange = { argOff = it },
              label = { Text("arg hook offset", fontSize = 9.sp) },
              modifier = Modifier.weight(1.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("arghook") { onArgHook(argOff) }
            DevRunButton("arglog") { onArgLog() }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = capSec,
              onValueChange = { capSec = it },
              label = { Text("capture seconds", fontSize = 9.sp) },
              modifier = Modifier.weight(1.8f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("capture") { onCapture(capSec) }
          }
        }
      }
    }
  }
}

@Composable
fun DeveloperReCard(
  onFields: () -> Unit,
  onFindFields: () -> Unit,
  onFindMgr: () -> Unit,
  onMgrDiag: () -> Unit,
  onFdump: () -> Unit,
  onFieldsDiag: () -> Unit,
  onObjDump: (addr: String) -> Unit,
  onDumpSo: (outPath: String) -> Unit,
  onExport: (name: String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  var objAddr by remember { mutableStateOf("0x7f8ba10000") }
  var dumpPath by remember { mutableStateOf("/sdcard/libg.so") }
  var exportName by remember { mutableStateOf("JNI_OnLoad") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("dev_re_card"),
    colors = CardDefaults.cardColors(containerColor = InxernalCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder),
    shape = RoundedCornerShape(6.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Field Enumeration & Reverse Engineering",
          color = InxernalText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = InxernalTextDim
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            DevRunButton("fields") { onFields() }
            DevRunButton("findfields") { onFindFields() }
            DevRunButton("findmgr") { onFindMgr() }
            DevRunButton("mgrdiag") { onMgrDiag() }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            DevRunButton("fdump") { onFdump() }
            DevRunButton("fieldsdiag") { onFieldsDiag() }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = objAddr,
              onValueChange = { objAddr = it },
              label = { Text("obj addr (0x..)", fontSize = 9.sp) },
              modifier = Modifier.weight(2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("objdump") { onObjDump(objAddr) }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = dumpPath,
              onValueChange = { dumpPath = it },
              label = { Text("dump out path", fontSize = 9.sp) },
              modifier = Modifier.weight(2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("dumpso") { onDumpSo(dumpPath) }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = exportName,
              onValueChange = { exportName = it },
              label = { Text("export symbol", fontSize = 9.sp) },
              modifier = Modifier.weight(2f),
              textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = InxernalText),
              singleLine = true,
              colors = devTextFieldColors()
            )
            DevRunButton("export") { onExport(exportName) }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF241B18), RoundedCornerShape(4.dp))
              .border(1.dp, InxernalWarn.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(8.dp)
          ) {
            Text(
              text = "Heuristic scans (fieldscan, vtscan) flag Promon memory watches. Safe path uses live vtables.",
              color = InxernalWarn,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
fun DevRunButton(
  text: String,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    modifier = Modifier
      .height(44.dp)
      .testTag("dev_run_button_$text"),
    colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
    shape = RoundedCornerShape(4.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
  ) {
    Text(
      text = text,
      color = InxernalText,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun devTextFieldColors() = OutlinedTextFieldDefaults.colors(
  focusedBorderColor = InxernalAccent,
  unfocusedBorderColor = InxernalBorder,
  focusedContainerColor = InxernalSurfaceVariant,
  unfocusedContainerColor = InxernalSurfaceVariant
)
