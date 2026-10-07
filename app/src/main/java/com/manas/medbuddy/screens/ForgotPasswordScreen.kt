package com.manas.medbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Reset password", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Enter your email and we’ll send a reset link.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; sent = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email address") },
            singleLine = true
        )
        if (sent) Text("If this email has an account, a reset link has been sent.", color = MaterialTheme.colorScheme.primary)
        Button(onClick = { if (email.isNotBlank()) sent = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Send reset link", modifier = Modifier.padding(vertical = 6.dp))
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to sign in") }
    }
}
