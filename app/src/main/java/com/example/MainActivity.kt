package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MeasurementRepository
import com.example.ui.MeasurementViewModel
import com.example.ui.screens.EstadisticasTab
import com.example.ui.screens.HistorialTab
import com.example.ui.screens.InfoTab
import com.example.ui.screens.RegistrarTab
import com.example.ui.screens.PerfilTab
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Composition setup
        val database = AppDatabase.getDatabase(this)
        val repository = MeasurementRepository(database.measurementDao(), database.goalDao())
        val viewModel = ViewModelProvider(
            this,
            MeasurementViewModel.Factory(repository, this)
        )[MeasurementViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MeasurementViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe trigger alerts and display them via Material 3 Snackbar
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    val topBarTitle = when (currentTab) {
        0 -> "Registro de Medidas"
        1 -> "Nueva Medición"
        2 -> "Estadísticas y Tendencias"
        3 -> "Ayuda Médica y Guía"
        4 -> "Perfil de Usuario"
        else -> "Registro"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Persistent standard Material 3 Navigation Bar respect safe navigation bars window insets
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Historial") },
                    label = { Text("Historial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_historial")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Registrar") },
                    label = { Text("Registrar", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_registrar")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Estadísticas") },
                    label = { Text("Gráficos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_estadisticas")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Guía") },
                    label = { Text("Guía", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_guia")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_perfil")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // Floating Action Button on the history screen to go write some metrics
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.selectTab(1) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_measurement")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Medidas")
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                0 -> HistorialTab(viewModel = viewModel)
                1 -> RegistrarTab(viewModel = viewModel)
                2 -> EstadisticasTab(viewModel = viewModel)
                3 -> InfoTab()
                4 -> PerfilTab(viewModel = viewModel)
            }
        }
    }
}
