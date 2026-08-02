package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.NavItem
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.ScanQrScreen
import com.example.ui.screens.StudentScreen
import com.example.ui.theme.AbsensiSholatTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbsensiSholatTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var currentRoute by remember { mutableStateOf(NavItem.Home.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                NavItem.Home.route -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToScan = { currentRoute = NavItem.Scan.route }
                )
                NavItem.Scan.route -> ScanQrScreen(
                    viewModel = viewModel
                )
                NavItem.Students.route -> StudentScreen(
                    viewModel = viewModel
                )
                NavItem.Report.route -> MonthlyReportScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

