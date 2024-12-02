package com.project.projectmap.ui.screens.auth.register
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Bagian Fungsi Untuk View
@Composable
fun RegisterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C)) // Background dark
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Text
            HeaderText()

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            TextFieldWithLabel(label = "Email")

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            TextFieldWithLabel(label = "Password", isPassword = true)

            Spacer(modifier = Modifier.height(8.dp))

            // Re-enter Password Field
            TextFieldWithLabel(label = "Re-enter Password", isPassword = true)

            Spacer(modifier = Modifier.height(8.dp))

            // Calorie Target Field
            TextFieldWithLabel(label = "Set Your Calories Target", keyboardType = KeyboardType.Number)

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            RegisterButton()

            Spacer(modifier = Modifier.height(16.dp))

            // Or login with text
            LoginWithText()

            Spacer(modifier = Modifier.height(8.dp))

            // Google Login Button
            GoogleLoginButton()
        }
    }
}
// Bagian Fungsi Untuk View End

// Component Untuk View
@Composable
fun HeaderText() {
    Text(
        text = "Welcome! Create Your Account Now",
        color = Color(0xFFB983FF), // Light purple
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun TextFieldWithLabel(
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = remember { "" },
            onValueChange = { /* Handle text change */ },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.color(
                containerColor = Color.Transparent,
                focusedBorderColor = Color(0xFFB983FF),
                unfocusedBorderColor = Color.Gray,
                textColor = Color.White,
                cursorColor = Color.White,
                placeholderColor = Color.Gray
            ),
            placeholder = {
                Text(text = label, color = Color.Gray)
            },
            singleLine = true
        )
    }
}

@Composable
fun RegisterButton() {
    Button(
        onClick = { /* Handle register click */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB983FF))
    ) {
        Text(text = "Register", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginWithText() {
    Text(text = "Or Login with", color = Color.White)
}

@Composable
fun GoogleLoginButton() {
    Button(
        onClick = { /* Handle Google login */ },
        modifier = Modifier
            .size(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Text(text = "G", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
// Component Untuk View End
