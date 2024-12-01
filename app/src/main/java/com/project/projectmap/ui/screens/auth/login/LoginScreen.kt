package com.project.projectmap.ui.screens.auth.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.project.projectmap.buildconfig.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Google Sign-In configuration
    val token = BuildConfig.GOOGLE_API_TOKEN
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
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = auth.currentUser
                                user?.let { firebaseUser ->
                                    // Check if user has already set target
                                    db.collection("userTargets")
                                        .document(firebaseUser.uid)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            isLoading = false
                                            val isNewUser = !document.exists()
                                            onLoginSuccess(isNewUser)
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            errorMessage = "Error checking user data"
                                        }
                                }
                            } else {
                                isLoading = false
                                handleFirebaseException(authTask.exception) { message ->
                                    errorMessage = message
                                }
                            }
                        }
                } ?: run {
                    isLoading = false
                    errorMessage = "Google Sign-In failed: ID token is null"
                }
            } catch (e: ApiException) {
                isLoading = false
                errorMessage = "Google Sign-In error: ${e.message}"
            }
        } else {
            isLoading = false
            errorMessage = "Google Sign-In cancelled"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Welcome Text
        Text(
            text = "Welcome back! Let's\nlog in to your account",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Purple80
            ),
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Email and Password cannot be empty"
                } else {
                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.let { firebaseUser ->
                                    // Check if user has already set target
                                    db.collection("userTargets")
                                        .document(firebaseUser.uid)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            isLoading = false
                                            val isNewUser = !document.exists()
                                            onLoginSuccess(isNewUser)
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            errorMessage = "Error checking user data"
                                        }
                                }
                            } else {
                                isLoading = false
                                handleFirebaseException(task.exception) { message ->
                                    errorMessage = message
                                }
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
                    "Login",
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
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Or Login with Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PurpleGrey80,
                thickness = 1.dp
            )
            Text(
                text = "Or Login with",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = PurpleGrey40
                )
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PurpleGrey80,
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Google Sign In Button
        OutlinedButton(
            onClick = {
                if (!isLoading) {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier
                .size(width = 120.dp, height = 60.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Purple40
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Sign In",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Register Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Don't have an account? ",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
            Text(
                "Register Now",
                modifier = Modifier.clickable(
                    enabled = !isLoading,
                    onClick = onRegisterClick
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

private fun handleFirebaseException(exception: Exception?, onErrorMessage: (String) -> Unit) {
    val message = when (exception) {
        is FirebaseAuthInvalidCredentialsException ->
            "Invalid credentials. Please check your email or password."
        is FirebaseAuthInvalidUserException ->
            "No account found with this email."
        is FirebaseAuthUserCollisionException ->
            "This email is already associated with another account."
        is FirebaseAuthException -> {
            if (exception.errorCode == "ERROR_TOO_MANY_REQUESTS")
                "Unusual activity detected. Please try again later."
            else
                exception.message ?: "Authentication failed."
        }
        else -> exception?.message ?: "Login failed"
    }
    onErrorMessage(message)
}