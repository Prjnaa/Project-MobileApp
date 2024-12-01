package com.project.projectmap.ui.screens.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.projectmap.R
import com.project.projectmap.ui.theme.ProjectmapTheme

@Composable
@Preview(showBackground = true)
fun MainTrackerScreenPreviewLight() {
    ProjectmapTheme(darkTheme = false) {
        MainTrackerScreen()
    }
}


@Composable
fun MainTrackerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TopBar()
        CurrentStats()
        Tracker()
        ChallengeList()
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
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
            // User Section
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                Button(
                    onClick = { /* TODO: Handle Button 1 click */ },
                    modifier = Modifier
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp)
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
                }

                VerticalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Button(
                    onClick = { /* TODO: Handle Button 2 click */ },
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Row(
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
}


@Composable
fun CurrentStats(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MacroItem(title = "Carbs", progress = 0.8f)
        MacroItem(title = "Protein", progress = 0.6f)
        MacroItem(title = "Fat", progress = 0.4f)
    }
}


@Composable
fun Tracker(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
//        CHARACTER
        Image(
            painter = painterResource(id = R.drawable.bunny),
            contentDescription = "Tracker Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        )

//        MAIN CALORIE TRACKER
        CalorieTracker(current = 1000, target = 2000)

//        TRACK EAT BUTTONS
        TrackEatButton()

//        SET NEW TARGET
        SetNewTargetLink()
    }
}

@Composable
fun ChallengeList(modifier: Modifier = Modifier) {

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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(width = 250.dp, height = 48.dp),
        ) {
            Text(text = "Track Eat")
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
        modifier = Modifier
            .padding(top = 16.dp)

    )
}

//MACRO ITEM
@Composable
fun MacroItem(title: String, progress: Float) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = progress,

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