package com.manas.medbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.manas.medbuddy.model.Medicine

@Composable
fun HomeScreen(onAddMedicine: () -> Unit, onLogout: () -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var medicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(userId) {
        if (userId == null) {
            loading = false
            onDispose { }
        } else {
            val reference = FirebaseDatabase.getInstance().reference.child("medicines").child(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    medicines = snapshot.children.mapNotNull { child -> child.getValue(Medicine::class.java)?.copy(id = child.key.orEmpty()) }
                    loading = false
                }
                override fun onCancelled(error: DatabaseError) { loading = false }
            }
            reference.addValueEventListener(listener)
            onDispose { reference.removeEventListener(listener) }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Good morning", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Your health dashboard", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { FirebaseAuth.getInstance().signOut(); onLogout() }) { Text("Log out") }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your next reminder", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(if (medicines.isEmpty()) "No medicine added" else medicines.first().name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(if (medicines.isEmpty()) "Add your first medicine below" else "${medicines.first().dosage} at ${medicines.first().time}", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Text("Quick actions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionCard("+", "Add medicine", Modifier.weight(1f), onAddMedicine)
            Spacer(Modifier.width(12.dp))
            ActionCard("R", "My reports", Modifier.weight(1f), {})
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionCard("A", "Appointments", Modifier.weight(1f), {})
            Spacer(Modifier.width(12.dp))
            ActionCard("H", "Health log", Modifier.weight(1f), {})
        }
        Text("My medicines", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        when {
            loading -> Text("Loading medicines...")
            medicines.isEmpty() -> Text("No medicines yet. Tap Add medicine to begin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> medicines.forEach { MedicineCard(it) }
        }
    }
}

@Composable
private fun ActionCard(symbol: String, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(symbol, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            Text(title, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MedicineCard(medicine: Medicine) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(medicine.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text("${medicine.dosage} - ${medicine.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
