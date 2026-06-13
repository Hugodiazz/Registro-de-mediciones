package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MeasurementViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    var ageInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("170") }
    var genderInput by remember { mutableStateOf("Masculino") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Card Container
            Surface(
                color = Color(0xFF1D2123),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.2f)),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .testTag("onboarding_card")
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    
                    // Welcome Header Title and Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "¡Bienvenido a Métrica!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Para ofrecerte una experiencia personalizada y obtener mejores resultados, necesitamos conocerte un poco más.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB9CACB),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Input Form
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Gender Selector (Horizontal Choice)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Sexo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                letterSpacing = 0.5.sp
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
                                        .padding(vertical = 12.dp)
                                        .testTag("onboarding_gender_male"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Masculino",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMale) Color.Black else Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isMale) Color(0xFF00F0FF) else Color.Transparent)
                                        .clickable { genderInput = "Femenino" }
                                        .padding(vertical = 12.dp)
                                        .testTag("onboarding_gender_female"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Femenino",
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isMale) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        // Age input
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Edad (años)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = ageInput,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() }) {
                                        ageInput = it
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("Ej: 25", color = Color(0xFFB9CACB).copy(alpha = 0.4f)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFB9CACB)
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
                                    .testTag("onboarding_age_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Height input
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Altura (cm)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF),
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() || char == '.' }) {
                                        heightInput = it
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("Ej: 175", color = Color(0xFFB9CACB).copy(alpha = 0.4f)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Height,
                                        contentDescription = null,
                                        tint = Color(0xFFB9CACB)
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
                                    .testTag("onboarding_height_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Validation error banner with smooth transitions
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Submission CTA Button
                    Button(
                        onClick = {
                            val ageStr = ageInput.trim()
                            val heightStr = heightInput.trim()

                            // Validations
                            val ageParsed = ageStr.toIntOrNull()
                            if (ageParsed == null || ageParsed <= 0 || ageParsed > 120) {
                                errorMessage = "Por favor ingresa una edad válida entre 1 y 120 años."
                                return@Button
                            }

                            val heightParsed = heightStr.toDoubleOrNull()
                            if (heightParsed == null || heightParsed < 50.0 || heightParsed > 280.0) {
                                errorMessage = "Por favor ingresa una altura válida entre 50 y 280 cm."
                                return@Button
                            }

                            // If valid, save the profile and onboarding successfully resets flow
                            viewModel.saveProfile(
                                name = "Usuario",
                                age = ageStr,
                                gender = genderInput,
                                height = heightStr,
                                photoUri = ""
                            )
                            viewModel.showUiMessage("¡Datos de bienvenida configurados!")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_submit_button")
                    ) {
                        Text(
                            text = "Guardar y Continuar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
