package com.project.projectmap.ui.screens.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.project.projectmap.firebase.model.DailyIntake
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarPage(onClose: () -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    var dailyHistory by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Fetch data for the selected date
    LaunchedEffect(selectedDate) {
        try {
            isLoading = true
            val dailyIntake = fetchDailyIntake(
                db = db,
                userId = currentUser?.uid ?: "",
                selectedDate = formattedDate
            )

            if (dailyIntake != null) {
                // Convert items to list and sort by timestamp
                dailyHistory = dailyIntake.items.map { (key, value) ->
                    mapOf(
                        "name" to value.name,
                        "calories" to value.calories,
                        "protein" to value.protein,
                        "fat" to value.fat,
                        "carbs" to value.carbs,
                        "timestamp" to (value.timestamp ?: System.currentTimeMillis())
                    )
                }.sortedByDescending { it["timestamp"] as Long }
            } else {
                dailyHistory = emptyList()
            }
            Log.d("CalendarPage", "Daily History: $dailyIntake")
        } catch (e: Exception) {
            errorMessage = "Error fetching data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        // Top Bar with Close Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Month Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, "Previous month")
            }

            Text(
                text = "${
                    currentMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    )
                } ${currentMonth.year}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, "Next month")
            }
        }

        // Weekday Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Calendar Grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        for (week in 0..5) {
            if (week * 7 < firstDayOfWeek + lastDayOfMonth.dayOfMonth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayOfWeek in 0..6) {
                        val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 1
                        if (dayNumber in 1..lastDayOfMonth.dayOfMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val isSelected = date == selectedDate

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(
                                        enabled = date <= LocalDate.now(),
                                        onClick = { selectedDate = date }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        date > LocalDate.now() -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onBackground
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        // Daily History Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily History",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(dailyHistory) { history ->
                            ChallengeItem(
                                index = dailyHistory.indexOf(history),
                                title = history["name"]?.toString() ?: "Unknown",
                                text = "Calories: ${(history["calories"] as? Number)?.toInt() ?: 0}",
                                isDone = true,
                                timestamp = (history["timestamp"] as? Long) ?: System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(
    index: Int,
    title: String = "Default Title $index",
    text: String,
    isDone: Boolean,
    timestamp: Long
//    coinCount: Int
) {
    val capitalizedTitle = title.split(" ").joinToString(" ") { it.capitalize() }
    val timeString = remember(timestamp) {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        formatter.format(date)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = capitalizedTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeString,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = text,
                    fontSize = 16.sp,
                )
//                Text(
//                    "+ $coinCount Coins", fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isDone) 1f else 0.0f)
//                )
            }
        }
    }
}

suspend fun fetchDailyIntake(
    db: FirebaseFirestore,
    userId: String,
    selectedDate: String
): DailyIntake? {
    val documentId = "$userId-$selectedDate"
    val snapshot = db.collection("intakes")
        .document(documentId)
        .get()
        .await()

    return if (snapshot.exists()) {
        snapshot.toObject<DailyIntake>()
    } else {
        null
    }
}