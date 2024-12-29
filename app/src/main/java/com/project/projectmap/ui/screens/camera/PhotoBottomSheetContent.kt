package com.project.projectmap.ui.screens.camera

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.project.projectmap.firebase.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoBottomSheetContent(
    foodName: String,
    onDismiss: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    Log.d("PhotoBottomSheet", "Received foodName: $foodName") // Log penerimaan nama makanan

    val context = LocalContext.current

    // Format nama makanan: from "fried_rice" -> "Fried Rice"
    val formattedFoodName = remember(foodName) {
        foodName.split("_").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        }
    }

    // Add base values to store original API values (per 100g)
    var baseFat by remember { mutableStateOf(0f) }
    var baseCarbohydrate by remember { mutableStateOf(0f) }
    var baseProtein by remember { mutableStateOf(0f) }

    // Values that will be displayed and adjusted by serving size
    var fat by remember { mutableStateOf(0f) }
    var carbohydrate by remember { mutableStateOf(0f) }
    var protein by remember { mutableStateOf(0f) }
    var servingSize by remember { mutableStateOf(100f) }
    var foodNameState by remember { mutableStateOf(formattedFoodName) }
    var isEditable by remember { mutableStateOf(false) }
    var plusCoin by remember { mutableStateOf(0) }

    val calories = remember(fat, carbohydrate, protein) {
        String.format("%.1f", (protein * 4) + (carbohydrate * 4) + (fat * 9))
    }

    // Function to update nutritional values based on serving size
    fun updateNutritionalValues(newServingSize: Float) {
        val multiplier = newServingSize / 100f
        fat = baseFat * multiplier
        carbohydrate = baseCarbohydrate * multiplier
        protein = baseProtein * multiplier
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    val editIcon = painterResource(id = R.drawable.editable_24)
    val editDoneIcon = painterResource(id = R.drawable.done_24)

    // Panggil API saat foodNameState berubah (setelah di format)
    LaunchedEffect(foodNameState) {
        val apiKey = "8yZlZFOTNQbB86Aqx9ok0w==ZIgi4W2SC1j85Yqz"
        val client = OkHttpClient()
        val url = "https://api.calorieninjas.com/v1/nutrition?query=${foodNameState}"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Api-Key", apiKey)
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string()
                if (response.isSuccessful && !bodyString.isNullOrEmpty()) {
                    val json = JSONObject(bodyString)
                    val items = json.getJSONArray("items")
                    if (items.length() > 0) {
                        val item = items.getJSONObject(0)
                        // Store base values from API (per 100g)
                        val apiFat = item.optDouble("fat_total_g", fat.toDouble())
                        val apiCarbs = item.optDouble("carbohydrates_total_g", carbohydrate.toDouble())
                        val apiProtein = item.optDouble("protein_g", protein.toDouble())

                        // Update state di main thread
                        withContext(Dispatchers.Main) {
                            // Set base values (per 100g)
                            baseFat = apiFat.toFloat()
                            baseCarbohydrate = apiCarbs.toFloat()
                            baseProtein = apiProtein.toFloat()

                            // Update displayed values based on serving size
                            updateNutritionalValues(servingSize)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "No data found for $foodNameState", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to fetch data from API", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("PhotoBottomSheet", "Error: ${e.message}")
                }
            }
        }
    }

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
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = if (isEditable) MaterialTheme.colorScheme.primary else Color.Transparent,
                unfocusedBorderColor = if (isEditable) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                disabledBorderColor = Color.Transparent
            )
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                // Add serving size input
                EditableNutrientInfo(
                    name = "Serving Size (g)",
                    amount = servingSize,
                    onValueChange = { newSize ->
                        if (newSize > 0) {
                            servingSize = newSize
                            updateNutritionalValues(newSize)
                        }
                    }
                )
                EditableNutrientInfo("Fat", fat) { newValue ->
                    fat = newValue
                    // Update base value when manually edited
                    baseFat = newValue / (servingSize / 100f)
                }
                EditableNutrientInfo("Carbohydrate", carbohydrate) { newValue ->
                    carbohydrate = newValue
                    baseCarbohydrate = newValue / (servingSize / 100f)
                }
                EditableNutrientInfo("Protein", protein) { newValue ->
                    protein = newValue
                    baseProtein = newValue / (servingSize / 100f)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val user = currentUser
                    if (user == null) {
                        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (foodNameState.isBlank()) {
                        Toast.makeText(
                            context,
                            "Food name cannot be empty!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (fat < 0 || carbohydrate < 0 || protein < 0 || servingSize <= 0) {
                        Toast.makeText(
                            context,
                            "Invalid nutrient values or serving size!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val currentDate = getCurrentDate()
                    val uniqueKey = System.currentTimeMillis().toString()
                    val dbRef = db.collection("intakes").document("${user.uid}-$currentDate")

                    plusCoin = addCoins(
                        uid = user.uid,
                        db = db,
                        calories = calories.toFloat(),
                        fat = fat,
                        protein = protein,
                        carbs = carbohydrate
                    )

                    val foodEntry = FoodItem(
                        name = foodNameState,
                        calories = calories.toFloat(),
                        protein = protein,
                        fat = fat,
                        carbs = carbohydrate,
                        servingSize = servingSize,
                        plusCoins = plusCoin,
                        timestamp = System.currentTimeMillis()
                    )

                    // Check if document exists first
                    dbRef.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Document exists, proceed with update
                                val batch = db.batch()
                                batch.update(dbRef, "items.$uniqueKey", foodEntry)
                                batch.update(
                                    dbRef,
                                    "totalCalories", FieldValue.increment(calories.toDouble()),
                                    "totalProtein", FieldValue.increment(protein.toDouble()),
                                    "totalFat", FieldValue.increment(fat.toDouble()),
                                    "totalCarbs", FieldValue.increment(carbohydrate.toDouble())
                                )

                                batch.commit()
                                    .addOnSuccessListener {
                                        Log.d("PhotoBottomSheet", "Document updated successfully!")
                                        onSubmitSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("PhotoBottomSheet", "Error updating document", e)
                                        Toast.makeText(
                                            context,
                                            "Error updating data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // Document doesn't exist, create it
                                val initialData = hashMapOf(
                                    "items" to hashMapOf(uniqueKey to foodEntry),
                                    "totalCalories" to calories.toDouble(),
                                    "totalProtein" to protein.toDouble(),
                                    "totalFat" to fat.toDouble(),
                                    "totalCarbs" to carbohydrate.toDouble(),
                                    "date" to currentDate
                                )

                                dbRef.set(initialData)
                                    .addOnSuccessListener {
                                        Log.d("PhotoBottomSheet", "New document created successfully!")
                                        onSubmitSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("PhotoBottomSheet", "Error creating document", e)
                                        Toast.makeText(
                                            context,
                                            "Error creating data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("PhotoBottomSheet", "Error checking document", e)
                            Toast.makeText(
                                context,
                                "Error checking data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ConstantsStyle.DEFAULT_HEIGHT_VAL),
                shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
                colors = ButtonDefaults.buttonColors(
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
            value = String.format("%.1f", amount),
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}

fun addCoins(uid: String, db: FirebaseFirestore, calories: Float, fat: Float, protein: Float, carbs: Float): Int {
    var coinsToAdd = 0

    val proteinRatio = protein / calories
    val fatRatio = fat / calories
    val carbsRatio = carbs / calories

    if (proteinRatio > fatRatio && proteinRatio > carbsRatio) {
        // Food with the highest protein-to-calorie ratio is considered the healthiest
        coinsToAdd = (calories / 7).toInt() // Add more coins for healthier food
    } else if (fatRatio > proteinRatio && fatRatio > carbsRatio) {
        // Food with the highest fat-to-calorie ratio
        coinsToAdd = (calories / 10).toInt() // Add fewer coins for higher fat content
    } else {
        // Food with the highest carbs-to-calorie ratio
        coinsToAdd = (calories / 8).toInt() // Add coins for balanced or carb-heavy food
    }

    CoroutineScope(Dispatchers.IO).launch {
        val userRef = db.collection("users").document(uid)

        try {
            val document = userRef.get().await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let {
                    val updatedUser = it.addCoins(coinsToAdd)
                    userRef.set(updatedUser).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return coinsToAdd
}