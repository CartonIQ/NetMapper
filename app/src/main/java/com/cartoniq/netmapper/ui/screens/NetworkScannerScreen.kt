package com.cartoniq.netmapper.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.ads.AdManager
import com.cartoniq.netmapper.network.NetHost
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.PrimaryButton
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.components.SecondaryButton
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmDivider
import com.cartoniq.netmapper.ui.theme.NmOrange
import com.cartoniq.netmapper.ui.theme.NmStrong
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.viewmodel.NetworkViewModel

@Composable
fun NetworkScannerScreen(vm: NetworkViewModel, onBack: () -> Unit, onOpenPorts: () -> Unit) {
    val state by vm.scan.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.refreshLocalIp() }

    ScreenScaffold(stringResource(R.string.tool_scanner), onBack, scroll = false) {
        NmCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val ip = state.localIp
                if (ip == null) {
                    Text(stringResource(R.string.scan_no_wifi), color = NmTextSecondary, fontSize = 14.sp)
                } else {
                    Text(stringResource(R.string.scan_your_ip), fontSize = 13.sp, color = NmTextSecondary)
                    Text(ip, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = NmTextMain)
                    Text(
                        stringResource(R.string.scan_subnet, ip.substringBeforeLast('.')),
                        fontSize = 12.sp,
                        color = NmTextSecondary
                    )
                }
                if (state.scanning || state.finished) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / 254f },
                        modifier = Modifier.fillMaxWidth(),
                        color = NmAccent,
                        trackColor = NmDivider
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.scan_progress, state.progress),
                            fontSize = 12.sp,
                            color = NmTextSecondary
                        )
                        Text(
                            stringResource(R.string.scan_found, state.hosts.size),
                            fontSize = 12.sp,
                            color = NmTextMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        if (state.scanning) {
            SecondaryButton(stringResource(R.string.action_stop), { vm.stopNetworkScan() })
        } else {
            PrimaryButton(
                stringResource(R.string.scan_start),
                { AdManager.runWithReward(context) { vm.startNetworkScan() } },
                enabled = state.localIp != null
            )
        }
        Spacer(Modifier.height(12.dp))

        if (state.hosts.isEmpty()) {
            Text(
                if (state.finished) stringResource(R.string.scan_empty_done) else stringResource(R.string.scan_idle_hint),
                color = NmTextSecondary,
                fontSize = 13.sp
            )
        } else {
            Text(stringResource(R.string.scan_tap_hint), color = NmTextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.hosts, key = { it.ip }) { host ->
                    HostRow(host) {
                        vm.openPortTool(host.ip)
                        onOpenPorts()
                    }
                }
            }
        }
    }
}

@Composable
private fun HostRow(host: NetHost, onClick: () -> Unit) {
    NmCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(NmStrong)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(host.ip, fontWeight = FontWeight.Bold, color = NmTextMain)
                if (host.hostname.isNotEmpty()) {
                    Text(
                        host.hostname,
                        fontSize = 12.sp,
                        color = NmTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (host.isSelf) Tag(stringResource(R.string.scan_this_device), NmAccent)
                if (host.isGateway) Tag(stringResource(R.string.scan_router), NmOrange)
            }
            if (!host.isSelf) {
                Text(
                    stringResource(R.string.scan_latency, host.latencyMs),
                    fontSize = 12.sp,
                    color = NmTextSecondary
                )
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(top = 2.dp)
    )
}
