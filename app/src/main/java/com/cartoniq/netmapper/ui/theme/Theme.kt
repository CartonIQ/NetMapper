package com.cartoniq.netmapper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NetMapperColors = lightColorScheme(
    primary = NmAccent,
    onPrimary = Color.White,
    secondary = NmAccent,
    onSecondary = Color.White,
    background = NmBackground,
    onBackground = NmTextMain,
    surface = NmSurface,
    onSurface = NmTextMain,
    surfaceVariant = NmAccentSoft,
    onSurfaceVariant = NmTextSecondary,
    surfaceTint = Color.Transparent,
    outline = NmDivider,
    error = NmWeak
)

/** Light-only theme: the app never switches to a dark palette. */
@Composable
fun NetMapperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NetMapperColors,
        typography = NmTypography,
        content = content
    )
}
