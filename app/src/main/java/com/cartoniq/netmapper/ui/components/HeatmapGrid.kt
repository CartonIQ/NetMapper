package com.cartoniq.netmapper.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.wifiSignalColor

const val HEAT_SIZE = 10

/** 10x10 tappable canvas. Each captured cell is coloured green / yellow / red by RSSI. */
@Composable
fun HeatmapGrid(
    cells: List<Int?>,
    selected: Int?,
    onCellTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tap by rememberUpdatedState(onCellTap)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cw = size.width / HEAT_SIZE.toFloat()
                    val ch = size.height / HEAT_SIZE.toFloat()
                    val col = (offset.x / cw).toInt().coerceIn(0, HEAT_SIZE - 1)
                    val row = (offset.y / ch).toInt().coerceIn(0, HEAT_SIZE - 1)
                    tap(row * HEAT_SIZE + col)
                }
            }
    ) {
        val gap = 3.dp.toPx()
        val cw = size.width / HEAT_SIZE
        val ch = size.height / HEAT_SIZE
        for (r in 0 until HEAT_SIZE) {
            for (c in 0 until HEAT_SIZE) {
                val idx = r * HEAT_SIZE + c
                val rssi = cells.getOrNull(idx)
                val color = if (rssi != null) wifiSignalColor(rssi) else Color(0xFFEEF1F3)
                val topLeft = Offset(c * cw + gap / 2f, r * ch + gap / 2f)
                val cellSize = Size(cw - gap, ch - gap)
                drawRoundRect(color, topLeft, cellSize, CornerRadius(6.dp.toPx()))
                if (selected == idx) {
                    drawRoundRect(
                        NmTextMain, topLeft, cellSize, CornerRadius(6.dp.toPx()),
                        style = Stroke(2.5.dp.toPx())
                    )
                }
            }
        }
    }
}
