package com.devdiaz.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devdiaz.data.MeasurementEntity
import com.devdiaz.ui.MeasurementViewModel
import com.devdiaz.utils.BodyCalculator
import com.devdiaz.utils.PdfExporter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialTab(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.measurements.collectAsState()
    val isLb by viewModel.useLb.collectAsState()
    val context = LocalContext.current

    // Period filter state mimicking HTML selector
    var selectedPeriod by remember { mutableStateOf("Todos") }
    val filteredMeasurements = remember(measurements, selectedPeriod) {
        val now = System.currentTimeMillis()
        when (selectedPeriod) {
            "Semana" -> measurements.filter { it.timestamp >= now - 7L * 24 * 3600 * 1000 }
            "Mes" -> measurements.filter { it.timestamp >= now - 30L * 24 * 3600 * 1000 }
            "3 Meses" -> measurements.filter { it.timestamp >= now - 90L * 24 * 3600 * 1000 }
            "Año" -> measurements.filter { it.timestamp >= now - 365L * 24 * 3600 * 1000 }
            else -> measurements
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Redesigned Top Header: Title & PDF Button side by side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Historial de Registros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${filteredMeasurements.size} mediciones mostradas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            if (measurements.isNotEmpty()) {
                Surface(
                    onClick = {
                        PdfExporter.exportAndSharePdf(context, measurements)
                    },
                    color = Color(0xFF1D2123), // bg-surface-container in HTML
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("export_pdf_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Color(0xFF00F0FF), // clinical-cyan
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PDF", 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Horizontal filter chips row mimicking the HTML
        val periods = listOf("Todos", "Semana", "Mes", "3 Meses", "Año")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            periods.forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF00F0FF) else Color(0xFF1D2123))
                        .clickable { selectedPeriod = period }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = period,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color(0xFF94A3B8)
                    )
                }
            }
        }

        if (filteredMeasurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                color = Color(0xFF00F0FF).copy(alpha = 0.08f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = "Sin registros",
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Analizando tendencias clínicas...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ingresa tus medidas corporales en la pestaña de registro para visualizar tus registros históricos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredMeasurements,
                    key = { it.id }
                ) { measurement ->
                    MeasurementItemCard(
                        item = measurement,
                        isLb = isLb,
                        onDeleteClick = { viewModel.deleteMeasurement(measurement) }
                    )
                }
            }
        }
    }
}

@Composable
fun MeasurementItemCard(
    item: MeasurementEntity,
    isLb: Boolean,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Convert units dynamically
    val weightMult = if (isLb) 2.20462 else 1.0
    val weightUnit = if (isLb) "lbs" else "kg"
    val displayWeight = item.weight * weightMult
    
    val bmi = BodyCalculator.calculateBMI(item.weight, item.height)
    val bmiClass = BodyCalculator.getBMIClassification(bmi)
    val bmiColor = BodyCalculator.getBMIColor(bmi)

    val fatClass = BodyCalculator.getBodyFatClassification(item.fatPercentage, item.gender)
    val fatColor = BodyCalculator.getBodyFatColor(item.fatPercentage, item.gender)

    val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    val dateStr = try {
        dateFormat.format(Date(item.timestamp))
    } catch (e: Exception) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.timestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("measurement_card_${item.id}"),
        color = Color(0xFF1D2123), // bg-surface-container in HTML (#1d2123)
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            // Header Row: Calendar Icon + Date + Collapse Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The 3-Column horizontal grid layout from clinical HTML template
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weight (Column 1)
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PESO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = "Peso",
                                tint = Color(0xFF00F0FF), // clinical-cyan
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "%.1f %s", displayWeight, weightUnit),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Body Fat (Column 2)
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "GRASA CORP.",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = if (item.fatPercentage > 0.0) String.format(Locale.US, "%.1f%%", item.fatPercentage) else "N/A",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.fatPercentage > 0.0) Color(fatColor) else Color(0xFF64748B)
                        )
                    }
                }

                // BMI (Column 3 - aligned right)
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "IMC",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", bmi),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(bmiColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clinical Badges matching the HTML design: colored background transparent, solid border
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BMI Status Badge
                Box(
                    modifier = Modifier
                        .background(Color(bmiColor).copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(bmiColor).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bmiClass,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(bmiColor)
                    )
                }

                // Fat % Status Badge (if recorded)
                if (item.fatPercentage > 0.0) {
                    Box(
                        modifier = Modifier
                            .background(Color(fatColor).copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(fatColor).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = fatClass,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(fatColor)
                        )
                    }
                }
            }

            // Expanded tape metrics breakdown
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.White.copy(alpha = 0.08f))

                    Text(
                        text = "Medidas y Cintometría",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F0FF) // clinical-cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid of sub-measurements (cuello, cintura, cadera etc)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TapeMeasurementLabel(title = "Cuello", value = item.neck)
                        TapeMeasurementLabel(title = "Cintura", value = item.waist)
                        TapeMeasurementLabel(title = "Cadera", value = item.hip, isOnlyFemale = true, isFemale = item.gender.lowercase() == "femenino")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TapeMeasurementLabel(title = "Bíceps", value = item.bicep)
                        TapeMeasurementLabel(title = "Pecho", value = item.chest)
                        TapeMeasurementLabel(title = "Muslo", value = item.thigh)
                        TapeMeasurementLabel(title = "Pantorrilla", value = item.calf)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TapeMeasurementLabel(title = "Antebrazo", value = item.forearm)
                    }

                    if (item.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Notas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    }

                    // Delete button section
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("delete_button_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Eliminar registro", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TapeMeasurementLabel(
    title: String,
    value: Double,
    isOnlyFemale: Boolean = false,
    isFemale: Boolean = false
) {
    if (isOnlyFemale && !isFemale) return

    Column(modifier = Modifier.padding(end = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
        Text(
            text = if (value > 0.0) "${value} cm" else "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCBD5E1)
        )
    }
}
