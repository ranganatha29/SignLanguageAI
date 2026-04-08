package com.example.sign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sign.ui.theme.*
import com.example.sign.ui.viewmodel.SignViewModel
import com.example.sign.ui.components.CameraPreview
import com.example.sign.ui.components.QuickPhraseChip
import com.example.sign.ui.components.CallActionButton

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable

@Composable
fun VoiceCallScreen(
    isAvailable: Boolean = true,
    onBackClick: () -> Unit,
    viewModel: SignViewModel = viewModel()
) {
    val transcript by viewModel.transcript.collectAsState()
    val translation by viewModel.translation.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isSpeakerMuted by viewModel.isSpeakerMuted.collectAsState()
    val isAvatarSigning by viewModel.isAvatarSigning.collectAsState()
    val currentGestureImage by viewModel.currentGestureImage.collectAsState()
    
    var showLanguageMenu by remember { mutableStateOf(false) }
    val languages = listOf("English", "Hindi", "Kannada", "Spanish", "French", "German")

    // Start call automatically when screen opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startDemo("Voice")
    }

    // Animation for the avatar's movement based on "speech"
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAvatarSigning) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Voice Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = when (callState) {
                            "Connected" -> Color.Green
                            "Dialing" -> Color.Yellow
                            else -> Color.Red
                        }
                        Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(callState, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                
                IconButton(
                    onClick = { showLanguageMenu = true }
                ) {
                    Icon(Icons.Default.Translate, contentDescription = "Language", tint = Color.White)
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang, color = Color.White) },
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
                
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Visual Feedback Area (Sign Avatar & User Input)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (callState == "Dialing") {
                    // DIALING STATE UI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Calling...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Waiting for response...", color = TextSecondary, fontSize = 14.sp)
                    }
                } else if (callState == "NoAnswer") {
                    // NO ANSWER UI
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = Color.Red.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Red)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("No Answer", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("The person is unavailable.", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text("Try Again Later")
                        }
                    }
                } else {
                    // Main View: Animated Sign Avatar / Hand Gesture
                    Surface(
                        modifier = Modifier
                            .size(280.dp * avatarScale),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF00C2FF), Color(0xFFFF4081))))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (currentGestureImage != null) {
                                Image(
                                    painter = painterResource(id = currentGestureImage!!),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(48.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Dynamic Avatar based on Transcript content
                                    val avatarIcon = when {
                                        transcript.contains("Hello", ignoreCase = true) -> Icons.Default.WavingHand
                                        transcript.contains("Help", ignoreCase = true) -> Icons.Default.Info
                                        transcript.contains("Doctor", ignoreCase = true) -> Icons.Default.MedicalServices
                                        else -> Icons.Default.Face
                                    }
                                    
                                    Icon(
                                        avatarIcon,
                                        contentDescription = "Sign Avatar",
                                        modifier = Modifier.size(120.dp),
                                        tint = if (isAvatarSigning) Color(0xFF00C2FF) else Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        if (isAvatarSigning) "Avatar Signing..." else "Sign Avatar", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Small View: Camera Preview (Deaf user signing input)
                if (callState == "Connected") {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(width = 90.dp, height = 130.dp)
                            .offset(y = (-20).dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        CameraPreview(
                            onSignDetected = { sign ->
                                viewModel.onSignDetected(sign)
                            }
                        )
                    }
                }
            }
            
            // Translation & Transcript Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                // Outgoing: My Signs to their Speech
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF00C2FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val flag = when(selectedLanguage) {
                            "Hindi" -> "🇮🇳 "
                            "Kannada" -> "🇮🇳 "
                            "Spanish" -> "🇪🇸 "
                            "French" -> "🇫🇷 "
                            "German" -> "🇩🇪 "
                            else -> "🇺🇸 "
                        }
                        Text("Signs to $flag$selectedLanguage:", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Text(
                    translation,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                
                // Incoming: Their Speech to my Transcript
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Their Speech to Text:", color = TextSecondary, fontSize = 12.sp)
                }
                Text(
                    transcript,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick Phrases for Voice Call
                Text("Quick Actions", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickPhrases = listOf("Emergency", "Doctor", "Help", "Yes", "No", "Thank you")
                    items(quickPhrases) { phrase ->
                        QuickPhraseChip(phrase) { viewModel.onSignDetected(phrase) }
                    }
                }
                
                // Bottom Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionButton(
                        icon = if (isSpeakerMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        containerColor = if (isSpeakerMuted) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        onClick = { viewModel.toggleSpeaker() }
                    )
                    CallActionButton(
                        icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        containerColor = if (isMicMuted) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        onClick = { viewModel.toggleMic() }
                    )
                    CallActionButton(
                        icon = Icons.Default.CallEnd, 
                        containerColor = Color(0xFFFF5252),
                        iconColor = Color.White,
                        onClick = onBackClick
                    )
                }
            }
        }
    }
}
