package com.project.projectmap.module

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.project.projectmap.firebase.model.DailyIntake
import kotlinx.coroutines.tasks.await

suspend fun geIntakeData(intakeId: String, db: FirebaseFirestore): DailyIntake? {
    val db = db

    return try {
        val docSnapshot = db.collection("intakes")
            .document(intakeId)
            .get()
            .await()
        docSnapshot.toObject<DailyIntake>()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}