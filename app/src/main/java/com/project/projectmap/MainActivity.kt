package com.project.projectmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.projectmap.ui.theme.ProjectmapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectmapTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainApp(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 48.dp)) {
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    border = BorderStroke(1.5.dp, Color.Gray),
                ) {
                    Text(text = "Box 1", modifier = Modifier.padding(8.dp, 6.dp))
                }
                OutlinedButton(
                    onClick = { /*TODO*/ },
                    border = BorderStroke(1.5.dp, Color.Gray),
                ) {
                    Text(text = "Box 1", modifier = Modifier.padding(8.dp, 6.dp))
                }
            }

        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = { 0.75f }, strokeWidth = 6.dp, modifier = Modifier.size(48.dp))
                Text(text = "Carbs")
            }
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = { 0.75f }, strokeWidth = 6.dp, modifier = Modifier.size(48.dp))
                Text(text = "Protein")
            }
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = { 0.75f }, strokeWidth = 6.dp, modifier = Modifier.size(48.dp))
                Text(text = "Fat")
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    ProjectmapTheme { MainApp() }
}
