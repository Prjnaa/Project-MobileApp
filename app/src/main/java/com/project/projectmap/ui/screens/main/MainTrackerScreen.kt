package com.project.projectmap.ui.screens.main

import android.content.Intent
import android.icu.text.DecimalFormat
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.components.msc.getCurrentDate
import com.project.projectmap.firebase.model.FoodItem
import com.project.projectmap.ui.screens.camera.CameraActivity
import com.project.projectmap.ui.theme.ProjectmapTheme
import com.project.projectmap.ui.viewModel.MainTrackerViewModel
import okhttp3.internal.format
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
@Preview(showBackground = true)
fun MainTrackerScreenPreview() {
    ProjectmapTheme(darkTheme = false) {
        MainTrackerScreen()
    }
}

@Composable
@Preview(showBackground = true)
fun ChallengesPreview() {
    ProjectmapTheme(darkTheme = false) {
//        HistoryList()
    }
}

// Main UI Function
@Composable
fun MainTrackerScreen(
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTarget: () -> Unit = {},
    viewModel: MainTrackerViewModel = MainTrackerViewModel()
) {
    val user by viewModel.user.collectAsState()
    val intake by viewModel.dailyIntake.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val items = intake.items.values.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(ConstantsStyle.APP_PADDING_VAL)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        user?.let { userInfo ->
            TopBar(
                onNavToBadges = onNavigateToBadges,
                onNavToProfile = onNavigateToProfile,
                userName = userInfo.profile.name,
                usersPoints = userInfo.profile.coin
            )
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(
                errorMessage ?: "Error",
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            intake?.let { data ->
                CurrentStats(
                    carbsProgress = data.totalCarbs,
                    proteinProgress = data.totalProtein,
                    fatProgress = data.totalFat,
                    carbsTarget = user.targets?.carbsTarget ?: 0f,
                    proteinTarget = user.targets?.proteinTarget ?: 0f,
                    fatTarget = user.targets?.fatTarget ?: 0f
                )
            }
            user.targets?.let { targetData ->
                Tracker(
                    currentCalories = intake?.totalCalories?.toInt() ?: 0,
                    targetCalories = targetData.calorieTarget.toInt(),
                    onNavToNewTarget = onNavigateToNewTarget
                )

            }
        }

        HistoryList(
            onNavToCalendar = onNavigateToCalendar,
            items = items
        )
    }
}

@Composable
fun TopBar(
    onNavToBadges: () -> Unit = {},
    onNavToProfile: () -> Unit = {},
    userName: String = "user",
    usersPoints: Int = 0,
) {
    var formattedPts by remember { mutableStateOf<String?>("") }
    when(usersPoints) {
        in 0..999 -> {
            formattedPts = usersPoints.toString()
        }
        in 1000..999999 -> {
            formattedPts = "${usersPoints / 1000}k"
        }
        in 1000000..999999999 -> {
            formattedPts = "${usersPoints / 1000000}M"
        }
        else -> {
            formattedPts = "999M+"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
//            NAVIGATE TO PROFILE BUTTON
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        onNavToProfile()
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Button Section
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.streak_icon_24),
//                        contentDescription = "Streak Icon",
//                        tint = MaterialTheme.colorScheme.onPrimary,
//                        modifier = Modifier.size(30.dp)
//                    )
//                    Text(
//                        text = "200",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }

//                VerticalDivider(
//                    thickness = 2.dp,
//                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
//                    modifier = Modifier.padding(vertical = 10.dp)
//                )

//                NAVIGATE TO BADGES BUTTON
                Row(
                    modifier = Modifier.clickable { onNavToBadges() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.coin_24),
                        contentDescription = "Streak Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = formattedPts ?: "0",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MacroItem(title = "Carbs", progress = carbsProgress, target = carbsTarget)
        MacroItem(title = "Protein", progress = proteinProgress, target = proteinTarget)
        MacroItem(title = "Fat", progress = fatProgress, target = fatTarget)
    }
}


@Composable
fun Tracker(
    currentCalories: Int = 999, targetCalories: Int = 9999, onNavToNewTarget: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-42).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.char_bunny),
            contentDescription = "Calories Icon",
            modifier = Modifier
                .scale(0.75f)
        )
//        CHARACTER
//        ArObjectViewer(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 12.dp, bottom = 32.dp),
//            modelFilePath = "app/src/main/java/com/project/projectmap/assets/3d/bunny_blend.glb"
//        )

//        MAIN CALORIE TRACKER
        CalorieTracker(current = currentCalories, target = targetCalories)

//        TRACK EAT BUTTONS
        TrackEatButton(onLaunchCamera = {
            val intent = Intent(context, CameraActivity::class.java)
            context.startActivity(intent)  // Launch the CameraActivity
        })

//        SET NEW TARGET
        SetNewTargetLink(onNavToNewTarget = onNavToNewTarget)
    }
}

@Composable
fun HistoryList(
    onNavToCalendar: () -> Unit = {},
    items: List<FoodItem>
) {
    // Sort items by timestamp in descending order (newest first)
    val sortedItems = remember(items) {
        items.sortedByDescending { it.timestamp }
    }

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .offset(y = (-42).dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_MD_VAL),

        ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_SM_VAL),
                    contentPadding = PaddingValues(2.dp)
                ) {
//                    CALENDAR NAVIGATE BUTTON
                    IconButton(
                        onClick = {
                            onNavToCalendar()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.calendar_24),
                            contentDescription = "Calendar Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
                color = Color.Transparent
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(sortedItems) { index, item ->
//                        val random = (1..100).random()
//                        val isDone = Random.nextBoolean()
                        val cal = item.calories.roundToInt().toString()
                        HistoryItem(
                            index = index,
                            title = item.name,
                            text = cal,
                            timestamp = item.timestamp
                        )
                    }
                }
            }
        }
    }
}

//MAIN TRACKER SCREEN COMPOSABLE COMPONENTS

//CALORIE TRACKER ARC
@Composable
fun CalorieTracker(current: Int, target: Int) {
    val progress = current.toFloat() / target
    val sweepAngle = progress * 180f

    val arcColor = MaterialTheme.colorScheme.primary
    val arcBackroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .size(width = 300.dp, height = 280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background arc
            drawArc(
                color = arcBackroundColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 32.dp.toPx())
            )
            // Foreground arc
            drawArc(
                color = arcColor, // Deep Purple
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 32.dp.toPx())
            )
        }

        // Text in the center
        Column(
            modifier = Modifier
                .offset(y = (-24).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$current",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "of $target calories",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

//TRACK EAT BUTTON
@Composable
fun TrackEatButton(onLaunchCamera: () -> Unit = {}) {
    Row(modifier = Modifier.offset(y = (-100).dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { onLaunchCamera() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(width = 250.dp, height = 48.dp),
        ) {
            Text(text = "Track Eat", fontSize = 16.sp)
        }
    }
}

//SET NEW TARGET LINK
@Composable
fun SetNewTargetLink(
    onNavToNewTarget: () -> Unit = {}
) {
    Text(
        text = "Set New Target",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.tertiary,
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .padding(top = 16.dp)
            .offset(y = (-72).dp)
            .clickable {
                onNavToNewTarget()
            }

    )
}

//MACRO ITEM
@Composable
fun MacroItem(title: String, progress: Float, target: Float) {
    val constrainedProgress = if (progress > 0) progress / target else 0f
    val decimalFormat = DecimalFormat("#.#")
    val surplus = if (progress > target) progress - target else 0f
    val formattedSurplus = decimalFormat.format(surplus)


    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = constrainedProgress,

            modifier = Modifier
                .size(48.dp),
            strokeWidth = 6.dp,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = "+ $formattedSurplus g",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary.copy(if (surplus > 0) 1f else 0f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = if (surplus > 0) 1f else 0f),
                    shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_SM_VAL)
                )
                .padding(vertical = 2.dp, horizontal = 8.dp)
        )
    }
}

//CHALLENGE ITEM
@Composable
fun HistoryItem(
    index: Int,
    title: String = "Default Title $index",
    text: String,
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
            .fillMaxWidth(),  // Ensure it fills the width
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL)
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
                    text = "$text Calories",
                    fontSize = 16.sp,
                )
            }
//            Column(
//                horizontalAlignment = Alignment.End,
//                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
//            ) {
//
//                Row(
//                ) {
//                    Text(
//                        "+ $coinCount Coins", fontSize = 14.sp,
//                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isDone) 1f else 0.0f)
//                    )
//                }
//            }
        }
    }
}


////3D VIEW
//@Composable
//fun ArObjectViewer(
//    modifier: Modifier = Modifier,
//    modelFilePath: String
//) {
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            ArSceneView(context).apply {
//                // Initialize ARCore scene
//                scene.addOnUpdateListener {
//                    // Load the 3D model
//                    ModelRenderable.builder()
//                        .setSource(context, Uri.parse(modelFilePath))
//                        .build()
//                        .thenAccept { modelRenderable ->
//                            // Create an AnchorNode
//                            val anchorNode = AnchorNode().apply {
//                                setParent(scene)
//                            }
//
//                            // Get the TransformationSystem from an ArFragment
//                            val arFragment = (context as FragmentActivity).supportFragmentManager
//                                .findFragmentById(R.id.arFragment) as ArFragment
//
//                            val transformableNode =
//                                TransformableNode(arFragment.transformationSystem).apply {
//                                    renderable = modelRenderable
//                                    setParent(anchorNode)
//                                }
//
//                            // Add nodes to the scene
//                            scene.addChild(anchorNode)
//                            transformableNode.select()
//                        }
//                        .exceptionally {
//                            Log.e("ArObjectViewer", "Error loading model: ${it.message}")
//                            null
//                        }
//                }
//            }
//        }
//    )
//}