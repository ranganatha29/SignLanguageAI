package com.example.sign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sign.ui.theme.*
import com.example.sign.data.remote.FirebaseManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(onBackClick: () -> Unit) {
    val database = FirebaseDatabase.getInstance().reference
    var alerts by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var latestAlert by remember { mutableStateOf<Map<String, String>?>(null) }

    // Listen for History & Alerts
    DisposableEffect(Unit) {
        android.util.Log.d("ParentDashboard", "Starting Firebase Listener...")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Map<String, String>>()
                android.util.Log.d("ParentDashboard", "Data changed! Items count: ${snapshot.childrenCount}")
                
                for (child in snapshot.children) {
                    val alert = mapOf(
                        "message" to (child.child("message").getValue(String::class.java) ?: "No Message"),
                        "timestamp" to (child.child("timestamp").getValue(String::class.java) ?: "No Time"),
                        "from" to (child.child("from").getValue(String::class.java) ?: "Unknown")
                    )
                    list.add(alert)
                }
                alerts = list.reversed()
                if (list.isNotEmpty()) latestAlert = list.last()
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("ParentDashboard", "Firebase Error: ${error.message}")
            }
        }
        database.child("alerts").addValueEventListener(listener)
        onDispose { database.child("alerts").removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GradientStart)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(brush = Brush.verticalGradient(colors = listOf(GradientStart, GradientEnd)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Latest Alert Card (LOUD)
                latestAlert?.let { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (alert["message"]!!.contains("Emergency", true)) Color(0xFFFF5252) else CardBackground
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("LATEST ALERT", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text(alert["message"] ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(alert["timestamp"] ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text("Activity History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

                // History List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(alerts) { alert ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(alert["message"] ?: "", color = Color.White, fontWeight = FontWeight.Medium)
                                    Text(alert["timestamp"] ?: "", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
