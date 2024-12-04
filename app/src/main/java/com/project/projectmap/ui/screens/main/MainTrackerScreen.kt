package com.project.projectmap.ui.screens.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.projectmap.R
import com.project.projectmap.components.msc.Constants
import com.project.projectmap.ui.viewModel.CalorieTrackerViewModel
import com.project.projectmap.ui.theme.ProjectmapTheme
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
        ChallengeList()
    }
}

// Main UI Function
@Composable
fun MainTrackerScreen(
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNewTarget: () -> Unit = {},
    viewModel: CalorieTrackerViewModel = CalorieTrackerViewModel()
) {
    val nutritionData by viewModel.nutritionData.collectAsState()
    val nutritionTarget by viewModel.nutritionTarget.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        TopBar(
            onNavToBadges = onNavigateToBadges,
            onNavToProfile = onNavigateToProfile,
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(
                errorMessage ?: "Error",
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            nutritionData?.let { data ->
                CurrentStats(
                    carbsProgress = data.currentCarbs,
                    proteinProgress = data.currentProtein,
                    fatProgress = data.currentFat
                )
            }
            nutritionTarget?.let { targetData ->
                Tracker(
                    currentCalories = nutritionData.currentCalories,
                    targetCalories = targetData.totalCalories
                )

            }
        }

        ChallengeList(
            onNavToCalendar = onNavigateToCalendar
        )
    }
}

@Composable
fun TopBar(
    onNavToBadges: () -> Unit = {},
    onNavToProfile: () -> Unit = {}
) {
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
                    text = "User Name",
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.streak_icon_24),
                        contentDescription = "Streak Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "200",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }


                VerticalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

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
                        text = "1200",
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
    fatProgress: Float = 0.0f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MacroItem(title = "Carbs", progress = carbsProgress)
        MacroItem(title = "Protein", progress = proteinProgress)
        MacroItem(title = "Fat", progress = fatProgress)
    }
}


@Composable
fun Tracker(
    currentCalories: Int = 999, targetCalories: Int = 9999
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//        CHARACTER
        Image(
            painter = painterResource(id = R.drawable.bunny),
            contentDescription = "Tracker Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 32.dp)
        )

//        MAIN CALORIE TRACKER
        CalorieTracker(current = currentCalories, target = targetCalories)

//        TRACK EAT BUTTONS
        TrackEatButton()

//        SET NEW TARGET
        SetNewTargetLink()
    }
}

@Composable
fun ChallengeList(
    onNavToCalendar: () -> Unit = {}
) {
    val items = List(20) { index -> "This is challenge number #${index}" } //Sample

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),

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
                    text = "Today's Challenges",
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
                    shape = RoundedCornerShape(16.dp),
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    val random = (1..100).random() //Sample
                    val isDone = Random.nextBoolean() //Sample
                    ChallengeItem(index = index, text = item, isDone = isDone, coinCount = random)
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
fun TrackEatButton() {
    Row(modifier = Modifier.offset(y = (-112).dp), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { /*TODO*/ },
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
fun SetNewTargetLink() {
    Text(
        text = "Set New Target",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.tertiary,
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .padding(top = 16.dp)
            .offset(y = (-72).dp)
            .clickable { /*TODO: Set New target link*/ }

    )
}

//MACRO ITEM
@Composable
fun MacroItem(title: String, progress: Float) {
    val constrainedProgress = progress.coerceIn(0f, 1f)
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
    }
}

//CHALLENGE ITEM
@Composable
fun ChallengeItem(
    index: Int,
    title: String = "Default Title $index",
    text: String,
    isDone: Boolean,
    coinCount: Int
) {
    val capitalizedTitle = title.split(" ").joinToString(" ") { it.capitalize() }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),  // Ensure it fills the width
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(Constants.ROUNDED_CORNER_VAL)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),  // Add padding to the Row
            horizontalArrangement = Arrangement.SpaceBetween,  // Space out content
            verticalAlignment = Alignment.Top // Center align vertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = capitalizedTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = text,
                    fontSize = 16.sp,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
            ) {

                Icon(
                    painter = painterResource(R.drawable.check_circle_24),
                    contentDescription = "Check Icon",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isDone) 1f else 0.0f),
                    modifier = Modifier
                        .size(32.dp)
                )

                Row(
                ) {
                    Text(
                        "+ $coinCount Coins", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isDone) 1f else 0.0f)
                    )
                }
            }
        }
    }
}
