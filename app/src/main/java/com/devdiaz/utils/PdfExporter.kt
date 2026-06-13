package com.devdiaz.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.devdiaz.data.MeasurementEntity
import com.devdiaz.data.GoalEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Generates a PDF of measurements, stats, and goals, saves it to cache, and triggers Android share sheet.
     */
    fun exportAndSharePdf(
        context: Context, 
        measurements: List<MeasurementEntity>, 
        goals: List<GoalEntity> = emptyList()
    ) {
        if (measurements.isEmpty()) return

        val file = generatePdfFile(context, measurements, goals) ?: return
        sharePdf(context, file)
    }

    private fun generatePdfFile(
        context: Context, 
        list: List<MeasurementEntity>, 
        goalsList: List<GoalEntity>
    ): File? {
        val sortedList = list.sortedBy { it.timestamp }
        val first = sortedList.firstOrNull()
        val latest = sortedList.lastOrNull() ?: return null

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        val pageWidth = 595 // A4 standard width
        val pageHeight = 842 // A4 standard height
        val margin = 40f

        // Stats calculations
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sevenDaysMs = 7 * oneDayMs
        val thirtyDaysMs = 30 * oneDayMs

        // Weekly splits
        val recent7DaysLogs = sortedList.filter { it.timestamp >= now - sevenDaysMs }
        val previous7DaysLogs = sortedList.filter { it.timestamp in (now - 2 * sevenDaysMs)..(now - sevenDaysMs) }

        val weeklyWeightCur = if (recent7DaysLogs.isNotEmpty()) recent7DaysLogs.map { it.weight }.average() else latest.weight
        val weeklyWeightPrev = if (previous7DaysLogs.isNotEmpty()) previous7DaysLogs.map { it.weight }.average() else first?.weight ?: latest.weight

        val recent7FatLogs = recent7DaysLogs.filter { it.fatPercentage > 0.0 }
        val prev7FatLogs = previous7DaysLogs.filter { it.fatPercentage > 0.0 }
        val weeklyFatCur = if (recent7FatLogs.isNotEmpty()) recent7FatLogs.map { it.fatPercentage }.average() else latest.fatPercentage
        val weeklyFatPrev = if (prev7FatLogs.isNotEmpty()) prev7FatLogs.map { it.fatPercentage }.average() else first?.fatPercentage ?: latest.fatPercentage

        // Monthly splits
        val recent30DaysLogs = sortedList.filter { it.timestamp >= now - thirtyDaysMs }
        val previous30DaysLogs = sortedList.filter { it.timestamp in (now - 2 * thirtyDaysMs)..(now - thirtyDaysMs) }

        val monthlyWeightCur = if (recent30DaysLogs.isNotEmpty()) recent30DaysLogs.map { it.weight }.average() else latest.weight
        val monthlyWeightPrev = if (previous30DaysLogs.isNotEmpty()) previous30DaysLogs.map { it.weight }.average() else first?.weight ?: latest.weight

        val monthlyFatCur = if (recent30DaysLogs.filter { it.fatPercentage > 0.0 }.isNotEmpty()) {
            recent30DaysLogs.filter { it.fatPercentage > 0.0 }.map { it.fatPercentage }.average()
        } else latest.fatPercentage
        val monthlyFatPrev = if (previous30DaysLogs.filter { it.fatPercentage > 0.0 }.isNotEmpty()) {
            previous30DaysLogs.filter { it.fatPercentage > 0.0 }.map { it.fatPercentage }.average()
        } else first?.fatPercentage ?: latest.fatPercentage

        // Total differences
        val weightDiff = latest.weight - (first?.weight ?: latest.weight)
        val fatDiff = latest.fatPercentage - (first?.fatPercentage ?: latest.fatPercentage)

        val totalPages = 2 + ((sortedList.size - 1) / 22) + 1 // Page 1: Dashboard, Page 2: Advanced Analysis, Page 3+: Historical Table

        // -----------------------------------------------------------------------------------------
        // PAGE 1: ADVANCED DASHBOARD & CUSTOM GOALS
        // -----------------------------------------------------------------------------------------
        val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(page1Info)
        val canvas1 = page1.canvas

        // Header Banner (Deep elegant charcoal)
        paint.color = Color.parseColor("#1F2937")
        paint.style = Paint.Style.FILL
        canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 16f
        canvas1.drawText("INFORME AVANZADO DE REGISTRO CORPORAL", margin, 40f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#9CA3AF")
        canvas1.drawText("Análisis de Progreso, Tendencias Temporales y Control de Metas", margin, 65f, textPaint)
        canvas1.drawText("Pág 1 de $totalPages | Info de: ${latest.gender} (${latest.height} cm)", pageWidth - margin - 220f, 65f, textPaint)

        var currentY = 125f

        // Section A: Resumen de Progreso General
        paint.color = Color.parseColor("#F3F4F6")
        canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 115f, paint)

        paint.color = Color.parseColor("#D1D5DB")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 115f, paint)

        textPaint.color = Color.parseColor("#111827")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 12f
        canvas1.drawText("1. EVOLUCIÓN HISTÓRICA GENERAL", margin + 15f, currentY + 25f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 10f
        textPaint.color = Color.parseColor("#374151")

        // Weight Row
        val signWeight = if (weightDiff >= 0) "+" else ""
        canvas1.drawText(
            "Peso Corporal:   Inicial: ${String.format("%.1f", first?.weight ?: latest.weight)} kg   →   Actual: ${String.format("%.1f", latest.weight)} kg   |   Cambio: $signWeight${String.format("%.2f", weightDiff)} kg",
            margin + 15f, currentY + 52f, textPaint
        )

        // Fat Row
        val fInStr = if ((first?.fatPercentage ?: 0.0) > 0.0) "${String.format("%.1f", first?.fatPercentage)}%" else "S/R"
        val fActStr = if (latest.fatPercentage > 0.0) "${String.format("%.1f", latest.fatPercentage)}%" else "S/R"
        val signFat = if (fatDiff >= 0) "+" else ""
        val fChgStr = if ((first?.fatPercentage ?: 0.0) > 0.0 && latest.fatPercentage > 0.0) {
            "|   Cambio: $signFat${String.format("%.2f", fatDiff)}%"
        } else ""
        canvas1.drawText(
            "Grasa Corporal:   Inicial: $fInStr   →   Actual: $fActStr   $fChgStr",
            margin + 15f, currentY + 74f, textPaint
        )

        // BMI / IMC
        val bmiVal = BodyCalculator.calculateBMI(latest.weight, latest.height)
        val bmiClass = BodyCalculator.getBMIClassification(bmiVal)
        canvas1.drawText(
            "Índice de Masa Corporal (IMC):   ${String.format("%.1f", bmiVal)}   (${bmiClass})",
            margin + 15f, currentY + 96f, textPaint
        )

        currentY += 135f

        // Section B: Análisis Estadístico de Medias (Advanced Trends)
        paint.color = Color.parseColor("#F9FAFB")
        paint.style = Paint.Style.FILL
        canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 115f, paint)

        paint.color = Color.parseColor("#E5E7EB")
        paint.style = Paint.Style.STROKE
        canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 115f, paint)

        textPaint.color = Color.parseColor("#1F2937")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 12f
        canvas1.drawText("2. TENDENCIAS TEMPORALES (PROMEDIOS DE CONTROL)", margin + 15f, currentY + 25f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#4B5563")

        // Draw weekly split
        val wWeightDiff = weeklyWeightCur - weeklyWeightPrev
        val signWWeight = if (wWeightDiff >= 0) "+" else ""
        canvas1.drawText(
            "Promedio Semanal (Últimos 7 días):",
            margin + 15f, currentY + 54f, textPaint
        )
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#111827")
        canvas1.drawText(
            "${String.format("%.1f", weeklyWeightCur)} kg   (Vs semana anterior: ${String.format("%.1f", weeklyWeightPrev)} kg  |  $signWWeight${String.format("%.1f", wWeightDiff)} kg)",
            margin + 200f, currentY + 54f, textPaint
        )

        // Draw monthly split
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = Color.parseColor("#4B5563")
        val mWeightDiff = monthlyWeightCur - monthlyWeightPrev
        val signMWeight = if (mWeightDiff >= 0) "+" else ""
        canvas1.drawText(
            "Promedio Mensual (Últimos 30 días):",
            margin + 15f, currentY + 84f, textPaint
        )
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#111827")
        canvas1.drawText(
            "${String.format("%.1f", monthlyWeightCur)} kg   (Vs mes anterior: ${String.format("%.1f", monthlyWeightPrev)} kg  |  $signMWeight${String.format("%.1f", mWeightDiff)} kg)",
            margin + 200f, currentY + 84f, textPaint
        )

        currentY += 135f

        // Section C: Control de Metas Deportivas y Estética
        textPaint.color = Color.parseColor("#111827")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 12f
        canvas1.drawText("3. SEGUIMIENTO DE METAS PERSONALIZADAS", margin, currentY + 15f, textPaint)

        currentY += 30f

        if (goalsList.isEmpty()) {
            paint.color = Color.parseColor("#F9FAFB")
            paint.style = Paint.Style.FILL
            canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 60f, paint)

            paint.color = Color.parseColor("#E5E7EB")
            paint.style = Paint.Style.STROKE
            canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 60f, paint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textPaint.textSize = 10f
            textPaint.color = Color.GRAY
            canvas1.drawText("No has establecido metas de progreso en la aplicación.", margin + 20f, currentY + 35f, textPaint)
        } else {
            // Draw up to 5 goals nicely with progress bars on PDF page 1
            val goalsToDraw = goalsList.take(5)
            for (g in goalsToDraw) {
                // Background Card
                paint.color = Color.parseColor("#FAFAFA")
                paint.style = Paint.Style.FILL
                canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 54f, paint)

                paint.color = Color.parseColor("#E5E7EB")
                paint.style = Paint.Style.STROKE
                canvas1.drawRect(margin, currentY, pageWidth - margin, currentY + 54f, paint)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 9.5f
                textPaint.color = Color.parseColor("#111827")
                canvas1.drawText("Meta de ${g.type}", margin + 15f, currentY + 20f, textPaint)

                // Calculate progress
                val (progressPerc, statusTxt) = getGoalProgressInExporter(g, latest, sortedList)

                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 8.5f
                textPaint.color = Color.parseColor("#4B5563")
                canvas1.drawText(
                    "Inicial: ${String.format("%.1f", g.startingValue)}   →   Métrica Objetivo: ${String.format("%.1f", g.targetValue)}   |   Proceso: $statusTxt",
                    margin + 15f, currentY + 33f, textPaint
                )

                // Draw solid bar
                val barLeft = pageWidth - margin - 170f
                val barTop = currentY + 16f
                val barRight = pageWidth - margin - 20f
                val barBottom = currentY + 30f

                paint.color = Color.parseColor("#E5E7EB")
                paint.style = Paint.Style.FILL
                canvas1.drawRect(barLeft, barTop, barRight, barBottom, paint)

                // Colored progress rect
                val fillFraction = progressPerc / 100f
                val filledRight = barLeft + (barRight - barLeft) * fillFraction
                paint.color = if (progressPerc >= 100) Color.parseColor("#10B981") else Color.parseColor("#6750A4")
                canvas1.drawRect(barLeft, barTop, filledRight, barBottom, paint)

                // Progress label
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 8.5f
                textPaint.color = if (progressPerc >= 100) Color.parseColor("#047857") else Color.parseColor("#4F46E5")
                canvas1.drawText("$progressPerc%", barRight + 5f, currentY + 27f, textPaint)

                currentY += 62f
            }
        }

        // Page Footer
        textPaint.color = Color.parseColor("#9CA3AF")
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val printDate = dateFormatter.format(Date())
        canvas1.drawText("Registro Corporal | Informe creado el $printDate | Exportado en formato PDF", margin, pageHeight - 30f, textPaint)

        pdfDocument.finishPage(page1)

        // -----------------------------------------------------------------------------------------
        // PAGE 2: ADVANCED ANTHROPOMETRIC & METABOLIC REPORT (RF-05)
        // -----------------------------------------------------------------------------------------
        val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(page2Info)
        val canvas2 = page2.canvas

        // Header Banner (Deep metallic dark blue)
        paint.color = Color.parseColor("#0F172A")
        paint.style = Paint.Style.FILL
        canvas2.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 15f
        canvas2.drawText("ANÁLISIS ANTROPOMÉTRICO Y METABÓLICO AVANZADO", margin, 40f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#94A3B8")
        canvas2.drawText("Composición de Tejidos, Metabolismo y Distribución de Riesgos de Grasa", margin, 65f, textPaint)
        canvas2.drawText("Pág 2 de $totalPages | Registro Actual", pageWidth - margin - 180f, 65f, textPaint)

        var y2 = 125f

        // Shared Preferences loaded for age & gender
        val sharedPrefs = context.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)
        val profileAge = sharedPrefs.getString("age", "") ?: ""
        val profileGender = sharedPrefs.getString("gender", "") ?: ""
        val age = profileAge.toIntOrNull() ?: 30
        val gender = if (profileGender.isNotBlank()) profileGender else latest.gender

        // 1. Composición Corporal en Detalle
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 120f, paint)
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 120f, paint)

        textPaint.color = Color.parseColor("#1E293B")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas2.drawText("1. COMPOSICIÓN CORPORAL (KATCH-MCARDLE & FFMI)", margin + 15f, y2 + 25f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#475569")
        
        val fatPercentageVal = if (latest.fatPercentage > 0) latest.fatPercentage else 0.0
        val lbmKg = latest.weight * (1.0 - (fatPercentageVal / 100.0))
        val fKg = latest.weight - lbmKg
        val hM = latest.height / 100.0
        val ffmiVal = if (hM > 0.0) lbmKg / (hM * hM) else 0.0

        canvas2.drawText("• Masa Libre de Grasa / Masa Magra: ${String.format("%.1f", lbmKg)} kg", margin + 20f, y2 + 52f, textPaint)
        canvas2.drawText("• Masa Grasa Total: ${String.format("%.1f", fKg)} kg (${String.format("%.1f", fatPercentageVal)}%)", margin + 20f, y2 + 74f, textPaint)
        canvas2.drawText("• Índice de Masa Libre de Grasa (FFMI): ${String.format("%.2f", ffmiVal)}", margin + 20f, y2 + 96f, textPaint)

        y2 += 140f

        // 2. Salud Metabólica y Cardio-Visceral
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 120f, paint)
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 120f, paint)

        textPaint.color = Color.parseColor("#1E293B")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas2.drawText("2. SALUD METABÓLICA Y TASA ENERGÉTICA (TMB & GETD)", margin + 15f, y2 + 25f, textPaint)

        val bmrVal = 370.0 + (21.6 * lbmKg)
        val tdeeSed = bmrVal * 1.2
        val tdeeMod = bmrVal * 1.55

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#475569")
        canvas2.drawText("• Tasa Metabólica Basal (TMB): ${String.format("%.0f", bmrVal)} kcal / día (requerimiento básico absoluto)", margin + 20f, y2 + 52f, textPaint)
        canvas2.drawText("• GETD Actividad Sedentaria: ${String.format("%.0f", tdeeSed)} kcal / día", margin + 20f, y2 + 74f, textPaint)
        canvas2.drawText("• GETD Actividad Moderada (3-5 días/sem): ${String.format("%.0f", tdeeMod)} kcal / día", margin + 20f, y2 + 96f, textPaint)

        y2 += 140f

        // Section 3: Salud de Riesgos (ICC e ICA)
        paint.color = Color.parseColor("#FFFBEB") // Light gold warm banner
        paint.style = Paint.Style.FILL
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 110f, paint)
        paint.color = Color.parseColor("#F59E0B")
        paint.style = Paint.Style.STROKE
        canvas2.drawRect(margin, y2, pageWidth - margin, y2 + 110f, paint)

        textPaint.color = Color.parseColor("#78350F")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10.5f
        canvas2.drawText("3. ADVERTENCIA DE SALUD PÚBLICA / ÍNDICES DE DISTRIBUCIÓN DE GRASA", margin + 15f, y2 + 25f, textPaint)

        val healthVals = BodyCalculator.calculateAdvancedHealth(latest.waist, latest.hip, latest.height, age, gender)
        
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9f
        textPaint.color = Color.parseColor("#92400E")
        canvas2.drawText("• Índice Cintura-Cadera (ICC): ${String.format("%.2f", healthVals.waistToHipRatio)} (Gasto metabólico centralizado)", margin + 20f, y2 + 50f, textPaint)
        canvas2.drawText("• Índice Cintura-Altura (ICA): ${String.format("%.2f", healthVals.waistToHeightRatio)} (Riesgos cardiovasculares asociados)", margin + 20f, y2 + 72f, textPaint)
        canvas2.drawText("• Área de Grasa Visceral Estimada: ${String.format("%.1f", healthVals.visceralFatArea)} cm² | Riesgo de Grasa Visceral: ${healthVals.visceralRisk}", margin + 20f, y2 + 94f, textPaint)

        // Page 2 Footer
        textPaint.color = Color.parseColor("#94A3B8")
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas2.drawText("Registro Corporal | Informe Científico Multivariado | Pág 2 de $totalPages", margin, pageHeight - 30f, textPaint)

        pdfDocument.finishPage(page2)

        // -----------------------------------------------------------------------------------------
        // PAGES 3+: HISTORIC DATA DATATABLE
        // -----------------------------------------------------------------------------------------
        val itemsPerPage = 22
        var currentItemIdx = 0
        var pageNum = 3

        while (currentItemIdx < sortedList.size) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Header banner
            paint.color = Color.parseColor("#374151")
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 80f, paint)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 14f
            canvas.drawText("TABLA DETALLADA DE MEDICIONES", margin, 35f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 9f
            textPaint.color = Color.parseColor("#D1D5DB")
            canvas.drawText("Histórico completo de los registros guardados chronológicamente", margin, 58f, textPaint)
            canvas.drawText("Página $pageNum de $totalPages", pageWidth - margin - 90f, 58f, textPaint)

            var rowY = 110f

            // Table Header Row
            paint.color = Color.parseColor("#4B5563")
            paint.style = Paint.Style.FILL
            canvas.drawRect(margin, rowY, pageWidth - margin, rowY + 23f, paint)

            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 8.5f

            var colX = margin + 8f
            canvas.drawText("Fecha", colX, rowY + 15f, textPaint)
            colX += 65f
            canvas.drawText("Peso (kg)", colX, rowY + 15f, textPaint)
            colX += 60f
            canvas.drawText("Grasa %", colX, rowY + 15f, textPaint)
            colX += 55f
            canvas.drawText("IMC", colX, rowY + 15f, textPaint)
            colX += 50f
            canvas.drawText("Cintura", colX, rowY + 15f, textPaint)
            colX += 50f
            canvas.drawText("Cuello", colX, rowY + 15f, textPaint)
            colX += 45f
            canvas.drawText("Cadera", colX, rowY + 15f, textPaint)
            colX += 45f
            canvas.drawText("Porción", colX, rowY + 15f, textPaint) // Notes/Gender reference
            colX += 45f
            canvas.drawText("Pecho", colX, rowY + 15f, textPaint)

            rowY += 23f

            var countInPage = 0
            while (currentItemIdx < sortedList.size && countInPage < itemsPerPage) {
                val item = sortedList[currentItemIdx]

                // Alternate colors
                if (countInPage % 2 == 1) {
                    paint.color = Color.parseColor("#F9FAFB")
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(margin, rowY, pageWidth - margin, rowY + 22f, paint)
                }

                // Grid border line
                paint.color = Color.parseColor("#E5E7EB")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(margin, rowY + 22f, pageWidth - margin, rowY + 22f, paint)

                textPaint.color = Color.parseColor("#374151")
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 8.5f

                colX = margin + 8f
                // Date
                canvas.drawText(dateFormatter.format(Date(item.timestamp)), colX, rowY + 14f, textPaint)
                colX += 65f
                // Weight
                canvas.drawText(String.format("%.1f", item.weight), colX, rowY + 14f, textPaint)
                colX += 60f
                // Fat
                val fatValStr = if (item.fatPercentage > 0.0) "${String.format("%.1f", item.fatPercentage)}%" else "-"
                canvas.drawText(fatValStr, colX, rowY + 14f, textPaint)
                colX += 55f
                // IMC
                val bmiValInternal = BodyCalculator.calculateBMI(item.weight, item.height)
                canvas.drawText(String.format("%.1f", bmiValInternal), colX, rowY + 14f, textPaint)
                colX += 50f
                // Waist / Cintura
                val waistValStr = if (item.waist > 0.0) "${item.waist} cm" else "-"
                canvas.drawText(waistValStr, colX, rowY + 14f, textPaint)
                colX += 50f
                // Neck
                val neckValStr = if (item.neck > 0.0) "${item.neck} cm" else "-"
                canvas.drawText(neckValStr, colX, rowY + 14f, textPaint)
                colX += 45f
                // Hip
                val hipValStr = if (item.hip > 0.0) "${item.hip} cm" else "-"
                canvas.drawText(hipValStr, colX, rowY + 14f, textPaint)
                colX += 45f
                // Brazo/Bicep
                val bicepValStr = if (item.bicep > 0.0) "${item.bicep} cm" else "-"
                canvas.drawText(bicepValStr, colX, rowY + 14f, textPaint)
                colX += 45f
                // Pecho
                val chestValStr = if (item.chest > 0.0) "${item.chest} cm" else "-"
                canvas.drawText(chestValStr, colX, rowY + 14f, textPaint)

                rowY += 22f
                currentItemIdx++
                countInPage++
            }

            // Footer
            textPaint.color = Color.parseColor("#9CA3AF")
            textPaint.textSize = 8f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Registro Corporal | Informe de Histórico detallado", margin, pageHeight - 30f, textPaint)

            pdfDocument.finishPage(page)
            pageNum++
        }

        // Write to folder
        try {
            val cacheFolder = File(context.cacheDir, "pdf_reports")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val pdfFile = File(cacheFolder, "Registro_Corporal_Medidas_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.flush()
            outputStream.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    private fun getGoalProgressInExporter(
        goal: GoalEntity, 
        latest: MeasurementEntity?, 
        measurements: List<MeasurementEntity>
    ): Pair<Int, String> {
        if (latest == null) return 0 to "S/R"
        
        val currentValue = when (goal.type) {
            "Peso (kg)" -> latest.weight
            "Grasa (%)" -> {
                measurements.lastOrNull { it.fatPercentage > 0.0 }?.fatPercentage ?: 0.0
            }
            "Cintura (cm)" -> latest.waist
            "Cuello (cm)" -> latest.neck
            "Bíceps (cm)" -> latest.bicep
            "Pecho (cm)" -> latest.chest
            else -> 0.0
        }

        if (currentValue <= 0.0) return 0 to "Falta ingreso"

        val isDownward = goal.targetValue < goal.startingValue
        
        return if (isDownward) {
            if (currentValue <= goal.targetValue) {
                100 to "Completada 🎉"
            } else {
                val totalToLose = goal.startingValue - goal.targetValue
                val lostSoFar = goal.startingValue - currentValue
                if (totalToLose <= 0.0) 100 to "Completada 🎉"
                else {
                    val progressPercent = ((lostSoFar / totalToLose) * 100).coerceIn(0.0, 100.0).toInt()
                    val remaining = currentValue - goal.targetValue
                    progressPercent to "Faltan ${String.format("%.1f", remaining)} ${if (goal.type.contains("kg")) "kg" else if (goal.type.contains("%")) "%" else "cm"}"
                }
            }
        } else {
            if (currentValue >= goal.targetValue) {
                100 to "Completada"
            } else {
                val totalToGain = goal.targetValue - goal.startingValue
                val gainedSoFar = currentValue - goal.startingValue
                if (totalToGain <= 0.0) 100 to "Completada"
                else {
                    val progressPercent = ((gainedSoFar / totalToGain) * 100).coerceIn(0.0, 100.0).toInt()
                    val remaining = goal.targetValue - currentValue
                    progressPercent to "Faltan ${String.format("%.1f", remaining)} ${if (goal.type.contains("kg")) "kg" else if (goal.type.contains("%")) "%" else "cm"}"
                }
            }
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "com.aistudio.registrocorporal.vpxyz.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Avanzado de Medidas Corporales")
                putExtra(Intent.EXTRA_TEXT, "Adjunto mi reporte avanzado de medidas corporales, evolución de peso y metas alcanzadas.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Reporte PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
