package com.project.projectmap.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.project.projectmap.R
import com.project.projectmap.firebase.model.User
import com.project.projectmap.ui.screens.badges.Item
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BadgesViewModel : ViewModel() {
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _items = MutableStateFlow(emptyList<Item>())
    val items: StateFlow<List<Item>> = _items

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private var userListener: ListenerRegistration? = null

    private val _showInsufficientFundsMessage = MutableStateFlow(false)
    val showInsufficientFundsMessage: StateFlow<Boolean> = _showInsufficientFundsMessage

    init {
        startListeningForData()
        loadDummyItems()
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
            _isLoading.value = false
        } ?: run {
            _errorMessage.value = "No user logged in"
        }
    }

    private fun loadDummyItems() {
        val dummyItems = listOf(
            Item(
                id = "tanaman1",
                name = "Tanaman 1",
                price = 10,
                category = "Tanaman",
                imageRes = R.drawable.tanaman1
            ),
            Item(
                id = "tanaman2",
                name = "Tanaman 2",
                price = 20,
                category = "Tanaman",
                imageRes = R.drawable.tanaman2
            ),
            Item(
                id = "tanaman3",
                name = "Tanaman 3",
                price = 30,
                category = "Tanaman",
                imageRes = R.drawable.tanaman3
            )
        )
        _items.value = dummyItems
    }


    fun buyItem(item: Item) {
        if (user.value.profile.coin >= item.price && !user.value.items.contains(item.id)) {
            val updatedUser = user.value.copy(
                profile = user.value.profile.copy(
                    coin = user.value.profile.coin - item.price
                ),
                items = user.value.items + item.id
            )
            _user.value = updatedUser

            // Update the user data in Firestore
            currentUser?.let { user ->
                db.collection("users")
                    .document(user.uid)
                    .set(updatedUser)
                    .addOnFailureListener { e ->
                        _errorMessage.value = "Error updating user data: ${e.message}"
                    }
            }
        } else {
            _showInsufficientFundsMessage.value = true
            viewModelScope.launch {
                delay(2000)
                _showInsufficientFundsMessage.value = false
            }
        }
    }

    fun equipItem(itemId: String) {
        val updatedUser = user.value.copy(
            equippedItem = itemId
        )
        _user.value = updatedUser

        // Update the user data in Firestore
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .set(updatedUser)
                .addOnFailureListener { e ->
                    _errorMessage.value = "Error updating user data: ${e.message}"
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }
}