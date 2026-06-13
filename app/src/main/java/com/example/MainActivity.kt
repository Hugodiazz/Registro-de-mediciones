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
import com.example.ui.screens.OnboardingScreen
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
    val profileAge by viewModel.profileAge.collectAsState()

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

    if (profileAge.isBlank()) {
        OnboardingScreen(viewModel = viewModel)
    } else {
        val topBarTitle = when (currentTab) {
            0 -> "Métrica"
            1 -> "Métrica"
            2 -> "Métrica"
            3 -> "Ayuda Médica y Guía"
            4 -> "Métrica"
            else -> "Métrica"
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentTab != 4) {
                val photoUri by viewModel.profilePhotoUri.collectAsState()
                val isLite by viewModel.isLiteMode.collectAsState()
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (currentTab != 0 && currentTab != 4) {
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
                        if (currentTab != 0 && currentTab != 4) {
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
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (currentTab != 4) {
                // Sleek bottom navigation mimicking the HTML design
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    color = Color(0xFF1D2123), // bg-surface-container in HTML
                    border = androidx.compose.foundation.BorderStroke(width = 0.5.dp, color = Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Historial
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectTab(0) }
                                .testTag("nav_tab_historial"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentTab == 0) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(horizontal = 22.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Historial",
                                    tint = if (currentTab == 0) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "HISTORIAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == 0) Color.White else Color(0xFF94A3B8).copy(alpha = 0.6f)
                            )
                        }

                        // Registrar (Middle item ensuring we can still easily register new metrics)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectTab(1) }
                                .testTag("nav_tab_registrar"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentTab == 1) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(horizontal = 22.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddBox,
                                    contentDescription = "Registrar",
                                    tint = if (currentTab == 1) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "REGISTRAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == 1) Color.White else Color(0xFF94A3B8).copy(alpha = 0.6f)
                            )
                        }

                        // Estadísticas (formerly Análisis)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectTab(2) }
                                .testTag("nav_tab_estadisticas"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (currentTab == 2) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                                    .padding(horizontal = 22.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = "Estadísticas",
                                    tint = if (currentTab == 2) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "ESTADÍSTICAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == 2) Color.White else Color(0xFF94A3B8).copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {}
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
}
