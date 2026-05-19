package com.example.rttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.example.rttracker.data.WorkoutDatabase
import com.example.rttracker.ui.home.HomeScreen
import com.example.rttracker.ui.theme.RtTrackerTheme
import com.example.rttracker.ui.workout.WorkoutScreen
import com.example.rttracker.viewmodel.HomeViewModel
import com.example.rttracker.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        val db = WorkoutDatabase.getDatabase(applicationContext)
        val workoutDao = db.workoutDao()

        setContent {
            RtTrackerTheme {
                MainAppScreen(workoutDao)
            }
        }
    }
}

@Composable
fun MainAppScreen(workoutDao: com.example.rttracker.data.WorkoutDao) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()

    val items = listOf(
        NavigationItem("home", "Home", Icons.Default.Home),
        NavigationItem("workout", "Workout", Icons.Default.FitnessCenter)
    )

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color.Transparent)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = false
                                            }
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 48.dp, height = 32.dp)
                                    .background(
                                        color = if (isSelected) com.example.rttracker.ui.theme.ObsidianPurple.copy(alpha = 0.18f) else Color.Transparent,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 14.dp,
                                            bottomEnd = 12.dp
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) com.example.rttracker.ui.theme.ObsidianPurple else com.example.rttracker.ui.theme.ObsidianTextDim,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                fontFamily = com.example.rttracker.ui.theme.OpenDyslexic,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) com.example.rttracker.ui.theme.ObsidianPurple else com.example.rttracker.ui.theme.ObsidianTextDim
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Instantiate shared view models above NavHost
        val homeViewModel: HomeViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(workoutDao) as T
                }
            }
        )
        val workoutViewModel: WorkoutViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WorkoutViewModel(workoutDao) as T
                }
            }
        )

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(120)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(120)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(120)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(120)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(120)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(120)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(120)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(120)
                )
            }
        ) {
            composable("home") {
                val workoutDates by homeViewModel.workoutDates.collectAsState()
                val leastRecentMuscleGroups by homeViewModel.leastRecentExercises.collectAsState()
                val selectedDayWorkouts by homeViewModel.selectedDayWorkouts.collectAsState()
                val selectedDate by homeViewModel.selectedDate.collectAsState()

                HomeScreen(
                    workoutDates = workoutDates,
                    leastRecentMuscleGroups = leastRecentMuscleGroups,
                    selectedDayWorkouts = selectedDayWorkouts,
                    selectedDate = selectedDate,
                    onDateSelected = { homeViewModel.selectDate(it) },
                    onDeleteSession = { sessionId -> 
                        homeViewModel.deleteSession(sessionId)
                        if (workoutViewModel.editingSessionId.value == sessionId) {
                            workoutViewModel.clearWorkoutState()
                        }
                    },
                    onDeleteSet = { setId -> homeViewModel.deleteSet(setId) },
                    onUpdateSet = { setId, weight, reps -> homeViewModel.updateSet(setId, weight, reps) },
                    onNavigateToWorkout = { customDate ->
                        workoutViewModel.startNewWorkout(customDate)
                        navController.navigate("workout")
                    },
                    onEditSession = { sessionId ->
                        workoutViewModel.loadExistingSession(sessionId)
                        navController.navigate("workout")
                    },
                    onEditSessionByDate = { dateMs ->
                        workoutViewModel.loadExistingSessionForDate(dateMs)
                        navController.navigate("workout")
                    }
                )
            }
            composable("workout") {
                val exercises by workoutViewModel.exercises.collectAsState()
                val allExercises by workoutViewModel.allExercises.collectAsState()
                val customWorkoutDate by workoutViewModel.customWorkoutDate.collectAsState()

                WorkoutScreen(
                    exercises = exercises,
                    allExercises = allExercises,
                    onAddSet = { workoutViewModel.addSet(it) },
                    onDeleteSet = { exIdx, setIdx -> workoutViewModel.deleteSet(exIdx, setIdx) },
                    onUpdateSet = { exIdx, setIdx, w, r ->
                        workoutViewModel.updateSet(exIdx, setIdx, w, r)
                    },
                    onFinishWorkout = {
                        coroutineScope.launch {
                            val targetDate = customWorkoutDate ?: System.currentTimeMillis()
                            val job = workoutViewModel.finishWorkout()
                            job.join()
                            homeViewModel.selectDate(targetDate)
                            navController.popBackStack()
                        }
                    },
                    onAddExercise = { name, muscleGroup ->
                        workoutViewModel.addExercise(name, muscleGroup)
                    },
                    onDeleteExercise = { exercise ->
                        workoutViewModel.deleteExercise(exercise)
                    },
                    customWorkoutDate = customWorkoutDate
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
