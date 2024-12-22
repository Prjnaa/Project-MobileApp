package com.project.projectmap.utilities

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

val USER_ID = stringPreferencesKey("user_id")
val TIMESTAMP = longPreferencesKey("timestamp")

const val EXPIRY_TIME = 1000 * 60 * 60 * 24 * 30L

suspend fun saveSession(context: Context, userId:String) {
    context.dataStore.edit { prefs ->
        prefs[USER_ID] = userId
        prefs[TIMESTAMP] = System.currentTimeMillis()
    }
}

suspend fun isSessionExpired(context: Context): Boolean {
    val prefs = context.dataStore.data.first()
    val userId = prefs[USER_ID]
    val timestamp = prefs[TIMESTAMP]

    return if (userId != null && timestamp != null) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp
        if (elapsedTime > EXPIRY_TIME) {
            // Session expired, clear the data
            clearSession(context)
            true
        } else {
            false
        }
    } else {
        true // No userId or timestamp, consider it expired
    }
}

suspend fun clearSession(context: Context) {
    context.dataStore.edit { prefs ->
        prefs.remove(USER_ID)
        prefs.remove(TIMESTAMP)
    }
}