package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.InxernalEngine
import com.example.ui.components.ConsoleTab
import com.example.ui.components.ControlPanelTab
import com.example.ui.components.DeveloperTab
import com.example.ui.components.HeaderBar
import com.example.ui.components.SettingsTab
import com.example.ui.theme.InxernalAccent
import com.example.ui.theme.InxernalBorder
import com.example.ui.theme.InxernalCardBg
import com.example.ui.theme.InxernalSurfaceVariant
import com.example.ui.theme.InxernalText
import com.example.ui.theme.InxernalTextDim
import com.example.ui.theme.InxernalTextFaint

enum class InxernalNavTab(val title: String, val icon: ImageVector) {
  CONTROL("Control", Icons.Default.Dashboard),
  DEVELOPER("Developer", Icons.Default.Code),
  CONSOLE("Console", Icons.Default.Terminal),
  SETTINGS("Bridge", Icons.Default.Settings)
}

@Composable
fun InxernalMainScreen(
  engine: InxernalEngine,
  modifier: Modifier = Modifier
) {
  val state by engine.state.collectAsState()
  val logs by engine.logs.collectAsState()
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F1012))
      .windowInsetsPadding(WindowInsets.systemBars),
    containerColor = Color(0xFF0F1012),
    topBar = {
      HeaderBar(state = state)
    },
    bottomBar = {
      InxernalBottomBar(
        selectedIndex = selectedTabIndex,
        onSelectTab = { selectedTabIndex = it }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTabIndex) {
        0 -> ControlPanelTab(engine = engine, state = state)
        1 -> DeveloperTab(engine = engine, state = state)
        2 -> ConsoleTab(engine = engine, logs = logs)
        3 -> SettingsTab(engine = engine, state = state)
      }
    }
  }
}

@Composable
fun InxernalBottomBar(
  selectedIndex: Int,
  onSelectTab: (Int) -> Unit
) {
  NavigationBar(
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, InxernalBorder)
      .testTag("bottom_nav_bar"),
    containerColor = InxernalCardBg,
    tonalElevation = 0.dp
  ) {
    InxernalNavTab.entries.forEachIndexed { index, tab ->
      val isSelected = selectedIndex == index
      NavigationBarItem(
        selected = isSelected,
        onClick = { onSelectTab(index) },
        icon = {
          Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            modifier = Modifier.size(20.dp)
          )
        },
        label = {
          Text(
            text = tab.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = InxernalAccent,
          selectedTextColor = InxernalAccent,
          indicatorColor = InxernalSurfaceVariant,
          unselectedIconColor = InxernalTextDim,
          unselectedTextColor = InxernalTextFaint
        )
      )
    }
  }
}
