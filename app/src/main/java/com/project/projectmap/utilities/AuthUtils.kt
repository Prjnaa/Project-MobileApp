package com.project.projectmap.utilities

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
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
    val auth = FirebaseAuth.getInstance()
    val userRef = db.collection("users").document(userId)

    val firestoreListener = userRef.addSnapshotListener { snapshot, _ ->
        // Periksa apakah snapshot ada dan ada pengguna yang sedang login
        if (snapshot != null && snapshot.exists() && auth.currentUser != null) {
            val currentUser = auth.currentUser
            // Pastikan userId yang cocok dengan currentUser.uid
            if (currentUser?.uid == userId) {
                currentUser.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onUserDeleted() // Panggil callback setelah berhasil menghapus user
                    }
                }
            }
        }
    }

    return firestoreListener
}

fun clearCachedUserData(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().apply()
}
