package com.devdiaz.utils

import kotlin.math.log10

object BodyCalculator {

    /**
     * Calculates Body Mass Index (IMC)
     * Weight in kg, Height in cm
     */
    fun calculateBMI(weight: Double, height: Double): Double {
        if (height <= 0.0 || weight <= 0.0) return 0.0
        val heightInMeters = height / 100.0
        return weight / (heightInMeters * heightInMeters)
    }

    /**
     * Gets Spanish classification text for BMI
     */
    fun getBMIClassification(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Bajo peso"
            bmi in 18.5..24.99 -> "Normal (Saludable)"
            bmi in 25.0..29.99 -> "Sobrepeso"
            else -> "Obesidad"
        }
    }

    /**
     * Gets classification color representation
     * Returns a Hex code color string or similar
     */
    fun getBMIColor(bmi: Double): Long {
        return when {
            bmi < 18.5 -> 0xFF03A9F4 // Light blue
            bmi in 18.5..24.99 -> 0xFF4CAF50 // Green
            bmi in 25.0..29.99 -> 0xFFFF9800 // Orange
            else -> 0xFFF44336 // Red
        }
    }

    /**
     * Calculates Body Fat % using US Navy method
     * Height, Neck, Waist, Hip in cm.
     * Hip is only used for Femenino.
     */
    fun calculateBodyFat(
        gender: String,
        height: Double,
        neck: Double,
        waist: Double,
        hip: Double
    ): Double {
        if (height <= 0.0 || neck <= 0.0 || waist <= 0.0) return 0.0

        return try {
            if (gender.lowercase() == "femenino") {
                if (hip <= 0.0) return 0.0
                val logVal = waist + hip - neck
                if (logVal <= 0.0) return 0.0
                val denominator = 1.29579 - 0.35004 * log10(logVal) + 0.22100 * log10(height)
                if (denominator == 0.0) return 0.0
                val fat = (495.0 / denominator) - 450.0
                if (fat.isNaN() || fat.isInfinite()) 0.0 else fat.coerceIn(2.0, 60.0)
            } else {
                val logVal = waist - neck
                if (logVal <= 0.0) return 0.0
                val denominator = 1.0324 - 0.19077 * log10(logVal) + 0.15456 * log10(height)
                if (denominator == 0.0) return 0.0
                val fat = (495.0 / denominator) - 450.0
                if (fat.isNaN() || fat.isInfinite()) 0.0 else fat.coerceIn(2.0, 60.0)
            }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Gets Spanish classification text for Body Fat Percentage (based on ACE guidelines)
     */
    fun getBodyFatClassification(fatPercent: Double, gender: String): String {
        if (fatPercent <= 0.0) return "N/A"
        val isFemale = gender.lowercase() == "femenino"

        return if (isFemale) {
            when {
                fatPercent < 10.0 -> "Grasa críticamente baja"
                fatPercent in 10.0..13.99 -> "Grasa esencial (Atleta)"
                fatPercent in 14.0..20.99 -> "Atleta"
                fatPercent in 21.0..24.99 -> "Fitness"
                fatPercent in 25.0..31.99 -> "Aceptable"
                else -> "Exceso de grasa (Obesidad)"
            }
        } else {
            when {
                fatPercent < 2.0 -> "Grasa críticamente baja"
                fatPercent in 2.0..5.99 -> "Grasa esencial"
                fatPercent in 6.0..13.99 -> "Atleta"
                fatPercent in 14.0..17.99 -> "Fitness"
                fatPercent in 18.0..24.99 -> "Aceptable"
                else -> "Exceso de grasa (Obesidad)"
            }
        }
    }

    /**
     * Gets classification color representation for Body Fat
     */
    fun getBodyFatColor(fatPercent: Double, gender: String): Long {
        if (fatPercent <= 0.0) return 0xFF757575
        val isFemale = gender.lowercase() == "femenino"

        return if (isFemale) {
            when {
                fatPercent < 10.0 -> 0xFFE91E63 // Pink / Warn
                fatPercent in 10.0..20.99 -> 0xFF4CAF50 // Green
                fatPercent in 21.0..24.99 -> 0xFF009688 // Teal
                fatPercent in 25.0..31.99 -> 0xFFFF9800 // Orange
                else -> 0xFFF44336 // Red
            }
        } else {
            when {
                fatPercent < 2.0 -> 0xFFE91E63 // Pink / Warn
                fatPercent in 2.0..13.99 -> 0xFF4CAF50 // Green
                fatPercent in 14.0..17.99 -> 0xFF009688 // Teal
                fatPercent in 18.0..24.99 -> 0xFFFF9800 // Orange
                else -> 0xFFF44336 // Red
            }
        }
    }

    // --- NEW ADVANCED CALCULATIONS (RF-05) ---

    data class CompositionCalculations(
        val bmi: Double,
        val fatPercentage: Double,
        val fatMassKg: Double,
        val leanMassKg: Double,
        val ffmi: Double
    )

    data class HealthCalculations(
        val waistToHipRatio: Double,
        val waistToHeightRatio: Double,
        val visceralFatArea: Double,
        val visceralRisk: String
    )

    data class MetabolicCalculations(
        val bmr: Double,
        val tdeeSedentary: Double,
        val tdeeLight: Double,
        val tdeeModerate: Double,
        val tdeeIntense: Double
    )

    data class SymmetryCalculations(
        val bicepImbalance: Double,
        val forearmImbalance: Double,
        val thighImbalance: Double,
        val calfImbalance: Double,
        val vTaperRatio: Double
    )

    data class ReevesProportions(
        val chestIdeal: Double,
        val waistIdeal: Double,
        val hipIdeal: Double,
        val neckIdeal: Double,
        val bicepIdeal: Double,
        val forearmIdeal: Double,
        val thighIdeal: Double,
        val calfIdeal: Double
    )

    fun calculateAdvancedComposition(weight: Double, height: Double, fatPercentage: Double): CompositionCalculations {
        val bmi = calculateBMI(weight, height)
        if (weight <= 0.0 || height <= 0.0) {
            return CompositionCalculations(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        val fatPercentageVal = if (fatPercentage > 0) fatPercentage else 0.0
        val fatMass = weight * (fatPercentageVal / 100.0)
        val leanMass = weight - fatMass
        val heightM = height / 100.0
        val ffmi = if (heightM > 0.0) leanMass / (heightM * heightM) else 0.0
        return CompositionCalculations(bmi, fatPercentageVal, fatMass, leanMass, ffmi)
    }

    fun calculateAdvancedHealth(waist: Double, hip: Double, height: Double, age: Int, gender: String): HealthCalculations {
        val waistToHip = if (hip > 0.0) waist / hip else 0.0
        val waistToHeight = if (height > 0.0) waist / height else 0.0
        val resolvedAge = if (age > 0) age else 30
        
        val vfa = if (waist <= 0.0) 0.0 else {
            if (gender.lowercase() == "femenino") {
                (1.03 * waist + 0.56 * resolvedAge - 51.2).coerceAtLeast(0.0)
            } else {
                (1.48 * waist + 0.35 * resolvedAge - 88.5).coerceAtLeast(0.0)
            }
        }
        
        val risk = when {
            vfa <= 0.0 -> "Bajo"
            vfa < 100.0 -> "Bajo"
            vfa in 100.0..140.0 -> "Moderado"
            else -> "Alto"
        }
        return HealthCalculations(waistToHip, waistToHeight, vfa, risk)
    }

    fun calculateAdvancedMetabolic(leanMassKg: Double): MetabolicCalculations {
        if (leanMassKg <= 0.0) return MetabolicCalculations(0.0, 0.0, 0.0, 0.0, 0.0)
        val bmr = 370.0 + (21.6 * leanMassKg)
        return MetabolicCalculations(
            bmr = bmr,
            tdeeSedentary = bmr * 1.2,
            tdeeLight = bmr * 1.375,
            tdeeModerate = bmr * 1.55,
            tdeeIntense = bmr * 1.725
        )
    }

    fun calculateAdvancedSymmetry(
        bicepL: Double, bicepR: Double,
        forearmL: Double, forearmR: Double,
        thighL: Double, thighR: Double,
        calfL: Double, calfR: Double,
        chest: Double, waist: Double
    ): SymmetryCalculations {
        fun imbalance(l: Double, r: Double): Double {
            if (l <= 0.0 || r <= 0.0) return 0.0
            val maxVal = maxOf(l, r)
            return if (maxVal > 0.0) (kotlin.math.abs(l - r) / maxVal) * 100.0 else 0.0
        }
        
        val bImb = imbalance(bicepL, bicepR)
        val fImb = imbalance(forearmL, forearmR)
        val tImb = imbalance(thighL, thighR)
        val cImb = imbalance(calfL, calfR)
        val vTaper = if (waist > 0.0) chest / waist else 0.0
        
        return SymmetryCalculations(bImb, fImb, tImb, cImb, vTaper)
    }

    fun calculateAdvancedReeves(height: Double, currentChest: Double): ReevesProportions {
        val baseChest = if (currentChest > 0.0) currentChest else {
            val h = if (height > 0.0) height else 170.0
            h * 0.58
        }
        return ReevesProportions(
            chestIdeal = baseChest,
            waistIdeal = baseChest * 0.70,
            hipIdeal = baseChest * 0.85,
            neckIdeal = baseChest * 0.38,
            bicepIdeal = baseChest * 0.38,
            forearmIdeal = baseChest * 0.30,
            thighIdeal = baseChest * 0.60,
            calfIdeal = baseChest * 0.38
        )
    }
}
