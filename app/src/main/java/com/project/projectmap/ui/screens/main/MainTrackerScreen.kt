package com.project.projectmap.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.annotations.concurrent.Background

@Composable
@Preview
fun MainPreview() {
    MainTrackerScrreen()
}

@Composable
fun MainTrackerScrreen() {
    Row {
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
