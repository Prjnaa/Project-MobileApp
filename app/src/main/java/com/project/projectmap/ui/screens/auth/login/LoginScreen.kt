package com.project.projectmap.ui.screens.auth.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.BuildConfig
import com.project.projectmap.R
import com.project.projectmap.components.msc.PasswordInput

// Bagian Fungsi Untuk View
@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

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
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight(0.25f)
                .offset(y = 16.dp)
        ) {
            Text(
                text = "Welcome back! Let's log in to your account",
                style = TextStyle(lineHeight = 46.sp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
            )
        }

//        EMAIL TEXT FIELD
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

//        PASSWORD FIELD
        PasswordInput(
            password = password,
            onPasswordChange = { password = it },
            modifier = Modifier.fillMaxWidth()
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
//            LOGIN BUTTON
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Login",
                        fontSize = 16.sp
                    )
                }
            }

//            REGISTER LINK
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.offset(y = (-12).dp)
            ) {
                Text(
                    text = "Don’t have an account?",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Register",
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onNavigateToRegister() }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary.copy(0.5f),
                    thickness = 1.dp
                )
                Text(
                    text = "OR",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary.copy(0.5f),
                    thickness = 1.dp
                )
            }

//            GOOGLE LOGIN BUTTON
            Button(
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(0.75f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .offset(y = (16).dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Google", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
}

// Bagian Fungsi Untuk View End

// Component
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
// Component End

