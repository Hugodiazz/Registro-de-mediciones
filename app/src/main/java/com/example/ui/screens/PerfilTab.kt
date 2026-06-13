package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MeasurementViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PerfilTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    // Observe saved profile state from SharedPreferences (via VM flows)
    val savedName by viewModel.profileName.collectAsState()
    val savedAge by viewModel.profileAge.collectAsState()
    val savedGender by viewModel.profileGender.collectAsState()
    val savedHeight by viewModel.profileHeight.collectAsState()
    val savedPhotoUri by viewModel.profilePhotoUri.collectAsState()

    val savedGoalType by viewModel.userGoalType.collectAsState()
    val savedTargetWeight by viewModel.userTargetWeight.collectAsState()
    val savedTargetFat by viewModel.userTargetFat.collectAsState()
    val savedActivityLevel by viewModel.userActivityLevel.collectAsState()
    val isLb by viewModel.useLb.collectAsState()

    // Local form state
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("Masculino") }
    var heightInput by remember { mutableStateOf("") }
    var photoUriState by remember { mutableStateOf("") }

    var goalTypeInput by remember { mutableStateOf("") }
    var targetWeightInput by remember { mutableStateOf("") }
    var targetFatInput by remember { mutableStateOf("") }
    var activityLevelInput by remember { mutableStateOf("Moderado") }

    // Sync local state when saved state updates
    LaunchedEffect(savedName, savedAge, savedGender, savedHeight, savedPhotoUri, savedGoalType, savedTargetWeight, savedTargetFat, savedActivityLevel) {
        nameInput = savedName
        ageInput = savedAge
        genderInput = if (savedGender.isNotBlank()) savedGender else "Masculino"
        heightInput = savedHeight
        photoUriState = savedPhotoUri

        goalTypeInput = savedGoalType
        targetWeightInput = savedTargetWeight
        targetFatInput = savedTargetFat
        activityLevelInput = savedActivityLevel
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                // Grant persistable permission so it is accessible across app restarts
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            photoUriState = it.toString()
            // Proactively save photo status
            viewModel.saveProfile(nameInput, ageInput, genderInput, heightInput, it.toString())
        }
    }

    AnimatedContent(
        targetState = isEditing,
        transitionSpec = {
            if (targetState) {
                // Slide in from right, slide out to left
                (slideInHorizontally { width -> width } + fadeIn()) with
                        (slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                // Slide in from left, slide out to right
                (slideInHorizontally { width -> -width } + fadeIn()) with
                        (slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "PerfilAnimation"
    ) { editing ->
        if (editing) {
            // --- EDIT MODE: ACTUALIZAR DATOS ---
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF101415))
            ) {
                // Header (Internal top navigation bar matching the HTML header for update page)
                Surface(
                    color = Color(0xFF191C1E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Actualizar Datos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF) // clinical cyan text
                        )
                        Spacer(modifier = Modifier.size(40.dp)) // Spacer to keep title centered
                    }
                }

                // Edit Form fields Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Photo Portrait Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF0b0f10))
                                    .border(2.dp, Color(0xFF00F0FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUriState.isNotBlank()) {
                                    AsyncImage(
                                        model = photoUriState,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color(0xFFb9cacb),
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F0FF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Cambiar Foto",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "IDENTIDAD DE ATLETA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFb9cacb),
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Datos de Identidad Card
                    Surface(
                        color = Color(0xFF1D2022), // bento-card
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF3B494B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Datos de Identidad",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                letterSpacing = 1.sp
                            )

                            // Nombre Completo Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Nombre Completo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFb9cacb)
                                )
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    placeholder = { Text("Nombre completo", color = Color(0xFFb9cacb).copy(alpha = 0.4f)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = Color(0xFFb9cacb)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00F0FF),
                                        unfocusedBorderColor = Color(0xFF3B494B),
                                        focusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_name_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Edad Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Edad",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFb9cacb)
                                )
                                OutlinedTextField(
                                    value = ageInput,
                                    onValueChange = { ageInput = it },
                                    placeholder = { Text("Tu edad", color = Color(0xFFb9cacb).copy(alpha = 0.4f)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFFb9cacb)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00F0FF),
                                        unfocusedBorderColor = Color(0xFF3B494B),
                                        focusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_age_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            // Género Choice Row
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Sexo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFb9cacb)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFF323537).copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(1.dp, Color(0xFF3B494B), RoundedCornerShape(8.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isMale = genderInput == "Masculino"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isMale) Color(0xFF00F0FF) else Color.Transparent)
                                            .clickable { genderInput = "Masculino" }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Masculino",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMale) Color.Black else Color(0xFFb9cacb)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (!isMale) Color(0xFF00F0FF) else Color.Transparent)
                                            .clickable { genderInput = "Femenino" }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Femenino",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isMale) Color.Black else Color(0xFFb9cacb)
                                        )
                                    }
                                }
                            }

                            // Altura Field
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Altura (cm)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFb9cacb)
                                )
                                OutlinedTextField(
                                    value = heightInput,
                                    onValueChange = { heightInput = it },
                                    placeholder = { Text("175", color = Color(0xFFb9cacb).copy(alpha = 0.4f)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Straighten,
                                            contentDescription = null,
                                            tint = Color(0xFFb9cacb)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00F0FF),
                                        unfocusedBorderColor = Color(0xFF3B494B),
                                        focusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f),
                                        unfocusedContainerColor = Color(0xFF323537).copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("profile_height_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                // Sticky Action Guardians Footer
                Surface(
                    color = Color(0xFF0b0f10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val heightDouble = heightInput.toDoubleOrNull()
                                val ageInt = ageInput.toIntOrNull()

                                val finalName = nameInput.trim().ifBlank { "Julian Alvarez" }
                                val finalAge = if (ageInput.isNotBlank() && (ageInt == null || ageInt <= 0)) "24" else ageInput.trim()
                                val finalHeight = if (heightInput.isNotBlank() && (heightDouble == null || heightDouble <= 0.0)) "170" else heightInput.trim()

                                viewModel.saveProfile(
                                    finalName,
                                    finalAge,
                                    genderInput,
                                    finalHeight,
                                    photoUriState
                                )
                                isEditing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("profile_save_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00F0FF) // neon cyan
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar",
                                    tint = Color.Black
                                )
                                Text(
                                    text = "Guardar Cambios",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- VIEW MODE: MI PERFIL VIEW ---
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF101415))
            ) {
                // Return Arrow Top Bar
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.goBackFromProfile() },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Perfil",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF) // clinical cyan text
                        )
                        Spacer(modifier = Modifier.size(40.dp)) // Spacer to keep title centered
                    }
                }

                // Profile Scrollable Body Layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Left Column style card: Avatar Container Section
                    Surface(
                        color = Color.Transparent, // card-surface
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.dp, Color(0xFF323537)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Avatar section items vertical Column
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0b0f10))
                                        .border(0.dp, Color(0xFF323537), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (savedPhotoUri.isNotBlank()) {
                                        AsyncImage(
                                            model = savedPhotoUri,
                                            contentDescription = "Avatar de Carlos",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Avatar de Carlos",
                                            tint = Color(0xFFb9cacb),
                                            modifier = Modifier.size(90.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = savedName.ifBlank { "SIN NOMBRE" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Right Column layout: Biometric Data KPIs Grid row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card: EDAD
                        BiometricKpiCard(
                            label = "EDAD",
                            icon = Icons.Default.Cake,
                            valueStr = savedAge.ifBlank { "24" },
                            unitStr = "años",
                            modifier = Modifier.weight(1f)
                        )

                        // Card: GENERO
                        BiometricKpiCard(
                            label = "SEXO",
                            icon = Icons.Default.Person,
                            valueStr = savedGender.ifBlank { "Masculino" },
                            unitStr = "",
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    // Card Height Row (Col-span 2 style)
                    Surface(
                        color = Color(0xFF191C1E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF323537)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ALTURA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFb9cacb),
                                        letterSpacing = 1.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = savedHeight.ifBlank { "160" },
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "cm",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFb9cacb),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Identity save link trigger card:
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("go_to_update_data_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF) // cyan neon container
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Actualizar",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Actualizar Datos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // --- HOJA DE RUTA Y METAS CARD ---
                    Surface(
                        color = Color(0xFF191C1E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF323537)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "HOJA DE RUTA Y METAS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFb9cacb),
                                    letterSpacing = 1.sp
                                )
                            }

                            // 1. Goal selection options
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Tipo de Enfoque Principal (Meta)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFb9cacb)
                                )

                                val isGoalLose = goalTypeInput == "Reducción de Peso / Pérdida de Grasa" || goalTypeInput.isBlank()
                                // Loss of Weight Choice Card
                                Surface(
                                    onClick = { goalTypeInput = "Reducción de Peso / Pérdida de Grasa" },
                                    color = if (isGoalLose) Color(0xFF00F0FF).copy(alpha = 0.05f) else Color.Transparent,
                                    border = BorderStroke(
                                        width = if (isGoalLose) 2.dp else 1.dp,
                                        color = if (isGoalLose) Color(0xFF00F0FF) else Color(0xFF323537)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isGoalLose) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color(0xFF272a2c),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (isGoalLose) Color(0xFF00F0FF) else Color(0xFFb9cacb),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Pérdida de Grasa",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Enfoque en definición, salud metabólica y control de riesgos",
                                                fontSize = 11.sp,
                                                color = Color(0xFFb9cacb),
                                                lineHeight = 15.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Muscles gaining choice card
                                Surface(
                                    onClick = { goalTypeInput = "Ganancia de Masa Muscular" },
                                    color = if (!isGoalLose) Color(0xFF00F0FF).copy(alpha = 0.05f) else Color.Transparent,
                                    border = BorderStroke(
                                        width = if (!isGoalLose) 2.dp else 1.dp,
                                        color = if (!isGoalLose) Color(0xFF00F0FF) else Color(0xFF323537)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (!isGoalLose) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color(0xFF272a2c),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = if (!isGoalLose) Color(0xFF00F0FF) else Color(0xFFb9cacb),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Aumento de Peso / Ganancia de Masa Muscular",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Enfoque en hipertrofia y desarrollo estético",
                                                fontSize = 11.sp,
                                                color = Color(0xFFb9cacb),
                                                lineHeight = 15.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Weight and Fat objective numerical rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Column Weight Obj
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Peso Objetivo (${if (isLb) "lbs" else "kg"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFb9cacb)
                                    )
                                    OutlinedTextField(
                                        value = targetWeightInput,
                                        onValueChange = { targetWeightInput = it },
                                        placeholder = { Text("--", color = Color(0xFFb9cacb).copy(alpha = 0.4f)) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = Color(0xFF00F0FF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF00F0FF),
                                            unfocusedBorderColor = Color(0xFF323537),
                                            focusedContainerColor = Color(0xFF101415),
                                            unfocusedContainerColor = Color(0xFF101415)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("goal_weight_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }

                                // Column Fat Obj
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "% Grasa Obj.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFb9cacb)
                                    )
                                    OutlinedTextField(
                                        value = targetFatInput,
                                        onValueChange = { targetFatInput = it },
                                        placeholder = { Text("--", color = Color(0xFFb9cacb).copy(alpha = 0.4f)) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Percent,
                                                contentDescription = null,
                                                tint = Color(0xFF00F0FF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF00F0FF),
                                            unfocusedBorderColor = Color(0xFF323537),
                                            focusedContainerColor = Color(0xFF101415),
                                            unfocusedContainerColor = Color(0xFF101415)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("goal_fat_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }

                            // 3. GETD Nivel de Actividad selector
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Nivel de Actividad Física (GETD)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFb9cacb)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF101415), shape = RoundedCornerShape(24.dp))
                                        .border(1.dp, Color(0xFF323537), RoundedCornerShape(24.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    val levels = listOf("Sedentario", "Ligero", "Moderado", "Intenso")
                                    levels.forEach { level ->
                                        val isSelected = activityLevelInput == level
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) Color(0xFF00F0FF) else Color.Transparent)
                                                .clickable { activityLevelInput = level }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = level,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else Color(0xFFb9cacb)
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Save Roadmap button
                            Button(
                                onClick = {
                                    viewModel.saveGoalSettings(
                                        goalTypeInput,
                                        targetWeightInput.trim(),
                                        targetFatInput.trim(),
                                        activityLevelInput
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("goal_save_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00F0FF) // cyan neon container
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsRun,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Guardar Objetivo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricKpiCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueStr: String,
    unitStr: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF191C1E), // card-surface
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF323537)),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFb9cacb),
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = valueStr,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (unitStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unitStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFb9cacb),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}
