package com.cartoniq.netmapper.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.network.WifiNetwork
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.PrimaryButton
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmDivider
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.wifiSignalColor
import com.cartoniq.netmapper.viewmodel.NetworkViewModel
import kotlin.math.abs

@Composable
fun ChannelAnalyzerScreen(vm: NetworkViewModel, hasPermission: Boolean, onBack: () -> Unit) {
    val state by vm.channels.collectAsState()

    LaunchedEffect(hasPermission) {
        if (hasPermission) vm.scanChannels()
    }

    val networks = state.networks
    val best = remember(networks) {
        listOf(1, 6, 11).minByOrNull { ch ->
            networks.count { it.band == "2.4 GHz" && abs(it.channel - ch) <= 2 }
        }
    }

    ScreenScaffold(stringResource(R.string.tool_channels), onBack) {
        PrimaryButton(
            text = if (state.loading) stringResource(R.string.ch_scanning) else stringResource(R.string.action_scan),
            onClick = { vm.scanChannels() },
            enabled = hasPermission && !state.loading
        )
        Spacer(Modifier.height(12.dp))

        if (!hasPermission) {
            MessageCard(stringResource(R.string.perm_body))
        } else if (networks.isEmpty()) {
            MessageCard(
                if (state.loading) stringResource(R.string.ch_scanning) else stringResource(R.string.ch_empty)
            )
        } else {
            NmCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.ch_chart_title),
                        fontWeight = FontWeight.Bold,
                        color = NmTextMain
                    )
                    Spacer(Modifier.height(12.dp))
                    SignalBarChart(networks)
                    if (best != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.ch_best, best),
                            color = NmAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.ch_list_title, networks.size),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NmTextMain
            )
            Spacer(Modifier.height(10.dp))
            networks.forEach { n ->
                NetworkRow(n)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MessageCard(text: String) {
    NmCard(Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(16.dp), color = NmTextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SignalBarChart(networks: List<WifiNetwork>) {
    val shown = networks.take(10)
    if (shown.isEmpty()) return
    Column {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val w = size.width
            val h = size.height
            for (i in 0..3) {
                val y = h * i / 3f
                drawLine(NmDivider, Offset(0f, y), Offset(w, y), 1.dp.toPx())
            }
            val slot = w / shown.size
            shown.forEachIndexed { i, n ->
                val frac = ((n.rssi + 100) / 60f).coerceIn(0.06f, 1f)
                val barW = slot * 0.6f
                val left = slot * i + (slot - barW) / 2f
                drawRoundRect(
                    wifiSignalColor(n.rssi),
                    Offset(left, h - h * frac),
                    Size(barW, h * frac),
                    CornerRadius(8.dp.toPx())
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            shown.forEach { n ->
                Text(
                    stringResource(R.string.ch_short, n.channel),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = NmTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NetworkRow(n: WifiNetwork) {
    NmCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(wifiSignalColor(n.rssi))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (n.ssid.isEmpty()) stringResource(R.string.ch_hidden) else n.ssid,
                    fontWeight = FontWeight.Bold,
                    color = NmTextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.ch_row_meta, n.band, n.security),
                    fontSize = 12.sp,
                    color = NmTextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.ch_dbm, n.rssi),
                    fontWeight = FontWeight.Bold,
                    color = wifiSignalColor(n.rssi)
                )
                Text(
                    stringResource(R.string.ch_channel_label, n.channel),
                    fontSize = 12.sp,
                    color = NmTextSecondary
                )
            }
        }
    }
}
