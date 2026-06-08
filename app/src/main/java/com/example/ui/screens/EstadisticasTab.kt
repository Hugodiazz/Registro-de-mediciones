package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoalEntity
import com.example.data.MeasurementEntity
import com.example.ui.MeasurementViewModel
import com.example.ui.components.*
import com.example.utils.BodyCalculator
import com.example.utils.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val measurements by viewModel.measurements.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val scrollState = rememberScrollState()

    // Goal creation local inputs linked to viewmodel
    val goalType by viewModel.goalTypeInput.collectAsState()
    val goalTarget by viewModel.goalTargetInput.collectAsState()
    var isGoalTypeDropdownExpanded by remember { mutableStateOf(false) }

    // Get advanced averages splits
    val advStats = viewModel.getAdvancedStats()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- TITLE & EXPORT HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Métricas y Metas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = {
                    PdfExporter.exportAndSharePdf(context, measurements, goals)
                },
                modifier = Modifier.testTag("export_pdf_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share, 
                    contentDescription = "Exportar PDF",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("PDF Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (measurements.isEmpty()) {
            // Emtpy State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Gráficos",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Registros Insuficientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ingresa tus primeras medidas corporales en la pestaña de registro para desbloquear el análisis avanzado y seguimiento de metas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val chronList = measurements.sortedBy { it.timestamp }
            val firstLog = chronList.first()
            val latestLog = chronList.last()

            // Calculations
            val weightChange = latestLog.weight - firstLog.weight
            val fatChange = latestLog.fatPercentage - firstLog.fatPercentage

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTrendCard(
                    title = "Evolución Peso",
                    currentValue = "${latestLog.weight} kg",
                    changeValue = "${if (weightChange >= 0) "+" else ""}${String.format("%.1f", weightChange)} kg",
                    isPositiveProgress = weightChange <= 0,
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )

                val hasFatVal = firstLog.fatPercentage > 0 && latestLog.fatPercentage > 0
                MetricTrendCard(
                    title = "Grasa Corporal",
                    currentValue = if (latestLog.fatPercentage > 0) "${String.format("%.1f", latestLog.fatPercentage)}%" else "S/R",
                    changeValue = if (hasFatVal) "${if (fatChange >= 0) "+" else ""}${String.format("%.1f", fatChange)}%" else "--",
                    isPositiveProgress = fatChange <= 0,
                    icon = Icons.Default.Percent,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- ADVANCED SPLIT WEEKLY / MONTHLY CONTRASTS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats, 
                            contentDescription = "Estadísticas avanzadas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Promedios y Tendencias Avanzadas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Weekly metrics block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Promedio de Peso Semanal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Últimos 7 días vs semana anterior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%.1f", advStats.weeklyAvgWeightCurrent)} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black
                            )
                            val wkDiff = advStats.weeklyAvgWeightCurrent - advStats.weeklyAvgWeightPrevious
                            val color = if (wkDiff <= 0.0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            Text(
                                text = "${if (wkDiff >= 0) "+" else ""}${String.format("%.1f", wkDiff)} kg",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Monthly metrics block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Promedio de Peso Mensual", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Últimos 30 días vs mes anterior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%.1f", advStats.monthlyAvgWeightCurrent)} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black
                            )
                            val mnDiff = advStats.monthlyAvgWeightCurrent - advStats.monthlyAvgWeightPrevious
                            val color = if (mnDiff <= 0.0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            Text(
                                text = "${if (mnDiff >= 0) "+" else ""}${String.format("%.1f", mnDiff)} kg",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }

            // Get user profile age/gender safely
            val profileAgeStr by viewModel.profileAge.collectAsState()
            val profileGender by viewModel.profileGender.collectAsState()
            val age = profileAgeStr.toIntOrNull() ?: 30
            val gender = if (profileGender.isNotBlank()) profileGender else latestLog.gender

            val fatP = latestLog.fatPercentage
            val fatKg = latestLog.weight * (fatP / 100.0)
            val leanKg = latestLog.weight - fatKg

            val isWideScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp > 600

            // Responsive Layout Composable
            @Composable
            fun CardGrid(
                card1: @Composable () -> Unit,
                card2: @Composable () -> Unit
            ) {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) { card1() }
                        Box(modifier = Modifier.weight(1f)) { card2() }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        card1()
                        card2()
                    }
                }
            }

            // --- BLOQUE DE COMPOSICIÓN CORPORAL AVANZADA ---
            Text(
                text = "Composición Corporal Avanzada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
            CardGrid(
                card1 = { DonutCompositionCard(leanKg = leanKg, fatKg = fatKg, modifier = Modifier.fillMaxWidth()) },
                card2 = { com.example.ui.components.FfmiTrendCard(history = measurements, modifier = Modifier.fillMaxWidth()) }
            )

            // --- BLOQUE DE SALUD METABÓLICA Y NUTRICIÓN ---
            Text(
                text = "Salud Metabólica y Requerimiento Diario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
            CardGrid(
                card1 = {
                    HealthAlertsCard(
                        waist = latestLog.waist,
                        hip = latestLog.hip,
                        height = latestLog.height,
                        age = age,
                        gender = gender,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                card2 = { NutritionMetabolicCard(leanMassKg = leanKg, modifier = Modifier.fillMaxWidth()) }
            )

            // --- BLOQUE DE SIMETRÍA Y PROPORCIONES ESTÉTICAS ---
            Text(
                text = "Simetría y Proporciones Estéticas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
            CardGrid(
                card1 = { SymmetryAnalysisCard(log = latestLog, modifier = Modifier.fillMaxWidth()) },
                card2 = {
                    val actualBicep = (if (latestLog.bicepLeft > 0) latestLog.bicepLeft else if (latestLog.bicepRight > 0) latestLog.bicepRight else latestLog.bicep)
                    val actualForearm = (if (latestLog.forearmLeft > 0) latestLog.forearmLeft else if (latestLog.forearmRight > 0) latestLog.forearmRight else 0.0)
                    val actualThigh = (if (latestLog.thighLeft > 0) latestLog.thighLeft else if (latestLog.thighRight > 0) latestLog.thighRight else latestLog.thigh)
                    val actualCalf = (if (latestLog.calfLeft > 0) latestLog.calfLeft else if (latestLog.calfRight > 0) latestLog.calfRight else latestLog.calf)
                    
                    ReevesProportionsCard(
                        height = latestLog.height,
                        chest = latestLog.chest,
                        waist = latestLog.waist,
                        hip = latestLog.hip,
                        neck = latestLog.neck,
                        bicep = actualBicep,
                        forearm = actualForearm,
                        thigh = actualThigh,
                        calf = actualCalf,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            // --- CONCURRENT LINE CHARTS PANEL (RF-03) ---
            Text(
                text = "Gráficos de Evolución Corporal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )

            // 1. Chart: Peso
            val pesoPoints = chronList.map { it.timestamp to it.weight }
            BodyLineChart(
                title = "Evolución de Peso",
                dataPoints = pesoPoints,
                valueSuffix = "kg",
                lineColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_peso")
            )

            // 2. Chart: Grasa Corporal (%)
            val grasaPoints = chronList.filter { it.fatPercentage > 0 }.map { it.timestamp to it.fatPercentage }
            BodyLineChart(
                title = "Grasa Corporal",
                dataPoints = grasaPoints,
                valueSuffix = "%",
                lineColor = Color(0xFF009688),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_grasa")
            )

            // 3. Chart: Cintura (cm)
            val cinturaPoints = chronList.filter { it.waist > 0 }.map { it.timestamp to it.waist }
            BodyLineChart(
                title = "Perímetro de Cintura",
                dataPoints = cinturaPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFFFF5722),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_cintura")
            )

            // 4. Chart: Cuello (cm)
            val cuelloPoints = chronList.filter { it.neck > 0 }.map { it.timestamp to it.neck }
            BodyLineChart(
                title = "Perímetro de Cuello",
                dataPoints = cuelloPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFF9C27B0),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_cuello")
            )

            // 5. Chart: Bíceps (cm)
            val bicepPoints = chronList.filter { it.bicep > 0 }.map { it.timestamp to it.bicep }
            BodyLineChart(
                title = "Perímetro de Bíceps",
                dataPoints = bicepPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFFE91E63),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_bicep")
            )

            // 6. Chart: Antebrazo Izquierdo (cm)
            val forearmLeftPoints = chronList.filter { it.forearmLeft > 0 }.map { it.timestamp to it.forearmLeft }
            BodyLineChart(
                title = "Perímetro de Antebrazo Izq.",
                dataPoints = forearmLeftPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFF3F51B5),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_forearm_left")
            )

            // 7. Chart: Antebrazo Derecho (cm)
            val forearmRightPoints = chronList.filter { it.forearmRight > 0 }.map { it.timestamp to it.forearmRight }
            BodyLineChart(
                title = "Perímetro de Antebrazo Der.",
                dataPoints = forearmRightPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFF2196F3),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_forearm_right")
            )

            // 8. Chart: Pecho (cm)
            val chestPoints = chronList.filter { it.chest > 0 }.map { it.timestamp to it.chest }
            BodyLineChart(
                title = "Perímetro de Pecho",
                dataPoints = chestPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFF4CAF50),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_chest")
            )

            // --- PERSONALIZED GOALS & MILESTONES TRACK ---
            Card(
                modifier = Modifier.fillMaxWidth().testTag("personalized_goals_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag, 
                            contentDescription = "Metas", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Establecer Meta Personalizada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Goal form selector and input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isGoalTypeDropdownExpanded,
                            onExpandedChange = { isGoalTypeDropdownExpanded = !isGoalTypeDropdownExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = goalType,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGoalTypeDropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                label = { Text("Métrica", fontSize = 11.sp) }
                            )

                            ExposedDropdownMenu(
                                expanded = isGoalTypeDropdownExpanded,
                                onDismissRequest = { isGoalTypeDropdownExpanded = false }
                            ) {
                                val metricsOptions = listOf("Peso (kg)", "Grasa (%)", "Cintura (cm)", "Cuello (cm)", "Bíceps (cm)", "Pecho (cm)")
                                metricsOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            viewModel.goalTypeInput.value = option
                                            isGoalTypeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = goalTarget,
                            onValueChange = { viewModel.goalTargetInput.value = it },
                            label = { Text("Valor Objetivo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("goal_target_input"),
                            placeholder = { Text("Ej: 75") },
                            singleLine = true
                        )

                        IconButton(
                            onClick = { viewModel.addNewGoal() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                .testTag("add_goal_button"),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Guardar Meta")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Active goals list tracker
                    Text(
                        text = "Tus Metas Activas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    if (goals.isEmpty()) {
                        Text(
                            text = "No has configurado ninguna meta. Define tu peso objetivo o porcentaje de grasa ideal en el formulario de arriba para visualizar tu progreso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            goals.forEach { goal ->
                                val (progressPerc, statusText) = viewModel.getGoalProgress(goal, latestLog)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents, 
                                                contentDescription = "Meta",
                                                tint = if (progressPerc >= 100) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Meta de ${goal.type}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteGoal(goal) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete, 
                                                contentDescription = "Eliminar Meta",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Descriptive metrics row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Inicial: ${String.format("%.1f", goal.startingValue)}   →   Objetivo: ${String.format("%.1f", goal.targetValue)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (progressPerc >= 100) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Visual progress indicator bar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { progressPerc / 100f },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp),
                                            color = if (progressPerc >= 100) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                        Text(
                                            text = "$progressPerc%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- CLINICAL BMI / IMC SECTOR ---
            val lastBmi = BodyCalculator.calculateBMI(latestLog.weight, latestLog.height)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tu Estado de IMC Actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = String.format("%.2f", lastBmi),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(BodyCalculator.getBMIColor(lastBmi))
                            )
                            Text(
                                text = BodyCalculator.getBMIClassification(lastBmi),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = "IMC",
                            tint = Color(BodyCalculator.getBMIColor(lastBmi)).copy(alpha = 0.8f),
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Range Bar
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        val progressFraction = ((lastBmi - 12.0) / (35.0 - 12.0)).coerceIn(0.01, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction)
                                .background(
                                    color = Color(BodyCalculator.getBMIColor(lastBmi)),
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bajo (<18.5)", fontSize = 9.sp, color = Color(0xFF03A9F4), fontWeight = FontWeight.SemiBold)
                        Text("Normal (18.5-24.9)", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                        Text("Sobrepeso (25-29.9)", fontSize = 10.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.SemiBold)
                        Text("Obeso (>=30)", fontSize = 10.sp, color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun MetricTrendCard(
    title: String,
    currentValue: String,
    changeValue: String,
    isPositiveProgress: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = currentValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val badgeColor = if (isPositiveProgress) Color(0xFF4CAF50) else Color(0xFFF44336)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color = badgeColor, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$changeValue desde inicio",
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun MetricChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
