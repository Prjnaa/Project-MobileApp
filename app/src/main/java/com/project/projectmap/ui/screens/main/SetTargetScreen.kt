package com.project.projectmap.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.firebase.model.Profile
import com.project.projectmap.firebase.model.User
import com.project.projectmap.firebase.model.UserTargets
import java.util.Date

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
fun SetTargetScreen(
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    var fat by remember { mutableStateOf("0") }
    var carbohydrate by remember { mutableStateOf("0") }
    var protein by remember { mutableStateOf("0") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val calories = calculateCalories(fat, protein, carbohydrate)

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Initially load user's existing targets if any
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = db.collection("users").document(userId)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                fat = documentSnapshot.getString("fat") ?: "0"
                carbohydrate = documentSnapshot.getString("carbohydrate") ?: "0"
                protein = documentSnapshot.getString("protein") ?: "0"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(ConstantsStyle.APP_PADDING_VAL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
    ) {
        // Header with close button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set Your Target",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
            )

            IconButton(
                onClick = onClose,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close Icon",
                )
            }
        }

        // Calories Section
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Target Calories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = calories.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " Calories/day",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }
            }

            Column {
                MacroInputField(
                    label = "Fat (grams)",
                    value = fat,
                    onValueChange = { fat = it },
                    isLoading = isLoading
                )

                MacroInputField(
                    label = "Carbohydrate (grams)",
                    value = carbohydrate,
                    onValueChange = { carbohydrate = it },
                    isLoading = isLoading
                )

                MacroInputField(
                    label = "Protein (grams)",
                    value = protein,
                    onValueChange = { protein = it },
                    isLoading = isLoading
                )
            }
        }

        // Error Message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }


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
                            val uid = user.uid

                            val newTarget = User(
                                profile = Profile(
                                    name = user.displayName ?: "",
                                    email = user.email ?: "",
                                ),
                                targets = UserTargets(
                                    calorieTarget = calories.toFloat(),
                                    fatTarget = fatValue,
                                    carbsTarget = carbValue,
                                    proteinTarget = proteinValue
                                )
                            )

                            db.collection("users").document(uid)
                                .update("targets", newTarget.targets)
                                .addOnSuccessListener {
                                    onContinue()
                                }
                                .addOnFailureListener {
                                    errorMessage = "Failed to save target"
                                    isLoading = false
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
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


//COMPONENTS FUNCTION
fun calculateCalories(fat: String, protein: String, carbohydrate: String): Int {
    val fatValue = fat.toFloatOrNull() ?: 0f
    val proteinValue = protein.toFloatOrNull() ?: 0f
    val carbohydrateValue = carbohydrate.toFloatOrNull() ?: 0f

    return ((fatValue * 9) + (proteinValue * 4) + (carbohydrateValue * 4)).toInt()
}

@Composable
fun MacroInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    focusedBorderColor: Color = MaterialTheme.colorScheme.onBackground,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.isEmpty() || it.toFloatOrNull() != null) {
                    onValueChange(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = unfocusedBorderColor,
                focusedBorderColor = focusedBorderColor
            ),
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
            enabled = !isLoading
        )
    }
}