package com.project.projectmap.ui.screens.camera

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoBottomSheetContent(
    onDismiss: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    var fat by remember { mutableStateOf(1.9f) }
    var carbohydrate by remember { mutableStateOf(5.8f) }
    var protein by remember { mutableStateOf(4.1f) }
    var foodName by remember { mutableStateOf("Milk") }

    val calories = remember(fat, carbohydrate, protein) {
        String.format("%.1f", (protein * 4) + (carbohydrate * 4) + (fat * 9))
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        // Editable Food Name
        TextField(
            value = foodName,
            onValueChange = { foodName = it },
            singleLine = true,
            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            )
        )

        Text(text = "Calories")

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
            onClick = {
                currentUser?.let { user ->
                    // Get current date
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date()
                    )

                    // Create nutrition entry
                    val nutritionEntry = hashMapOf(
                        "userId" to user.uid,
                        "date" to currentDate,
                        "fat" to fat,
                        "protein" to protein,
                        "carbohydrate" to carbohydrate,
                        "calories" to calories.toFloat(),
                        "foodName" to foodName,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    // Add to daily entries collection
                    db.collection("dailyNutrition")
                        .add(nutritionEntry)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Nutrition tracked successfully!", Toast.LENGTH_SHORT).show()
                            // Panggil callback submit success
                            onSubmitSuccess()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
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
                // Gantilah koma dengan titik dan pastikan bahwa input valid sebagai angka desimal
                val sanitizedInput = it.replace(",", ".")

                // Cek apakah input valid (angka desimal) dan ubah ke Float
                val parsedValue = try {
                    sanitizedInput.toFloat()
                } catch (e: NumberFormatException) {
                    // Jika parsing gagal, fallback ke 0f
                    0f
                }

                // Update nilai setelah parsing berhasil
                onValueChange(parsedValue)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            )
        )
    }
}

