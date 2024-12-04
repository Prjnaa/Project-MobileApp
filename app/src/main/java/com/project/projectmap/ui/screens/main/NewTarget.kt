package com.project.projectmap.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.ui.theme.Purple40
import com.project.projectmap.ui.theme.Purple80
import java.util.*

data class NutritionTarget(
    val userId: String = "",
    val fat: Float = 0f,
    val protein: Float = 0f,
    val carbohydrate: Float = 0f,
    val totalCalories: Int = 0,
    val timestamp: Long = 0,
    val lastUpdated: Date = Date()
)

@Composable
fun NewTargetScreen(
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    var fat by remember { mutableStateOf("10") }
    var carbohydrate by remember { mutableStateOf("10") }
    var protein by remember { mutableStateOf("10") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val calories = calculateCalories(fat, protein, carbohydrate)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Initially load user's existing targets if any
    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            db.collection("userTargets")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.data?.let { data ->
                            fat = (data["fat"] as? Number)?.toFloat()?.toString() ?: "10"
                            protein = (data["protein"] as? Number)?.toFloat()?.toString() ?: "10"
                            carbohydrate = (data["carbohydrate"] as? Number)?.toFloat()?.toString() ?: "10"
                        }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header with close button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Set Your Target",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple80
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        // Calories Section
        Column(
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text(
                text = "Target Calories",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = calories.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple80
                )
                Text(
                    text = " Calories/day",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }
        }

        // Input Fields
        Text(
            text = "Fat (grams)",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = fat,
            onValueChange = {
                if (it.isEmpty() || it.toFloatOrNull() != null) {
                    fat = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Purple40
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Text(
            text = "Carbohydrate (grams)",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = carbohydrate,
            onValueChange = {
                if (it.isEmpty() || it.toFloatOrNull() != null) {
                    carbohydrate = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Purple40
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Text(
            text = "Protein (grams)",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = protein,
            onValueChange = {
                if (it.isEmpty() || it.toFloatOrNull() != null) {
                    protein = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Purple40
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        // Error Message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                val fatValue = fat.toFloatOrNull()
                val carbValue = carbohydrate.toFloatOrNull()
                val proteinValue = protein.toFloatOrNull()

                when {
                    fatValue == null || carbValue == null || proteinValue == null -> {
                        errorMessage = "Please enter valid numbers for all fields"
                    }
                    fatValue < 0 || carbValue < 0 || proteinValue < 0 -> {
                        errorMessage = "Values cannot be negative"
                    }
                    else -> {
                        isLoading = true
                        errorMessage = null

                        auth.currentUser?.let { user ->
                            val nutritionTarget = NutritionTarget(
                                userId = user.uid,
                                fat = fatValue,
                                protein = proteinValue,
                                carbohydrate = carbValue,
                                totalCalories = calories,
                                timestamp = System.currentTimeMillis(),
                                lastUpdated = Date()
                            )

                            db.collection("userTargets")
                                .document(user.uid)
                                .set(nutritionTarget)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onContinue()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Error saving targets: ${e.message}"
                                }
                        } ?: run {
                            isLoading = false
                            errorMessage = "No user logged in"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple80,
                contentColor = Purple40
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Purple40
                )
            } else {
                Text(
                    "Save Target",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun calculateCalories(fat: String, protein: String, carbohydrate: String): Int {
    val fatValue = fat.toFloatOrNull() ?: 0f
    val proteinValue = protein.toFloatOrNull() ?: 0f
    val carbohydrateValue = carbohydrate.toFloatOrNull() ?: 0f

    return ((fatValue * 9) + (proteinValue * 4) + (carbohydrateValue * 4)).toInt()
}