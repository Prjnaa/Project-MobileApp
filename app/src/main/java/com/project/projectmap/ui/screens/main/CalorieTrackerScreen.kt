package com.project.projectmap.ui.screens.main

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.ui.screens.camera.MainActivity
import com.project.projectmap.ui.theme.Purple40
import com.project.projectmap.ui.theme.Purple80


fun saveCaloriesToDatabase(currentCalories: Int, totalCalories: Int) {
    val db = FirebaseFirestore.getInstance()
    val caloriesData = hashMapOf(
        "currentCalories" to currentCalories,
        "totalCalories" to totalCalories,
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("calories")
        .add(caloriesData)
        .addOnSuccessListener { documentReference ->
            println("Calories saved with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            println("Error saving calories: $e")
        }
}

@Composable
@Preview
fun CalorieTrackerScreen(onNavigateToCalendar: () -> Unit = {}, onNavigateToBadges: () -> Unit = {}) {
    // Wrap entire content in a scrollable column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // Main scroll for entire screen
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // User Info Bar
        UserInfoBar(onBadgesClick = onNavigateToBadges)

        Spacer(modifier = Modifier.height(24.dp))

        // Macronutrient Progress
        MacronutrientRow()

        Spacer(modifier = Modifier.height(24.dp))

        // Bunny Image
        BunnyImage()

        Spacer(modifier = Modifier.height(24.dp))

        // Calorie Progress
        CalorieProgress(
            progress = 0.53f,
            totalCalories = 2000,
            currentCalories = 1057
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Track Eat Button
        TrackEatButton()

        Spacer(modifier = Modifier.height(8.dp))

        // Set New Target Text
        SetNewTarget()

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Challenges with fixed height
        DailyChallenges(onHistoryClick = onNavigateToCalendar)

        // Add bottom spacing to ensure content doesn't get cut off
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UserInfoBar(onBadgesClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF3E5F5),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Info
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.profile_icon),
                contentDescription = "Profile",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hosea",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Points
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fire_icon),
                contentDescription = "Fire points",
                modifier = Modifier.size(20.dp)
            )
            Text(text = "2", modifier = Modifier.padding(horizontal = 4.dp))

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.coin_icon),
                contentDescription = "Coins",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBadgesClick() }
            )
            Text(text = "250",
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onBadgesClick() })
        }
    }
}

@Composable
fun MacronutrientRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MacronutrientItem("Carb", 0.65f, Color(0xFF8B80F9))
        MacronutrientItem("Protein", 0.45f, Color(0xFF8B80F9))
        MacronutrientItem("Fat", 0.30f, Color(0xFF8B80F9))
    }
}

@Composable
fun MacronutrientItem(name: String, progress: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(70.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 8.dp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BunnyImage() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bunny),
            contentDescription = "Cute bunny with carrot",
            modifier = Modifier.size(160.dp)
        )
    }
}

@Composable
fun CalorieProgress(
    progress: Float,
    totalCalories: Int,
    currentCalories: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Container for both arc and text to ensure proper layering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Progress Arc
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val thickness = 32.dp.toPx()
                val startAngle = 180f
                val sweepAngle = 180f

                // Draw background arc
                drawArc(
                    color = Purple80,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness),
                    size = size.copy(height = size.height * 2)
                )

                // Draw progress arc
                drawArc(
                    color = Purple40,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    style = Stroke(width = thickness),
                    size = size.copy(height = size.height * 2)
                )
            }

            // Calorie text positioned exactly in the center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = 20.dp) // Adjusted to be more centered within the arc
            ) {
                Text(
                    text = String.format("%.1f", currentCalories.toFloat()),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    ),
                    color = Color(0xFF8B80F9)
                )
                Text(
                    text = "of $totalCalories calories",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
fun TrackEatButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple80
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text("Track Eat")
    }
}

@Composable
fun SetNewTarget() {
    Text(
        text = "set new target",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray
    )
}

@Composable
fun DailyChallenges(onHistoryClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Fixed height for the card to show ~3.5 items
            .height(380.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily History:",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    painter = painterResource(id = R.drawable.history_icon),
                    contentDescription = "History",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onHistoryClick()}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Separate scrollable container for challenge items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Calculate height to show ~3.5 items
                    // Available space = Card height (380) - padding (32) - header (~40) - spacing (16) ≈ 292dp
                    .height(292.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ChallengeItem("Challenge 4", "Drink Milk today!", true)
                Spacer(modifier = Modifier.height(8.dp))
                ChallengeItem("Challenge 3", "Drink Fanta today!", false)
                Spacer(modifier = Modifier.height(8.dp))
                ChallengeItem("Challenge 2", "Eat Soto today!", false)
                Spacer(modifier = Modifier.height(8.dp))
                ChallengeItem("Challenge 1", "Eat Veggie Salad!", true)
                Spacer(modifier = Modifier.height(8.dp))
                ChallengeItem("Challenge 0", "Drink Water!", true)
                Spacer(modifier = Modifier.height(8.dp))
                ChallengeItem("Previous Challenge", "Exercise 30 minutes!", true)
            }
        }
    }
}

@Composable
fun ChallengeItem(title: String, description: String, isCompleted: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B80F9).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF8B80F9)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.coin_icon),
                        contentDescription = "Coins",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+10 coins",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            if (isCompleted) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Completed",
                    tint = Color(0xFF8B80F9),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}