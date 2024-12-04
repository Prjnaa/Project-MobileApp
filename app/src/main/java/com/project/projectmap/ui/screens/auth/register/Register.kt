package com.project.projectmap.ui.screens.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.module.getCurrentDate
import com.project.projectmap.module.saveDailyIntake
import com.project.projectmap.components.msc.PasswordInput
import com.project.projectmap.firebase.model.Profile
import com.project.projectmap.module.saveUserProfile
import com.project.projectmap.firebase.model.DailyIntake
import com.project.projectmap.firebase.model.User
import com.project.projectmap.firebase.model.UserTargets

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
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        // Header Text
        Row(
            modifier = Modifier
                .fillMaxHeight(0.25f)
                .offset(y = 16.dp)
        ) {
            Text(
                text = "Let's Create your account !",
                style = TextStyle(lineHeight = 46.sp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
            )
        }

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Password Field
        PasswordInput(
            password = password,
            onPasswordChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // Re-enter Password Field
        PasswordInput(
            password = confirmPassword,
            onPasswordChange = { confirmPassword = it },
            label = "Confirm Password",
            modifier = Modifier.fillMaxWidth(),
        )

        // Register Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                        auth.createUserWithEmailAndPassword(email, password, )
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val currentUser = auth.currentUser
                                    val userId = currentUser?.uid

                                    val displayNameUpdate = userProfileChangeRequest {
                                        displayName = username
                                    }

                                    if (userId != null) {
                                        val profile = Profile(
                                            name = username,
                                            email = email,
                                        )

                                        val targets = UserTargets(
                                            calorieTarget = 0f,
                                            proteinTarget = 0f,
                                            fatTarget = 0f,
                                            carbsTarget = 0f
                                        )

                                        currentUser?.updateProfile(displayNameUpdate)

                                        saveUserProfile(
                                            userId = userId,
                                            profile = profile,
                                            targets = targets,
                                            db = db,
                                            onComplete = { success ->
                                                if (success) {
                                                    val currentDate = getCurrentDate()
                                                    val initialDailyIntake = DailyIntake()

                                                    saveDailyIntake(
                                                        userId,
                                                        currentDate,
                                                        initialDailyIntake,
                                                        db,
                                                        onComplete = { success ->
                                                            if (success) {
                                                                onRegisterSuccess()
                                                            }
                                                        }
                                                    )

                                                } else {
                                                    isLoading = false
                                                }
                                            },
                                            errorMessage = { error ->
                                                errorMessage = error
                                            }
                                        )
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
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Register",
                        style = TextStyle(
                            fontSize = 16.sp,
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
                    style = TextStyle(fontSize = 14.sp)
                )
            }

            // Login Link
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.offset(y = (-12).dp)
            ) {
                Text(
                    text = "Already have an account?",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Login",
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onNavigateToLogin() }
                )
            }
        }

    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}

