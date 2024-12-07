package com.project.projectmap.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

fun isSessionExpired(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val lastLoginTime = sharedPreferences.getLong("last_login_time", 0)
    val currentTime = System.currentTimeMillis()
    val sixtyDaysInMillis = 60L * 24 * 60 * 60 * 1000
    return currentTime - lastLoginTime > sixtyDaysInMillis
}

fun saveLoginInfo(context: Context, userId: String) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit()
        .putString("cached_user_id", userId)
        .putLong("last_login_time", System.currentTimeMillis())
        .apply()
}



fun getCachedUserId(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("cached_user_id", null)
}

fun listenToUserDeletion(userId: String, onUserDeleted: () -> Unit): ListenerRegistration {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)
    return userRef.addSnapshotListener { snapshot, _ ->
        if (snapshot == null || !snapshot.exists()) {
            onUserDeleted()
        }
    }
}

fun clearCachedUserData(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().apply()
}
