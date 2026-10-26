package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.model.Medicine
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
fun MedicineScreen(
    onBack: () -> Unit,
    onNavigateToAddMedicine: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Completed"
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val userId = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (_: Exception) {
        null
    }

    DisposableEffect(userId) {
        if (userId == null) {
            loading = false
            onDispose { }
        } else {
            try {
                val reference = FirebaseDatabase.getInstance().reference.child("medicines").child(userId)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        medicines = snapshot.children.mapNotNull { child: DataSnapshot ->
                            child.getValue(Medicine::class.java)?.copy(id = child.key ?: "")
                        }
                        loading = false
                    }
                    override fun onCancelled(error: DatabaseError) { loading = false }
                }
                reference.addValueEventListener(listener)
                onDispose { reference.removeEventListener(listener) }
            } catch (_: Exception) {
                loading = false
                onDispose { }
            }
        }
    }

    // Default sample data if firebase empty
    val displayMedicines = if (medicines.isEmpty() && !loading) {
        listOf(
            Medicine(id = "1", name = "Paracetamol", dosage = "500 mg", time = "Daily • 8:00 AM", instructions = "1 tablet - Daily", isActive = true, isTaken = true),
            Medicine(id = "2", name = "Vitamin D", dosage = "1000 IU", time = "Daily • 2:00 PM", instructions = "1 tablet - Daily", isActive = true, isTaken = false),
            Medicine(id = "3", name = "Omega 3", dosage = "500 mg", time = "Daily • 8:00 PM", instructions = "1 capsule - Daily", isActive = true, isTaken = false),
            Medicine(id = "4", name = "Calcium", dosage = "500 mg", time = "Daily • 8:00 PM", instructions = "1 tablet - Daily", isActive = true, isTaken = true),
            Medicine(id = "5", name = "Iron Supplement", dosage = "100 mg", time = "Daily • 10:00 AM", instructions = "1 tablet - Daily", isActive = true, isTaken = false)
        )
    } else {
        medicines
    }

    val filteredList = displayMedicines.filter { med ->
        val matchesSearch = med.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Active" -> med.isActive
            "Completed" -> med.isTaken
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Medicines", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search medicine...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = DarkSurface,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Filter Chips (All, Active, Completed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChipItem(label = "All (${displayMedicines.size})", isSelected = selectedFilter == "All") {
                    selectedFilter = "All"
                }
                FilterChipItem(label = "Active (${displayMedicines.count { it.isActive }})", isSelected = selectedFilter == "Active") {
                    selectedFilter = "Active"
                }
                FilterChipItem(label = "Completed", isSelected = selectedFilter == "Completed") {
                    selectedFilter = "Completed"
                }
            }

            // Medicines List
            if (loading) {
                CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (filteredList.isEmpty()) {
                Text("No medicines found.", color = TextSecondary, modifier = Modifier.padding(vertical = 20.dp))
            } else {
                filteredList.forEach { med ->
                    MedicineDetailCard(medicine = med)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // + Add Medicine Button
            GradientPillButton(
                text = "+ Add Medicine",
                onClick = onNavigateToAddMedicine
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) EmeraldPrimary else DarkSurface,
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.Black else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MedicineDetailCard(
    medicine: Medicine
) {
    var isActiveState by remember { mutableStateOf(medicine.isActive) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(EmeraldPrimary.copy(alpha = 0.2f), CyanPrimary.copy(alpha = 0.2f)))),
                contentAlignment = Alignment.Center
            ) {
                Text("💊", fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(medicine.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("${medicine.dosage} • ${medicine.instructions}", color = TextSecondary, fontSize = 13.sp)
                Text(medicine.time, color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Switch(
                checked = isActiveState,
                onCheckedChange = { isActiveState = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EmeraldPrimary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}
