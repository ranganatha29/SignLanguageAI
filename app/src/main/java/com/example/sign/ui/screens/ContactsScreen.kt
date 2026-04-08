package com.example.sign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sign.ui.theme.*
import com.example.sign.ui.viewmodel.SignViewModel

data class Contact(val name: String, val status: String, val isOnline: Boolean)

@Composable
fun ContactsScreen(
    callType: String, // "Video" or "Voice"
    onBackClick: () -> Unit,
    onContactClick: (Contact) -> Unit,
    viewModel: SignViewModel = viewModel()
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadContacts(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Select Contact ($callType)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("Search contacts...", color = TextSecondary, fontSize = 16.sp)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Contacts", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredContacts) { contact ->
                    ContactItem(contact, callType) { onContactClick(contact) }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, callType: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        contact.name.take(1),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (contact.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Green, CircleShape)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(contact.status, color = if (contact.isOnline) Color.Green else TextSecondary, fontSize = 12.sp)
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.background(
                    if (callType == "Video") VideoCallIcon.copy(alpha = 0.2f) else VoiceCallIcon.copy(alpha = 0.2f),
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = if (callType == "Video") Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = if (callType == "Video") VideoCallIcon else VoiceCallIcon
                )
            }
        }
    }
}
