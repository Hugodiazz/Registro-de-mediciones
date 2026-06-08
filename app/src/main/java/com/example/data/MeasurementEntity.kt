package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val weight: Double,           // in kg
    val fatPercentage: Double,    // in %
    val height: Double,           // in cm
    val neck: Double,             // in cm
    val waist: Double,            // in cm
    val hip: Double,              // in cm (for females mainly, 0.0 for males)
    val chest: Double = 0.0,      // in cm (optional)
    val bicep: Double = 0.0,      // in cm (optional)
    val bicepLeft: Double = 0.0,  // in cm (optional)
    val bicepRight: Double = 0.0, // in cm (optional)
    val forearmLeft: Double = 0.0, // in cm (optional)
    val forearmRight: Double = 0.0, // in cm (optional)
    val thigh: Double = 0.0,      // in cm (optional)
    val thighLeft: Double = 0.0,  // in cm (optional)
    val thighRight: Double = 0.0, // in cm (optional)
    val calf: Double = 0.0,       // in cm (optional)
    val calfLeft: Double = 0.0,   // in cm (optional)
    val calfRight: Double = 0.0,  // in cm (optional)
    val gender: String,           // "Masculino" or "Femenino"
    val notes: String = ""        // optional comments
)
