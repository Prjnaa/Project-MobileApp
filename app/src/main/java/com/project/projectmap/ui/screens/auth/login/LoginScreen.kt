package com.project.projectmap.ui.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun LoginScreen() {
    showUI()
}

@Composable
private fun showUI() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            // must be deleted
            .padding(16.dp, 32.dp, 16.dp, 24.dp)
            .background(MaterialTheme.colorScheme.background)
        ,
    ) {
        Text(
            text = "Welcome Back!"
        )
    }
}