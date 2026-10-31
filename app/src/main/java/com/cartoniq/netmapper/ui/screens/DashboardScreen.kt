package com.cartoniq.netmapper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.ads.BannerAd
import com.cartoniq.netmapper.navigation.Routes
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.PrimaryButton
import com.cartoniq.netmapper.ui.components.SectionTitle
import com.cartoniq.netmapper.ui.components.ToolIcon
import com.cartoniq.netmapper.ui.components.ToolIconType
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmBackground
import com.cartoniq.netmapper.ui.theme.NmOrange
import com.cartoniq.netmapper.ui.theme.NmPurple
import com.cartoniq.netmapper.ui.theme.NmStrong
import com.cartoniq.netmapper.ui.theme.NmSurface
import com.cartoniq.netmapper.ui.theme.NmTeal
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.NmWeak
import com.cartoniq.netmapper.ui.theme.wifiSignalColor
import com.cartoniq.netmapper.viewmodel.NetworkViewModel
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    vm: NetworkViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val wifi by vm.wifiInfo.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermission) {
        while (true) {
            vm.refreshWifiInfo()
            delay(3000)
        }
    }

    Scaffold(
        containerColor = NmBackground,
        bottomBar = {
            Surface(color = NmSurface, shadowElevation = 6.dp) { BannerAd() }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.app_name),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NmTextMain
                    )
                    Text(stringResource(R.string.tagline), fontSize = 14.sp, color = NmTextSecondary)
                }
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NmAccent
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Connection summary
            NmCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val info = wifi
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (info != null) wifiSignalColor(info.rssi) else NmWeak)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        if (info != null) {
                            Text(
                                if (info.ssid.isNotEmpty()) stringResource(R.string.dash_connected_to, info.ssid)
                                else stringResource(R.string.dash_wifi_connected),
                                fontWeight = FontWeight.Bold,
                                color = NmTextMain
                            )
                            Text(
                                stringResource(R.string.dash_signal_ip, info.rssi, info.ip.ifEmpty { "-" }),
                                fontSize = 13.sp,
                                color = NmTextSecondary
                            )
                        } else {
                            Text(
                                stringResource(R.string.dash_not_connected),
                                fontWeight = FontWeight.Bold,
                                color = NmTextMain
                            )
                            Text(
                                stringResource(R.string.dash_not_connected_hint),
                                fontSize = 13.sp,
                                color = NmTextSecondary
                            )
                        }
                    }
                }
            }

            if (!hasPermission) {
                Spacer(Modifier.height(12.dp))
                NmCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.perm_title),
                            fontWeight = FontWeight.Bold,
                            color = NmTextMain
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.perm_body), fontSize = 13.sp, color = NmTextSecondary)
                        Spacer(Modifier.height(12.dp))
                        PrimaryButton(stringResource(R.string.action_grant), onRequestPermission)
                    }
                }
            }

            SectionTitle(stringResource(R.string.section_tools))

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolCard(
                    stringResource(R.string.tool_heatmap), stringResource(R.string.tool_heatmap_sub),
                    NmStrong, ToolIconType.HEATMAP,
                    Modifier.weight(1f).fillMaxHeight()
                ) { onNavigate(Routes.HEATMAP) }
                ToolCard(
                    stringResource(R.string.tool_channels), stringResource(R.string.tool_channels_sub),
                    NmPurple, ToolIconType.CHANNELS,
                    Modifier.weight(1f).fillMaxHeight()
                ) { onNavigate(Routes.CHANNELS) }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolCard(
                    stringResource(R.string.tool_scanner), stringResource(R.string.tool_scanner_sub),
                    NmAccent, ToolIconType.SCANNER,
                    Modifier.weight(1f).fillMaxHeight()
                ) { onNavigate(Routes.SCANNER) }
                ToolCard(
                    stringResource(R.string.tool_signal), stringResource(R.string.tool_signal_sub),
                    NmOrange, ToolIconType.SIGNAL,
                    Modifier.weight(1f).fillMaxHeight()
                ) { onNavigate(Routes.SIGNAL) }
            }

            SectionTitle(stringResource(R.string.section_more))
            WideToolCard(
                stringResource(R.string.tool_wifi_info), stringResource(R.string.tool_wifi_info_sub),
                NmTeal, ToolIconType.WIFI
            ) { onNavigate(Routes.WIFI_INFO) }
            Spacer(Modifier.height(12.dp))
            WideToolCard(
                stringResource(R.string.tool_ports), stringResource(R.string.tool_ports_sub),
                NmPurple, ToolIconType.PORTS
            ) { onNavigate(Routes.PORTS) }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
private fun IconBadge(accent: Color, type: ToolIconType) {
    Box(
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        ToolIcon(type, accent)
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    accent: Color,
    type: ToolIconType,
    modifier: Modifier,
    onClick: () -> Unit
) {
    NmCard(modifier = modifier, onClick = onClick) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconBadge(accent, type)
            Spacer(Modifier.height(14.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NmTextMain)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = NmTextSecondary)
        }
    }
}

@Composable
private fun WideToolCard(
    title: String,
    subtitle: String,
    accent: Color,
    type: ToolIconType,
    onClick: () -> Unit
) {
    NmCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(accent, type)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NmTextMain)
                Text(subtitle, fontSize = 12.sp, color = NmTextSecondary)
            }
        }
    }
}
