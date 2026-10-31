package com.cartoniq.netmapper.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val band: String,
    val security: String
)

data class WifiDetails(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val channel: Int,
    val band: String,
    val ip: String,
    val gateway: String,
    val netmask: String,
    val dns: String
)

data class CellSignal(val dbm: Int, val network: String, val level: Int)

fun frequencyToChannel(freq: Int): Int = when (freq) {
    2484 -> 14
    in 2412..2472 -> (freq - 2407) / 5
    in 5160..5885 -> (freq - 5000) / 5
    in 5955..7115 -> (freq - 5950) / 5
    else -> 0
}

fun frequencyToBand(freq: Int): String = when (freq) {
    in 2400..2500 -> "2.4 GHz"
    in 5100..5900 -> "5 GHz"
    in 5925..7200 -> "6 GHz"
    else -> "?"
}

fun ipToString(ip: Int): String =
    "${ip and 0xff}.${(ip shr 8) and 0xff}.${(ip shr 16) and 0xff}.${(ip shr 24) and 0xff}"

class WifiScanner(context: Context) {

    private val app = context.applicationContext
    private val wifi = app.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephony = app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** RSSI of the connected Wi-Fi network, or null when not connected. */
    @Suppress("DEPRECATION")
    fun currentRssi(): Int? {
        if (!wifi.isWifiEnabled) return null
        val info = wifi.connectionInfo ?: return null
        val rssi = info.rssi
        return if (rssi in -126..-1) rssi else null
    }

    @Suppress("DEPRECATION")
    fun currentDetails(): WifiDetails? {
        if (!wifi.isWifiEnabled) return null
        val info = wifi.connectionInfo ?: return null
        if (info.rssi !in -126..-1) return null
        val rawSsid = info.ssid?.trim('"').orEmpty()
        val ssid = if (rawSsid == "<unknown ssid>") "" else rawSsid
        val dhcp = wifi.dhcpInfo
        val freq = info.frequency
        return WifiDetails(
            ssid = ssid,
            bssid = info.bssid.orEmpty(),
            rssi = info.rssi,
            linkSpeedMbps = info.linkSpeed,
            frequencyMhz = freq,
            channel = frequencyToChannel(freq),
            band = frequencyToBand(freq),
            ip = if (info.ipAddress != 0) ipToString(info.ipAddress) else "",
            gateway = if (dhcp != null && dhcp.gateway != 0) ipToString(dhcp.gateway) else "",
            netmask = if (dhcp != null && dhcp.netmask != 0) ipToString(dhcp.netmask) else "",
            dns = if (dhcp != null && dhcp.dns1 != 0) ipToString(dhcp.dns1) else ""
        )
    }

    /** Triggers a scan, waits briefly, then returns the visible networks sorted by signal. */
    @Suppress("DEPRECATION")
    suspend fun scanNetworks(): List<WifiNetwork> {
        if (!hasLocationPermission()) return emptyList()
        try {
            wifi.startScan()
        } catch (e: SecurityException) {
            // fall through and read cached results
        }
        delay(2500)
        return readResults()
    }

    @SuppressLint("MissingPermission")
    fun readResults(): List<WifiNetwork> {
        return try {
            wifi.scanResults.map { r ->
                val caps = r.capabilities.orEmpty()
                val security = when {
                    caps.contains("WPA3") || caps.contains("SAE") -> "WPA3"
                    caps.contains("WPA2") -> "WPA2"
                    caps.contains("WPA") -> "WPA"
                    caps.contains("WEP") -> "WEP"
                    else -> "Open"
                }
                @Suppress("DEPRECATION")
                val name = r.SSID.orEmpty()
                WifiNetwork(
                    ssid = name,
                    bssid = r.BSSID.orEmpty(),
                    rssi = r.level,
                    frequency = r.frequency,
                    channel = frequencyToChannel(r.frequency),
                    band = frequencyToBand(r.frequency),
                    security = security
                )
            }.sortedByDescending { it.rssi }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    fun carrierName(): String = telephony?.networkOperatorName.orEmpty()

    @SuppressLint("MissingPermission")
    fun readCell(): CellSignal? {
        val tm = telephony ?: return null
        if (!hasLocationPermission()) return null
        val raw: List<CellInfo>? = try {
            tm.allCellInfo
        } catch (e: SecurityException) {
            null
        }
        val cells = raw ?: return null
        return cells.sortedByDescending { it.isRegistered }.firstNotNullOfOrNull { toSignal(it) }
    }

    private fun make(dbm: Int, level: Int, type: String): CellSignal? =
        if (dbm == Int.MAX_VALUE || dbm >= 0 || dbm < -150) null else CellSignal(dbm, type, level)

    private fun toSignal(c: CellInfo): CellSignal? = when {
        Build.VERSION.SDK_INT >= 29 && c is CellInfoNr ->
            make(c.cellSignalStrength.dbm, c.cellSignalStrength.level, "5G")
        c is CellInfoLte -> make(c.cellSignalStrength.dbm, c.cellSignalStrength.level, "4G LTE")
        c is CellInfoWcdma -> make(c.cellSignalStrength.dbm, c.cellSignalStrength.level, "3G")
        c is CellInfoGsm -> make(c.cellSignalStrength.dbm, c.cellSignalStrength.level, "2G")
        c is CellInfoCdma -> make(c.cellSignalStrength.dbm, c.cellSignalStrength.level, "3G CDMA")
        else -> null
    }
}
