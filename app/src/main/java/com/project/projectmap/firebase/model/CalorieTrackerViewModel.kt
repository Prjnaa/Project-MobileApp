package com.project.projectmap.firebase.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class UserNutritionTarget(
    val fat: Float = 0f,
    val protein: Float = 0f,
    val carbohydrate: Float = 0f,
    val totalCalories: Int = 2000
)

data class NutritionData(
    val currentCarbs: Float = 0f,
    val currentProtein: Float = 0f,
    val currentFat: Float = 0f,
    val currentCalories: Int = 0
)

class CalorieTrackerViewModel : ViewModel() {
    private val _nutritionData = MutableStateFlow(NutritionData())
    val nutritionData: StateFlow<NutritionData> = _nutritionData

    private val _nutritionTarget = MutableStateFlow(UserNutritionTarget())
    val nutritionTarget: StateFlow<UserNutritionTarget> = _nutritionTarget

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            currentUser?.let { user ->
                try {
                    // Get user targets
                    val targetDoc = db.collection("userTargets")
                        .document(user.uid)
                        .get()
                        .await()

                    if (targetDoc.exists()) {
                        _nutritionTarget.value = UserNutritionTarget(
                            fat = targetDoc.getDouble("fat")?.toFloat() ?: 0f,
                            protein = targetDoc.getDouble("protein")?.toFloat() ?: 0f,
                            carbohydrate = targetDoc.getDouble("carbohydrate")?.toFloat() ?: 0f,
                            totalCalories = targetDoc.getLong("totalCalories")?.toInt() ?: 2000
                        )

                        // Get today's nutrition entries
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                            Date()
                        )
                        val nutritionEntries = db.collection("dailyNutrition")
                            .whereEqualTo("userId", user.uid)
                            .whereEqualTo("date", currentDate)
                            .get()
                            .await()
                        
                        var currentCarbs = 0f
                        var currentProtein = 0f
                        var currentFat = 0f
                        var currentCalories = 0

                        for (entry in nutritionEntries.documents) {
                            currentCarbs += entry.getDouble("carbohydrate")?.toFloat() ?: 0f
                            currentProtein += entry.getDouble("protein")?.toFloat() ?: 0f
                            currentFat += entry.getDouble("fat")?.toFloat() ?: 0f
                            currentCalories += entry.getDouble("calories")?.toInt() ?: 0
                        }

                        _nutritionData.value = NutritionData(
                            currentCarbs = currentCarbs,
                            currentProtein = currentProtein,
                            currentFat = currentFat,
                            currentCalories = currentCalories
                        )
                    } else {
                        _errorMessage.value = "No nutrition target found. Please set your target."
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error loading nutrition data: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            } ?: run {
                _errorMessage.value = "No user logged in"
                _isLoading.value = false
            }
        }
    }
}