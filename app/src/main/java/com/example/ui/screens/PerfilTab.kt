package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MeasurementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Observe saved profile state from SharedPreferences (via VM flows)
    val savedName by viewModel.profileName.collectAsState()
    val savedAge by viewModel.profileAge.collectAsState()
    val savedGender by viewModel.profileGender.collectAsState()
    val savedHeight by viewModel.profileHeight.collectAsState()
    val savedPhotoUri by viewModel.profilePhotoUri.collectAsState()

    // Local form state
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("Masculino") }
    var heightInput by remember { mutableStateOf("") }
    var photoUriState by remember { mutableStateOf("") }

    // Sync local state when saved state updates
    LaunchedEffect(savedName, savedAge, savedGender, savedHeight, savedPhotoUri) {
        nameInput = savedName
        ageInput = savedAge
        genderInput = if (savedGender.isNotBlank()) savedGender else "Masculino"
        heightInput = savedHeight
        photoUriState = savedPhotoUri
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
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- PHOTO PORTRAIT SECTION ---
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(130.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photoUriState.isNotBlank()) {
                    AsyncImage(
                        model = photoUriState,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .testTag("profile_photo_display")
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin foto de perfil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(75.dp)
                    )
                }
            }

            // Edit Photo Trigger Button
            IconButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .testTag("profile_photo_picker")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar Foto",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Basic Info Summary Badge
        if (savedName.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = savedName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${if (savedAge.isNotBlank()) "$savedAge años • " else ""}$savedHeight cm • $savedGender",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            Text(
                text = "Completa tu perfil para personalizar la experiencia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        // --- PROFILE EDIT FIELDS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar Datos Personales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Name complete field
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                    placeholder = { Text("Ej. Juan Pérez") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Age and height row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Edad") },
                        leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null) },
                        placeholder = { Text("Ej. 25") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_age_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Estatura (cm)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Height, contentDescription = null) },
                        placeholder = { Text("Ej. 175") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_height_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Gender Selection Label
                Column {
                    Text(
                        text = "Sexo / Género",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileGenderChoice(
                            text = "Masculino",
                            icon = Icons.Default.Male,
                            isSelected = genderInput == "Masculino",
                            onClick = { genderInput = "Masculino" },
                            modifier = Modifier.weight(1f)
                        )
                        ProfileGenderChoice(
                            text = "Femenino",
                            icon = Icons.Default.Female,
                            isSelected = genderInput == "Femenino",
                            onClick = { genderInput = "Femenino" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- ACTION SAVE BUTTON ---
        Button(
            onClick = {
                val heightDouble = heightInput.toDoubleOrNull()
                val ageInt = ageInput.toIntOrNull()

                if (nameInput.isBlank()) {
                    viewModel.saveProfile("", "", "", "", "")
                    return@Button
                }

                if (ageInput.isNotBlank() && (ageInt == null || ageInt <= 0)) {
                    // Friendly validation, not crash
                    viewModel.saveProfile(nameInput, "", genderInput, heightInput, photoUriState)
                } else if (heightInput.isNotBlank() && (heightDouble == null || heightDouble <= 0.0)) {
                    viewModel.saveProfile(nameInput, ageInput, genderInput, "", photoUriState)
                } else {
                    viewModel.saveProfile(
                        nameInput.trim(),
                        ageInput.trim(),
                        genderInput,
                        heightInput.trim(),
                        photoUriState
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_save_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guardar Perfil",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ProfileGenderChoice(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val strokeBorder = if (isSelected) {
        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(46.dp)
            .testTag("gender_choice_$text"),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = contentColor,
        border = strokeBorder
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
