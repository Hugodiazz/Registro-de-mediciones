package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MeasurementEntity
import com.example.utils.BodyCalculator

@Composable
fun DonutCompositionCard(
    leanKg: Double,
    fatKg: Double,
    modifier: Modifier = Modifier
) {
    val total = leanKg + fatKg
    val leanPercentage = if (total > 0.0) (leanKg / total * 100.0).toFloat() else 0f
    val fatPercentage = if (total > 0.0) (fatKg / total * 100.0).toFloat() else 0f
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Distribución de Masa Corporal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                val leanColor = MaterialTheme.colorScheme.primary
                val fatColor = Color(0xFF009688)
                
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val rect = androidx.compose.ui.geometry.Rect(
                        strokeWidth / 2, strokeWidth / 2,
                        size.width - strokeWidth / 2, size.height - strokeWidth / 2
                    )
                    
                    if (total > 0.0) {
                        // Draw lean arc
                        val leanSweep = (leanPercentage / 100f) * 360f
                        drawArc(
                            color = leanColor,
                            startAngle = -90f,
                            sweepAngle = leanSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        
                        // Draw fat arc
                        val fatSweep = (fatPercentage / 100f) * 360f
                        drawArc(
                            color = fatColor,
                            startAngle = -90f + leanSweep,
                            sweepAngle = fatSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                    } else {
                        // Draw empty gray ring
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f kg", total),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Peso Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Masa Magra", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(String.format("%.1f kg (%.0f%%)", leanKg, leanPercentage), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF009688), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Masa Grasa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(String.format("%.1f kg (%.0f%%)", fatKg, fatPercentage), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun FfmiTrendCard(
    history: List<MeasurementEntity>,
    modifier: Modifier = Modifier
) {
    // Filter logs and map to FFMI points
    val ffmiPoints = history.filter { it.weight > 0.0 && it.height > 0.0 && it.fatPercentage > 0.0 }
        .map { log ->
            val fatKg = log.weight * (log.fatPercentage / 100.0)
            val leanKg = log.weight - fatKg
            val heightM = log.height / 100.0
            val ffmi = if (heightM > 0.0) leanKg / (heightM * heightM) else 0.0
            log.timestamp to ffmi
        }
        
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Evolución del FFMI (Músculo/Altura)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (ffmiPoints.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se requieren más logs con % de grasa cargado para dibujar el histórico de FFMI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val latestFfmi = ffmiPoints.last().second
                Text(
                    text = "FFMI Actual: ${String.format("%.2f", latestFfmi)} (Masa libre de grasa en kg / altura en m²)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BodyLineChart(
                    title = "Historial FFMI",
                    dataPoints = ffmiPoints,
                    valueSuffix = "",
                    lineColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                )
            }
        }
    }
}

@Composable
fun HealthAlertsCard(
    waist: Double,
    hip: Double,
    height: Double,
    age: Int,
    gender: String,
    modifier: Modifier = Modifier
) {
    val health = BodyCalculator.calculateAdvancedHealth(waist, hip, height, age, gender)
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Riesgos de Salud Metabólica (Semáforo)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 1. ICC
            if (waist > 0 && hip > 0) {
                val isFemale = gender.lowercase() == "femenino"
                val limits = if (isFemale) Pair(0.80, 0.85) else Pair(0.90, 1.00)
                val (color, label) = when {
                    health.waistToHipRatio < limits.first -> Color(0xFF4CAF50) to "Saludable"
                    health.waistToHipRatio >= limits.second -> Color(0xFFF44336) to "Riesgo Alto"
                    else -> Color(0xFFFF9800) to "Riesgo Moderado"
                }
                HealthRiskItem(
                    title = "Índice Cintura-Cadera (ICC)",
                    value = String.format("%.2f", health.waistToHipRatio),
                    statusLabel = label,
                    statusColor = color
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Índice Cintura-Cadera (ICC)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Incompleto (Falta Cadera)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            // 2. ICA
            if (waist > 0 && height > 0) {
                val (color, label) = when {
                    health.waistToHeightRatio < 0.50 -> Color(0xFF4CAF50) to "Saludable"
                    health.waistToHeightRatio >= 0.60 -> Color(0xFFF44336) to "Riesgo Alto"
                    else -> Color(0xFFFF9800) to "Riesgo Moderado"
                }
                HealthRiskItem(
                    title = "Índice Cintura-Altura (ICA)",
                    value = String.format("%.2f", health.waistToHeightRatio),
                    statusLabel = label,
                    statusColor = color
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Índice Cintura-Altura (ICA)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Incompleto (Falta Altura)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            // 3. Grasa Visceral
            if (waist > 0) {
                val color = when (health.visceralRisk) {
                    "Bajo" -> Color(0xFF4CAF50)
                    "Moderado" -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                HealthRiskItem(
                    title = "Área de Grasa Visceral Estimada",
                    value = "${String.format("%.1f", health.visceralFatArea)} cm²",
                    statusLabel = "Riesgo ${health.visceralRisk}",
                    statusColor = color
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grasa Visceral Estimada", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Incompleto (Falta Cintura)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun HealthRiskItem(
    title: String,
    value: String,
    statusLabel: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = statusLabel,
                color = statusColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun NutritionMetabolicCard(
    leanMassKg: Double,
    userGoalType: String = "Reducción de Peso / Pérdida de Grasa",
    userActivityLevel: String = "Moderado",
    modifier: Modifier = Modifier
) {
    val initialFactor = when (userActivityLevel) {
        "Sedentario" -> 0
        "Ligero" -> 1
        "Moderado" -> 2
        "Intenso" -> 3
        else -> 2
    }
    var selectedFactor by remember(userActivityLevel) { mutableStateOf(initialFactor) }
    
    val metabolic = BodyCalculator.calculateAdvancedMetabolic(leanMassKg)
    val resolvedTdee = when (selectedFactor) {
        0 -> metabolic.tdeeSedentary
        1 -> metabolic.tdeeLight
        2 -> metabolic.tdeeModerate
        else -> metabolic.tdeeIntense
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Requerimiento Calórico Diario (TMB & GETD)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (leanMassKg <= 0.0) {
                Text(
                    text = "Se requiere registrar peso y % de grasa corporal para calcular la TMB vía Katch-McArdle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tasa Metabólica Basal (TMB):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%.0f", metabolic.bmr)} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("GETD (Gasto Total):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format("%.0f", resolvedTdee)} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Fórmula Katch-McArdle basada en tu masa magra.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                // Activity selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nivel de Actividad Física:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NutritionChip("Sedent.", selectedFactor == 0, onClick = { selectedFactor = 0 }, modifier = Modifier.weight(1f))
                        NutritionChip("Ligero", selectedFactor == 1, onClick = { selectedFactor = 1 }, modifier = Modifier.weight(1f))
                        NutritionChip("Mod.", selectedFactor == 2, onClick = { selectedFactor = 2 }, modifier = Modifier.weight(1f))
                        NutritionChip("Intenso", selectedFactor == 3, onClick = { selectedFactor = 3 }, modifier = Modifier.weight(1f))
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                
                // Dynamic Calorie Targets based on Profile
                val isWeightLoss = userGoalType == "Reducción de Peso / Pérdida de Grasa"
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isWeightLoss) "Régimen Recomendado para Pérdida de Grasa:" else "Régimen Recomendado para Aumento Muscular:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWeightLoss) Color(0xFF009688) else Color(0xFFFF9800)
                    )
                    
                    if (isWeightLoss) {
                        // Weight loss layout highlighting deficit
                        Surface(
                            color = Color(0xFF009688).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF009688).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Déficit Moderado (-15%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF009688))
                                    Text("Óptimo para salud y músculo", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${String.format("%.0f", resolvedTdee * 0.85)} kcal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF009688))
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Déficit Extremo (-20%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF44336))
                                    Text("${String.format("%.0f", resolvedTdee * 0.80)} kcal", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mantenimiento", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("${String.format("%.0f", resolvedTdee)} kcal", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    } else {
                        // Muscle Gain layout highlighting surplus
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFF9800).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Superávit Magro (+10%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                                    Text("Óptimo para ganancia de músculo limpia", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${String.format("%.0f", resolvedTdee * 1.10)} kcal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF9800))
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Superávit Alto (+15%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    Text("${String.format("%.0f", resolvedTdee * 1.15)} kcal", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mantenimiento", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("${String.format("%.0f", resolvedTdee)} kcal", fontSize = 13.sp, fontWeight = FontWeight.Black)
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
fun CalorieTargetItem(
    title: String,
    calories: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text("$calories kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NutritionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(28.dp)
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SymmetryBilateralCard(
    latestLog: MeasurementEntity,
    modifier: Modifier = Modifier
) {
    val bL = latestLog.bicepLeft
    val bR = latestLog.bicepRight
    val tL = latestLog.thighLeft
    val tR = latestLog.thighRight
    val cL = latestLog.calfLeft
    val cR = latestLog.calfRight
    val chest = latestLog.chest
    val waist = latestLog.waist

    val symmetry = BodyCalculator.calculateAdvancedSymmetry(
        bicepL = bL, bicepR = bR,
        forearmL = latestLog.forearm, forearmR = latestLog.forearm,
        thighL = tL, thighR = tR,
        calfL = cL, calfR = cR,
        chest = chest, waist = waist
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Análisis de Simetría Bilateral",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic V-Taper highlight if chest and waist are loaded
            if (chest > 0.0 && waist > 0.0) {
                val ratio = symmetry.vTaperRatio
                val classification = when {
                    ratio >= 1.4 -> "Excelente (V-Shape Clásico)"
                    ratio >= 1.2 -> "Buen V-Taper"
                    else -> "V-Taper Moderado"
                }
                val ratioColor = if (ratio >= 1.3) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                
                Surface(
                    color = ratioColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ratioColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Índice V-Taper (Pecho/Cintura)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            Text(String.format("%.2f x", ratio), fontSize = 18.sp, fontWeight = FontWeight.Black, color = ratioColor)
                        }
                        Text(classification, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ratioColor)
                    }
                }
            }

            // Muscle list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Biceps
                SymmetryRowItem(
                    label = "Bíceps (Izq / Der)",
                    valL = bL,
                    valR = bR,
                    imbalancePercent = symmetry.bicepImbalance
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 2. Thighs
                SymmetryRowItem(
                    label = "Muslos (Izq / Der)",
                    valL = tL,
                    valR = tR,
                    imbalancePercent = symmetry.thighImbalance
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 3. Calves
                SymmetryRowItem(
                    label = "Pantorrillas (Izq / Der)",
                    valL = cL,
                    valR = cR,
                    imbalancePercent = symmetry.calfImbalance
                )
            }
        }
    }
}

@Composable
fun SymmetryRowItem(
    label: String,
    valL: Double,
    valR: Double,
    imbalancePercent: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (valL > 0.0 && valR > 0.0) {
                Text(
                    text = String.format("%.1f cm vs %.1f cm", valL, valR),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Datos incompletos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        if (valL > 0.0 && valR > 0.0) {
            val (badgeColor, statusText) = when {
                imbalancePercent < 1.0 -> Color(0xFF4CAF50) to "Simétrico"
                imbalancePercent <= 3.0 -> Color(0xFFFF9800) to "Leve"
                else -> Color(0xFFF44336) to "Desbalance"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = statusText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format("%.1f%%", imbalancePercent),
                        color = badgeColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SteveReevesProportionsCard(
    latestLog: MeasurementEntity,
    isLb: Boolean,
    modifier: Modifier = Modifier
) {
    val height = latestLog.height
    val currentChest = latestLog.chest
    
    val reeves = BodyCalculator.calculateAdvancedReeves(height, currentChest)
    val unit = "cm"

    val items = listOf(
        ReevesItemData("Pecho", currentChest, reeves.chestIdeal),
        ReevesItemData("Cintura", latestLog.waist, reeves.waistIdeal),
        ReevesItemData("Cadera", latestLog.hip, reeves.hipIdeal),
        ReevesItemData("Cuello", latestLog.neck, reeves.neckIdeal),
        ReevesItemData("Bíceps", maxOf(latestLog.bicepLeft, latestLog.bicepRight, latestLog.bicep), reeves.bicepIdeal),
        ReevesItemData("Antebrazo", latestLog.forearm, reeves.forearmIdeal),
        ReevesItemData("Muslo", maxOf(latestLog.thighLeft, latestLog.thighRight, latestLog.thigh), reeves.thighIdeal),
        ReevesItemData("Pantorrilla", maxOf(latestLog.calfLeft, latestLog.calfRight, latestLog.calf), reeves.calfIdeal)
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "Proporciones Clásicas (Steve Reeves)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Canon estético basado en tu estructura ósea/estatura (${String.format("%.0f", height)} cm)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Medida", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
                Text("Actual", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Ideal Reeves", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                Text("Diferencia", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    val hasActual = item.actual > 0.0
                    val diff = item.actual - item.ideal
                    val diffText = when {
                        !hasActual -> "-"
                        diff > 0.0 -> String.format("+%.1f %s", diff, unit)
                        diff < 0.0 -> String.format("%.1f %s", diff, unit)
                        else -> "Meta"
                    }
                    val diffColor = when {
                        !hasActual -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        diff > 0.0 -> Color(0xFF4CAF50)     // above classical
                        diff < -1.0 -> Color(0xFFFF9800)   // deficit
                        else -> Color(0xFF4CAF50)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text(
                            text = if (hasActual) String.format("%.1f %s", item.actual, unit) else "Sin reg.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasActual) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = String.format("%.1f %s", item.ideal, unit),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = diffText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = diffColor,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

data class ReevesItemData(
    val name: String,
    val actual: Double,
    val ideal: Double
)


