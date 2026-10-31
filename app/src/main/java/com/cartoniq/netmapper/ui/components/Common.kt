package com.cartoniq.netmapper.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmBackground
import com.cartoniq.netmapper.ui.theme.NmDivider
import com.cartoniq.netmapper.ui.theme.NmSurface
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = NmBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NmBackground,
                    titleContentColor = NmTextMain,
                    navigationIconContentColor = NmTextMain
                )
            )
        }
    ) { pad ->
        val base = Modifier
            .padding(pad)
            .fillMaxSize()
        val mod = if (scroll) base.verticalScroll(rememberScrollState()) else base
        Column(modifier = mod.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = NmSurface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NmAccent,
            contentColor = Color.White,
            disabledContainerColor = NmDivider,
            disabledContentColor = NmTextSecondary
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, color = if (enabled) NmAccent else NmTextSecondary)
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = NmTextMain) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = NmTextSecondary, fontSize = 14.sp)
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        color = NmTextMain,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
    )
}

enum class ToolIconType { HEATMAP, CHANNELS, SCANNER, SIGNAL, WIFI, PORTS }

@Composable
fun ToolIcon(type: ToolIconType, color: Color, modifier: Modifier = Modifier.size(28.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = w * 0.09f
        when (type) {
            ToolIconType.HEATMAP -> {
                val gap = w * 0.08f
                val cell = (w - 2 * gap) / 3f
                for (r in 0..2) {
                    for (c in 0..2) {
                        drawRoundRect(
                            color.copy(alpha = 0.3f + 0.23f * ((r + c) % 4)),
                            Offset(c * (cell + gap), r * (cell + gap)),
                            Size(cell, cell),
                            CornerRadius(cell * 0.25f)
                        )
                    }
                }
            }
            ToolIconType.CHANNELS -> {
                val p = Path()
                p.moveTo(w * 0.05f, h * 0.9f)
                p.cubicTo(w * 0.1f, h * 0.0f, w * 0.5f, h * 0.0f, w * 0.55f, h * 0.9f)
                drawPath(p, color, style = Stroke(sw, cap = StrokeCap.Round))
                val q = Path()
                q.moveTo(w * 0.4f, h * 0.9f)
                q.cubicTo(w * 0.45f, h * 0.2f, w * 0.9f, h * 0.2f, w * 0.95f, h * 0.9f)
                drawPath(q, color.copy(alpha = 0.55f), style = Stroke(sw, cap = StrokeCap.Round))
            }
            ToolIconType.SCANNER -> {
                drawCircle(color, radius = w * 0.46f, style = Stroke(sw))
                drawCircle(color.copy(alpha = 0.6f), radius = w * 0.28f, style = Stroke(sw))
                drawCircle(color, radius = w * 0.09f)
                drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.85f, h * 0.2f), sw)
            }
            ToolIconType.SIGNAL -> {
                val heights = listOf(0.3f, 0.5f, 0.75f, 1.0f)
                val bw = w * 0.17f
                heights.forEachIndexed { i, f ->
                    val bh = h * f
                    drawRoundRect(
                        color,
                        Offset(i * w * 0.27f + w * 0.02f, h - bh),
                        Size(bw, bh),
                        CornerRadius(bw / 2f)
                    )
                }
            }
            ToolIconType.WIFI -> {
                val cx = w / 2f
                val cy = h * 0.85f
                for (r in listOf(w * 0.75f, w * 0.5f, w * 0.25f)) {
                    drawArc(
                        color, 225f, 90f, false,
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(2 * r, 2 * r),
                        style = Stroke(sw, cap = StrokeCap.Round)
                    )
                }
                drawCircle(color, radius = w * 0.07f, center = Offset(cx, cy))
            }
            ToolIconType.PORTS -> {
                val bh = h * 0.24f
                for (i in 0..2) {
                    val y = i * h * 0.38f
                    drawRoundRect(
                        color.copy(alpha = 0.35f + 0.3f * i),
                        Offset(0f, y),
                        Size(w, bh),
                        CornerRadius(bh / 2f)
                    )
                    drawCircle(Color.White, radius = bh * 0.22f, center = Offset(w - bh * 0.5f, y + bh / 2f))
                }
            }
        }
    }
}
