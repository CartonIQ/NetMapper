package com.cartoniq.netmapper.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.ads.AdManager
import com.cartoniq.netmapper.ui.components.HEAT_SIZE
import com.cartoniq.netmapper.ui.components.HeatmapGrid
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.PrimaryButton
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.components.SecondaryButton
import com.cartoniq.netmapper.ui.theme.NmMedium
import com.cartoniq.netmapper.ui.theme.NmStrong
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.NmWeak
import com.cartoniq.netmapper.ui.theme.wifiSignalColor
import com.cartoniq.netmapper.viewmodel.NetworkViewModel
import kotlinx.coroutines.delay

private fun shareText(context: Context, text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

@Composable
fun HeatmapScreen(vm: NetworkViewModel, onBack: () -> Unit) {
    val state by vm.heat.collectAsState()
    val context = LocalContext.current
    val exportTitle = stringResource(R.string.heat_export_title)
    val chooserTitle = stringResource(R.string.heat_export_chooser)

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshRssi()
            delay(1000)
        }
    }

    val captured = state.cells.filterNotNull()
    val live = state.liveRssi

    ScreenScaffold(stringResource(R.string.tool_heatmap), onBack) {
        NmCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.heat_live), fontSize = 13.sp, color = NmTextSecondary)
                    if (live != null) {
                        Text(
                            stringResource(R.string.ch_dbm, live),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = wifiSignalColor(live)
                        )
                    } else {
                        Text(
                            stringResource(R.string.heat_live_none),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NmWeak
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.heat_captured, captured.size),
                        fontSize = 13.sp,
                        color = NmTextMain,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (captured.isNotEmpty()) {
                        Text(
                            stringResource(R.string.heat_average, captured.average().toInt()),
                            fontSize = 13.sp,
                            color = NmTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.heat_hint), fontSize = 13.sp, color = NmTextSecondary)
        Spacer(Modifier.height(12.dp))

        NmCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                HeatmapGrid(
                    cells = state.cells,
                    selected = state.selected,
                    onCellTap = { vm.captureCell(it) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendDot(NmStrong, stringResource(R.string.legend_strong))
            LegendDot(NmMedium, stringResource(R.string.legend_medium))
            LegendDot(NmWeak, stringResource(R.string.legend_weak))
            LegendDot(Color(0xFFEEF1F3), stringResource(R.string.legend_empty))
        }

        val sel = state.selected
        if (state.noSignal) {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.heat_no_signal), color = NmWeak, fontSize = 13.sp)
        } else if (sel != null) {
            val value = state.cells[sel]
            if (value != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.heat_cell_info, sel / HEAT_SIZE + 1, sel % HEAT_SIZE + 1, value),
                    color = wifiSignalColor(value),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton(
                stringResource(R.string.action_clear),
                { vm.clearHeatmap() },
                Modifier.weight(1f)
            )
            PrimaryButton(
                stringResource(R.string.heat_export),
                {
                    if (captured.isEmpty()) {
                        Toast.makeText(context, R.string.heat_export_empty, Toast.LENGTH_SHORT).show()
                    } else {
                        AdManager.runWithReward(context) {
                            shareText(context, vm.heatmapText(exportTitle), chooserTitle)
                        }
                    }
                },
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = NmTextSecondary)
    }
}
