package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GoalEntity
import com.example.data.MeasurementEntity
import com.example.data.MeasurementRepository
import com.example.utils.BodyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdvStats(
    val totalWeightChange: Double = 0.0,
    val totalFatChange: Double = 0.0,
    val weeklyAvgWeightCurrent: Double = 0.0,
    val weeklyAvgWeightPrevious: Double = 0.0,
    val monthlyAvgWeightCurrent: Double = 0.0,
    val monthlyAvgWeightPrevious: Double = 0.0,
    val weeklyAvgFatCurrent: Double = 0.0,
    val weeklyAvgFatPrevious: Double = 0.0,
    val monthlyAvgFatCurrent: Double = 0.0,
    val monthlyAvgFatPrevious: Double = 0.0,
    val recentWeight: Double = 0.0,
    val initialWeight: Double = 0.0,
    val recentFat: Double = 0.0,
    val initialFat: Double = 0.0
)

class MeasurementViewModel(private val repository: MeasurementRepository, private val context: android.content.Context) : ViewModel() {

    // --- USER PROFILE SAVED STATE (SharedPreferences persistence) ---
    private val sharedPrefs = context.getSharedPreferences("UserProfilePrefs", android.content.Context.MODE_PRIVATE)

    val profileName = MutableStateFlow(sharedPrefs.getString("name", "") ?: "")
    val profileAge = MutableStateFlow(sharedPrefs.getString("age", "") ?: "")
    val profileGender = MutableStateFlow(sharedPrefs.getString("gender", "Masculino") ?: "")
    val profileHeight = MutableStateFlow(sharedPrefs.getString("height", "170") ?: "")
    val profilePhotoUri = MutableStateFlow(sharedPrefs.getString("photoUri", "") ?: "")

    fun saveProfile(name: String, age: String, gender: String, height: String, photoUri: String) {
        sharedPrefs.edit().apply {
            putString("name", name)
            putString("age", age)
            putString("gender", gender)
            putString("height", height)
            putString("photoUri", photoUri)
            apply()
        }
        profileName.value = name
        profileAge.value = age
        profileGender.value = gender
        profileHeight.value = height
        profilePhotoUri.value = photoUri

        // personalization: update basic measurement inputs
        heightInput.value = height
        genderInput.value = gender

        _uiMessage.value = "¡Perfil guardado con éxito!"
    }

    // Expose all measurements from DB ordered by time desc
    val measurements: StateFlow<List<MeasurementEntity>> = repository.allMeasurements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current screen/tab selection: 0 = Historial, 1 = Registrar, 2 = Estadísticas, 3 = Guía/Ideal, 4 = Perfil
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // --- FORM FIELDS STATE ---
    val weightInput = MutableStateFlow("")
    val heightInput = MutableStateFlow(profileHeight.value.ifBlank { "170" })
    val genderInput = MutableStateFlow(profileGender.value.ifBlank { "Masculino" })

    val isFatManual = MutableStateFlow(true) // True = Manual, False = Calculated (US Navy)
    val manualFatInput = MutableStateFlow("")

    val neckInput = MutableStateFlow("")
    val waistInput = MutableStateFlow("")
    val hipInput = MutableStateFlow("") // Only Femenino

    // Optional measurements
    val chestInput = MutableStateFlow("")
    val bicepInput = MutableStateFlow("")
    val bicepLeftInput = MutableStateFlow("")
    val bicepRightInput = MutableStateFlow("")
    val forearmLeftInput = MutableStateFlow("")
    val forearmRightInput = MutableStateFlow("")
    val thighInput = MutableStateFlow("")
    val thighLeftInput = MutableStateFlow("")
    val thighRightInput = MutableStateFlow("")
    val calfInput = MutableStateFlow("")
    val calfLeftInput = MutableStateFlow("")
    val calfRightInput = MutableStateFlow("")

    val notesInput = MutableStateFlow("")

    // Status notifications/alerts
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearMessage() {
        _uiMessage.value = null
    }

    // --- REALTIME DYNAMIC CALCULATIONS ---
    // Combined flow calculates BMI dynamically as user types weight/height
    val calculatedBmi: StateFlow<Double> = combine(weightInput, heightInput) { weightStr, heightStr ->
        val weight = weightStr.toDoubleOrNull() ?: 0.0
        val height = heightStr.toDoubleOrNull() ?: 0.0
        BodyCalculator.calculateBMI(weight, height)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Combined flow calculates Body Fat % dynamically using US Navy formula
    val calculatedBodyFat: StateFlow<Double> = combine(
        genderInput, heightInput, neckInput, waistInput, hipInput
    ) { gender, heightStr, neckStr, waistStr, hipStr ->
        val height = heightStr.toDoubleOrNull() ?: 0.0
        val neck = neckStr.toDoubleOrNull() ?: 0.0
        val waist = waistStr.toDoubleOrNull() ?: 0.0
        val hip = hipStr.toDoubleOrNull() ?: 0.0
        BodyCalculator.calculateBodyFat(gender, height, neck, waist, hip)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * Pre-fills inputs based on the target measurement (for editing/replicating),
     * or pre-fills baseline stats using the last logged measurement to save typing.
     */
    fun prefillFromLastLog() {
        viewModelScope.launch {
            val logs = measurements.value
            if (logs.isNotEmpty()) {
                val last = logs.first() // Sorted DESC, so first is latest
                heightInput.value = last.height.toString()
                genderInput.value = last.gender
                neckInput.value = if (last.neck > 0) last.neck.toString() else ""
                waistInput.value = if (last.waist > 0) last.waist.toString() else ""
                hipInput.value = if (last.hip > 0) last.hip.toString() else ""
                chestInput.value = if (last.chest > 0) last.chest.toString() else ""
                bicepInput.value = if (last.bicep > 0) last.bicep.toString() else ""
                bicepLeftInput.value = if (last.bicepLeft > 0) last.bicepLeft.toString() else ""
                bicepRightInput.value = if (last.bicepRight > 0) last.bicepRight.toString() else ""
                forearmLeftInput.value = if (last.forearmLeft > 0) last.forearmLeft.toString() else ""
                forearmRightInput.value = if (last.forearmRight > 0) last.forearmRight.toString() else ""
                thighInput.value = if (last.thigh > 0) last.thigh.toString() else ""
                thighLeftInput.value = if (last.thighLeft > 0) last.thighLeft.toString() else ""
                thighRightInput.value = if (last.thighRight > 0) last.thighRight.toString() else ""
                calfInput.value = if (last.calf > 0) last.calf.toString() else ""
                calfLeftInput.value = if (last.calfLeft > 0) last.calfLeft.toString() else ""
                calfRightInput.value = if (last.calfRight > 0) last.calfRight.toString() else ""
            }
        }
    }

    fun clearForm() {
        weightInput.value = ""
        manualFatInput.value = ""
        notesInput.value = ""
        bicepLeftInput.value = ""
        bicepRightInput.value = ""
        forearmLeftInput.value = ""
        forearmRightInput.value = ""
        thighLeftInput.value = ""
        thighRightInput.value = ""
        calfLeftInput.value = ""
        calfRightInput.value = ""
    }

    /**
     * Saves the current form as a measurement log in the database.
     */
    fun saveMeasurement() {
        viewModelScope.launch {
            val weight = weightInput.value.toDoubleOrNull()
            val height = heightInput.value.toDoubleOrNull()

            if (weight == null || weight <= 0.0) {
                _uiMessage.value = "Por favor, ingresa un peso válido mayor a 0."
                return@launch
            }
            if (height == null || height <= 0.0) {
                _uiMessage.value = "Por favor, ingresa una altura válida mayor a 0."
                return@launch
            }

            // Determine target fat percentage
            val finalFat = if (isFatManual.value) {
                val manualStr = manualFatInput.value
                if (manualStr.isNotBlank()) {
                    val manualVal = manualStr.toDoubleOrNull()
                    if (manualVal == null || manualVal < 0.0 || manualVal > 100.0) {
                        _uiMessage.value = "Ingresa un porcentaje de grasa manual válido (0% - 100%)."
                        return@launch
                    }
                    manualVal
                } else {
                    0.0 // not provided
                }
            } else {
                val calculatedVal = calculatedBodyFat.value
                if (calculatedVal <= 0.0) {
                    _uiMessage.value = "Cálculo de grasa incompleto. Revisa cuello, cintura y cadera."
                    return@launch
                }
                calculatedVal
            }

            val neck = neckInput.value.toDoubleOrNull() ?: 0.0
            val waist = waistInput.value.toDoubleOrNull() ?: 0.0
            val hip = hipInput.value.toDoubleOrNull() ?: 0.0

            if (!isFatManual.value) {
                if (neck <= 0.0 || waist <= 0.0) {
                    _uiMessage.value = "Para calcular la grasa se requiere el cuello y la cintura."
                    return@launch
                }
                if (genderInput.value.lowercase() == "femenino" && hip <= 0.0) {
                    _uiMessage.value = "Las mujeres requieren la medida de la cadera para calcular la grasa."
                    return@launch
                }
            }

            val chest = chestInput.value.toDoubleOrNull() ?: 0.0
            val bicep = bicepInput.value.toDoubleOrNull() ?: 0.0
            val bicepLeft = bicepLeftInput.value.toDoubleOrNull() ?: 0.0
            val bicepRight = bicepRightInput.value.toDoubleOrNull() ?: 0.0
            val thigh = thighInput.value.toDoubleOrNull() ?: 0.0
            val thighLeft = thighLeftInput.value.toDoubleOrNull() ?: 0.0
            val thighRight = thighRightInput.value.toDoubleOrNull() ?: 0.0
            val calf = calfInput.value.toDoubleOrNull() ?: 0.0
            val calfLeft = calfLeftInput.value.toDoubleOrNull() ?: 0.0
            val calfRight = calfRightInput.value.toDoubleOrNull() ?: 0.0

            val forearmLeft = forearmLeftInput.value.toDoubleOrNull() ?: 0.0
            val forearmRight = forearmRightInput.value.toDoubleOrNull() ?: 0.0

            val entity = MeasurementEntity(
                timestamp = System.currentTimeMillis(),
                weight = weight,
                fatPercentage = finalFat,
                height = height,
                neck = neck,
                waist = waist,
                hip = hip,
                chest = chest,
                bicep = bicep,
                bicepLeft = bicepLeft,
                bicepRight = bicepRight,
                forearmLeft = forearmLeft,
                forearmRight = forearmRight,
                thigh = thigh,
                thighLeft = thighLeft,
                thighRight = thighRight,
                calf = calf,
                calfLeft = calfLeft,
                calfRight = calfRight,
                gender = genderInput.value,
                notes = notesInput.value.trim()
            )

            repository.insert(entity)
            _uiMessage.value = "¡Registro guardado con éxito!"
            clearForm()
            _currentTab.value = 0 // Go back to History tab
        }
    }

    /**
     * Delete log from database
     */
    fun deleteMeasurement(entity: MeasurementEntity) {
        viewModelScope.launch {
            repository.delete(entity)
            _uiMessage.value = "Registro eliminado."
        }
    }

    val goalTypeInput = MutableStateFlow("Peso (kg)")
    val goalTargetInput = MutableStateFlow("")

    fun addNewGoal() {
        viewModelScope.launch {
            val targetVal = goalTargetInput.value.toDoubleOrNull()
            if (targetVal == null || targetVal <= 0.0) {
                _uiMessage.value = "Por favor, ingresa un valor de meta válido mayor a 0."
                return@launch
            }
            
            // Determine starting/benchmark value from the latest measurement
            val lastLog = measurements.value.firstOrNull()
            val startingVal = when (goalTypeInput.value) {
                "Peso (kg)" -> lastLog?.weight ?: 0.0
                "Grasa (%)" -> {
                    measurements.value.firstOrNull { it.fatPercentage > 0.0 }?.fatPercentage ?: 0.0
                }
                "Cintura (cm)" -> lastLog?.waist ?: 0.0
                "Cuello (cm)" -> lastLog?.neck ?: 0.0
                "Bíceps (cm)" -> lastLog?.bicep ?: 0.0
                "Pecho (cm)" -> lastLog?.chest ?: 0.0
                else -> 0.0
            }
            
            val goal = GoalEntity(
                type = goalTypeInput.value,
                targetValue = targetVal,
                startingValue = startingVal,
                timestamp = System.currentTimeMillis()
            )
            
            repository.insertGoal(goal)
            _uiMessage.value = "¡Meta de ${goal.type} configurada con éxito!"
            goalTargetInput.value = ""
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _uiMessage.value = "Meta eliminada."
        }
    }

    fun getGoalProgress(goal: GoalEntity, latest: MeasurementEntity?): Pair<Int, String> {
        if (latest == null) return 0 to "Sin registros"
        
        val currentValue = when (goal.type) {
            "Peso (kg)" -> latest.weight
            "Grasa (%)" -> {
                measurements.value.firstOrNull { it.fatPercentage > 0.0 }?.fatPercentage ?: 0.0
            }
            "Cintura (cm)" -> latest.waist
            "Cuello (cm)" -> latest.neck
            "Bíceps (cm)" -> latest.bicep
            "Pecho (cm)" -> latest.chest
            else -> 0.0
        }

        if (currentValue <= 0.0) return 0 to "Falta ingresar medida"

        // Direction of goal: loss (downward) or build (upward)
        val isDownward = goal.targetValue < goal.startingValue
        
        return if (isDownward) {
            if (currentValue <= goal.targetValue) {
                100 to "Meta cumplida 🎉"
            } else {
                val totalToLose = goal.startingValue - goal.targetValue
                val lostSoFar = goal.startingValue - currentValue
                if (totalToLose <= 0.0) 100 to "Meta cumplida 🎉"
                else {
                    val progressPercent = ((lostSoFar / totalToLose) * 100).coerceIn(0.0, 100.0).toInt()
                    val remaining = currentValue - goal.targetValue
                    val unit = if (goal.type.contains("kg")) "kg" else if (goal.type.contains("%")) "%" else "cm"
                    progressPercent to "Faltan ${String.format("%.1f", remaining)} $unit"
                }
            }
        } else {
            if (currentValue >= goal.targetValue) {
                100 to "Meta cumplida 🎉"
            } else {
                val totalToGain = goal.targetValue - goal.startingValue
                val gainedSoFar = currentValue - goal.startingValue
                if (totalToGain <= 0.0) 100 to "Meta cumplida 🎉"
                else {
                    val progressPercent = ((gainedSoFar / totalToGain) * 100).coerceIn(0.0, 100.0).toInt()
                    val remaining = goal.targetValue - currentValue
                    val unit = if (goal.type.contains("kg")) "kg" else if (goal.type.contains("%")) "%" else "cm"
                    progressPercent to "Faltan ${String.format("%.1f", remaining)} $unit"
                }
            }
        }
    }

    fun getAdvancedStats(): AdvStats {
        val rawList = measurements.value
        if (rawList.isEmpty()) return AdvStats()

        val chronList = rawList.sortedBy { it.timestamp }
        val first = chronList.first()
        val latest = chronList.last()

        val weightChange = latest.weight - first.weight
        
        val firstWithFat = chronList.firstOrNull { it.fatPercentage > 0.0 }
        val latestWithFat = chronList.lastOrNull { it.fatPercentage > 0.0 }
        val fatChange = if (firstWithFat != null && latestWithFat != null) {
            latestWithFat.fatPercentage - firstWithFat.fatPercentage
        } else {
            0.0
        }

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val sevenDaysMs = 7 * oneDayMs
        val thirtyDaysMs = 30 * oneDayMs

        // Weekly splits
        val recent7DaysLogs = chronList.filter { it.timestamp >= now - sevenDaysMs }
        val previous7DaysLogs = chronList.filter { it.timestamp in (now - 2 * sevenDaysMs)..(now - sevenDaysMs) }

        val weeklyWeightCur = if (recent7DaysLogs.isNotEmpty()) recent7DaysLogs.map { it.weight }.average() else latest.weight
        val weeklyWeightPrev = if (previous7DaysLogs.isNotEmpty()) previous7DaysLogs.map { it.weight }.average() else first.weight

        val recent7FatLogs = recent7DaysLogs.filter { it.fatPercentage > 0.0 }
        val prev7FatLogs = previous7DaysLogs.filter { it.fatPercentage > 0.0 }
        val weeklyFatCur = if (recent7FatLogs.isNotEmpty()) recent7FatLogs.map { it.fatPercentage }.average() else latest.fatPercentage
        val weeklyFatPrev = if (prev7FatLogs.isNotEmpty()) prev7FatLogs.map { it.fatPercentage }.average() else first.fatPercentage

        // Monthly splits
        val recent30DaysLogs = chronList.filter { it.timestamp >= now - thirtyDaysMs }
        val previous30DaysLogs = chronList.filter { it.timestamp in (now - 2 * thirtyDaysMs)..(now - thirtyDaysMs) }

        val monthlyWeightCur = if (recent30DaysLogs.isNotEmpty()) recent30DaysLogs.map { it.weight }.average() else latest.weight
        val monthlyWeightPrev = if (previous30DaysLogs.isNotEmpty()) previous30DaysLogs.map { it.weight }.average() else first.weight

        val recent30FatLogs = recent30DaysLogs.filter { it.fatPercentage > 0.0 }
        val prev30FatLogs = previous30DaysLogs.filter { it.fatPercentage > 0.0 }
        val monthlyFatCur = if (recent30FatLogs.isNotEmpty()) recent30FatLogs.map { it.fatPercentage }.average() else latest.fatPercentage
        val monthlyFatPrev = if (prev30FatLogs.isNotEmpty()) prev30FatLogs.map { it.fatPercentage }.average() else first.fatPercentage

        return AdvStats(
            totalWeightChange = weightChange,
            totalFatChange = fatChange,
            weeklyAvgWeightCurrent = weeklyWeightCur,
            weeklyAvgWeightPrevious = weeklyWeightPrev,
            monthlyAvgWeightCurrent = monthlyWeightCur,
            monthlyAvgWeightPrevious = monthlyWeightPrev,
            weeklyAvgFatCurrent = weeklyFatCur,
            weeklyAvgFatPrevious = weeklyFatPrev,
            monthlyAvgFatCurrent = monthlyFatCur,
            monthlyAvgFatPrevious = monthlyFatPrev,
            recentWeight = latest.weight,
            initialWeight = first.weight,
            recentFat = latest.fatPercentage,
            initialFat = first.fatPercentage
        )
    }

    // Factory to instantiate ViewModel safely
    class Factory(private val repository: MeasurementRepository, private val context: android.content.Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MeasurementViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MeasurementViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
