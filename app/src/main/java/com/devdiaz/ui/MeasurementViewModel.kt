package com.devdiaz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devdiaz.data.GoalEntity
import com.devdiaz.data.MeasurementEntity
import com.devdiaz.data.MeasurementRepository
import com.devdiaz.utils.BodyCalculator
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

    // LITE or PRO Mode state from SharedPreferences
    val isLiteMode = MutableStateFlow(sharedPrefs.getBoolean("isLiteMode", false))
    val useLb = MutableStateFlow(false)

    // --- DYNAMIC GOAL SETTING (RF-06 & RF-07) ---
    val userGoalType = MutableStateFlow(sharedPrefs.getString("userGoalType", "Reducción de Peso / Pérdida de Grasa") ?: "Reducción de Peso / Pérdida de Grasa")
    val userTargetWeight = MutableStateFlow(sharedPrefs.getString("userTargetWeight", "") ?: "")
    val userTargetFat = MutableStateFlow(sharedPrefs.getString("userTargetFat", "") ?: "")
    val userActivityLevel = MutableStateFlow(sharedPrefs.getString("userActivityLevel", "Moderado") ?: "Moderado")

    fun toggleLiteMode(enabled: Boolean) {
        sharedPrefs.edit().apply {
            putBoolean("isLiteMode", enabled)
            putBoolean("useLb", false)
        }.apply()
        isLiteMode.value = enabled
        useLb.value = false
    }

    fun toggleUseLb(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("useLb", false).apply()
        useLb.value = false
    }

    fun saveGoalSettings(goalType: String, targetWeight: String, targetFat: String, activityLevel: String) {
        val latestMeas = measurements.value.firstOrNull()
        val tWeight = targetWeight.toDoubleOrNull()

        if (tWeight != null && tWeight > 0.0) {
            val goalEnum = goalType.ifBlank { "Reducción de Peso / Pérdida de Grasa" }
            if (latestMeas == null) {
                _uiMessage.value = "Primero registra una medición de peso actual en 'Registrar' para poder comprobar tu meta."
                return
            }
            val currentWeight = latestMeas.weight
            val unitStr = "kg"

            if (goalEnum == "Reducción de Peso / Pérdida de Grasa") {
                if (tWeight >= currentWeight) {
                    _uiMessage.value = "Para Reducción de Peso, el peso objetivo ($targetWeight $unitStr) debe ser menor al actual (${String.format("%.1f", currentWeight)} $unitStr)."
                    return
                }
            } else if (goalEnum == "Aumento de Peso / Ganancia de Masa Muscular") {
                if (tWeight <= currentWeight) {
                    _uiMessage.value = "Para Aumento de Peso, el peso objetivo ($targetWeight $unitStr) debe ser mayor al actual (${String.format("%.1f", currentWeight)} $unitStr)."
                    return
                }
            }
        }

        sharedPrefs.edit().apply {
            putString("userGoalType", goalType)
            putString("userTargetWeight", targetWeight)
            putString("userTargetFat", targetFat)
            putString("userActivityLevel", activityLevel)
            apply()
        }
        userGoalType.value = goalType
        userTargetWeight.value = targetWeight
        userTargetFat.value = targetFat
        userActivityLevel.value = activityLevel

        viewModelScope.launch {
            val latestMeasInner = measurements.value.firstOrNull()
            if (tWeight != null && tWeight > 0.0) {
                val weightInKg = tWeight
                val startingWeight = latestMeasInner?.weight ?: weightInKg
                
                val existingPesoGoals = goals.value.filter { it.type == "Peso (kg)" }
                existingPesoGoals.forEach { repository.deleteGoal(it) }
                
                repository.insertGoal(GoalEntity(
                    type = "Peso (kg)",
                    targetValue = weightInKg,
                    startingValue = startingWeight,
                    timestamp = System.currentTimeMillis()
                ))
            }
            
            val tFat = targetFat.toDoubleOrNull()
            if (tFat != null && tFat > 0.0) {
                val startingFat = latestMeasInner?.fatPercentage ?: tFat
                val existingFatGoals = goals.value.filter { it.type == "Grasa (%)" }
                existingFatGoals.forEach { repository.deleteGoal(it) }
                
                repository.insertGoal(GoalEntity(
                    type = "Grasa (%)",
                    targetValue = tFat,
                    startingValue = startingFat,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        _uiMessage.value = "¡Hoja de ruta guardada con éxito!"
    }

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
    private var previousTab = 0

    fun selectTab(tab: Int) {
        if (_currentTab.value != tab && _currentTab.value != 4) {
            previousTab = _currentTab.value
        }
        _currentTab.value = tab
    }

    fun goBackFromProfile() {
        _currentTab.value = previousTab
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
    val forearmInput = MutableStateFlow("")
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

    fun showUiMessage(message: String) {
        _uiMessage.value = message
    }

    // --- REALTIME DYNAMIC CALCULATIONS ---
    // Combined flow calculates BMI dynamically as user types weight/height
    val calculatedBmi: StateFlow<Double> = combine(weightInput, heightInput, useLb) { weightStr, heightStr, _ ->
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
                forearmInput.value = if (last.forearm > 0) last.forearm.toString() else ""
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
        forearmInput.value = ""
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
            // Helper function to validate double inputs
            fun validateField(valueStr: String, fieldName: String, isRequired: Boolean, min: Double, max: Double, unit: String = "cm"): Double? {
                val trimmed = valueStr.trim()
                if (trimmed.isEmpty()) {
                    if (isRequired) {
                        _uiMessage.value = "La medida de $fieldName es requerida."
                        return null
                    }
                    return 0.0
                }
                val parsed = trimmed.toDoubleOrNull()
                if (parsed == null) {
                    _uiMessage.value = "Por favor ingresa un número válido para: $fieldName"
                    return null
                }
                if (parsed <= 0.0) {
                    _uiMessage.value = "La medida de $fieldName debe ser un número positivo mayor que cero."
                    return null
                }
                if (parsed < min || parsed > max) {
                    _uiMessage.value = "La medida de $fieldName tiene un valor fuera del rango lógico ($min $unit - $max $unit)."
                    return null
                }
                return parsed
            }

            // 1. Validate Weight
            val weight = validateField(weightInput.value, "Peso", isRequired = true, min = 20.0, max = 600.0, unit = "kg")
                ?: return@launch

            // 2. Validate Height
            val height = validateField(heightInput.value, "Altura", isRequired = true, min = 50.0, max = 280.0, unit = "cm")
                ?: return@launch

            // 3. Validate optional/required body tape measurements
            // If we use Calculated Fat %, neck, waist, (and hip for females) are strictly required!
            val gender = genderInput.value
            val isFemale = gender.lowercase() == "femenino"
            val isNavyFat = !isFatManual.value

            val neck = validateField(neckInput.value, "Cuello", isRequired = isNavyFat, min = 15.0, max = 80.0)
                ?: return@launch
            val waist = validateField(waistInput.value, "Cintura", isRequired = isNavyFat, min = 30.0, max = 250.0)
                ?: return@launch
            val hip = validateField(hipInput.value, "Cadera", isRequired = (isNavyFat && isFemale), min = 40.0, max = 250.0)
                ?: return@launch

            // 4. Check basic logical consistency between relational fields
            if (neck > 0.0 && waist > 0.0 && neck >= waist) {
                _uiMessage.value = "El cuello ($neck cm) debe ser menor que la cintura ($waist cm)."
                return@launch
            }
            if (height > 0.0) {
                if (neck > 0.0 && neck >= height) {
                    _uiMessage.value = "El cuello ($neck cm) debe ser menor que la altura ($height cm)."
                    return@launch
                }
                if (waist > 0.0 && waist >= height) {
                    _uiMessage.value = "La cintura ($waist cm) debe ser menor que la altura ($height cm)."
                    return@launch
                }
                if (hip > 0.0 && hip >= height) {
                    _uiMessage.value = "La cadera ($hip cm) debe ser menor que la altura ($height cm)."
                    return@launch
                }
            }

            // 5. Determine target fat percentage
            val finalFat = if (isFatManual.value) {
                val manualStr = manualFatInput.value.trim()
                if (manualStr.isNotEmpty()) {
                    val manualVal = manualStr.toDoubleOrNull()
                    if (manualVal == null) {
                        _uiMessage.value = "Por favor ingresa un número válido para el % de grasa."
                        return@launch
                    }
                    if (manualVal < 0.0 || manualVal > 100.0) {
                        _uiMessage.value = "El porcentaje de grasa manual debe estar entre 0% y 100%."
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

            // 6. Validate rest of optional fields
            val chest = validateField(chestInput.value, "Pecho", isRequired = false, min = 40.0, max = 200.0)
                ?: return@launch
            val bicep = validateField(bicepInput.value, "Bíceps", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch
            val bicepLeft = validateField(bicepLeftInput.value, "Bíceps Izquierdo", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch
            val bicepRight = validateField(bicepRightInput.value, "Bíceps Derecho", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch
            val forearm = validateField(forearmInput.value, "Antebrazo", isRequired = false, min = 10.0, max = 80.0)
                ?: return@launch
            val thigh = validateField(thighInput.value, "Muslo", isRequired = false, min = 20.0, max = 150.0)
                ?: return@launch
            val thighLeft = validateField(thighLeftInput.value, "Muslo Izquierdo", isRequired = false, min = 20.0, max = 150.0)
                ?: return@launch
            val thighRight = validateField(thighRightInput.value, "Muslo Derecho", isRequired = false, min = 20.0, max = 150.0)
                ?: return@launch
            val calf = validateField(calfInput.value, "Pantorrilla", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch
            val calfLeft = validateField(calfLeftInput.value, "Pantorrilla Izquierda", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch
            val calfRight = validateField(calfRightInput.value, "Pantorrilla Derecha", isRequired = false, min = 10.0, max = 100.0)
                ?: return@launch

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
                forearm = forearm,
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

    fun getAdvancedStats(customList: List<MeasurementEntity>? = null): AdvStats {
        val rawList = customList ?: measurements.value
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
