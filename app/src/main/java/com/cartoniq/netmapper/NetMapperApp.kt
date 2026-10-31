package com.cartoniq.netmapper

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.netmapper.navigation.NavGraph
import com.cartoniq.netmapper.viewmodel.NetworkViewModel

@Composable
fun NetMapperApp() {
    val context = LocalContext.current
    val vm: NetworkViewModel = viewModel()
    var hasPermission by remember { mutableStateOf(vm.hasLocationPermission()) }
    var fromButton by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = vm.hasLocationPermission()
        if (!hasPermission && fromButton) {
            // Permanently denied: send the user to the app settings page.
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
        fromButton = false
    }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(permissions)
    }

    NavGraph(
        vm = vm,
        hasPermission = hasPermission,
        onRequestPermission = {
            hasPermission = vm.hasLocationPermission()
            if (!hasPermission) {
                fromButton = true
                launcher.launch(permissions)
            }
        }
    )
}
