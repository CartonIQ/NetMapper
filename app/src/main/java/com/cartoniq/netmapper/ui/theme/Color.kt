package com.cartoniq.netmapper.ui.theme

import androidx.compose.ui.graphics.Color

val NmBackground = Color(0xFFF8F9FA)
val NmSurface = Color(0xFFFFFFFF)
val NmTextMain = Color(0xFF2C3E50)
val NmTextSecondary = Color(0xFF7F8C8D)
val NmAccent = Color(0xFF3498DB)
val NmStrong = Color(0xFF2ECC71)
val NmMedium = Color(0xFFF1C40F)
val NmWeak = Color(0xFFE74C3C)
val NmDivider = Color(0xFFECF0F1)
val NmAccentSoft = Color(0xFFEAF4FB)
val NmPurple = Color(0xFF9B59B6)
val NmOrange = Color(0xFFE67E22)
val NmTeal = Color(0xFF1ABC9C)

/** Wi-Fi RSSI -> colour (green / yellow / red). */
fun wifiSignalColor(rssi: Int): Color = when {
    rssi >= -60 -> NmStrong
    rssi >= -75 -> NmMedium
    else -> NmWeak
}

/** Cellular dBm -> colour (green / yellow / red). */
fun cellSignalColor(dbm: Int): Color = when {
    dbm >= -90 -> NmStrong
    dbm >= -105 -> NmMedium
    else -> NmWeak
}
