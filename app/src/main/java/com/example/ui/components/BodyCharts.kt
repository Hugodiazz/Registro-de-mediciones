package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyLineChart(
    title: String,
    dataPoints: List<Pair<Long, Double>>, // timestamp -> value
    valueSuffix: String,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    goalValue: Double? = null,
    goalDirection: String = "downward", // "downward" (loss) vs "upward" (build)
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.size < 2) {
                // Return an elegant empty state inside the chart frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aún no hay suficientes registros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Agrega al menos 2 registros para ver el gráfico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                val sortedPoints = dataPoints.sortedBy { it.first }
                val values = sortedPoints.map { it.second }
                
                // Include goalValue in plotting boundaries so it is visible on screen
                val allValues = values + (if (goalValue != null) listOf(goalValue) else emptyList())
                val maxVal = allValues.maxOrNull() ?: 10.0
                val minVal = allValues.minOrNull() ?: 0.0

                // Add margin to min/max so lines don't clip at top/bottom border
                val valueRange = maxVal - minVal
                val yPadding = if (valueRange == 0.0) 2.0 else valueRange * 0.15
                val plottedMin = (minVal - yPadding).coerceAtLeast(0.0)
                val plottedMax = maxVal + yPadding

                val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
                val surfaceColor = MaterialTheme.colorScheme.surface

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val leftMargin = 55f
                    val bottomMargin = 45f
                    val rightMargin = 20f
                    val topMargin = 20f

                    val chartWidth = width - leftMargin - rightMargin
                    val chartHeight = height - topMargin - bottomMargin

                    // Draw grid horizontal helper lines (4 lines)
                    val gridLinesCount = 4
                    for (i in 0 until gridLinesCount) {
                        val fraction = i.toFloat() / (gridLinesCount - 1)
                        val y = topMargin + chartHeight * fraction
                        val isLast = i == gridLinesCount - 1

                        // Grid line
                        drawLine(
                            color = Color.Gray.copy(alpha = if (isLast) 0.5f else 0.15f),
                            start = Offset(leftMargin, y),
                            end = Offset(width - rightMargin, y),
                            strokeWidth = if (isLast) 2f else 1f
                        )
                    }

                    // Compute points positions
                    val xPositions = mutableListOf<Float>()
                    val yPositions = mutableListOf<Float>()

                    val maxIdx = sortedPoints.size - 1

                    for (i in sortedPoints.indices) {
                        val tFraction = if (maxIdx == 0) 0.5f else i.toFloat() / maxIdx
                        val x = leftMargin + chartWidth * tFraction
                        xPositions.add(x)

                        val dValue = sortedPoints[i].second
                        val vFraction = if (plottedMax == plottedMin) 0.5f else (dValue - plottedMin) / (plottedMax - plottedMin)
                        val y = topMargin + chartHeight * (1f - vFraction.toFloat())
                        yPositions.add(y)
                    }

                    // 1. Draw solid gradient path area underneath the curve
                    val fillPath = Path()
                    fillPath.moveTo(leftMargin, topMargin + chartHeight)
                    for (i in sortedPoints.indices) {
                        fillPath.lineTo(xPositions[i], yPositions[i])
                    }
                    fillPath.lineTo(width - rightMargin, topMargin + chartHeight)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f),
                                lineColor.copy(alpha = 0.01f)
                            ),
                            startY = topMargin,
                            endY = topMargin + chartHeight
                        )
                    )

                    // 2. Draw smooth stroke outline line on top of gradient
                    val strokePath = Path()
                    for (i in sortedPoints.indices) {
                        if (i == 0) {
                            strokePath.moveTo(xPositions[i], yPositions[i])
                        } else {
                            strokePath.lineTo(xPositions[i], yPositions[i])
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(
                            width = 6f,
                            pathEffect = PathEffect.cornerPathEffect(40f)
                        )
                    )

                    // 3. Draw dots representing core records and high-contrast tooltip text labels
                    for (i in sortedPoints.indices) {
                        val cx = xPositions[i]
                        val cy = yPositions[i]

                        // Outer ring of dot
                        drawCircle(
                            color = surfaceColor,
                            radius = 11f,
                            center = Offset(cx, cy)
                        )
                        // Inner dot colored
                        drawCircle(
                            color = lineColor,
                            radius = 6.5f,
                            center = Offset(cx, cy)
                        )
                    }

                    // 4. Draw Goal Line if it exists
                    goalValue?.let { gVal ->
                        val vFraction = if (plottedMax == plottedMin) 0.5f else (gVal - plottedMin) / (plottedMax - plottedMin)
                        val gy = topMargin + chartHeight * (1f - vFraction.toFloat())

                        if (gy in topMargin..(topMargin + chartHeight)) {
                            val latestValue = values.lastOrNull() ?: 0.0
                            val isAlert = goalDirection == "downward" && latestValue > gVal
                            val isCelebrate = goalDirection == "upward" && latestValue >= gVal

                            val goalLineColor = when {
                                isAlert -> Color(0xFFF44336) // Red warning
                                isCelebrate -> Color(0xFF4CAF50) // Green celebration
                                else -> lineColor.copy(alpha = 0.6f)
                            }

                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            drawLine(
                                color = goalLineColor,
                                start = Offset(leftMargin, gy),
                                end = Offset(width - rightMargin, gy),
                                strokeWidth = 3f,
                                pathEffect = pathEffect
                            )
                        }
                    }
                }

                // Bottom Labels (Dates) & Side Labels (Min/Max values)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, top = 4.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val firstDate = dateFormatter.format(Date(sortedPoints.first().first))
                    val midIdx = sortedPoints.size / 2
                    val midDate = dateFormatter.format(Date(sortedPoints[midIdx].first))
                    val lastDate = dateFormatter.format(Date(sortedPoints.last().first))

                    Text(
                        text = firstDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (sortedPoints.size > 2) {
                        Text(
                            text = midDate,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = lastDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Show Min, Max and Current statistics on the chart footer card
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentVal = values.last()
                    val averageVal = values.average()

                    // Exclude goalValue from min/max display so we only show actual measurements
                    val actualMin = values.minOrNull() ?: minVal
                    val actualMax = values.maxOrNull() ?: maxVal

                    StatSummaryLabel(title = "Mínimo", value = "${String.format("%.1f", actualMin)} $valueSuffix")
                    StatSummaryLabel(title = "Promedio", value = "${String.format("%.1f", averageVal)} $valueSuffix")
                    StatSummaryLabel(title = "Máximo", value = "${String.format("%.1f", actualMax)} $valueSuffix")
                    StatSummaryLabel(title = "Último", value = "${String.format("%.1f", currentVal)} $valueSuffix", highlightColor = lineColor)
                }

                // Inline Goal Status Alert / Celebration Badge
                goalValue?.let { gVal ->
                    val latestValue = values.lastOrNull() ?: 0.0
                    val isAlert = goalDirection == "downward" && latestValue > gVal
                    val isCelebrate = goalDirection == "upward" && latestValue >= gVal

                    val badgeColor = when {
                        isAlert -> Color(0xFFF44336)       // warning red
                        isCelebrate -> Color(0xFF4CAF50)   // success green
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val badgeText = when {
                        isAlert -> "⚠️ Alerta: ¡Estás por encima de la meta! (${String.format("%.1f", latestValue)} > ${String.format("%.1f", gVal)} $valueSuffix)"
                        isCelebrate -> "🎉 ¡Meta superada con éxito! (${String.format("%.1f", latestValue)} >= ${String.format("%.1f", gVal)} $valueSuffix)"
                        goalDirection == "downward" -> "🎯 Meta: ${String.format("%.1f", gVal)} $valueSuffix (Límite máximo)"
                        else -> "🎯 Meta: ${String.format("%.1f", gVal)} $valueSuffix o superior"
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        contentColor = badgeColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatSummaryLabel(
    title: String,
    value: String,
    highlightColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = highlightColor
        )
    }
}
