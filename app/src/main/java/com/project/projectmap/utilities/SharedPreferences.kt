package com.project.projectmap.utilities

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

//private const val PREF_NAME = "user_prefs"
//private const val USER_ID_KEY = "user_id"
//private const val LAST_LOGIN_TIMESTAMP_KEY = "last_login_timestamp"
//private const val EXPIRY_DURATION = 30 * 24 * 60 *  60 * 1000L
//
//fun saveUserData(context: Context, userId: String) {
//    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    val editor = prefs.edit()
//    editor.putString(USER_ID_KEY, userId)
//    editor.putLong(LAST_LOGIN_TIMESTAMP_KEY, System.currentTimeMillis())
//    editor.apply()
//}
//
//fun getCachedUserId(context: Context): String? {
//    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    return prefs.getString(USER_ID_KEY, null)
//}
//
//fun isSessionExpired(context: Context): Boolean {
//    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    val lastLoginTimestamp = prefs.getLong(LAST_LOGIN_TIMESTAMP_KEY, 0L)
//    return System.currentTimeMillis() - lastLoginTimestamp > EXPIRY_DURATION
//}
//
//fun clearCachedUserData(context: Context) {
//    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    val editor = prefs.edit()
//    editor.clear()
//    editor.apply()
//}

