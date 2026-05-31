package com.example.rttracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rttracker.data.Exercise
import com.example.rttracker.data.ExerciseSet
import com.example.rttracker.data.WorkoutDao
import com.example.rttracker.data.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar


data class ExerciseState(
    val exercise: Exercise,
    val sets: List<MutableSetState> = listOf(MutableSetState()),
    val previousSessionSets: List<ExerciseSet> = emptyList()
)

data class MutableSetState(
    val weight: String = "",
    val reps: String = ""
)

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    private val _exercises = MutableStateFlow<List<ExerciseState>>(emptyList())
    val exercises: StateFlow<List<ExerciseState>> = _exercises.asStateFlow()

    val allExercises: StateFlow<List<Exercise>> = workoutDao.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customWorkoutDate = MutableStateFlow<Long?>(null)
    val customWorkoutDate: StateFlow<Long?> = _customWorkoutDate.asStateFlow()

    private val _editingSessionId = MutableStateFlow<Long?>(null)
    val editingSessionId: StateFlow<Long?> = _editingSessionId.asStateFlow()

    fun setCustomWorkoutDate(timestamp: Long?) {
        _customWorkoutDate.value = timestamp
    }

    fun startNewWorkout(customDate: Long?) {
        _exercises.value = emptyList()
        _customWorkoutDate.value = customDate
        _editingSessionId.value = null
    }

    fun loadExistingSession(sessionId: Long) {
        viewModelScope.launch {
            _editingSessionId.value = sessionId
            val session = workoutDao.getSessionById(sessionId)
            if (session != null) {
                _customWorkoutDate.value = session.date
            }
            
            val sets = workoutDao.getSetsForSession(sessionId)
            val groupedByExercise = sets.groupBy { it.exerciseId }
            val loadedExercises = mutableListOf<ExerciseState>()
            
            groupedByExercise.forEach { (exerciseId, exerciseSets) ->
                val exercise = workoutDao.getExerciseById(exerciseId)
                if (exercise != null) {
                    val previous = workoutDao.getPreviousSessionSets(exerciseId, sessionId)
                    val mappedSets = exerciseSets.map { set ->
                        MutableSetState(
                            weight = if (set.weight % 1f == 0f) set.weight.toInt().toString() else set.weight.toString(),
                            reps = set.reps.toString()
                        )
                    }
                    loadedExercises.add(
                        ExerciseState(
                            exercise = exercise,
                            sets = mappedSets,
                            previousSessionSets = previous
                        )
                    )
                }
            }
            
            _exercises.value = loadedExercises
        }
    }

    fun loadExistingSessionForDate(dateMs: Long) {
        viewModelScope.launch {
            val targetCal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val sessions = workoutDao.getAllSessionsList()
            val matchingSession = sessions.firstOrNull { session ->
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.date }
                sessionCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                sessionCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
            }
            if (matchingSession != null) {
                loadExistingSession(matchingSession.id)
            } else {
                startNewWorkout(dateMs)
            }
        }
    }
    init {
        viewModelScope.launch {
            workoutDao.updateLegsMuscleGroups()
        }
    }

    fun addExercise(name: String, muscleGroup: String) {
        viewModelScope.launch {
            var exercise = workoutDao.getExerciseByName(name)
            if (exercise == null) {
                val id = workoutDao.insertExercise(Exercise(name = name, muscleGroup = muscleGroup))
                exercise = Exercise(id = id, name = name, muscleGroup = muscleGroup)
            }

            val previous = workoutDao.getPreviousSessionSets(exercise.id, System.currentTimeMillis())
            _exercises.value = _exercises.value + ExerciseState(exercise, previousSessionSets = previous)
        }
    }

    fun addSet(exerciseIndex: Int) {
        val current = _exercises.value.toMutableList()
        val exercise = current[exerciseIndex]
        current[exerciseIndex] = exercise.copy(sets = exercise.sets + MutableSetState())
        _exercises.value = current
    }

    fun deleteSet(exerciseIndex: Int, setIndex: Int) {
        val current = _exercises.value.toMutableList()
        val exercise = current[exerciseIndex]
        val sets = exercise.sets.toMutableList()
        if (sets.size > 1) {
            sets.removeAt(setIndex)
            current[exerciseIndex] = exercise.copy(sets = sets)
            _exercises.value = current
        } else {
            // Remove exercise card entirely if the last set is deleted
            current.removeAt(exerciseIndex)
            _exercises.value = current
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: String? = null, reps: String? = null) {
        val current = _exercises.value.toMutableList()
        val exercise = current[exerciseIndex]
        val sets = exercise.sets.toMutableList()
        val set = sets[setIndex]
        sets[setIndex] = set.copy(
            weight = weight ?: set.weight,
            reps = reps ?: set.reps
        )
        current[exerciseIndex] = exercise.copy(sets = sets)
        _exercises.value = current
    }

    fun finishWorkout(): kotlinx.coroutines.Job {
        return viewModelScope.launch {
            val sessionDate = _customWorkoutDate.value ?: System.currentTimeMillis()
            val existingSessionId = _editingSessionId.value
            val sessionId = if (existingSessionId != null) {
                workoutDao.deleteSetsForSession(existingSessionId)
                existingSessionId
            } else {
                workoutDao.insertSession(WorkoutSession(date = sessionDate))
            }
            
            var insertedAnySet = false
            var setOrder = 0
            _exercises.value.forEach { exerciseState ->
                exerciseState.sets.forEach { set ->
                    if (set.weight.isNotEmpty() && set.reps.isNotEmpty()) {
                        workoutDao.insertSet(
                            ExerciseSet(
                                exerciseId = exerciseState.exercise.id,
                                sessionId = sessionId,
                                weight = set.weight.toFloatOrNull() ?: 0f,
                                reps = set.reps.toIntOrNull() ?: 0,
                                order = setOrder++
                            )
                        )
                        insertedAnySet = true
                    }
                }
            }
            
            if (!insertedAnySet) {
                workoutDao.deleteSession(sessionId)
            }

            _exercises.value = emptyList() // Clear for next time
            _customWorkoutDate.value = null // Reset custom date
            _editingSessionId.value = null // Reset editing session ID
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            workoutDao.deleteExercise(exercise)
            // Prevent foreign key constraint crashes if deleted exercise is currently in the active workout list
            _exercises.value = _exercises.value.filter { it.exercise.id != exercise.id }
        }
    }

    fun clearWorkoutState() {
        _exercises.value = emptyList()
        _customWorkoutDate.value = null
        _editingSessionId.value = null
    }
}
