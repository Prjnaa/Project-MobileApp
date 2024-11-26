package com.project.projectmap.ui.screens.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.ui.theme.Purple40
import com.project.projectmap.ui.theme.Purple80
import com.project.projectmap.ui.theme.PurpleGrey40
import com.project.projectmap.ui.theme.PurpleGrey80
import kotlinx.coroutines.tasks.await

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start) {
            Spacer(modifier = Modifier.height(80.dp))

            // Welcome Text
            Text(
                text = "Welcome! Create Your\nAccount Now",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Purple80),
                lineHeight = 34.sp)

        Spacer(modifier = Modifier.height(48.dp))

        // Username Field
        Text(
            text = "Username",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Choose a username", color = PurpleGrey40) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PurpleGrey80,
                focusedBorderColor = Purple40
            ),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        Text(
            text = "Email",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your email", color = PurpleGrey40) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PurpleGrey80,
                focusedBorderColor = Purple40
            ),
            singleLine = true,
            enabled = !isLoading
        )

            Spacer(modifier = Modifier.height(24.dp))

        // Password Field
        Text(
            text = "Password",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Enter your password", color = PurpleGrey40) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PurpleGrey80,
                focusedBorderColor = Purple40
            ),
            singleLine = true,
            enabled = !isLoading
        )

            Spacer(modifier = Modifier.height(24.dp))

        // Re-enter Password Field
        Text(
            text = "Re-enter Password",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            placeholder = { Text("Confirm Password", color = PurpleGrey40) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PurpleGrey80,
                focusedBorderColor = Purple40
            ),
            singleLine = true,
            enabled = !isLoading
        )

            Spacer(modifier = Modifier.height(32.dp))

        // Register Button
        Button(
            onClick = {
                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    errorMessage = "All fields are required"
                } else if (username.length < 3) {
                    errorMessage = "Username must be at least 3 characters long"
                } else if (!isValidEmail(email)) {
                    errorMessage = "Please enter a valid email address"
                } else if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters long"
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else {
                    isLoading = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val currentUser = auth.currentUser
                                currentUser?.let { user ->
                                    // Create user profile in Firestore
                                    val userProfile = hashMapOf(
                                        "uid" to user.uid,
                                        "username" to username,
                                        "email" to email,
                                        "initialTarget" to false
                                    )

                                    db.collection("users")
                                        .document(user.uid)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            // Update user profile with display name
                                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(username)
                                                .build()

                                            user.updateProfile(profileUpdates)
                                                .addOnCompleteListener { profileTask ->
                                                    isLoading = false
                                                    if (profileTask.isSuccessful) {
                                                        onRegisterSuccess()
                                                    } else {
                                                        errorMessage = profileTask.exception?.message ?: "Profile update failed"
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Error saving user profile: ${e.message}"
                                        }
                                }
                            } else {
                                isLoading = false
                                errorMessage = task.exception?.message ?: "Registration failed"
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple80),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Purple40
                )
            } else {
                Text(
                    "Register",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Purple40
                    )
                )
            }
        }

            // Error Message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = TextStyle(fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.weight(1f))

        // Login Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Already have an account? ",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
            Text(
                "Login Now",
                modifier = Modifier.clickable(
                    enabled = !isLoading,
                    onClick = onNavigateToLogin
                ),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = PurpleGrey40,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}