
package com.project.projectmap.ui.screens.auth.login

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.BuildConfig
import com.project.projectmap.R
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.components.msc.PasswordInput
import com.project.projectmap.firebase.model.Profile
//import com.project.projectmap.utilities.saveLoginInfo

@Composable
fun LoginScreen(
    onLoginSuccess: (Boolean) -> Unit = {},
    onTargetNotFound: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_API_TOKEN)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                handleGoogleSignInResult(
                    data = data,
                    auth = auth,
                    db = db,
                    onTargetNotFound = onTargetNotFound,
                    onLoginSuccess = onLoginSuccess,
                    onErrorMessage = { errorMessage = it },
                    context = context
                )
            }
        } else {
            errorMessage = "Google Sign-In cancelled"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ConstantsStyle.APP_PADDING_VAL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top)
    ) {
        // Welcome Text
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

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL),
            modifier = Modifier.fillMaxWidth()
        )

        // Password Input
        PasswordInput(
            password = password,
            onPasswordChange = {
                password = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Error Message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        // Buttons and Links
        LoginButtons(
            email = email,
            password = password,
            isLoading = isLoading,
            onLoginClick = {
                handleEmailPasswordLogin(
                    auth = auth,
                    db = db,
                    email = email,
                    onTargetNotFound = onTargetNotFound,
                    password = password,
                    setIsLoading = { isLoading = it },
                    onLoginSuccess = onLoginSuccess,
                    onErrorMessage = { errorMessage = it },
                    context = context
                )
            },
            onGoogleLoginClick = { launcher.launch(googleSignInClient.signInIntent) },
            onNavigateToRegister = onNavigateToRegister
        )
    }
}

@Composable
private fun LoginButtons(
    email: String,
    password: String,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Login Button
        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_VAL)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        // Register Link
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
            modifier = Modifier
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                color = MaterialTheme.colorScheme.onBackground.copy(0.25f),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .weight(0.425f)
            )
            Text(
                text = "OR",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(0.4f),
                modifier = Modifier
                    .weight(0.15f)
            )
            Divider(
                color = MaterialTheme.colorScheme.onBackground.copy(0.25f),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .weight(0.425f)
            )
        }

        // Google Login Button
        Button(
            onClick = onGoogleLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(0.75f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
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

private fun handleGoogleSignInResult(
    data: Intent,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onTargetNotFound: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit,
    onErrorMessage: (String) -> Unit,
    context: Context
) {
    try {
        val account =
            GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        val idToken = account?.idToken
        if (idToken == null) {
            onErrorMessage("Google Sign-In failed: ID token is null")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    auth.currentUser?.let { user ->
                        handleUserLogin(
                            db,
                            user,
                            onTargetNotFound = onTargetNotFound,
                            onLoginSuccess = onLoginSuccess,
                            onErrorMessage = onErrorMessage,
                            setIsLoading = { false },
                            context = context
                        )
                    }
                } else {
                    onErrorMessage("Authentication failed: ${authTask.exception?.message}")
                }
            }
    } catch (e: ApiException) {
        onErrorMessage("Google Sign-In error: ${e.message}")
    }
}

private fun handleEmailPasswordLogin(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    email: String,
    password: String,
    onTargetNotFound: () -> Unit,
    setIsLoading: (Boolean) -> Unit,
    onLoginSuccess: (Boolean) -> Unit,
    onErrorMessage: (String) -> Unit,
    context: Context
) {
    if (email.isEmpty() || password.isEmpty()) {
        onErrorMessage("Please fill in both email and password")
        return
    }

    setIsLoading(true)
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            setIsLoading(false)
            if (task.isSuccessful) {
                auth.currentUser?.let { user ->
                    handleUserLogin(
                        db,
                        user,
                        onTargetNotFound = onTargetNotFound,
                        onLoginSuccess = onLoginSuccess,
                        onErrorMessage = onErrorMessage,
                        setIsLoading = setIsLoading,
                        context = context
                    )
                }
            } else {
                onErrorMessage("Login failed: ${task.exception?.message}")
            }
        }
}

private fun handleUserLogin(
    db: FirebaseFirestore,
    user: FirebaseUser,
    onTargetNotFound: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit,
    onErrorMessage: (String) -> Unit,
    setIsLoading: (Boolean) -> Unit,
    context: Context
) {
    db.collection("users")
        .document(user.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val userData = document.data
                if (userData?.containsKey("targets") == true) {
//                    saveLoginInfo(context, user.uid)
                    onLoginSuccess(false)
                } else {
                    onTargetNotFound()
                }
            } else {
                saveNewUserProfile(db, user)
                onLoginSuccess(true)
            }
        }
        .addOnFailureListener {
            onErrorMessage("Failed to fetch user data")
        }
}

private fun saveNewUserProfile(db: FirebaseFirestore, user: FirebaseUser) {
    val profile = Profile(
        name = user.displayName ?: "user",
        email = user.email ?: "not set"
    )
    db.collection("users").document(user.uid).set(profile)
}
