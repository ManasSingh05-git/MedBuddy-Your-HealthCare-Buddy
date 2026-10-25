package com.manas.medbuddy.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.manas.medbuddy.ui.components.GlassCard
import com.manas.medbuddy.ui.components.GradientPillButton
import com.manas.medbuddy.ui.theme.CyanPrimary
import com.manas.medbuddy.ui.theme.DarkBackground
import com.manas.medbuddy.ui.theme.DarkSurface
import com.manas.medbuddy.ui.theme.EmeraldPrimary
import com.manas.medbuddy.ui.theme.TextPrimaryDark as TextPrimary
import com.manas.medbuddy.ui.theme.TextSecondaryDark as TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_personal_info", Context.MODE_PRIVATE) }

    val userEmail = try {
        FirebaseAuth.getInstance().currentUser?.email ?: prefs.getString("email", "manas.singh@nmims.edu")!!
    } catch (_: Exception) {
        prefs.getString("email", "manas.singh@nmims.edu")!!
    }

    var fullName by remember { mutableStateOf(prefs.getString("fullName", "Manas Singh")!!) }
    var phone by remember { mutableStateOf(prefs.getString("phone", "+91 98765 43210")!!) }
    var bloodGroup by remember { mutableStateOf(prefs.getString("bloodGroup", "O+")!!) }
    var age by remember { mutableStateOf(prefs.getString("age", "22")!!) }
    var gender by remember { mutableStateOf(prefs.getString("gender", "Male")!!) }
    var emergencyNotes by remember { mutableStateOf(prefs.getString("emergencyNotes", "No severe drug allergies.")!!) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Personal Information", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("User Profile Details", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email Address
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { },
                        enabled = false,
                        label = { Text("Email Address (Registered)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = TextSecondary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Blood Group
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        )

                        // Age
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Gender
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Emergency Medical Notes
                    OutlinedTextField(
                        value = emergencyNotes,
                        onValueChange = { emergencyNotes = it },
                        label = { Text("Medical Notes / Allergies", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, unfocusedBorderColor = TextSecondary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            GradientPillButton(
                text = if (isSaving) "Saving..." else "Save Personal Information",
                enabled = !isSaving,
                onClick = {
                    isSaving = true
                    prefs.edit()
                        .putString("fullName", fullName.trim())
                        .putString("phone", phone.trim())
                        .putString("bloodGroup", bloodGroup.trim())
                        .putString("age", age.trim())
                        .putString("gender", gender.trim())
                        .putString("emergencyNotes", emergencyNotes.trim())
                        .apply()

                    isSaving = false
                    Toast.makeText(context, "Personal Information Saved Successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            )
        }
    }
}
