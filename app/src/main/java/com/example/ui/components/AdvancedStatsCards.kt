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
    modifier: Modifier = Modifier
) {
    var selectedFactor by remember { mutableStateOf(1) } // 0 = Sedentario, 1 = Ligero, 2 = Moderado, 3 = Intenso
    
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
                    Text("Tasa Metabólica Basal (TMB):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.0f", metabolic.bmr)} kcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
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
                
                // Dynamic Calorie Targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CalorieTargetItem("Definición (Déficit)", "${String.format("%.0f", resolvedTdee - 500)}", Color(0xFFF44336), Modifier.weight(1f))
                    CalorieTargetItem("Mantenimiento", "${String.format("%.0f", resolvedTdee)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    CalorieTargetItem("Volumen (Superávit)", "${String.format("%.0f", resolvedTdee + 350)}", Color(0xFF4CAF50), Modifier.weight(1f))
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
fun SymmetryAnalysisCard(
    log: MeasurementEntity,
    modifier: Modifier = Modifier
) {
    val sym = BodyCalculator.calculateAdvancedSymmetry(
        bicepL = log.bicepLeft, bicepR = log.bicepRight,
        forearmL = log.forearmLeft, forearmR = log.forearmRight,
        thighL = log.thighLeft, thighR = log.thighRight,
        calfL = log.calfLeft, calfR = log.calfRight,
        chest = log.chest, waist = log.waist
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
            
            // Symmetry rows for: Bicep, Forearm, Thigh, Calf
            val list = listOf(
                Triple("Bíceps", Pair(log.bicepLeft, log.bicepRight), sym.bicepImbalance),
                Triple("Antebrazo", Pair(log.forearmLeft, log.forearmRight), sym.forearmImbalance),
                Triple("Muslo", Pair(log.thighLeft, log.thighRight), sym.thighImbalance),
                Triple("Pantorrilla", Pair(log.calfLeft, log.calfRight), sym.calfImbalance)
            )
            
            list.forEach { (label, values, imbalance) ->
                val (l, r) = values
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        if (l > 0.0 && r > 0.0) {
                            val color = if (imbalance < 2.0) Color(0xFF4CAF50) else if (imbalance < 5.0) Color(0xFFFF9800) else Color(0xFFF44336)
                            val statusText = if (imbalance < 2.0) "Óptima" else "Desbalance"
                            Text(
                                text = "${String.format("%.1f%%", imbalance)} ($statusText)",
                                style = MaterialTheme.typography.labelSmall,
                                color = color,
                                fontWeight = FontWeight.Black
                            )
                        } else {
                            Text("Falta una de las medidas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                    
                    // Custom bar chart
                    if (l > 0.0 || r > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left limb bar
                            val normalizedL = if (l > 0) l / maxOf(l, r) else 0.0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (l >= r) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
                                    )
                            ) {
                                Text(
                                    text = if (l > 0) "${String.format("%.1f", l)} cm" else "",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                                )
                            }
                            
                            // Mid separator
                            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                            
                            // Right limb bar
                            val normalizedR = if (r > 0) r / maxOf(l, r) else 0.0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (r >= l) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
                                    )
                            ) {
                                Text(
                                    text = if (r > 0) "${String.format("%.1f", r)} cm" else "",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (sym.vTaperRatio > 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Relación Tórax-Cintura (V-Taper)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.2f", sym.vTaperRatio),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ReevesProportionsCard(
    height: Double,
    chest: Double,
    waist: Double,
    hip: Double,
    neck: Double,
    bicep: Double,
    forearm: Double,
    thigh: Double,
    calf: Double,
    modifier: Modifier = Modifier
) {
    val ideals = BodyCalculator.calculateAdvancedReeves(height, chest)
    
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
                text = "Proporciones Áureas (Steve Reeves)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Medidas ideales calculadas en base a tu estructura ósea de tórax (${String.format("%.0f", ideals.chestIdeal)} cm):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val list = listOf(
                Quadruple("Bíceps", bicep, ideals.bicepIdeal, "cm"),
                Quadruple("Antebrazo", forearm, ideals.forearmIdeal, "cm"),
                Quadruple("Cuello", neck, ideals.neckIdeal, "cm"),
                Quadruple("Cintura", waist, ideals.waistIdeal, "cm"),
                Quadruple("Cadera", hip, ideals.hipIdeal, "cm"),
                Quadruple("Muslo", thigh, ideals.thighIdeal, "cm"),
                Quadruple("Pantorrilla", calf, ideals.calfIdeal, "cm")
            )
            
            list.filter { it.current > 0.0 }.forEach { item ->
                val percent = (item.current / item.ideal * 100.0).coerceIn(10.0, 150.0).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Actual: ${String.format("%.1f", item.current)} ${item.unit} / Ideal Steve Reeves: ${String.format("%.1f", item.ideal)} ${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val color = if (percent in 95f..105f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.weight(1f).height(6.dp),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${String.format("%.0f", percent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val name: A, val current: B, val ideal: C, val unit: D)
