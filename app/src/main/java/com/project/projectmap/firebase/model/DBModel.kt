package com.project.projectmap.firebase.model

import android.content.ClipData.Item

data class User(
    val profile: Profile = Profile(),
    val targets: UserTargets = UserTargets()
)

data class Profile(
    val name: String = "",
    val email: String = "",
)

data class UserTargets(
    val calorieTarget: Float = 0f,
    val proteinTarget: Float = 0f,
    val fatTarget: Float = 0f,
    val carbsTarget: Float = 0f
)

data class DailyIntake(
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalFat: Float = 0f,
    val totalCarbs: Float = 0f,
    val items: Map<String, FoodItem> = emptyMap()
)

data class FoodItem(
    val name: String = "",
    val calories: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carbs: Float = 0f,
    val plusCoins: Int = 0
//    val type: ItemType
)

enum class ItemType {
    Food,
    Drink
}
