package com.cartoniq.netmapper.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.ui.theme.NmAccent

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Use NetMapper", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("1. HEATMAP: Walk around your house, stand in a spot, and tap a square on the grid to capture your signal strength there.\n\n2. NETWORK SCANNER: Tap 'Scan' to find devices connected to your WiFi.\n\n3. CHANNEL ANALYZER: Check which WiFi channels are crowded.\n\n4. PRIVACY: We do not collect any personal data.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cartoniq.github.io/privacy/"))
                    context.startActivity(intent)
                }) {
                    Text("Read Full Privacy Policy", color = NmAccent, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it", color = NmAccent) } }
    )
}
