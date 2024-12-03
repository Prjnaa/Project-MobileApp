package com.project.projectmap.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.annotations.concurrent.Background
import kotlin.math.round

@Composable
@Preview
fun MainPreview() {
    MainTrackerScrreen()
}

@Composable
fun MainTrackerScrreen() {
    Row(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)) {
        Currency()
        CurrentStat()
        CharacterState()
        Tracker()
        Challenges()
    }

}

@Composable
fun Currency() {
    Column() {
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .background(color = Color.Blue),

        ) {
            
        }
    }
}

@Composable
fun CurrentStat() {

}

@Composable
fun CharacterState() {

}

@Composable
fun Tracker() {

}

@Composable
fun Challenges() {

}
