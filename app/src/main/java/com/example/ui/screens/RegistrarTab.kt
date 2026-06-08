package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MeasurementViewModel
import com.example.utils.BodyCalculator

@Composable
fun RegistrarTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Form inputs observed as Compose state
    val weight by viewModel.weightInput.collectAsState()
    val height by viewModel.heightInput.collectAsState()
    val gender by viewModel.genderInput.collectAsState()

    val isManualFat by viewModel.isFatManual.collectAsState()
    val manualFat by viewModel.manualFatInput.collectAsState()

    val neck by viewModel.neckInput.collectAsState()
    val waist by viewModel.waistInput.collectAsState()
    val hip by viewModel.hipInput.collectAsState()

    // Optional measurements
    val chest by viewModel.chestInput.collectAsState()
    val bicep by viewModel.bicepInput.collectAsState()
    val forearmLeft by viewModel.forearmLeftInput.collectAsState()
    val forearmRight by viewModel.forearmRightInput.collectAsState()
    val thigh by viewModel.thighInput.collectAsState()
    val calf by viewModel.calfInput.collectAsState()

    val notes by viewModel.notesInput.collectAsState()

    // Dynamic Live Calculations
    val liveBmi by viewModel.calculatedBmi.collectAsState()
    val liveNavyFat by viewModel.calculatedBodyFat.collectAsState()

    var showOptionalByTape by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nueva Medición",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Button to prefill variables
            TextButton(
                onClick = { viewModel.prefillFromLastLog() },
                modifier = Modifier.testTag("prefill_button")
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = "Precompletar")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Prellenar con anterior", fontWeight = FontWeight.Bold)
            }
        }

        // --- REAL-TIME CALCULATION PREVIEW CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Resultados Estimados (En tiempo real)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // IMC Live View
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IMC (Índice Masa)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (liveBmi > 0) String.format("%.1f", liveBmi) else "--",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (liveBmi > 0) {
                            val c = BodyCalculator.getBMIClassification(liveBmi)
                            Text(
                                text = c,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(BodyCalculator.getBMIColor(liveBmi))
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(55.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Body Fat % Live View
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grasa Estimada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )

                        val finalFatPercent = if (isManualFat) {
                            manualFat.toDoubleOrNull() ?: 0.0
                        } else {
                            liveNavyFat
                        }

                        Text(
                            text = if (finalFatPercent > 0) "${String.format("%.1f", finalFatPercent)}%" else "--",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (finalFatPercent > 0) {
                            val c = BodyCalculator.getBodyFatClassification(finalFatPercent, gender)
                            Text(
                                text = c,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(BodyCalculator.getBodyFatColor(finalFatPercent, gender))
                            )
                        } else {
                            Text(
                                text = if (isManualFat) "Basado en ingreso manual" else "Pendiente de medidas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // --- CORE PARAMETERS SECTION (GENDER, HEIGHT, WEIGHT) ---
        Text(
            text = "Datos Básicos",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Gender segmented select
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenderButton(
                label = "Masculino",
                icon = Icons.Default.Male,
                isSelected = gender == "Masculino",
                onClick = { viewModel.genderInput.value = "Masculino" },
                modifier = Modifier.weight(1f).testTag("gender_male_button")
            )
            GenderButton(
                label = "Femenino",
                icon = Icons.Default.Female,
                isSelected = gender == "Femenino",
                onClick = { viewModel.genderInput.value = "Femenino" },
                modifier = Modifier.weight(1f).testTag("gender_female_button")
            )
        }

        // Weight & Height fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTextFieldWithSuffix(
                value = weight,
                onValueChange = { viewModel.weightInput.value = it },
                label = "Peso",
                suffix = "kg",
                icon = Icons.Default.MonitorWeight,
                modifier = Modifier.weight(1f).testTag("weight_field")
            )

            FilledTextFieldWithSuffix(
                value = height,
                onValueChange = { viewModel.heightInput.value = it },
                label = "Altura",
                suffix = "cm",
                icon = Icons.Default.Height,
                modifier = Modifier.weight(1f).testTag("height_field")
            )
        }

        // --- BODY FAT INPUT DECISION ---
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "¿Cómo registrarás tu % de grasa?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceButton(
                text = "Manual (Teclear %)",
                isSelected = isManualFat,
                onClick = { viewModel.isFatManual.value = true },
                modifier = Modifier.weight(1f).testTag("manual_fat_choice")
            )
            ChoiceButton(
                text = "Calculadora Marina",
                isSelected = !isManualFat,
                onClick = { viewModel.isFatManual.value = false },
                modifier = Modifier.weight(1f).testTag("calc_fat_choice")
            )
        }

        // Render inputs depending on selected choice
        AnimatedContent(
            targetState = isManualFat,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "fat_input_animation"
        ) { manual ->
            if (manual) {
                // Manual block
                FilledTextFieldWithSuffix(
                    value = manualFat,
                    onValueChange = { viewModel.manualFatInput.value = it },
                    label = "Porcentaje de grasa",
                    suffix = "%",
                    icon = Icons.Default.Percent,
                    modifier = Modifier.fillMaxWidth().testTag("manual_fat_field")
                )
            } else {
                // Navy Calculator fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Medidas requeridas para el cálculo (Método Marina EE.UU.)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTextFieldWithSuffix(
                            value = neck,
                            onValueChange = { viewModel.neckInput.value = it },
                            label = "Cuello",
                            suffix = "cm",
                            icon = Icons.Default.TrendingFlat,
                            modifier = Modifier.weight(1f).testTag("neck_field")
                        )

                        FilledTextFieldWithSuffix(
                            value = waist,
                            onValueChange = { viewModel.waistInput.value = it },
                            label = "Cintura",
                            suffix = "cm",
                            icon = Icons.Default.TrendingFlat,
                            modifier = Modifier.weight(1f).testTag("waist_field")
                        )
                    }

                    // Female requires Hip
                    if (gender == "Femenino") {
                        FilledTextFieldWithSuffix(
                            value = hip,
                            onValueChange = { viewModel.hipInput.value = it },
                            label = "Cadera (Requerido mujeres)",
                            suffix = "cm",
                            icon = Icons.Default.TrendingFlat,
                            modifier = Modifier.fillMaxWidth().testTag("hip_field")
                        )
                    }
                }
            }
        }

        // --- OPTIONAL EXTRA BODY PARTS RECORDING ---
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showOptionalByTape = !showOptionalByTape }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsGymnastics,
                    contentDescription = "Medidas",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar otras medidas (Opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = if (showOptionalByTape) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle"
            )
        }

        AnimatedVisibility(
            visible = showOptionalByTape,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTextFieldWithSuffix(
                        value = chest,
                        onValueChange = { viewModel.chestInput.value = it },
                        label = "Pecho",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("chest_field")
                    )

                    FilledTextFieldWithSuffix(
                        value = bicep,
                        onValueChange = { viewModel.bicepInput.value = it },
                        label = "Brazo/Bíceps",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("bicep_field")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTextFieldWithSuffix(
                        value = forearmLeft,
                        onValueChange = { viewModel.forearmLeftInput.value = it },
                        label = "Antebr. Izq.",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("forearm_left_field")
                    )

                    FilledTextFieldWithSuffix(
                        value = forearmRight,
                        onValueChange = { viewModel.forearmRightInput.value = it },
                        label = "Antebr. Der.",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("forearm_right_field")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTextFieldWithSuffix(
                        value = thigh,
                        onValueChange = { viewModel.thighInput.value = it },
                        label = "Muslo",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("thigh_field")
                    )

                    FilledTextFieldWithSuffix(
                        value = calf,
                        onValueChange = { viewModel.calfInput.value = it },
                        label = "Pantorrilla",
                        suffix = "cm",
                        icon = Icons.Default.TrendingFlat,
                        modifier = Modifier.weight(1f).testTag("calf_field")
                    )
                }
            }
        }

        // --- NOTES / COMMENTS SECTION ---
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        OutlinedTextField(
            value = notes,
            onValueChange = { viewModel.notesInput.value = it },
            label = { Text("Comentarios o notas de la sesión") },
            placeholder = { Text("Ej. Medición en ayunas, después de entrenar, etc.") },
            modifier = Modifier.fillMaxWidth().testTag("notes_field"),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        // --- SUBMIT SAVE BUTTON ---
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.saveMeasurement() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_measurement_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Guardar")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guardar Medición",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(70.dp)) // Padding for bottom navbar safe scroll
    }
}

@Composable
fun GenderButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val tintColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tintColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = tintColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChoiceButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(38.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilledTextFieldWithSuffix(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp)) },
        trailingIcon = { Text(suffix, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(end = 6.dp)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}
