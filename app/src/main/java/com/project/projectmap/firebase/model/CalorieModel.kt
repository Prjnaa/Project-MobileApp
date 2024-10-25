package com.project.projectmap.firebase.model

data class Calories(
    val userId: String,
    val caloriesTarget: Int,
)

data class DailyCalories(
    val date: String,
    val caloriesCurrent: Int,
    val fatCurrent: Int,
    val proteinCurrent: Int,
    val carbsCurrent: Int
)
