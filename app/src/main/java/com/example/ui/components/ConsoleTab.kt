package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InxernalEngine
import com.example.model.LogLevel
import com.example.model.LogLine
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalError
import com.example.ui.theme.InxernalOnline
import com.example.ui.theme.InxernalSurfaceVariant
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint
import com.example.ui.theme.InxernalWarn

@Composable
fun ConsoleTab(
  engine: InxernalEngine,
  logs: List<LogLine>,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var commandText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val chipScrollState = rememberScrollState()

  // Auto-scroll to latest log line
  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.scrollToItem(logs.size - 1)
    }
  }

  val suggestions = listOf(
    "nplant 400001",
    "nharvest",
    "nfarm start",
    "nfarm stop",
    "nfields",
    "nquago status",
    "nspoof on",
    "status",
    "ping",
    "help"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0C0D0F))
      .padding(10.dp)
      .testTag("console_tab")
  ) {
    // Toolbar: Terminal Title + Clear + Copy
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "nxrth>",
          color = InxernalAccent,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "interactive terminal (${logs.size} lines)",
          color = InxernalTextFaint,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(
          onClick = {
            val fullLog = logs.joinToString("\n") { "[+${String.format("%.2f", it.timestamp)}s] ${it.message}" }
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Inxernal Log", fullLog))
            Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(32.dp)
        ) {
          Text("copy", color = InxernalTextDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }

        IconButton(
          onClick = { engine.clearLogs() },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Clear logs",
            tint = InxernalTextDim,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Main Log Output Box
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(Color(0xFF090A0C), RoundedCornerShape(4.dp))
        .border(1.dp, InxernalBorder, RoundedCornerShape(4.dp))
        .padding(8.dp)
    ) {
      if (logs.isEmpty()) {
        Text(
          text = "Terminal idle. Type 'help' or click a command below...",
          color = InxernalTextFaint,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.align(Alignment.Center)
        )
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          items(logs, key = { it.id }) { item ->
            LogItemRow(item)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick command chips row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(chipScrollState),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      suggestions.forEach { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(InxernalCardBg)
            .border(1.dp, InxernalBorder, RoundedCornerShape(3.dp))
            .clickable {
              commandText = cmd
              engine.dispatch(cmd)
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = cmd,
            color = InxernalTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Command Input Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = commandText,
        onValueChange = { commandText = it },
        placeholder = {
          Text(
            "Enter nxrth command...",
            color = InxernalTextFaint,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        },
        leadingIcon = {
          Text(
            "nxrth>",
            color = InxernalAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 8.dp)
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("console_command_input"),
        textStyle = androidx.compose.ui.text.TextStyle(
          color = InxernalText,
          fontSize = 13.sp,
          fontFamily = FontFamily.Monospace
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
          if (commandText.isNotBlank()) {
            engine.dispatch(commandText)
            commandText = ""
          }
        }),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = InxernalAccent,
          unfocusedBorderColor = InxernalBorder,
          focusedContainerColor = InxernalCardBg,
          unfocusedContainerColor = InxernalCardBg
        )
      )

      Spacer(modifier = Modifier.width(6.dp))

      Button(
        onClick = {
          if (commandText.isNotBlank()) {
            engine.dispatch(commandText)
            commandText = ""
          }
        },
        modifier = Modifier
          .height(50.dp)
          .testTag("console_send_button"),
        colors = ButtonDefaults.buttonColors(containerColor = InxernalSurfaceVariant),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, InxernalBorder)
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Send",
          tint = InxernalText,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun LogItemRow(log: LogLine) {
  val levelColor = when (log.level) {
    LogLevel.Info -> InxernalTextDim
    LogLevel.Success -> InxernalOnline
    LogLevel.Warning -> InxernalWarn
    LogLevel.Error -> InxernalError
    LogLevel.Command -> Color(0xFF5AB6FF)
    LogLevel.Debug -> InxernalTextFaint
  }

  val levelTag = when (log.level) {
    LogLevel.Info -> "INFO"
    LogLevel.Success -> " OK "
    LogLevel.Warning -> "WARN"
    LogLevel.Error -> "ERR "
    LogLevel.Command -> "CMD "
    LogLevel.Debug -> "DBG "
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = String.format("[+%05.1fs]", log.timestamp),
      color = InxernalTextFaint,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = "[$levelTag]",
      color = levelColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = log.message,
      color = when (log.level) {
        LogLevel.Command -> Color(0xFF90D0FF)
        LogLevel.Error -> InxernalError
        LogLevel.Success -> InxernalText
        else -> InxernalText
      },
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
