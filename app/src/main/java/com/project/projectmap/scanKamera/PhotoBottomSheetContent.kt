package com.project.projectmap.scanKamera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhotoBottomSheetContent() {
    var fat by remember { mutableStateOf(1.9f) }
    var carbohydrate by remember { mutableStateOf(5.8f) }
    var protein by remember { mutableStateOf(4.1f) }

    val calories = remember(fat, carbohydrate, protein) {
        String.format("%.1f", (protein * 4) + (carbohydrate * 4) + (fat * 9))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "Milk",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "calories")

        Text(
            text = "$calories Cals",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFA500),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        EditableNutrientInfo("Fat", fat) { fat = it }
        EditableNutrientInfo("Carbohydrate", carbohydrate) { carbohydrate = it }
        EditableNutrientInfo("Protein", protein) { protein = it }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Handle submit */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
        ) {
            Text("Submit", color = Color.White)
        }
    }
}

@Composable
fun EditableNutrientInfo(name: String, amount: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = name,
            fontSize = 16.sp,
            color = Color.Gray
        )
        TextField(
            value = amount.toString(),
            onValueChange = {
                onValueChange(it.toFloatOrNull() ?: 0f)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

// Kode lama (dikomentari):
/*
@Composable
fun PhotoBottomSheetContent() {
    var protein by remember { mutableStateOf(15.80f) }
    var carbohydrate by remember { mutableStateOf(15.80f) }
    var fat by remember { mutableStateOf(15.80f) }

    val calories = remember(protein, carbohydrate, fat) {
        ((protein * 4) + (carbohydrate * 4) + (fat * 9)).toInt()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "MILK",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalorieCircle(
                calories = calories,
                progress = calories / 2000f, // Assuming 2000 calories is 100%
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                EditableNutrientInfo("Carbohydrate", carbohydrate) { carbohydrate = it }
                Spacer(modifier = Modifier.height(8.dp))
                EditableNutrientInfo("Protein", protein) { protein = it }
                Spacer(modifier = Modifier.height(8.dp))
                EditableNutrientInfo("Fat", fat) { fat = it }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Handle submit */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCE93D8))
        ) {
            Text("Submit", color = Color.White)
        }
    }
}

@Composable
fun CalorieCircle(calories: Int, progress: Float, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f)
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE1BEE7),
            strokeWidth = 24.dp,
            strokeCap = StrokeCap.Round
        )
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFBA68C8),
            strokeWidth = 24.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$calories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Calories",
                fontSize = 10.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun EditableNutrientInfo(name: String, amount: Float, onValueChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .weight(0.3f)
        ) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFE1BEE7),
                strokeWidth = 7.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = amount / 100f, // Assuming 100g is 100%
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFBA68C8),
                strokeWidth = 7.dp,
                strokeCap = StrokeCap.Round
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(0.7f)) {
            TextField(
                value = amount.toString(),
                onValueChange = {
                    onValueChange(it.toFloatOrNull() ?: 0f)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp, textAlign = TextAlign.End),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFFBA68C8),
                    unfocusedIndicatorColor = Color(0xFFE1BEE7),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            Text(
                text = name,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}
*/