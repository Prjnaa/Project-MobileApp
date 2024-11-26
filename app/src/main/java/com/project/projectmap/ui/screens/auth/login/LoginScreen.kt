package com.project.projectmap.ui.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.projectmap.components.msc.InputType
import com.project.projectmap.components.msc.TextInputComponent

@Composable
@Preview
fun LoginScreen() {
    showUI()
}

@Composable
private fun showUI() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp, 32.dp, 20.dp, 24.dp)
        ,
    ) {
        Text(
            text = "Welcome Back ! \nLogin to your account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        loginFields()
    }
}

@Composable
private fun loginFields() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 32.dp),
    ) {
        TextInputComponent(
            label = "Email",
            value = email,
            onValueChange = { email = it},
            inputType = InputType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInputComponent(
            label = "Password",
            value = password,
            onValueChange = { password = it},
            inputType = InputType.Password
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInputComponent(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { confirmPassword = it},
            inputType = InputType.Password
        )
    }
}