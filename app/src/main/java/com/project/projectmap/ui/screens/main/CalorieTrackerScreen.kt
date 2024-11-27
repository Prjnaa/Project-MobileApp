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
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.ui.screens.camera.CameraActivity
import com.project.projectmap.ui.theme.Purple40
import com.project.projectmap.ui.theme.Purple80
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserNutritionTarget(
    val fat: Float = 0f,
    val protein: Float = 0f,
    val carbohydrate: Float = 0f,
    val totalCalories: Int = 2000 // default value
)

@Composable
fun CalorieTrackerScreen(
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTarget: () -> Unit = {}
) {
    var nutritionTarget by remember { mutableStateOf(UserNutritionTarget()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    // Dummy current values (bisa diganti dengan data real dari tracking)
    var currentCarbs by remember { mutableStateOf(0f) }
    var currentProtein by remember { mutableStateOf(0f) }
    var currentFat by remember { mutableStateOf(0f) }
    var currentCalories by remember { mutableStateOf(0) }

    // Effect untuk mengambil data target dari Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            try {
                // Get user targets
                val targetDoc = db.collection("userTargets")
                    .document(user.uid)
                    .get()
                    .await()

                if (targetDoc.exists()) {
                    nutritionTarget = UserNutritionTarget(
                        fat = targetDoc.getDouble("fat")?.toFloat() ?: 0f,
                        protein = targetDoc.getDouble("protein")?.toFloat() ?: 0f,
                        carbohydrate = targetDoc.getDouble("carbohydrate")?.toFloat() ?: 0f,
                        totalCalories = targetDoc.getLong("totalCalories")?.toInt() ?: 2000
                    )

                    // Get today's nutrition entries
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date()
                    )
                    val nutritionEntries = db.collection("dailyNutrition")
                        .whereEqualTo("userId", user.uid)
                        .whereEqualTo("date", currentDate)
                        .get()
                        .await()

                    // Sum up all nutrition values for today
                    currentCarbs = 0f
                    currentProtein = 0f
                    currentFat = 0f
                    currentCalories = 0

                    for (entry in nutritionEntries.documents) {
                        currentCarbs += entry.getDouble("carbohydrate")?.toFloat() ?: 0f
                        currentProtein += entry.getDouble("protein")?.toFloat() ?: 0f
                        currentFat += entry.getDouble("fat")?.toFloat() ?: 0f
                        currentCalories += entry.getDouble("calories")?.toInt() ?: 0
                    }
                } else {
                    errorMessage = "No nutrition target found. Please set your target."
                }
            } catch (e: Exception) {
                errorMessage = "Error loading nutrition data: ${e.message}"
            } finally {
                isLoading = false
            }
        } ?: run {
            errorMessage = "No user logged in"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // User Info Bar
        UserInfoBar(
            onBadgesClick = onNavigateToBadges,
            onProfileClick = onNavigateToProfile,
            username = currentUser?.displayName ?: "User"
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally),
                color = Purple40
            )
        } else {
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Macronutrient Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacronutrientItem(
                    name = "Carb",
                    progress = if (nutritionTarget.carbohydrate > 0)
                        currentCarbs / nutritionTarget.carbohydrate else 0f,
                    color = Color(0xFF8B80F9),
                    current = currentCarbs.toInt(),
                    target = nutritionTarget.carbohydrate.toInt()
                )
                MacronutrientItem(
                    name = "Protein",
                    progress = if (nutritionTarget.protein > 0)
                        currentProtein / nutritionTarget.protein else 0f,
                    color = Color(0xFF8B80F9),
                    current = currentProtein.toInt(),
                    target = nutritionTarget.protein.toInt()
                )
                MacronutrientItem(
                    name = "Fat",
                    progress = if (nutritionTarget.fat > 0)
                        currentFat / nutritionTarget.fat else 0f,
                    color = Color(0xFF8B80F9),
                    current = currentFat.toInt(),
                    target = nutritionTarget.fat.toInt()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        BunnyImage()

        Spacer(modifier = Modifier.height(24.dp))

        // Calorie Progress
        CalorieProgress(
            progress = if (nutritionTarget.totalCalories > 0)
                currentCalories.toFloat() / nutritionTarget.totalCalories else 0f,
            totalCalories = nutritionTarget.totalCalories,
            currentCalories = currentCalories
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackEatButton()

        Spacer(modifier = Modifier.height(8.dp))

        SetNewTarget(onNewTargetClick = onNavigateToNewTarget)

        Spacer(modifier = Modifier.height(24.dp))

        DailyChallenges(onHistoryClick = onNavigateToCalendar)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UserInfoBar(
    onBadgesClick: () -> Unit,
    onProfileClick: () -> Unit,
    username: String
) {
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
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onProfileClick),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = username,
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
            Text(
                text = "250",
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onBadgesClick() }
            )
        }
    }
}

@Composable
fun MacronutrientItem(
    name: String,
    progress: Float,
    color: Color,
    current: Int,
    target: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(70.dp)
        ) {
            CircularProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 8.dp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "$current/$target",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val thickness = 32.dp.toPx()
                val startAngle = 180f
                val sweepAngle = 180f

                drawArc(
                    color = Purple80,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness),
                    size = size.copy(height = size.height * 2)
                )

                drawArc(
                    color = Purple40,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    style = Stroke(width = thickness),
                    size = size.copy(height = size.height * 2)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 20.dp)
            ) {
                Text(
                    text = currentCalories.toString(),
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
            val intent = Intent(context, CameraActivity::class.java)
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
fun SetNewTarget(onNewTargetClick: () -> Unit) {
    Text(
        text = "set new target",
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNewTargetClick() },
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray
    )
}

@Composable
fun DailyChallenges(onHistoryClick: () -> Unit = {}) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    // State untuk menampung daftar tantangan dari Firestore
    var challenges by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Mengambil data tantangan dari Firestore
    LaunchedEffect(currentUser?.uid) {
        try {
            currentUser?.let { user ->
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val challengeSnapshot = db.collection("dailyNutrition")
                    .whereEqualTo("userId", user.uid)
                    .whereEqualTo("date", currentDate)
                    .get()
                    .await()

                challenges = challengeSnapshot.documents.mapNotNull { it.data }
            }
        } catch (e: Exception) {
            errorMessage = "Error fetching daily history: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // UI untuk Daily Challenges
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp), // Tetapkan tinggi sesuai kebutuhan
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
                        .clickable { onHistoryClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Gray
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                // Daftar tantangan dari Firestore
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(292.dp) // Sesuaikan tinggi
                        .verticalScroll(rememberScrollState())
                ) {
                    challenges.forEach { challenge ->
                        ChallengeItem(
                            title = challenge["foodName"]?.toString() ?: "Unknown",
                            description = "Calories: ${(challenge["calories"] as? Number)?.toInt() ?: 0}",
                            isCompleted = true // Sesuaikan logika untuk status selesai
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
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