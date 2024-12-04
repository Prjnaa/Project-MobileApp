package com.project.projectmap.module

import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.firebase.model.DailyIntake
import com.project.projectmap.firebase.model.Profile
import com.project.projectmap.firebase.model.User
import com.project.projectmap.firebase.model.UserTargets

fun saveUserProfile(
    userId: String,
    profile: Profile,
    targets: UserTargets,
    db: FirebaseFirestore,
    onComplete: (Boolean) -> Unit,
    errorMessage: (String) -> Unit
) {
    val firestore = db
    val userRef = firestore.collection("users").document(userId)

    val user = User(
        profile = profile,
        targets = targets
    )

    userRef.set(user).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onComplete(true)
        } else {
            onComplete(false)
            task.exception?.let { exception ->
                errorMessage(exception.message ?: "An error occurred")
            }
        }
    }
}

fun saveDailyIntake(
    userId: String,
    date: String,
    dailyIntake: DailyIntake,
    db: FirebaseFirestore,
    onComplete: (Boolean) -> Unit
) {
    db.collection("intakes").document("$userId-$date")
        .set(dailyIntake)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
}

fun saveHistory(
    userId: String,
    date: String,
    dailyIntake: DailyIntake,
    db: FirebaseFirestore,
    onComplete: (Boolean) -> Unit
) {
    db.collection("history").document("$userId-$date")
        .set(dailyIntake)
        .addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
}