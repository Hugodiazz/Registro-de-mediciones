package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
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
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun RegistrarTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val isLite by viewModel.isLiteMode.collectAsState()
    if (isLite) {
        LiteRegistrarScreen(viewModel = viewModel, modifier = modifier)
    } else {
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
        val forearm by viewModel.forearmInput.collectAsState()
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
                    text = "Métrica",
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
                            value = forearm,
                            onValueChange = { viewModel.forearmInput.value = it },
                            label = "Antebrazo",
                            suffix = "cm",
                            icon = Icons.Default.TrendingFlat,
                            modifier = Modifier.weight(1f).testTag("forearm_field")
                        )
                        Spacer(modifier = Modifier.weight(1f))
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

@Composable
fun LiteRegistrarScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val weight by viewModel.weightInput.collectAsState()
    val notes by viewModel.notesInput.collectAsState()

    var showCommentDialog by remember { mutableStateOf(false) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val onKeyClick: (String) -> Unit = { char ->
        val current = viewModel.weightInput.value
        if (current.length < 5) {
            if (char == ".") {
                if (!current.contains(".")) {
                    viewModel.weightInput.value = if (current.isEmpty()) "0." else "$current."
                }
            } else {
                viewModel.weightInput.value = current + char
            }
        }
    }

    val onDeleteClick: () -> Unit = {
        val current = viewModel.weightInput.value
        if (current.isNotEmpty()) {
            viewModel.weightInput.value = current.dropLast(1)
        }
    }

    if (showCommentDialog) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Comentario o Notas") },
            text = {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.notesInput.value = it },
                    label = { Text("Escribe un comentario") },
                    placeholder = { Text("Ej. Ayunas, después de entrenar...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(onClick = { showCommentDialog = false }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Central Area where the circle is centered. Squeezes gracefully when keyboard appears
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // --- DIAL CIRCLE HOVERING METRIC ---
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .clickable { isKeyboardVisible = !isKeyboardVisible }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = (size.minDimension / 2f) - 10f

                        // Draw standard surrounding guidance rings from mockup
                        drawCircle(
                            color = Color(0x1F7C4DFF),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Little circular dots matching high fidelity mockup colors
                        drawCircle(
                            color = Color(0xFF00E5FF), // Cyan dot
                            radius = 4.dp.toPx(),
                            center = Offset(
                                x = centerX + radius * cos(Math.toRadians(210.0)).toFloat(),
                                y = centerY + radius * sin(Math.toRadians(210.0)).toFloat()
                            )
                        )

                        drawCircle(
                            color = Color(0xFFFF4081), // Pinkish dot
                            radius = 3.5.dp.toPx(),
                            center = Offset(
                                x = centerX + radius * cos(Math.toRadians(310.0)).toFloat(),
                                y = centerY + radius * sin(Math.toRadians(310.0)).toFloat()
                            )
                        )

                        drawCircle(
                            color = Color(0xFFFF6D00), // Orange dot
                            radius = 4.dp.toPx(),
                            center = Offset(
                                x = centerX + radius * cos(Math.toRadians(45.0)).toFloat(),
                                y = centerY + radius * sin(Math.toRadians(45.0)).toFloat()
                            )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Peso Actual",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        val displayNum = if (weight.isNotBlank()) weight else "Ingresar"
                        Text(
                            text = displayNum,
                            style = if (weight.isNotBlank()) MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                                     else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("lite_dial_weight_value")
                        )

                        if (weight.isNotBlank()) {
                            Text(
                                text = "kg",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Toca para registrar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Keyboard/Action buttons section at the bottom
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- ACTION BUTTONS (Only Notes/Comments, no photo) ---
                if (!isKeyboardVisible) {
                    Button(
                        onClick = { showCommentDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("lite_comment_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = "Nota")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (notes.isBlank()) "Agregar Nota" else "Editar Nota", fontWeight = FontWeight.Bold)
                    }
                }

                // --- CUSTOM KEYBOARD FOR FLUID EMULATOR TYPING (Only visible when circle clicked) ---
                AnimatedVisibility(
                    visible = isKeyboardVisible,
                    enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick option to view/edit note right above keyboard
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (notes.isNotBlank()) "Nota: $notes" else "Sin notas registradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { showCommentDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (notes.isBlank()) "Añadir nota" else "Editar nota",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val keyRows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf(".", "0", "BACKSPACE")
                        )

                        val alphabets = mapOf(
                            "2" to "ABC", "3" to "DEF",
                            "4" to "GHI", "5" to "JKL", "6" to "MNO",
                            "7" to "PQRS", "8" to "TUV", "9" to "WXYZ"
                        )

                        keyRows.forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                            .clickable {
                                                if (key == "BACKSPACE") onDeleteClick() else onKeyClick(key)
                                            }
                                            .testTag("kb_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (key == "BACKSPACE") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Borrar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                val subText = alphabets[key] ?: ""
                                                if (subText.isNotEmpty()) {
                                                    Text(
                                                        text = subText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                        fontSize = 9.sp
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

                // --- DONE BUTTON ---
                Button(
                    onClick = { 
                        if (weight.isBlank() || weight.toDoubleOrNull() == null || weight.toDoubleOrNull()!! <= 0) {
                            viewModel.showUiMessage("Por favor ingresa un peso válido mayor a 0.")
                        } else {
                            viewModel.saveMeasurement() 
                            isKeyboardVisible = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("lite_done_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("Registrar Peso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
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
