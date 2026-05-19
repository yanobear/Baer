package com.example.rttracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertExercise(exercise: Exercise): Long

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSet(set: ExerciseSet)

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("""
        SELECT * FROM exercise_sets 
        WHERE exerciseId = :exerciseId 
        AND sessionId = (
            SELECT sessionId FROM exercise_sets 
            WHERE exerciseId = :exerciseId 
            AND sessionId < :currentSessionId 
            ORDER BY sessionId DESC LIMIT 1
        )
    """)
    suspend fun getPreviousSessionSets(exerciseId: Long, currentSessionId: Long): List<ExerciseSet>

    @Query("SELECT DISTINCT s.date FROM workout_sessions s INNER JOIN exercise_sets e ON s.id = e.sessionId")
    fun getWorkoutDates(): Flow<List<Long>>

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): Exercise?

    @Query("""
        SELECT muscleGroup 
        FROM exercises 
        LEFT JOIN exercise_sets ON exercises.id = exercise_sets.exerciseId 
        LEFT JOIN workout_sessions ON exercise_sets.sessionId = workout_sessions.id 
        WHERE (SELECT COUNT(*) FROM exercise_sets) > 0
        GROUP BY muscleGroup 
        ORDER BY COALESCE(MAX(date), 0) ASC
    """)
    fun getMuscleGroupsDoneLeastRecently(): Flow<List<String>>

    @Query("""
        SELECT s.id as setId, s.sessionId, ws.date as sessionDate, e.name as exerciseName, e.muscleGroup, s.weight, s.reps 
        FROM exercise_sets s 
        JOIN exercises e ON s.exerciseId = e.id 
        JOIN workout_sessions ws ON s.sessionId = ws.id
        WHERE ws.date >= :startOfDay AND ws.date < :endOfDay
        ORDER BY ws.date ASC, s.id ASC
    """)
    fun getWorkoutDetailsForDate(startOfDay: Long, endOfDay: Long): Flow<List<SessionWorkoutDetail>>

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM exercise_sets WHERE id = :setId")
    suspend fun deleteSet(setId: Long)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("UPDATE exercise_sets SET weight = :weight, reps = :reps WHERE id = :setId")
    suspend fun updateSet(setId: Long, weight: Float, reps: Int)

    @Query("UPDATE exercises SET muscleGroup = 'Quads' WHERE muscleGroup = 'Legs'")
    suspend fun updateLegsMuscleGroups()

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY `order` ASC")
    suspend fun getSetsForSession(sessionId: Long): List<ExerciseSet>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC LIMIT 1")
    suspend fun getSessionForDateRange(startOfDay: Long, endOfDay: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    suspend fun getAllSessionsList(): List<WorkoutSession>
}
