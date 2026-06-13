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
import androidx.compose.ui.draw.clip
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val isLite by viewModel.isLiteMode.collectAsState()
    if (isLite) {
        LiteEstadisticasScreen(viewModel = viewModel, modifier = modifier)
    } else {
        val context = LocalContext.current
        val measurementsRaw by viewModel.measurements.collectAsState()
        val measurements = remember(measurementsRaw) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            measurementsRaw
                .groupBy { sdf.format(java.util.Date(it.timestamp)) }
                .map { (_, list) -> list.maxByOrNull { it.timestamp }!! }
                .sortedByDescending { it.timestamp }
        }
        val goals by viewModel.goals.collectAsState()
        val userGoalType by viewModel.userGoalType.collectAsState()
        val userTargetWeight by viewModel.userTargetWeight.collectAsState()
        val userTargetFat by viewModel.userTargetFat.collectAsState()
        val userActivityLevel by viewModel.userActivityLevel.collectAsState()
        val isLb by viewModel.useLb.collectAsState()
        
        val scrollState = rememberScrollState()

        // Goal creation local inputs linked to viewmodel
        val goalType by viewModel.goalTypeInput.collectAsState()
        val goalTarget by viewModel.goalTargetInput.collectAsState()
        var isGoalTypeDropdownExpanded by remember { mutableStateOf(false) }

        // Get advanced averages splits
        val advStats = viewModel.getAdvancedStats(measurements)

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
            val isWeightLoss = userGoalType == "Reducción de Peso / Pérdida de Grasa"
            val unit = if (isLb) "lbs" else "kg"
            val convMult = if (isLb) 2.20462 else 1.0

            val displayFirstWeight = firstLog.weight * convMult
            val displayLatestWeight = latestLog.weight * convMult
            val weightChange = displayLatestWeight - displayFirstWeight
            val fatChange = latestLog.fatPercentage - firstLog.fatPercentage

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTrendCard(
                    title = "Evolución Peso",
                    currentValue = String.format(Locale.US, "%.1f %s", displayLatestWeight, unit),
                    changeValue = "${if (weightChange >= 0) "+" else ""}${String.format(Locale.US, "%.1f", weightChange)} $unit",
                    isPositiveProgress = if (isWeightLoss) weightChange <= 0 else weightChange >= 0,
                    icon = if (isWeightLoss) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
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

                    val displayWeeklyAvgWeightCurrent = advStats.weeklyAvgWeightCurrent * convMult
                    val displayWeeklyAvgWeightPrevious = advStats.weeklyAvgWeightPrevious * convMult
                    val wkDiff = displayWeeklyAvgWeightCurrent - displayWeeklyAvgWeightPrevious

                    val displayMonthlyAvgWeightCurrent = advStats.monthlyAvgWeightCurrent * convMult
                    val displayMonthlyAvgWeightPrevious = advStats.monthlyAvgWeightPrevious * convMult
                    val mnDiff = displayMonthlyAvgWeightCurrent - displayMonthlyAvgWeightPrevious

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
                                text = String.format(Locale.US, "%.1f %s", displayWeeklyAvgWeightCurrent, unit),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black
                            )
                            val wkDiffColor = if (if (isWeightLoss) wkDiff <= 0.0 else wkDiff >= 0.0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            Text(
                                text = "${if (wkDiff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", wkDiff)} $unit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = wkDiffColor
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
                                text = String.format(Locale.US, "%.1f %s", displayMonthlyAvgWeightCurrent, unit),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black
                            )
                            val mnDiffColor = if (if (isWeightLoss) mnDiff <= 0.0 else mnDiff >= 0.0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            Text(
                                text = "${if (mnDiff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", mnDiff)} $unit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = mnDiffColor
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

            // Block Definitions for Reordering (RF-06 & RF-07)
            val blockHealthNutrition = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        card2 = {
                            NutritionMetabolicCard(
                                leanMassKg = leanKg,
                                userGoalType = userGoalType,
                                userActivityLevel = userActivityLevel,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }

            val blockComposition = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                }
            }

            // Goal-based adaptive layout order block placement
            if (isWeightLoss) {
                // REDUCCIÓN DE PESO / PÉRDIDA DE GRASA
                blockHealthNutrition()
                blockComposition()
            } else {
                // AUMENTO DE MASA MUSCULAR / HIPERTROFIA
                blockComposition()
                blockHealthNutrition()
            }

            // --- CONCURRENT LINE CHARTS PANEL (RF-03) ---
            Text(
                text = "Gráficos de Evolución Corporal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )

            // 1. Chart: Peso
            val displayWeightPoints = if (isLb) {
                chronList.map { it.timestamp to it.weight * 2.20462 }
            } else {
                chronList.map { it.timestamp to it.weight }
            }
            val weightGoalVal = userTargetWeight.toDoubleOrNull()
            val weightGoalDir = if (isWeightLoss) "downward" else "upward"

            BodyLineChart(
                title = "Evolución de Peso",
                dataPoints = displayWeightPoints,
                valueSuffix = unit,
                lineColor = MaterialTheme.colorScheme.primary,
                goalValue = weightGoalVal,
                goalDirection = weightGoalDir,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_peso")
            )

            // 2. Chart: Grasa Corporal (%)
            val fatGoalVal = userTargetFat.toDoubleOrNull()
            
            val grasaPoints = chronList.filter { it.fatPercentage > 0 }.map { it.timestamp to it.fatPercentage }
            BodyLineChart(
                title = "Grasa Corporal",
                dataPoints = grasaPoints,
                valueSuffix = "%",
                lineColor = Color(0xFF009688),
                goalValue = fatGoalVal,
                goalDirection = "downward",
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

            // 6. Chart: Antebrazo (cm)
            val forearmPoints = chronList.filter { it.forearm > 0 }.map { it.timestamp to it.forearm }
            BodyLineChart(
                title = "Perímetro de Antebrazo",
                dataPoints = forearmPoints,
                valueSuffix = "cm",
                lineColor = Color(0xFF3F51B5),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_forearm")
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
                            imageVector = Icons.Default.EmojiEvents, 
                            contentDescription = "Metas", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tus Metas Activas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (goals.isEmpty()) {
                        Text(
                            text = "No hay metas activas configuradas. Puedes configurar tu peso y porcentaje de grasa objetivo en tu Perfil.",
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

@Composable
fun LiteEstadisticasScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val measurementsRaw by viewModel.measurements.collectAsState()
    val measurements = remember(measurementsRaw) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        measurementsRaw
            .groupBy { sdf.format(java.util.Date(it.timestamp)) }
            .map { (_, list) -> list.maxByOrNull { it.timestamp }!! }
            .sortedByDescending { it.timestamp }
    }
    val goals by viewModel.goals.collectAsState()
    val isLb by viewModel.useLb.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi Progreso",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val lastLog = measurements.firstOrNull()
        if (lastLog == null) {
            // Empty state for LITE screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Sin datos",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Aún no tienes mediciones registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ve a la sección 'Registrar' y guarda tu primera medición para ver los gráficos aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val currentWeight = if (isLb) lastLog.weight * 2.20462 else lastLog.weight
            val modeSuffix = if (isLb) "lbs" else "kg"

            val prevLog = if (measurements.size > 1) measurements[1] else null
            val diffRaw = if (prevLog != null) lastLog.weight - prevLog.weight else 0.0
            val diff = if (isLb) diffRaw * 2.20462 else diffRaw

            // --- HEADER CURRENT WEIGHT & TREND BADGE ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PESO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", currentWeight)} $modeSuffix",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Change badge from previous session
                    val isLoss = diff < 0
                    val badgeColor = if (isLoss) Color(0xFF4CAF50) else if (diff > 0) Color(0xFFFF9800) else Color.Gray
                    val badgeSign = if (diff > 0) "+" else ""

                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("trend_badge_lite")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLoss) Icons.Default.ArrowDownward else if (diff > 0) Icons.Default.ArrowUpward else Icons.Default.HorizontalRule,
                                contentDescription = "Cambio",
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$badgeSign${String.format("%.1f", diff)} $modeSuffix",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }

            // --- WEEKLY BAR CHART SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Historial Semanal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Draw the custom vertical bars
                    val chartLogs = measurements.take(7).reversed()
                    val maxVal = chartLogs.maxOfOrNull { if (isLb) it.weight * 2.20462 else it.weight } ?: 100.0
                    val minVal = chartLogs.minOfOrNull { if (isLb) it.weight * 2.20462 else it.weight } ?: 40.0
                    
                    val chartMax = maxVal * 1.1f
                    val chartMin = (minVal * 0.9f).coerceAtLeast(0.0)

                    // Locate weight goal target line
                    val weightGoalObj = goals.firstOrNull { it.type.lowercase().contains("peso") }
                    val goalLineVal = weightGoalObj?.targetValue?.let { if (isLb) it * 2.20462 else it }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        // Draw horizontal dashed Goal Line if exists
                        goalLineVal?.let { gVal ->
                            val ratio = ((gVal - chartMin) / (chartMax - chartMin)).coerceIn(0.0, 1.0).toFloat()
                            if (ratio in 0f..1f) {
                                val lineYPosition = (140 * (1f - ratio)).dp
                                
                                // Dotted goal indicator layout
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = lineYPosition)
                                        .height(1.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                )
                                Text(
                                    text = "Target: ${String.format("%.1f", gVal)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = lineYPosition - 14.dp, end = 4.dp)
                                )
                            }
                        }

                        // Let's render the columns
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val formatter = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                            chartLogs.forEachIndexed { index, m ->
                                val wVal = if (isLb) m.weight * 2.20462 else m.weight
                                val progress = ((wVal - chartMin) / (chartMax - chartMin)).coerceIn(0.0, 1.0).toFloat()
                                val barHeight = (140 * progress).dp

                                val dayLabel = formatter.format(java.util.Date(m.timestamp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = String.format("%.1f", wVal),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                color = if (index == chartLogs.lastIndex) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- TU IMC SECTION WITH DYNAMIC POSITIONING DOT ---
            val lastBmi = BodyCalculator.calculateBMI(lastLog.weight, lastLog.height)
            val bmiClassification = BodyCalculator.getBMIClassification(lastBmi)
            val bmiColor = BodyCalculator.getBMIColor(lastBmi)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Índice de Masa Corporal (IMC)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format("%.1f", lastBmi)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(bmiColor)
                        )
                        Surface(
                            color = Color(bmiColor).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = bmiClassification,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(bmiColor),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Multi-segment horizontal strip (representing brackets: Bajo, Normal, Sobrepeso, Obeso)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Bajo (Cyan)
                        Box(
                            modifier = Modifier
                                .weight(18.5f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF03A9F4))
                        )
                        // Normal (Green)
                        Box(
                            modifier = Modifier
                                .weight(6.4f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF4CAF50))
                        )
                        // Sobrepeso (Yellow)
                        Box(
                            modifier = Modifier
                                .weight(5.0f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFFF9800))
                        )
                        // Obeso (Red)
                        Box(
                            modifier = Modifier
                                .weight(10.0f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFF44336))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bajo", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Normal", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Sobrepeso", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Obeso", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- TU PROGRESO / YOUR PROGRESS SECTION ---
            val weightGoalObj = goals.firstOrNull { it.type.lowercase().contains("peso") }
            if (weightGoalObj != null) {
                val startWeight = if (isLb) weightGoalObj.startingValue * 2.20462 else weightGoalObj.startingValue
                val targetWeight = if (isLb) weightGoalObj.targetValue * 2.20462 else weightGoalObj.targetValue

                val isWeightLoss = startWeight > targetWeight
                val pct = if (isWeightLoss) {
                    ((startWeight - currentWeight) / (startWeight - targetWeight)).coerceIn(0.0, 1.0)
                } else {
                    ((currentWeight - startWeight) / (targetWeight - startWeight)).coerceIn(0.0, 1.0)
                }
                
                val isGoalCompleted = if (isWeightLoss) currentWeight <= targetWeight else currentWeight >= targetWeight
                val weightLeft = if (isGoalCompleted) 0.0 else (if (isWeightLoss) (currentWeight - targetWeight) else (targetWeight - currentWeight)).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Objetivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isGoalCompleted) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF00F0FF).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "¡Meta de Peso Alcanzada! 🎉\nVe a perfil para establecer una nueva meta.",
                                    color = Color(0xFF00F0FF),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Iniciaste", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${String.format("%.1f", startWeight)} $modeSuffix", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Faltan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${String.format("%.1f", weightLeft)} $modeSuffix", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Meta", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${String.format("%.1f", targetWeight)} $modeSuffix", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { pct.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${String.format("%.0f", pct * 100)}% Completado", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("100%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin objetivo de peso activo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Crea un objetivo en el apartado de Perfil para ver tu progreso porcentual aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
