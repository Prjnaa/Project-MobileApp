package com.project.projectmap.ui.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.project.projectmap.firebase.model.DailyIntake
import com.project.projectmap.firebase.model.User
import com.project.projectmap.module.getCurrentDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalorieTrackerViewModel : ViewModel() {
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user

    private val _dailyIntake = MutableStateFlow(DailyIntake())
    val dailyIntake: StateFlow<DailyIntake> = _dailyIntake

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private var intakeListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    init {
        startListeningForData()
    }

    private fun startListeningForData() {
        currentUser?.let { user ->
            // Listen to changes in the user document
            userListener = db.collection("users")
                .document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _errorMessage.value = "Error listening to user data: ${error.message}"
                        return@addSnapshotListener
                    }
                    snapshot?.toObject(User::class.java)?.let {
                        _user.value = it
                    }
                }

            // Listen to changes in the daily intake document
            val currentDate = getCurrentDate()
            intakeListener = db.collection("intakes")
                .document("${user.uid}-$currentDate")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _errorMessage.value =
                            "Error listening to daily intake data: ${error.message}"
                        return@addSnapshotListener
                    }
                    snapshot?.toObject(DailyIntake::class.java)?.let {
                        _dailyIntake.value = it
                    } ?: run {
                        _dailyIntake.value = DailyIntake()
                    }
                }
            _isLoading.value = false
        } ?: run {
            _errorMessage.value = "No user logged in"
        }
    }

    // Optionally, if you want to stop listening when the ViewModel is cleared, you can use the following:
    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
        intakeListener?.remove()
    }
}
