package com.project.projectmap.ui.screens.main

import android.content.Intent
import android.icu.text.DecimalFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.project.projectmap.R
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.firebase.model.FoodItem
import com.project.projectmap.ui.screens.camera.CameraActivity
import com.project.projectmap.ui.viewModel.MainTrackerViewModel
import kotlin.math.roundToInt

// Main UI Function
@Composable
fun MainTrackerScreen(
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTarget: () -> Unit = {}
) {
    val viewModel = MainTrackerViewModel()
    val user by viewModel.user.collectAsState()
    val intake by viewModel.dailyIntake.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Daftar item intake
    val items = intake.items.values.toList()

    // Dapatkan ID item yang di-equip dari Firestore
    val equippedItemId = user.equippedItem
    val accessoryRes =
        when (equippedItemId) {
            "tanaman1" -> R.drawable.tanaman1
            "tanaman2" -> R.drawable.tanaman2
            "tanaman3" -> R.drawable.tanaman3
            else -> null
        }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Moving Background",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier.background(colorResource(id = R.color.main_tracker_bg))
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.1f
                        scaleY = 1.1f

                        transformOrigin = TransformOrigin(0f, 0f)

                        translationY = -scrollState.value.toFloat() - 550f
                    })

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(ConstantsStyle.APP_PADDING_VAL)
                    .verticalScroll(scrollState), // <-- Pakai scrollState yg sama
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
                user?.let { userInfo ->
                    TopBar(
                        onNavToBadges = onNavigateToBadges,
                        onNavToProfile = onNavigateToProfile,
                        userName = userInfo.profile.name,
                        usersPoints = userInfo.profile.coin,
                        profilePicturePath = userInfo.profile.photoUrl
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (errorMessage != null) {
                    Text(
                        errorMessage ?: "Error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    intake?.let { data ->
                        CurrentStats(
                            carbsProgress = data.totalCarbs,
                            proteinProgress = data.totalProtein,
                            fatProgress = data.totalFat,
                            carbsTarget = user.targets?.carbsTarget ?: 0f,
                            proteinTarget = user.targets?.proteinTarget ?: 0f,
                            fatTarget = user.targets?.fatTarget ?: 0f)
                    }
                    user.targets?.let { targetData ->
                        Tracker(
                            currentCalories = intake?.totalCalories?.toInt() ?: 0,
                            targetCalories = targetData.calorieTarget.toInt(),
                            onNavToNewTarget = onNavigateToNewTarget,
                            equippedItemDrawableRes = accessoryRes)
                    }
                }

                HistoryList(onNavToCalendar = onNavigateToCalendar, items = items)
            }
    }
}

@Composable
fun TopBar(
    onNavToBadges: () -> Unit = {},
    onNavToProfile: () -> Unit = {},
    userName: String = "user",
    usersPoints: Int = 0,
    profilePicturePath: String = ""
) {
    var formattedPts by remember { mutableStateOf<String?>("") }
    val userPtsFloat = usersPoints.toFloat()
    val floatFormat = DecimalFormat("#.#")

    formattedPts =
        when (usersPoints) {
            in 0..999 -> {
                usersPoints.toString()
            }
            in 1000..999999 -> {
                "${floatFormat.format(userPtsFloat / 1000)}k"
            }
            in 1000000..999999999 -> {
                "${floatFormat.format(userPtsFloat / 1000000)}M"
            }
            in 1000000000..Int.MAX_VALUE -> {
                "${floatFormat.format(userPtsFloat / 1000000000)}B"
            }
            else -> {
                "0"
            }
        }

    Surface(
        modifier = Modifier.fillMaxWidth().height(54.dp),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                    //            NAVIGATE TO PROFILE BUTTON
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavToProfile() }) {
                            Image(
                                painter =
                                    rememberImagePainter(
                                        data = profilePicturePath,
                                        builder = {
                                            crossfade(true) // Transisi lembut saat memuat gambar
                                            placeholder(
                                                R.drawable
                                                    .baseline_person_24) // Placeholder saat gambar
                                                                         // sedang dimuat
                                            error(
                                                R.drawable
                                                    .baseline_person_24) // Gambar default jika URI
                                                                         // tidak valid
                                            fallback(
                                                R.drawable
                                                    .baseline_person_24) // Gambar default jika data
                                                                         // null
                                        }),
                                contentDescription = "User Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier.size(42.dp)
                                        .clip(CircleShape) // Membuat gambar berbentuk lingkaran
                                )
                            Text(
                                text = userName.split(" ").firstOrNull() ?: "",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Medium)
                        }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier.clickable { onNavToBadges() },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(4.dp, Alignment.Start)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.coin_24),
                                        contentDescription = "Streak Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp))
                                    Text(
                                        text = formattedPts ?: "0",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium)
                                }
                        }
                }
        }
}

@Composable
fun CurrentStats(
    carbsProgress: Float = 0.0f,
    proteinProgress: Float = 0.0f,
    fatProgress: Float = 0.0f,
    carbsTarget: Float,
    proteinTarget: Float,
    fatTarget: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            MacroItem(title = "Carbs", progress = carbsProgress, target = carbsTarget)
            MacroItem(title = "Protein", progress = proteinProgress, target = proteinTarget)
            MacroItem(title = "Fat", progress = fatProgress, target = fatTarget)
        }
}

// Inilah fungsi Tracker yang tidak diubah layout aslinya, hanya ditambahkan aksesoris
@Composable
fun Tracker(
    currentCalories: Int = 999,
    targetCalories: Int = 9999,
    onNavToNewTarget: () -> Unit = {},
    equippedItemDrawableRes: Int? = null // parameter opsional untuk aksesoris
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().offset(y = (-48).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Bungkus bunny + aksesoris dalam Box agar aksesoris menimpa bunny
        Box(contentAlignment = Alignment.Center) {
            // Bunny asli, ukuran & scale sama
            Image(
                painter = painterResource(id = R.drawable.char_bunny),
                contentDescription = "Calories Icon",
                modifier = Modifier.scale(0.75f))

            equippedItemDrawableRes?.let { res ->
                Image(
                    painter = painterResource(id = res),
                    contentDescription = "Accessory",
                    modifier =
                        Modifier.align(Alignment.BottomStart)
                            .size(150.dp)
                            .offset(x = 10.dp, y = -25.dp))
            }
        }

        // MAIN CALORIE TRACKER (tetap sama)
        CalorieTracker(current = currentCalories, target = targetCalories)

        // TRACK EAT BUTTON (tetap sama)
        TrackEatButton(
            onLaunchCamera = {
                val intent = Intent(context, CameraActivity::class.java)
                context.startActivity(intent)
            })

        // SET NEW TARGET (tetap sama)
        SetNewTargetLink(onNavToNewTarget = onNavToNewTarget)
    }
}

@Composable
fun HistoryList(onNavToCalendar: () -> Unit = {}, items: List<FoodItem>) {
    // Sort items by timestamp in descending order (newest first)
    val sortedItems = remember(items) { items.sortedByDescending { it.timestamp } }
    val expandedItemIndex = remember { mutableStateOf(-1) }

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    Surface(
        modifier = Modifier.fillMaxWidth().height(600.dp).offset(y = (-42).dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_MD_VAL),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Today's History",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = { /*TODO*/ },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_SM_VAL),
                            contentPadding = PaddingValues(2.dp)) {
                                //                    CALENDAR NAVIGATE BUTTON
                                IconButton(onClick = { onNavToCalendar() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.calendar_24),
                                        contentDescription = "Calendar Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp))
                                }
                            }
                    }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
                    color = Color.Transparent) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            itemsIndexed(sortedItems) { index, item ->
                                HistoryItem(
                                    index = index,
                                    title = item.name,
                                    calories = item.calories.roundToInt().toString(),
                                    servingSize = item.servingSize,
                                    coinsAdded = item.plusCoins,
                                    timestamp = item.timestamp,
                                    fat = item.fat,
                                    protein = item.protein,
                                    carbs = item.carbs,
                                    isExpanded = expandedItemIndex.value == index,
                                    onClick = {
                                        expandedItemIndex.value =
                                            if (expandedItemIndex.value == index) -1 else index
                                    })
                            }
                        }
                    }
            }
    }
}

// MAIN TRACKER SCREEN COMPOSABLE COMPONENTS

// CALORIE TRACKER ARC
@Composable
fun CalorieTracker(current: Int, target: Int) {
    val progress = current.toFloat() / target
    val sweepAngle = progress * 180f

    val arcColor = MaterialTheme.colorScheme.primary
    val arcBackroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier.size(width = 300.dp, height = 280.dp),
        contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background arc
                drawArc(
                    color = arcBackroundColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 32.dp.toPx()))
                // Foreground arc
                drawArc(
                    color = arcColor, // Deep Purple
                    startAngle = 180f,
                    sweepAngle = sweepAngle.coerceAtMost(180f),
                    useCenter = false,
                    style = Stroke(width = 32.dp.toPx()))
            }

            // Text in the center
            Column(
                modifier = Modifier.offset(y = (-24).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "$current",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center)
                        if (current > target) {
                            Text(
                                text = "+ ${current - target} calories",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color =
                                    MaterialTheme.colorScheme.tertiary.copy(
                                        if (current > target) 1f else 0f),
                                modifier =
                                    Modifier.align(Alignment.CenterHorizontally)
                                        .background(
                                            color =
                                                MaterialTheme.colorScheme.onTertiary.copy(
                                                    alpha = if (current < target) 0f else 1f),
                                            shape =
                                                RoundedCornerShape(
                                                    ConstantsStyle.ROUNDED_CORNER_SM_VAL))
                                        .padding(vertical = 2.dp, horizontal = 8.dp))
                        }
                    }

                    Text(
                        text = "of $target calories",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center)
                }
        }
}

// TRACK EAT BUTTON
@Composable
fun TrackEatButton(onLaunchCamera: () -> Unit = {}) {
    Row(modifier = Modifier.offset(y = (-96).dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { onLaunchCamera() },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(width = 250.dp, height = 48.dp),
        ) {
            Text(text = "Track Eat", fontSize = 16.sp)
        }
    }
}

// SET NEW TARGET LINK
@Composable
fun SetNewTargetLink(onNavToNewTarget: () -> Unit = {}) {
    Text(
        text = "Set New Target",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.tertiary,
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        modifier =
            Modifier.padding(top = 16.dp).offset(y = (-72).dp).clickable { onNavToNewTarget() })
}

// MACRO ITEM
@Composable
fun MacroItem(title: String, progress: Float, target: Float) {
    val constrainedProgress = if (progress > 0) progress / target else 0f
    val decimalFormat = DecimalFormat("#.#")
    val surplus = if (progress > target) progress - target else 0f
    val formattedSurplus = decimalFormat.format(surplus)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                progress = constrainedProgress,
                modifier = Modifier.size(48.dp),
                strokeWidth = 6.dp,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                text = "+ $formattedSurplus g",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary.copy(if (surplus > 0) 1f else 0f),
                modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                        .background(
                            color =
                                MaterialTheme.colorScheme.onTertiary.copy(
                                    alpha = if (surplus > 0) 1f else 0f),
                            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_SM_VAL))
                        .padding(vertical = 2.dp, horizontal = 8.dp))
        }
}

// @Composable
// fun HistoryItem(
//    index: Int,
//    title: String = "Default Title $index",
//    text: String,
//    coinsAdded: Int = 0,
//    timestamp: Long
//    //    coinCount: Int
// ) {
//    val capitalizedTitle = title.split(" ").joinToString(" ") { it.capitalize() }
//    val timeString =
//        remember(timestamp) {
//            val date = java.util.Date(timestamp)
//            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
//            formatter.format(date)
//        }
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(), // Ensure it fills the width
//        color = MaterialTheme.colorScheme.primary,
//        shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL)) {
//            Row(
//                modifier = Modifier.padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top) {
//                    Column(
//                        modifier = Modifier.weight(1f),
//                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically) {
//                                    Text(
//                                        text = capitalizedTitle,
//                                        fontSize = 20.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        modifier = Modifier.weight(1f))
//                                    Text(
//                                        text = timeString,
//                                        fontSize = 14.sp,
//                                        color =
//                                            MaterialTheme.colorScheme.onPrimary.copy(alpha =
// 0.7f))
//                                }
//                            Text(
//                                text = "$text Calories",
//                                fontSize = 16.sp,
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//
//                                Text(
//                                    text = "+ $coinsAdded coins",
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                )
//                            }
//                        }
//                }
//        }
// }

@Composable
fun HistoryItem(
    index: Int,
    title: String = "Default Title $index",
    calories: String,
    servingSize: Float = 0f,
    coinsAdded: Int = 0,
    timestamp: Long,
    fat: Float,
    protein: Float,
    carbs: Float,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val capitalizedTitle = title.split(" ").joinToString(" ") { it.capitalize() }
    val timeString =
        remember(timestamp) {
            val date = java.util.Date(timestamp)
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            formatter.format(date)
        }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL)) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row (Title and Timestamp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = capitalizedTitle,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f))
                                        Text(
                                            text = timeString,
                                            fontSize = 14.sp,
                                            color =
                                                MaterialTheme.colorScheme.onPrimary.copy(
                                                    alpha = 0.7f))
                                    }
                                Text(
                                    text = "$servingSize g",
                                    fontSize = 16.sp,
                                )
                                Text(
                                    text = "$calories Calories",
                                    fontSize = 16.sp,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text =
                                                if (isExpanded) "Hide Details"
                                                else "Click to Show Details",
                                            fontSize = 14.sp,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                        Text(
                                            text = "+ $coinsAdded coins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                            }
                    }

                // Expanded details section with hidden info
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Fat:",
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Left-aligned text
                                    )
                                Box(
                                    modifier =
                                        Modifier.weight(1f) // Garis mengisi sisa ruang
                                            .align(
                                                Alignment
                                                    .CenterVertically) // Align center vertically
                                    ) {
                                        Divider(
                                            modifier =
                                                Modifier.align(Alignment.CenterStart)
                                                    .padding(
                                                        start = 4.dp,
                                                        end = 4.dp,
                                                        top = 8.dp), // Garis mulai dari kiri
                                            color =
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                    alpha = 0.25f), // Warna garis
                                            thickness = 1.5.dp // Ketebalan garis
                                            )
                                    }
                                Text(
                                    text = "$fat g", // Nilai yang ditampilkan di sebelah kanan
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Right-aligned text
                                    )
                            }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Protein:",
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Left-aligned text
                                    )
                                Box(
                                    modifier =
                                        Modifier.weight(1f) // Garis mengisi sisa ruang
                                            .align(
                                                Alignment
                                                    .CenterVertically) // Align center vertically
                                    ) {
                                        Divider(
                                            modifier =
                                                Modifier.align(Alignment.CenterStart)
                                                    .padding(
                                                        start = 4.dp,
                                                        end = 4.dp,
                                                        top = 8.dp), // Garis mulai dari kiri
                                            color =
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                    alpha = 0.25f), // Warna garis
                                            thickness = 1.5.dp // Ketebalan garis
                                            )
                                    }
                                Text(
                                    text = "$protein g", // Nilai yang ditampilkan di sebelah kanan
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Right-aligned text
                                    )
                            }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Carbs:",
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Left-aligned text
                                    )
                                Box(
                                    modifier =
                                        Modifier.weight(1f) // Garis mengisi sisa ruang
                                            .align(
                                                Alignment
                                                    .CenterVertically) // Align center vertically
                                    ) {
                                        Divider(
                                            modifier =
                                                Modifier.align(Alignment.CenterStart)
                                                    .padding(
                                                        start = 4.dp,
                                                        end = 4.dp,
                                                        top = 8.dp), // Garis mulai dari kiri
                                            color =
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                    alpha = 0.25f), // Warna garis
                                            thickness = 1.5.dp // Ketebalan garis
                                            )
                                    }
                                Text(
                                    text = "$carbs g", // Nilai yang ditampilkan di sebelah kanan
                                    fontSize = 14.sp,
                                    modifier =
                                        Modifier.align(
                                            Alignment.CenterVertically) // Right-aligned text
                                    )
                            }
                    }
                }
            }
        }
}
