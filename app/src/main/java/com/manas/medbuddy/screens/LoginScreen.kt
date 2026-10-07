package com.manas.medbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onSignup: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome back", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Sign in to manage your health in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; showError = false },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = showError && email.isBlank()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = showError && password.isBlank()
        )
        TextButton(onClick = onForgotPassword, modifier = Modifier.fillMaxWidth()) {
            Text("Forgot password?")
        }
        if (showError) Text("Enter your email and password to continue.", color = MaterialTheme.colorScheme.error)
        Button(
            onClick = { if (email.isNotBlank() && password.isNotBlank()) onLogin() else showError = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sign in", modifier = Modifier.padding(vertical = 6.dp)) }

        TextButton(onClick = onSignup, modifier = Modifier.fillMaxWidth()) {
            Text("New to MedBuddy? Create an account")
        }
    }
}
