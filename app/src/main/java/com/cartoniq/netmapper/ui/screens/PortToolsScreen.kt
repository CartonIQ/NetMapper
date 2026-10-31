package com.cartoniq.netmapper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.netmapper.R
import com.cartoniq.netmapper.network.COMMON_PORTS
import com.cartoniq.netmapper.ui.components.NmCard
import com.cartoniq.netmapper.ui.components.PrimaryButton
import com.cartoniq.netmapper.ui.components.ScreenScaffold
import com.cartoniq.netmapper.ui.components.SecondaryButton
import com.cartoniq.netmapper.ui.theme.NmAccent
import com.cartoniq.netmapper.ui.theme.NmDivider
import com.cartoniq.netmapper.ui.theme.NmStrong
import com.cartoniq.netmapper.ui.theme.NmSurface
import com.cartoniq.netmapper.ui.theme.NmTextMain
import com.cartoniq.netmapper.ui.theme.NmTextSecondary
import com.cartoniq.netmapper.ui.theme.NmWeak
import com.cartoniq.netmapper.viewmodel.NetworkViewModel

@Composable
fun PortToolsScreen(vm: NetworkViewModel, onBack: () -> Unit) {
    val state by vm.tools.collectAsState()
    val context = LocalContext.current

    ScreenScaffold(stringResource(R.string.tool_ports), onBack) {
        OutlinedTextField(
            value = state.target,
            onValueChange = { vm.setTarget(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.pt_target)) },
            placeholder = { Text(stringResource(R.string.pt_target_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NmAccent,
                unfocusedBorderColor = Color(0xFFD5DBDB),
                focusedContainerColor = NmSurface,
                unfocusedContainerColor = NmSurface
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton(
                stringResource(R.string.pt_ping),
                {
                    if (state.target.isBlank()) {
                        Toast.makeText(context, R.string.pt_enter_host, Toast.LENGTH_SHORT).show()
                    } else {
                        vm.runPing()
                    }
                },
                Modifier.weight(1f),
                enabled = !state.running
            )
            PrimaryButton(
                stringResource(R.string.pt_ports),
                {
                    if (state.target.isBlank()) {
                        Toast.makeText(context, R.string.pt_enter_host, Toast.LENGTH_SHORT).show()
                    } else {
                        vm.runPortScan()
                    }
                },
                Modifier.weight(1f),
                enabled = !state.running
            )
        }
        if (state.running) {
            Spacer(Modifier.height(8.dp))
            SecondaryButton(stringResource(R.string.action_stop), { vm.stopTools() })
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.pt_note), fontSize = 12.sp, color = NmTextSecondary)
        Spacer(Modifier.height(16.dp))

        if (state.mode == 1 && state.pings.isNotEmpty()) {
            NmCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.pt_ping_title), fontWeight = FontWeight.Bold, color = NmTextMain)
                    Spacer(Modifier.height(8.dp))
                    state.pings.forEachIndexed { i, ms ->
                        if (ms != null) {
                            Text(
                                stringResource(R.string.pt_reply, i + 1, ms),
                                color = NmStrong,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        } else {
                            Text(
                                stringResource(R.string.pt_timeout, i + 1),
                                color = NmWeak,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    if (state.finished) {
                        val ok = state.pings.filterNotNull()
                        Spacer(Modifier.height(8.dp))
                        if (ok.isEmpty()) {
                            Text(
                                stringResource(R.string.pt_summary_fail),
                                color = NmTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            val loss = (state.pings.size - ok.size) * 100 / state.pings.size
                            Text(
                                stringResource(
                                    R.string.pt_summary,
                                    ok.min().toInt(),
                                    ok.average().toInt(),
                                    ok.max().toInt(),
                                    loss
                                ),
                                color = NmTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        if (state.mode == 2) {
            NmCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.pt_open_title), fontWeight = FontWeight.Bold, color = NmTextMain)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / COMMON_PORTS.size.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = NmAccent,
                        trackColor = NmDivider
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.pt_progress, state.progress, COMMON_PORTS.size),
                        fontSize = 12.sp,
                        color = NmTextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    state.openPorts.forEach { (port, name) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.pt_port_row, port),
                                color = NmStrong,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(name, color = NmTextSecondary, fontSize = 14.sp)
                        }
                    }
                    if (state.finished && state.openPorts.isEmpty()) {
                        Text(stringResource(R.string.pt_none_open), color = NmTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
