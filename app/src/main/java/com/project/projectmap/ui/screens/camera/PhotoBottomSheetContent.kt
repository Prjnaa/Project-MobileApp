package com.project.projectmap.ui.screens.camera

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.components.msc.getCurrentDate
import com.project.projectmap.firebase.model.FoodItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoBottomSheetContent(
    foodName: String,
    onDismiss: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    Log.d("PhotoBottomSheet", "Received foodName: $foodName") // Log penerimaan nama makanan

    var fat by remember { mutableStateOf(1.9f) }
    var carbohydrate by remember { mutableStateOf(5.8f) }
    var protein by remember { mutableStateOf(4.1f) }
    var foodNameState by remember { mutableStateOf(foodName) }
    var isEditable by remember { mutableStateOf(false) }

    val calories = remember(fat, carbohydrate, protein) {
        String.format("%.1f", (protein * 4) + (carbohydrate * 4) + (fat * 9))
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val editIcon = painterResource(id = R.drawable.editable_24)
    val editDoneIcon = painterResource(id = R.drawable.done_24)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .background(MaterialTheme.colorScheme.background)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
    ) {
        OutlinedTextField(
            value = foodNameState,
            onValueChange = {
                foodNameState = it
                Log.d("PhotoBottomSheet", "Updated foodNameState: $it")
            },
            singleLine = true,
            enabled = isEditable,
            textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
            trailingIcon = {
                IconButton(onClick = { isEditable = !isEditable }) {
                    Icon(
                        painter = if (isEditable) editDoneIcon else editIcon,
                        modifier = Modifier.size(32.dp),
                        contentDescription = "Edit food name"
                    )
                }
            },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (isEditable) MaterialTheme.colorScheme.primary else Color.Transparent,
                unfocusedBorderColor = if (isEditable) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                disabledBorderColor = Color.Transparent
            )

        )
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 16.dp)
            ) {
                Text(text = "Calories", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "$calories Cals",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                EditableNutrientInfo("Fat", fat) { fat = it }
                EditableNutrientInfo("Carbohydrate", carbohydrate) { carbohydrate = it }
                EditableNutrientInfo("Protein", protein) { protein = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    currentUser?.let { user ->
                        if (foodNameState.isBlank()) {
                            Toast.makeText(
                                context,
                                "Food name cannot be empty!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return@let
                        }

                        // Ensure nutrient values are valid
                        if (fat < 0 || carbohydrate < 0 || protein < 0) {
                            Toast.makeText(
                                context,
                                "Nutrient values cannot be negative!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@let
                        }

                        val currentDate = getCurrentDate()
                        val uniqueKey = System.currentTimeMillis().toString()

                        val userDailyRef = db.collection("intakes").document(
                            "${user.uid}-$currentDate"
                        )

                        val foodEntry = FoodItem(
                            name = foodNameState,
                            calories = calories.toFloat(),
                            protein = protein,
                            fat = fat,
                            carbs = carbohydrate,
                        )

                        val batch = db.batch()

                        batch.update(userDailyRef, "items.$uniqueKey", foodEntry)

                        batch.update(
                            userDailyRef,
                            "totalCalories", FieldValue.increment(calories.toDouble()),
                            "totalProtein", FieldValue.increment(protein.toDouble()),
                            "totalFat", FieldValue.increment(fat.toDouble()),
                            "totalCarbs", FieldValue.increment(carbohydrate.toDouble())
                        )

                        // Commit the batch
                        batch.commit()
                            .addOnSuccessListener {
                                Log.d("PhotoBottomSheet", "DocumentSnapshot successfully written!")
                                onSubmitSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.w("PhotoBottomSheet", "Error writing document", e)
                            }

                    } ?: run {
                        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ConstantsStyle.DEFAULT_HEIGHT_VAL),
                shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EditableNutrientInfo(name: String, amount: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 12.dp)
        )
        OutlinedTextField(
            value = amount.toString(),
            onValueChange = {
                if (it.isEmpty() || it.toFloatOrNull() != null) {
                    onValueChange(it.toFloatOrNull() ?: 0f)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(ConstantsStyle.DEFAULT_HEIGHT_VAL),
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
        )

    }
}
