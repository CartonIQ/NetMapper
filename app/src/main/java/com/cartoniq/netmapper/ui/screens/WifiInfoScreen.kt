package com.cartoniq.netmapper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.ui.components.InfoRow
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.wifiSignalColor
import com.cartoniq.netmapper.viewmodel.NetworkViewModel
import kotlinx.coroutines.delay

@Composable
fun WifiInfoScreen(vm: NetworkViewModel, onBack: () -> Unit) {
    val info by vm.wifiInfo.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshWifiInfo()
            delay(2000)
        }
    }

    ScreenScaffold(stringResource(R.string.tool_wifi_info), onBack) {
        val w = info
        NmCard(Modifier.fillMaxWidth()) {
            if (w == null) {
                Text(
                    stringResource(R.string.dash_not_connected_hint),
                    modifier = Modifier.padding(16.dp),
                    color = NmTextSecondary,
                    fontSize = 14.sp
                )
            } else {
                val unknown = stringResource(R.string.wi_unknown)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    InfoRow(stringResource(R.string.wi_ssid), w.ssid.ifEmpty { unknown })
                    InfoRow(stringResource(R.string.wi_bssid), w.bssid.ifEmpty { unknown })
                    InfoRow(
                        stringResource(R.string.wi_signal),
                        stringResource(R.string.ch_dbm, w.rssi),
                        wifiSignalColor(w.rssi)
                    )
                    InfoRow(stringResource(R.string.wi_speed), stringResource(R.string.wi_mbps, w.linkSpeedMbps))
                    InfoRow(stringResource(R.string.wi_freq), stringResource(R.string.wi_mhz, w.frequencyMhz, w.band))
                    InfoRow(stringResource(R.string.wi_channel), w.channel.toString())
                    InfoRow(stringResource(R.string.wi_ip), w.ip.ifEmpty { unknown })
                    InfoRow(stringResource(R.string.wi_gateway), w.gateway.ifEmpty { unknown })
                    InfoRow(stringResource(R.string.wi_mask), w.netmask.ifEmpty { unknown })
                    InfoRow(stringResource(R.string.wi_dns), w.dns.ifEmpty { unknown })
                }
            }
        }
    }
}
