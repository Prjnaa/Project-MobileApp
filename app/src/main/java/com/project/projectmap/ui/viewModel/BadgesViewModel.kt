package com.project.projectmap.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
            Item(id = "item1", name = "Teddy Bear", price = 10, category = "Toys"),
            Item(id = "item2", name = "Rubber Duck", price = 20, category = "Toys"),
            Item(id = "item3", name = "Beach Ball", price = 30, category = "Toys"),
            Item(id = "item4", name = "Carrot", price = 15, category = "Foods"),
            Item(id = "item5", name = "Apple", price = 25, category = "Foods"),
            Item(id = "item6", name = "Banana", price = 35, category = "Foods"),
            Item(id = "item7", name = "Orange Juice", price = 20, category = "Drinks"),
            Item(id = "item8", name = "Milkshake", price = 30, category = "Drinks"),
            Item(id = "item9", name = "Lemonade", price = 40, category = "Drinks")
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