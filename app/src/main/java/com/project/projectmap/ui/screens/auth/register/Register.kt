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
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.firebase.model.Calories
import com.project.projectmap.ui.theme.Purple40
import com.project.projectmap.ui.theme.Purple80
import com.project.projectmap.ui.theme.PurpleGrey40
import com.project.projectmap.ui.theme.PurpleGrey80

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var caloriesTarget by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

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

            // Email Field
            Text(
                text = "Email",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Value", color = PurpleGrey40) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PurpleGrey80, focusedBorderColor = Purple40),
                singleLine = true)

            Spacer(modifier = Modifier.height(24.dp))

            // Password Field
            Text(
                text = "Password",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Value", color = PurpleGrey40) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PurpleGrey80, focusedBorderColor = Purple40),
                singleLine = true)

            Spacer(modifier = Modifier.height(24.dp))

            // Re-enter Password Field
            Text(
                text = "Re-enter Password",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = { Text("Value", color = PurpleGrey40) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PurpleGrey80, focusedBorderColor = Purple40),
                singleLine = true)

            Spacer(modifier = Modifier.height(24.dp))

            //        caloriesTarget input
            Text(
                text = "Set Your Calories Target",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = caloriesTarget,
                onValueChange = { caloriesTarget = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Value", color = PurpleGrey40) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PurpleGrey80, focusedBorderColor = Purple40),
                trailingIcon = { Text("/day", color = PurpleGrey40) },
                singleLine = true)

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    if (password == confirmPassword) {
                        auth
                            .createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    //Add Calories to database
                                    currentUser?.let {
                                        val userCalRef = db.collection("calories").document(currentUser.uid)

                                        val data = Calories(
                                            userId = currentUser.uid,
                                            caloriesTarget = caloriesTarget.toInt(),
                                        )

                                        userCalRef.set(data)
                                    }

                                    onRegisterSuccess() // Navigate to next screen after successful
                                    // registration
                                    onNavigateToLogin() // Navigate to login screen
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    } else {
                        errorMessage = "Passwords do not match"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text(
                        "Register",
                        style =
                            TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Purple40))
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center) {
                    Text(
                        "Already have an account? ",
                        style = TextStyle(fontSize = 14.sp, color = Color.Black))
                    Text(
                        "Login Now",
                        modifier = Modifier.clickable(onClick = onNavigateToLogin),
                        style =
                            TextStyle(
                                fontSize = 14.sp,
                                color = PurpleGrey40,
                                fontWeight = FontWeight.SemiBold))
                }
        }
}
