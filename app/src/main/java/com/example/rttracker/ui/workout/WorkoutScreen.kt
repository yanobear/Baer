package com.example.rttracker.ui.workout

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import com.example.rttracker.ui.theme.ObsidianPurple
import com.example.rttracker.ui.theme.ObsidianSurface
import com.example.rttracker.ui.theme.ObsidianTextDim
import com.example.rttracker.ui.theme.OpenDyslexic
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.rttracker.ui.theme.ObsidianAccent
import com.example.rttracker.viewmodel.ExerciseState
import androidx.compose.ui.tooling.preview.Preview
import com.example.rttracker.ui.theme.RtTrackerTheme
import com.example.rttracker.data.Exercise
import com.example.rttracker.viewmodel.MutableSetState

@Preview(showBackground = true, backgroundColor = 0xFF171717)
@Composable
fun WorkoutScreenPreview() {
    RtTrackerTheme {
        WorkoutScreen(
            exercises = listOf(
                ExerciseState(
                    exercise = Exercise(name = "Bench Press", muscleGroup = "Chest"),
                    sets = listOf(
                        MutableSetState(weight = "60", reps = "10"),
                        MutableSetState()
                    )
                )
            ),
            allExercises = listOf(
                Exercise(name = "Bench Press", muscleGroup = "Chest"),
                Exercise(name = "Squat", muscleGroup = "Quads")
            ),
            onAddSet = { _ -> },
            onDeleteSet = { _, _ -> },
            onUpdateSet = { _, _, _, _ -> },
            onFinishWorkout = {},
            onCancelWorkout = {},
            onAddExercise = { _, _ -> },
            onDeleteExercise = {}
        )
    }
}

@Composable
fun SetInputCapsule(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFocused) ObsidianPurple else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontFamily = OpenDyslexic,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = ObsidianTextDim.copy(alpha = 0.3f),
                        fontFamily = OpenDyslexic,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun MuscleGroupChips(
    selectedGroup: String,
    onSelectedChange: (String) -> Unit,
    muscleGroups: List<String>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(muscleGroups) { group ->
            val isSelected = selectedGroup == group
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) ObsidianPurple.copy(alpha = 0.18f) else ObsidianSurface,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) ObsidianPurple else ObsidianSurface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectedChange(group) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group,
                    fontFamily = OpenDyslexic,
                    color = if (isSelected) ObsidianPurple else ObsidianTextDim,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WorkoutScreen(
    exercises: List<ExerciseState>,
    allExercises: List<Exercise>,
    onAddSet: (Int) -> Unit,
    onDeleteSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, String?, String?) -> Unit,
    onFinishWorkout: () -> Unit,
    onCancelWorkout: () -> Unit,
    onAddExercise: (String, String) -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    customWorkoutDate: Long? = null,
    isEditing: Boolean = false
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    if (showCancelConfirm) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCancelConfirm = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianPurple.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Discard changes?",
                        fontFamily = OpenDyslexic,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Are you sure you want to discard your changes? This action cannot be undone.",
                        fontFamily = OpenDyslexic,
                        fontSize = 12.sp,
                        color = ObsidianTextDim,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showCancelConfirm = false },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianTextDim.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Keep Editing",
                                fontFamily = OpenDyslexic,
                                color = ObsidianTextDim,
                                fontSize = 12.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Button(
                            onClick = {
                                showCancelConfirm = false
                                onCancelWorkout()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianPurple),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Discard",
                                fontFamily = OpenDyslexic,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
    var activeTab by remember { mutableStateOf(0) } // 0 = Existing, 1 = Custom
    var searchQuery by remember { mutableStateOf("") }
    var newExerciseName by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("Chest") }
    
    val muscleGroups = listOf("Chest", "Shoulders", "Triceps", "Biceps", "Back", "Quads", "Hamstrings")

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ObsidianPurple.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add Exercise",
                        fontFamily = OpenDyslexic,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Segmented Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (activeTab == 0) ObsidianPurple else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Search", 
                                fontFamily = OpenDyslexic,
                                color = if (activeTab == 0) Color.Black else ObsidianTextDim,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (activeTab == 1) ObsidianPurple else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Create", 
                                fontFamily = OpenDyslexic,
                                color = if (activeTab == 1) Color.Black else ObsidianTextDim,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (activeTab == 0) {
                        // Search bar
                        var isSearchFocused by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search", fontFamily = OpenDyslexic, color = ObsidianTextDim) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ObsidianTextDim) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(20.dp)
                                            .background(
                                                color = Color.White.copy(alpha = 0.1f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .clickable { searchQuery = "" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            textStyle = LocalTextStyle.current.copy(fontFamily = OpenDyslexic, color = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObsidianPurple,
                                unfocusedBorderColor = ObsidianTextDim.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val filteredExercises = allExercises.filter {
                            it.name.contains(searchQuery, ignoreCase = true) || 
                            it.muscleGroup.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredExercises.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No exercises found.", fontFamily = OpenDyslexic, color = ObsidianTextDim)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.height(180.dp)) {
                                items(filteredExercises) { ex ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Clickable area to add exercise
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        onAddExercise(ex.name, ex.muscleGroup)
                                                        showAddDialog = false
                                                        searchQuery = ""
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = ex.name, 
                                                        fontFamily = OpenDyslexic, 
                                                        fontWeight = FontWeight.SemiBold, 
                                                        fontSize = 14.sp,
                                                        color = Color.White
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = ex.muscleGroup, 
                                                        fontFamily = OpenDyslexic,
                                                        fontSize = 10.sp, 
                                                        color = ObsidianPurple,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(ObsidianPurple.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add",
                                                        tint = ObsidianPurple,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            // Delete Exercise Button (independently clickable)
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 8.dp, start = 8.dp)
                                                    .size(28.dp)
                                                    .background(
                                                        color = ObsidianPurple.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = ObsidianPurple.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { onDeleteExercise(ex) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Exercise",
                                                    tint = ObsidianPurple,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color.White.copy(alpha = 0.05f))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Create custom form tab
                        var isNameFocused by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = newExerciseName,
                            onValueChange = { newExerciseName = it },
                            label = { Text("Exercise Name", fontFamily = OpenDyslexic) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isNameFocused = it.isFocused },
                            textStyle = LocalTextStyle.current.copy(fontFamily = OpenDyslexic, color = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObsidianPurple,
                                unfocusedBorderColor = ObsidianTextDim.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Muscle Group:", fontFamily = OpenDyslexic, fontSize = 13.sp, color = ObsidianTextDim)
                        
                        MuscleGroupChips(
                            selectedGroup = selectedMuscleGroup,
                            onSelectedChange = { selectedMuscleGroup = it },
                            muscleGroups = muscleGroups
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (activeTab == 1) Arrangement.SpaceBetween else Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontFamily = OpenDyslexic, color = ObsidianTextDim)
                        }
                        if (activeTab == 1) {
                            val isNameEmpty = newExerciseName.isBlank()
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isNameEmpty) 0.5f else 1.0f
                                    }
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(ObsidianPurple, ObsidianAccent)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = !isNameEmpty) {
                                        onAddExercise(newExerciseName, selectedMuscleGroup)
                                        newExerciseName = ""
                                        showAddDialog = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Add Custom", 
                                    fontFamily = OpenDyslexic, 
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianPurple.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .background(ObsidianPurple, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (customWorkoutDate != null) "Active Session" else "Start Workout",
                                fontFamily = OpenDyslexic,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (customWorkoutDate != null) {
                                    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(customWorkoutDate))
                                } else {
                                    "Logging workout session"
                                },
                                fontFamily = OpenDyslexic,
                                fontSize = 12.sp,
                                color = ObsidianPurple
                            )
                        }
                    }
                }
            }

            itemsIndexed(exercises) { exerciseIndex, exerciseState ->
                ExerciseCard(
                    exerciseState = exerciseState,
                    onAddSet = { onAddSet(exerciseIndex) },
                    onDeleteSet = { setIndex -> onDeleteSet(exerciseIndex, setIndex) },
                    onUpdateSet = { setIndex, weight, reps -> 
                        onUpdateSet(exerciseIndex, setIndex, weight, reps) 
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = ObsidianPurple.copy(alpha = 0.4f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObsidianPurple)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercise", fontFamily = OpenDyslexic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        val canFinish = remember(exercises) {
            val allSets = exercises.flatMap { it.sets }
            allSets.isNotEmpty() && allSets.all { set -> set.weight.isNotBlank() && set.reps.isNotBlank() }
        }

        if (exercises.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val hasChanges = exercises.isNotEmpty()
                        if (hasChanges) {
                            showCancelConfirm = true
                        } else {
                            onCancelWorkout()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = ObsidianTextDim.copy(alpha = 0.4f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ObsidianTextDim)
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = OpenDyslexic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ObsidianTextDim
                    )
                }

                Button(
                    onClick = onFinishWorkout,
                    enabled = canFinish,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (canFinish) {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(ObsidianPurple, ObsidianAccent)
                                    )
                                } else {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(
                                            ObsidianSurface,
                                            ObsidianSurface
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (canFinish) Color.Transparent else ObsidianTextDim.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEditing) "Save" else "End",
                            fontFamily = OpenDyslexic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (canFinish) Color.Black else ObsidianTextDim.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exerciseState: ExerciseState,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onUpdateSet: (Int, String?, String?) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianPurple.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseState.exercise.name,
                    fontFamily = OpenDyslexic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .background(ObsidianPurple.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exerciseState.exercise.muscleGroup,
                        fontFamily = OpenDyslexic,
                        fontSize = 10.sp,
                        color = ObsidianPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Sets Table Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET",
                    fontFamily = OpenDyslexic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianTextDim,
                    modifier = Modifier.width(38.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "PREV",
                    fontFamily = OpenDyslexic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianTextDim,
                    modifier = Modifier.weight(0.9f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "KG",
                    fontFamily = OpenDyslexic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianTextDim,
                    modifier = Modifier.weight(1.1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "REPS",
                    fontFamily = OpenDyslexic,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianTextDim,
                    modifier = Modifier.weight(1.1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(36.dp)) // space for delete action
            }
            
            exerciseState.sets.forEachIndexed { setIndex, set ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(120)) + expandVertically(animationSpec = tween(120)),
                    exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Set Index Badge
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(28.dp)
                                .background(
                                    color = ObsidianPurple.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = ObsidianPurple.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${setIndex + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = OpenDyslexic,
                                color = ObsidianPurple
                            )
                        }

                        // Previous Session Set
                        val prevSetText = exerciseState.previousSessionSets.getOrNull(setIndex)?.let {
                            "${it.weight} × ${it.reps}"
                        } ?: "—"
                        Text(
                            text = prevSetText,
                            modifier = Modifier.weight(0.9f),
                            fontSize = 12.sp,
                            fontFamily = OpenDyslexic,
                            color = ObsidianTextDim,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Weight Input Capsule
                        SetInputCapsule(
                            value = set.weight,
                            onValueChange = { raw ->
                                val filtered = raw.filter { it.isDigit() || it == '.' }
                                val firstDot = filtered.indexOf('.')
                                val sanitized = if (firstDot != -1) {
                                    filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
                                } else {
                                    filtered
                                }
                                val finalWeight = sanitized.take(6)
                                onUpdateSet(setIndex, finalWeight, null)
                            },
                            placeholder = "—",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Reps Input Capsule
                        SetInputCapsule(
                            value = set.reps,
                            onValueChange = { raw ->
                                val sanitized = raw.filter { it.isDigit() }.take(3)
                                onUpdateSet(setIndex, null, sanitized)
                            },
                            placeholder = "—",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.1f)
                        )

                        // Delete Set Action Button
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(28.dp)
                                .background(
                                    color = ObsidianPurple.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = ObsidianPurple.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDeleteSet(setIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Set",
                                tint = ObsidianPurple,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onAddSet,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = ObsidianPurple)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set", fontFamily = OpenDyslexic, color = ObsidianPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}



