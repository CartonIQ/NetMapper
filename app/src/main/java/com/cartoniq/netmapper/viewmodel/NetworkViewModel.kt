package com.cartoniq.netmapper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.netmapper.network.COMMON_PORTS
import com.cartoniq.netmapper.network.CellSignal
import com.cartoniq.netmapper.network.NetHost
import com.cartoniq.netmapper.network.PingScanner
import com.cartoniq.netmapper.network.WifiDetails
import com.cartoniq.netmapper.network.WifiNetwork
import com.cartoniq.netmapper.network.WifiScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HeatmapState(
    val cells: List<Int?> = List(100) { null },
    val liveRssi: Int? = null,
    val selected: Int? = null,
    val noSignal: Boolean = false
)

data class ChannelState(
    val loading: Boolean = false,
    val scannedOnce: Boolean = false,
    val networks: List<WifiNetwork> = emptyList()
)

data class ScannerState(
    val scanning: Boolean = false,
    val finished: Boolean = false,
    val progress: Int = 0,
    val localIp: String? = null,
    val hosts: List<NetHost> = emptyList()
)

data class SignalState(
    val checked: Boolean = false,
    val signal: CellSignal? = null,
    val carrier: String = "",
    val history: List<Int> = emptyList()
)

data class ToolsState(
    val target: String = "",
    val running: Boolean = false,
    val mode: Int = 0, // 0 idle, 1 ping, 2 ports
    val finished: Boolean = false,
    val progress: Int = 0,
    val pings: List<Long?> = emptyList(),
    val openPorts: List<Pair<Int, String>> = emptyList()
)

class NetworkViewModel(app: Application) : AndroidViewModel(app) {

    private val scanner = WifiScanner(app)

    fun hasLocationPermission(): Boolean = scanner.hasLocationPermission()

    // ---------------- Wi-Fi details ----------------
    private val _wifi = MutableStateFlow<WifiDetails?>(null)
    val wifiInfo: StateFlow<WifiDetails?> = _wifi.asStateFlow()

    fun refreshWifiInfo() {
        _wifi.value = scanner.currentDetails()
    }

    // ---------------- Heatmap ----------------
    private val _heat = MutableStateFlow(HeatmapState())
    val heat: StateFlow<HeatmapState> = _heat.asStateFlow()

    fun refreshRssi() {
        val r = scanner.currentRssi()
        _heat.update { it.copy(liveRssi = r) }
    }

    fun captureCell(index: Int) {
        val r = scanner.currentRssi()
        _heat.update { s ->
            if (r == null) {
                s.copy(selected = index, noSignal = true, liveRssi = null)
            } else {
                val list = s.cells.toMutableList()
                list[index] = r
                s.copy(cells = list, selected = index, noSignal = false, liveRssi = r)
            }
        }
    }

    fun clearHeatmap() {
        _heat.update { it.copy(cells = List(100) { null }, selected = null, noSignal = false) }
    }

    fun heatmapText(title: String): String {
        val cells = _heat.value.cells
        val sb = StringBuilder(title).append('\n')
        for (row in 0 until 10) {
            val line = (0 until 10).joinToString(" ") { col ->
                cells[row * 10 + col]?.toString()?.padStart(4) ?: "  --"
            }
            sb.append(line).append('\n')
        }
        return sb.toString()
    }

    // ---------------- Channel analyzer ----------------
    private val _channels = MutableStateFlow(ChannelState())
    val channels: StateFlow<ChannelState> = _channels.asStateFlow()

    fun scanChannels() {
        if (_channels.value.loading) return
        _channels.update { it.copy(loading = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { scanner.scanNetworks() }
            _channels.value = ChannelState(loading = false, scannedOnce = true, networks = result)
        }
    }

    // ---------------- Network scanner ----------------
    private val _scan = MutableStateFlow(ScannerState())
    val scan: StateFlow<ScannerState> = _scan.asStateFlow()
    private var scanJob: Job? = null

    fun refreshLocalIp() {
        if (_scan.value.scanning) return
        _scan.update { it.copy(localIp = PingScanner.localIp()) }
    }

    fun startNetworkScan() {
        if (_scan.value.scanning) return
        val ip = PingScanner.localIp()
        if (ip == null) {
            _scan.value = ScannerState(localIp = null)
            return
        }
        scanJob = viewModelScope.launch {
            _scan.value = ScannerState(scanning = true, localIp = ip)
            try {
                PingScanner.scanSubnet(
                    localIp = ip,
                    onProgress = { p -> _scan.update { it.copy(progress = p) } },
                    onHost = { h -> _scan.update { s -> s.copy(hosts = (s.hosts + h).sortedBy { it.lastOctet }) } }
                )
            } finally {
                _scan.update { it.copy(scanning = false, finished = true) }
            }
        }
    }

    fun stopNetworkScan() {
        scanJob?.cancel()
    }

    // ---------------- Signal finder ----------------
    private val _signal = MutableStateFlow(SignalState())
    val signal: StateFlow<SignalState> = _signal.asStateFlow()

    fun refreshCell() {
        viewModelScope.launch(Dispatchers.Default) {
            val c = scanner.readCell()
            val carrier = scanner.carrierName()
            _signal.update { s ->
                s.copy(
                    checked = true,
                    signal = c,
                    carrier = carrier,
                    history = if (c != null) (s.history + c.dbm).takeLast(40) else s.history
                )
            }
        }
    }

    // ---------------- Ping & ports ----------------
    private val _tools = MutableStateFlow(ToolsState())
    val tools: StateFlow<ToolsState> = _tools.asStateFlow()
    private var toolsJob: Job? = null

    fun setTarget(value: String) {
        _tools.update { it.copy(target = value.trim()) }
    }

    fun openPortTool(host: String) {
        stopTools()
        _tools.value = ToolsState(target = host)
    }

    fun runPing() {
        val target = _tools.value.target
        if (target.isBlank() || _tools.value.running) return
        toolsJob = viewModelScope.launch {
            _tools.update { it.copy(running = true, mode = 1, finished = false, pings = emptyList(), openPorts = emptyList()) }
            try {
                repeat(4) {
                    val ms = withContext(Dispatchers.IO) { PingScanner.probe(target, 1000, 800) }
                    _tools.update { s -> s.copy(pings = s.pings + ms) }
                }
            } finally {
                _tools.update { it.copy(running = false, finished = true) }
            }
        }
    }

    fun runPortScan() {
        val target = _tools.value.target
        if (target.isBlank() || _tools.value.running) return
        toolsJob = viewModelScope.launch {
            _tools.update {
                it.copy(running = true, mode = 2, finished = false, progress = 0, pings = emptyList(), openPorts = emptyList())
            }
            try {
                PingScanner.scanPorts(
                    host = target,
                    ports = COMMON_PORTS,
                    onProgress = { p -> _tools.update { it.copy(progress = p) } },
                    onOpen = { port -> _tools.update { s -> s.copy(openPorts = (s.openPorts + port).sortedBy { it.first }) } }
                )
            } finally {
                _tools.update { it.copy(running = false, finished = true) }
            }
        }
    }

    fun stopTools() {
        toolsJob?.cancel()
    }
}
