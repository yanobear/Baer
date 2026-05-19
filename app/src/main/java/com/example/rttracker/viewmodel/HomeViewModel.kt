package com.example.rttracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rttracker.data.SessionWorkoutDetail
import com.example.rttracker.data.WorkoutDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar

class HomeViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    val workoutDates: StateFlow<List<Long>> = workoutDao.getWorkoutDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leastRecentExercises: StateFlow<List<String>> = workoutDao.getMuscleGroupsDoneLeastRecently()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDayWorkouts: StateFlow<List<SessionWorkoutDetail>> = _selectedDate
        .flatMapLatest { timestamp ->
            if (timestamp == null) {
                flowOf(emptyList<SessionWorkoutDetail>())
            } else {
                val cal = Calendar.getInstance()
                cal.timeInMillis = timestamp
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val endOfDay = cal.timeInMillis

                workoutDao.getWorkoutDetailsForDate(startOfDay, endOfDay)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        selectDate(System.currentTimeMillis())
    }

    fun selectDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            workoutDao.deleteSetsForSession(sessionId)
            workoutDao.deleteSession(sessionId)
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            workoutDao.deleteSet(setId)
        }
    }

    fun updateSet(setId: Long, weight: Float, reps: Int) {
        viewModelScope.launch {
            workoutDao.updateSet(setId, weight, reps)
        }
    }
}
