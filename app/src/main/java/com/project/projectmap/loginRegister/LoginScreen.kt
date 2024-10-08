package com.project.projectmap.loginRegister

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // Google Sign-In configuration
    val token = "412463754149-f22snvcm3h65f4ijbfrb85mkbhe9t9nm.apps.googleusercontent.com" // Replace with Web Client ID from Firebase Console
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    // Firebase Auth with Google credential
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = auth.currentUser
                                checkIfUserExists(user, onLoginSuccess)
                            } else {
                                handleFirebaseException(authTask.exception, onErrorMessage = { message ->
                                    errorMessage = message
                                })
                            }
                        }
                } ?: run {
                    errorMessage = "Google Sign-In failed: ID token is null."
                }
            } catch (e: ApiException) {
                errorMessage = "Google Sign-In error: ${e.message}"
            }
        } else {
            errorMessage = "Google Sign-In canceled."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button with Email and Password
        Button(onClick = {
            if (email.isEmpty() || password.isEmpty()) {
                errorMessage = "Email and Password cannot be empty"
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            checkIfUserExists(user, onLoginSuccess)
                        } else {
                            handleFirebaseException(task.exception, onErrorMessage = { message ->
                                errorMessage = message
                            })
                        }
                    }
            }
        }) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Google Sign-In Button
        Button(
            onClick = {
                launcher.launch(googleSignInClient.signInIntent)
            }
        ) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display error message if any
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option for Registration
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}

// Function to check if the user exists and trigger success action
private fun checkIfUserExists(user: FirebaseUser?, onLoginSuccess: () -> Unit) {
    user?.let {
        // Perform any additional checks or setup for new/existing users if necessary
        onLoginSuccess()
    }
}

// Function to handle FirebaseAuth exceptions
private fun handleFirebaseException(exception: Exception?, onErrorMessage: (String) -> Unit) {
    val message = when (exception) {
        is FirebaseAuthInvalidCredentialsException -> {
            "Invalid credentials. Please check your email or password."
        }
        is FirebaseAuthInvalidUserException -> {
            "No account found with this email."
        }
        is FirebaseAuthUserCollisionException -> {
            "This email is already associated with another account."
        }
        is FirebaseAuthException -> {
            if (exception.errorCode == "ERROR_TOO_MANY_REQUESTS") {
                "Unusual activity detected. Please try again later."
            } else {
                exception.message ?: "Authentication failed."
            }
        }
        else -> {
            exception?.message ?: "Login failed"
        }
    }
    onErrorMessage(message)
}
