package com.devdiaz.data

import kotlinx.coroutines.flow.Flow

class MeasurementRepository(
    private val measurementDao: MeasurementDao,
    private val goalDao: GoalDao
) {
    val allMeasurements: Flow<List<MeasurementEntity>> = measurementDao.getAllMeasurements()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun insert(measurement: MeasurementEntity): Long {
        return measurementDao.insertMeasurement(measurement)
    }

    suspend fun delete(measurement: MeasurementEntity) {
        measurementDao.deleteMeasurement(measurement)
    }

    suspend fun deleteById(id: Int) {
        measurementDao.deleteMeasurementById(id)
    }

    suspend fun insertGoal(goal: GoalEntity): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(id: Int) {
        goalDao.deleteGoalById(id)
    }

    suspend fun updateGoalStatus(id: Int, isCompleted: Boolean) {
        goalDao.updateGoalStatus(id, isCompleted)
    }
}
