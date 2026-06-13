package com.devdiaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,             // "Peso (kg)", "Grasa (%)", "Cintura (cm)", "Cuello (cm)", "Bíceps (cm)", "Pecho (cm)"
    val targetValue: Double,      // Target metric value
    val startingValue: Double,    // Benchmark value when set
    val timestamp: Long,          // Start date of the goal
    val isCompleted: Boolean = false
)
