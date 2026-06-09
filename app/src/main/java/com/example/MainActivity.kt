package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
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
            val photoUri by viewModel.profilePhotoUri.collectAsState()
            val isLite by viewModel.isLiteMode.collectAsState()
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (!isLite) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.toggleLiteMode(false) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("toggle_pro_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isLite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isLite) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.toggleLiteMode(true) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("toggle_lite_mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LITE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isLite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = topBarTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    // Botón redondo de Guía
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentTab == 3) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (currentTab == 3) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { viewModel.selectTab(3) }
                            .testTag("nav_top_guia"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Guía",
                            tint = if (currentTab == 3) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Botón redondo de Perfil con foto en círculo
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentTab == 4) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (currentTab == 4) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { viewModel.selectTab(4) }
                            .testTag("nav_top_perfil"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = if (currentTab == 4) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
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
