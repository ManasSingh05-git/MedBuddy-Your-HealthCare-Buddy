package com.manas.medbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(onSignup: () -> Unit, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create your account", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Start your simpler health journey today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(value = name, onValueChange = { name = it; showError = false }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Full name") }, isError = showError && name.isBlank())
        OutlinedTextField(value = email, onValueChange = { email = it; showError = false }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email address") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = showError && email.isBlank())
        OutlinedTextField(value = password, onValueChange = { password = it; showError = false }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Create password") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), visualTransformation = PasswordVisualTransformation(), isError = showError && password.length < 6)
        if (showError) Text("Complete all fields; password needs at least 6 characters.", color = MaterialTheme.colorScheme.error)
        Button(
            onClick = { if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) onSignup() else showError = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create account", modifier = Modifier.padding(vertical = 6.dp)) }
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Already have an account? Sign in") }
    }
}
