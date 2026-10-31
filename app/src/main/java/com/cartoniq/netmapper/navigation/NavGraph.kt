package com.cartoniq.netmapper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cartoniq.netmapper.ui.screens.ChannelAnalyzerScreen
import com.cartoniq.netmapper.ui.screens.DashboardScreen
import com.cartoniq.netmapper.ui.screens.HeatmapScreen
import com.cartoniq.netmapper.ui.screens.NetworkScannerScreen
import com.cartoniq.netmapper.ui.screens.PortToolsScreen
import com.cartoniq.netmapper.ui.screens.SignalFinderScreen
import com.cartoniq.netmapper.ui.screens.WifiInfoScreen
import com.cartoniq.netmapper.viewmodel.NetworkViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val HEATMAP = "heatmap"
    const val CHANNELS = "channels"
    const val SCANNER = "scanner"
    const val SIGNAL = "signal"
    const val WIFI_INFO = "wifi_info"
    const val PORTS = "ports"
}

@Composable
fun NavGraph(
    vm: NetworkViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                vm = vm,
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onNavigate = { route -> nav.navigate(route) }
            )
        }
        composable(Routes.HEATMAP) { HeatmapScreen(vm = vm, onBack = back) }
        composable(Routes.CHANNELS) {
            ChannelAnalyzerScreen(vm = vm, hasPermission = hasPermission, onBack = back)
        }
        composable(Routes.SCANNER) {
            NetworkScannerScreen(
                vm = vm,
                onBack = back,
                onOpenPorts = { nav.navigate(Routes.PORTS) }
            )
        }
        composable(Routes.SIGNAL) {
            SignalFinderScreen(vm = vm, hasPermission = hasPermission, onBack = back)
        }
        composable(Routes.WIFI_INFO) { WifiInfoScreen(vm = vm, onBack = back) }
        composable(Routes.PORTS) { PortToolsScreen(vm = vm, onBack = back) }
    }
}
