package com.project.projectmap.firebase.model

data class User(
    val profile: Profile = Profile()
)

data class Profile(
    val name: String = "",
    val email: String = "",
    val calorieTarget: Int = 0,
    val proteinTarget: Int = 0,
    val fatTarget: Int = 0,
    val carbsTarget: Int = 0
)

data class DailyIntake(
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalFat: Int = 0,
    val totalCarbs: Int = 0,
    val items: Map<String, FoodItem> = emptyMap()
)

data class FoodItem(
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0
)
