package com.example.rttracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rttracker.data.SessionWorkoutDetail
import com.example.rttracker.ui.theme.ObsidianPurple
import com.example.rttracker.ui.theme.ObsidianSurface
import com.example.rttracker.ui.theme.ObsidianTextDim
import com.example.rttracker.ui.theme.OpenDyslexic
import com.example.rttracker.ui.theme.RtTrackerTheme
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
 
@Preview(showBackground = true, backgroundColor = 0xFF171717)
@Composable
fun HomeScreenPreview() {
    RtTrackerTheme {
        HomeScreen(
            workoutDates = listOf(System.currentTimeMillis()),
            leastRecentMuscleGroups = listOf("Chest", "Legs", "Back"),
            selectedDayWorkouts = emptyList(),
            selectedDate = null,
            onDateSelected = {},
            onDeleteSession = {},
            onDeleteSet = {},
            onUpdateSet = { _, _, _ -> },
            onNavigateToWorkout = { _ -> },
            onEditSession = {},
            onEditSessionByDate = {}
        )
    }
}
 
@Composable
fun HomeScreen(
    workoutDates: List<Long>,
    leastRecentMuscleGroups: List<String>,
    selectedDayWorkouts: List<SessionWorkoutDetail>,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onUpdateSet: (Long, Float, Int) -> Unit,
    onNavigateToWorkout: (Long?) -> Unit,
    onEditSession: (Long) -> Unit,
    onEditSessionByDate: (Long) -> Unit
) {
    var editingSet by remember { mutableStateOf<SessionWorkoutDetail?>(null) }
    var editWeight by remember { mutableStateOf("") }
    var editReps by remember { mutableStateOf("") }
    var confirmEditSessionId by remember { mutableStateOf<Long?>(null) }
    var confirmEditSessionIndex by remember { mutableStateOf(1) }

    LaunchedEffect(editingSet) {
        editingSet?.let {
            editWeight = it.weight.toString()
            editReps = it.reps.toString()
        }
    }

    if (editingSet != null) {
        AlertDialog(
            onDismissRequest = { editingSet = null },
            title = { Text("Edit Set", fontFamily = OpenDyslexic) },
            text = {
                Column {
                    Text(text = editingSet!!.exerciseName, fontFamily = OpenDyslexic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextField(
                        value = editWeight,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() || it == '.' }
                            val firstDot = filtered.indexOf('.')
                            val sanitized = if (firstDot != -1) {
                                filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
                            } else {
                                filtered
                            }
                            editWeight = sanitized.take(6)
                        },
                        label = { Text("Weight (kg)", fontFamily = OpenDyslexic) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OpenDyslexic),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ObsidianPurple
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextField(
                        value = editReps,
                        onValueChange = { raw ->
                            editReps = raw.filter { it.isDigit() }.take(3)
                        },
                        label = { Text("Reps", fontFamily = OpenDyslexic) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OpenDyslexic),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = ObsidianPurple
                        )
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            editingSet?.let {
                                onDeleteSet(it.setId)
                            }
                            editingSet = null
                        }
                    ) {
                        Text("Delete Set", fontFamily = OpenDyslexic, color = Color.Red.copy(alpha = 0.7f))
                    }
                    
                    Row {
                        TextButton(onClick = { editingSet = null }) {
                            Text("Cancel", fontFamily = OpenDyslexic, color = ObsidianTextDim)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val canSave = editWeight.isNotBlank() && editReps.isNotBlank()
                        TextButton(
                            onClick = {
                                editingSet?.let {
                                    val w = editWeight.toFloatOrNull() ?: 0f
                                    val r = editReps.toIntOrNull() ?: 0
                                    onUpdateSet(it.setId, w, r)
                                }
                                editingSet = null
                            },
                            enabled = canSave
                        ) {
                            Text(
                                "Save", 
                                fontFamily = OpenDyslexic, 
                                color = if (canSave) ObsidianPurple else ObsidianTextDim.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            },
            containerColor = ObsidianSurface
        )
    }

    if (confirmEditSessionId != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { confirmEditSessionId = null }) {
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
                        text = "Would you like to edit Session #$confirmEditSessionIndex?",
                        fontFamily = OpenDyslexic,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { confirmEditSessionId = null },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianTextDim.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = OpenDyslexic,
                                color = ObsidianTextDim,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        
                        Button(
                            onClick = {
                                confirmEditSessionId?.let { sId ->
                                    onEditSession(sId)
                                }
                                confirmEditSessionId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianPurple),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Edit",
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



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // FULL-MONTH NAVIGABLE CALENDAR GRID (Now Collapsable!)
        MonthlyCalendar(
            workoutDates = workoutDates,
            selectedDate = selectedDate,
            onDaySelected = { cell ->
                onDateSelected(cell.timeInMillis)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                MuscleGroupStatus(leastRecentMuscleGroups)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (selectedDate != null) {
                val groupedSessions = selectedDayWorkouts.groupBy { it.sessionId }
                
                if (groupedSessions.isEmpty()) {
                    // CENTRAL CALL-TO-ACTION SUPER-ELLIPSE CARD (Empty State)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianTextDim.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Create your first workout",
                                    fontFamily = OpenDyslexic,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = "Strength training is the single most powerful tool for longevity, preserving muscle mass, bone density, and metabolic health as you age.",
                                    fontSize = 13.sp,
                                    color = ObsidianTextDim,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    fontFamily = OpenDyslexic
                                )
                                
                                Spacer(modifier = Modifier.height(28.dp))
                                
                                Button(
                                    onClick = { onNavigateToWorkout(selectedDate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                ) {
                                    Text("Let's do it", fontFamily = OpenDyslexic, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDate))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Workouts on $dateStr",
                                fontFamily = OpenDyslexic,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianPurple
                            )
                            
                            Button(
                                onClick = { onNavigateToWorkout(selectedDate) },
                                colors = ButtonDefaults.buttonColors(containerColor = ObsidianPurple),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "+ Add Session",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OpenDyslexic
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    groupedSessions.entries.forEachIndexed { index, entry ->
                        val sessionId = entry.key
                        val workouts = entry.value
                        val sessionDisplayIndex = index + 1
                        val firstWorkout = workouts.firstOrNull()
                        val sessionTimeStr = if (firstWorkout != null) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(firstWorkout.sessionDate))
                        } else ""

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (sessionTimeStr.isNotEmpty()) "Session #$sessionDisplayIndex ($sessionTimeStr)" else "Session #$sessionDisplayIndex",
                                            fontFamily = OpenDyslexic,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ObsidianPurple
                                        )
                                        Row {
                                            IconButton(onClick = {
                                                confirmEditSessionId = sessionId
                                                confirmEditSessionIndex = sessionDisplayIndex
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Session",
                                                    tint = ObsidianPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = { onDeleteSession(sessionId) }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Session",
                                                    tint = ObsidianPurple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                     val workoutsByMuscle = workouts.groupBy { it.muscleGroup }
                                     workoutsByMuscle.forEach { (muscle, muscleWorkouts) ->
                                         Text(
                                             text = muscle.uppercase(),
                                             fontFamily = OpenDyslexic,
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.ExtraBold,
                                             color = ObsidianPurple.copy(alpha = 0.8f),
                                             modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                                             letterSpacing = 1.sp
                                         )
                                         muscleWorkouts.forEach { workout ->
                                             WorkoutDetailItem(workout = workout, onClick = { editingSet = workout })
                                             Spacer(modifier = Modifier.height(4.dp))
                                         }
                                         Spacer(modifier = Modifier.height(8.dp))
                                     }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianPurple.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Ready for more?",
                                    fontFamily = OpenDyslexic,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToWorkout(selectedDate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPurple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Start Another Session",
                                        fontFamily = OpenDyslexic,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendar(
    workoutDates: List<Long>,
    selectedDate: Long?,
    onDaySelected: (Calendar) -> Unit
) {
    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }
    
    var calendarExpanded by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(selectedDate) {
        selectedDate?.let {
            currentMonthCalendar = Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    val daysInMonth = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonthCal = Calendar.getInstance().apply {
        timeInMillis = currentMonthCalendar.timeInMillis
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val startDayOfWeek = firstDayOfMonthCal.get(Calendar.DAY_OF_WEEK) - 1

    val cells = remember(currentMonthCalendar) {
        val list = mutableListOf<Calendar?>()
        for (i in 0 until startDayOfWeek) {
            list.add(null)
        }
        for (day in 1..daysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = currentMonthCalendar.timeInMillis
                set(Calendar.DAY_OF_MONTH, day)
            }
            list.add(dayCal)
        }
        list.chunked(7)
    }

    val todayCalendar = Calendar.getInstance()

    // Determine rows to render based on expanded/collapsed state
    val rowsToRender = if (calendarExpanded) {
        cells
    } else {
        val targetMs = selectedDate ?: System.currentTimeMillis()
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetMs }
        val selectedWeekRow = cells.firstOrNull { row ->
            row.any { cell ->
                cell != null &&
                cell.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) &&
                cell.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
            }
        }
        if (selectedWeekRow != null) listOf(selectedWeekRow) else listOf(cells.first())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                }
            ) {
                Text("<", fontFamily = OpenDyslexic, color = ObsidianPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            // Click title to toggle calendar expansion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { calendarExpanded = !calendarExpanded }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonthCalendar.time),
                    fontFamily = OpenDyslexic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (calendarExpanded) "▲" else "▼",
                    fontFamily = OpenDyslexic,
                    color = ObsidianPurple,
                    fontSize = 11.sp
                )
            }
            
            IconButton(
                onClick = {
                    currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }
            ) {
                Text(">", fontFamily = OpenDyslexic, color = ObsidianPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday Titles
        val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    fontFamily = OpenDyslexic,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = ObsidianTextDim,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Month Grid Rows (Filtered based on collapse state)
        rowsToRender.forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowCells.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell != null) {
                            val isSelected = selectedDate?.let {
                                val selCal = Calendar.getInstance().apply { timeInMillis = it }
                                cell.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR) &&
                                cell.get(Calendar.YEAR) == selCal.get(Calendar.YEAR)
                            } ?: false

                            val isToday = cell.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) &&
                                          cell.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
                            
                            val todayCompare = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }
                            val isFuture = cell.timeInMillis > todayCompare.timeInMillis

                            val hasWorkout = workoutDates.any { dateMs ->
                                val wCal = Calendar.getInstance().apply { timeInMillis = dateMs }
                                cell.get(Calendar.DAY_OF_YEAR) == wCal.get(Calendar.DAY_OF_YEAR) &&
                                cell.get(Calendar.YEAR) == wCal.get(Calendar.YEAR)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = if (!isFuture) {
                                    Modifier.clickable { onDaySelected(cell.clone() as Calendar) }
                                } else {
                                    Modifier
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(
                                            color = if (isSelected) ObsidianPurple else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cell.get(Calendar.DAY_OF_MONTH).toString(),
                                        fontFamily = OpenDyslexic,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else if (isToday) ObsidianPurple else if (isFuture) ObsidianTextDim.copy(alpha = 0.3f) else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            color = if (hasWorkout) ObsidianPurple else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
                if (rowCells.size < 7) {
                    for (i in 0 until (7 - rowCells.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailItem(workout: SessionWorkoutDetail, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.exerciseName,
                    fontFamily = OpenDyslexic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (workout.weight % 1f == 0f) "${workout.weight.toInt()} kg" else "${workout.weight} kg",
                    fontFamily = OpenDyslexic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = ObsidianPurple,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = "x",
                    fontFamily = OpenDyslexic,
                    fontSize = 12.sp,
                    color = ObsidianTextDim,
                    modifier = Modifier.width(16.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${workout.reps}",
                    fontFamily = OpenDyslexic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = ObsidianPurple,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun MuscleGroupStatus(leastRecentMuscleGroups: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Least recently trained:",
                    fontFamily = OpenDyslexic,
                    fontSize = 14.sp,
                    color = ObsidianTextDim,
                    modifier = Modifier.weight(1f)
                )
                if (leastRecentMuscleGroups.size > 2) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ObsidianTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            val visibleGroups = if (isExpanded) leastRecentMuscleGroups else leastRecentMuscleGroups.take(2)
            
            if (visibleGroups.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No workouts logged",
                    fontFamily = OpenDyslexic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianTextDim
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                visibleGroups.forEachIndexed { index, muscleGroup ->
                    Text(
                        text = "${index + 1}. $muscleGroup",
                        fontFamily = OpenDyslexic,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianPurple,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
