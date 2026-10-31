package com.cartoniq.netmapper.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.ui.components.InfoRow
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmDivider
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.cellSignalColor
import com.cartoniq.netmapper.viewmodel.NetworkViewModel
import kotlinx.coroutines.delay

@Composable
fun SignalFinderScreen(vm: NetworkViewModel, hasPermission: Boolean, onBack: () -> Unit) {
    val state by vm.signal.collectAsState()

    LaunchedEffect(hasPermission) {
        while (true) {
            vm.refreshCell()
            delay(2000)
        }
    }

    val sig = state.signal
    val color = if (sig != null) cellSignalColor(sig.dbm) else NmTextSecondary
    val quality = when {
        sig == null -> ""
        sig.dbm >= -80 -> stringResource(R.string.sig_excellent)
        sig.dbm >= -95 -> stringResource(R.string.sig_good)
        sig.dbm >= -108 -> stringResource(R.string.sig_fair)
        else -> stringResource(R.string.sig_poor)
    }

    ScreenScaffold(stringResource(R.string.tool_signal), onBack) {
        NmCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignalGauge(sig?.dbm, color)
                if (sig != null) {
                    Text(quality, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                } else {
                    Text(
                        if (state.checked) stringResource(R.string.sig_none) else stringResource(R.string.ch_scanning),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NmTextMain,
                        textAlign = TextAlign.Center
                    )
                    if (state.checked) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.sig_none_hint),
                            fontSize = 13.sp,
                            color = NmTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (sig != null) {
            Spacer(Modifier.height(12.dp))
            NmCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    InfoRow(stringResource(R.string.sig_type), sig.network)
                    if (state.carrier.isNotEmpty()) {
                        InfoRow(stringResource(R.string.sig_carrier), state.carrier)
                    }
                    InfoRow(stringResource(R.string.sig_bars), "${sig.level} / 4")
                }
            }
        }

        if (state.history.size > 1) {
            Spacer(Modifier.height(12.dp))
            NmCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.sig_history),
                        fontWeight = FontWeight.Bold,
                        color = NmTextMain
                    )
                    Spacer(Modifier.height(10.dp))
                    Sparkline(state.history)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.sig_scale_hint),
            fontSize = 12.sp,
            color = NmTextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SignalGauge(dbm: Int?, color: Color) {
    val target = if (dbm == null) 0f else ((dbm + 140) / 96f).coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, label = "gauge")
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                NmDivider, 150f, 240f, false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            if (animated > 0.005f) {
                drawArc(
                    color, 150f, 240f * animated, false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                dbm?.toString() ?: "--",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(stringResource(R.string.sig_unit), fontSize = 16.sp, color = NmTextSecondary)
        }
    }
}

@Composable
private fun Sparkline(values: List<Int>) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val w = size.width
        val h = size.height
        val step = w / (values.size - 1).coerceAtLeast(1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val frac = ((v + 140) / 96f).coerceIn(0f, 1f)
            val x = i * step
            val y = h - h * frac
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, NmAccent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}
