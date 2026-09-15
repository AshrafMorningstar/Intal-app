package com.example.model

enum class LogLevel {
  Info,
  Success,
  Warning,
  Error,
  Command,
  Debug
}

data class LogLine(
  val id: Long,
  val timestamp: Double,
  val level: LogLevel,
  val message: String
)

data class CropOption(
  val id: Int,
  val name: String,
  val growthTime: String,
  val growthSec: Int
)

enum class BridgeMode {
  AUTONOMOUS_ENGINE,
  SOCKET_BRIDGE
}

data class ScanMatch(
  val index: Int,
  val address: String,
  val value: String,
  val previousValue: String = ""
)

data class MemoryDumpResult(
  val offset: String,
  val length: Int,
  val lines: List<MemoryHexLine>
)

data class MemoryHexLine(
  val address: String,
  val hexBytes: String,
  val asciiText: String
)

data class InxernalState(
  val running: Boolean = false,
  val deviceOnline: Boolean = true,
  val engineAttached: Boolean = true,
  val heartbeatOk: Boolean = true,
  val farmLoop: Boolean = false,
  val farmWaitSeconds: Int = 120,
  val farmCropId: Int = 400001,
  val pid: Int = 14298,
  val deviceId: String = "127.0.0.1:5555",
  val libgBase: String = "0x7f8ba10000",
  val packageName: String = "com.supercell.hayday",
  val currentFields: List<Long> = listOf(
    100101L, 100102L, 100103L, 100104L, 100105L, 100106L,
    100107L, 100108L, 100109L, 100110L, 100111L, 100112L
  ),
  val scanMatches: Int = -1,
  val lastResponse: String = "OK engine ready (libg @ 0x7f8ba10000)",
  val quagoBlockActive: Boolean = true,
  val quagoBlockedPackets: Int = 47,
  val promonShieldBypassed: Boolean = true,
  val spoofActive: Boolean = true,
  val spoofProfile: String = "Galaxy S24 Ultra (Snapdragon 8 Gen 3)",
  val totalHarvested: Int = 0,
  val totalPlanted: Int = 0,
  val farmCycles: Int = 0,
  val bridgeMode: BridgeMode = BridgeMode.AUTONOMOUS_ENGINE,
  val host: String = "127.0.0.1",
  val port: Int = 31350,
  val activeScanResults: List<ScanMatch> = emptyList(),
  val latestDump: MemoryDumpResult? = null
)
