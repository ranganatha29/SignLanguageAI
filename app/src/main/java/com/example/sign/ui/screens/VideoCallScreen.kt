package com.example.sign.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.sign.ui.theme.*

import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sign.ui.components.CameraPreview
import com.example.sign.ui.components.QuickPhraseChip
import com.example.sign.ui.components.CallActionButton
import com.example.sign.ui.viewmodel.SignViewModel

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.animation.core.*

@Composable
fun VideoCallScreen(
    isAvailable: Boolean = true,
    onBackClick: () -> Unit,
    viewModel: SignViewModel = viewModel()
) {
    val translation by viewModel.translation.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val isAvatarSigning by viewModel.isAvatarSigning.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isVideoOff by viewModel.isVideoOff.collectAsState()
    val isSpeakerMuted by viewModel.isSpeakerMuted.collectAsState()
    val currentGestureImage by viewModel.currentGestureImage.collectAsState()
    
    // Animation for avatar overlay
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val avatarYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    val quickPhrases = when(selectedLanguage) {
        "Hindi" -> listOf("मदद", "आपातकालीन", "डॉक्टर", "भूख", "हाँ", "नहीं", "दोहराएं")
        "Kannada" -> listOf("ಸಹಾಯ", "ತುರ್ತು", "ಡಾಕ್ಟರ್", "ಹಸಿವು", "ಹೌದು", "ಇಲ್ಲ", "ಮತ್ತೊಮ್ಮೆ")
        "Spanish" -> listOf("Ayuda", "Emergencia", "Doctor", "Hambre", "Sí", "No", "Repetir")
        "French" -> listOf("Aide", "Urgence", "Docteur", "Faim", "Oui", "Non", "Répéter")
        "German" -> listOf("Hilfe", "Notfall", "Arzt", "Hunger", "Ja", "Nein", "Wiederholen")
        else -> listOf("Help", "Emergency", "Doctor", "Hungry", "Yes", "No", "Repeat")
    }
    var showLanguageMenu by remember { mutableStateOf(false) }
    val languages = listOf("English", "Hindi", "Kannada", "Spanish", "French", "German")

    // Start call automatically when screen opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startDemo("Video")
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
        Column(modifier = Modifier.fillMaxSize()) {
            // ... (Top Bar remains same)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = when (callState) {
                            "Connected" -> Color.Green
                            "Dialing" -> Color.Yellow
                            else -> Color.Red
                        }
                        Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(callState, color = Color.White, fontSize = 14.sp)
                    }
                }

                Row {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            }

            // Main Video Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
                        Text("Connecting to interpretation server", color = TextSecondary, fontSize = 14.sp)
                    }
                } else if (callState == "NoAnswer") {
                    // NO ANSWER / UNAVAILABLE UI
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
                        Text("User Unavailable", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("The recipient is not answering the call.", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text("Go Back")
                        }
                    }
                } else {
                    // CONNECTED STATE UI: Remote Participant's Video (Placeholder) + Avatar Overlay
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Remote Video View (Simulated with a placeholder or actual camera)
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(32.dp),
                            color = Color.DarkGray
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // In a real app, this would be the remote video stream
                                Icon(
                                    Icons.Default.Person, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(200.dp),
                                    tint = Color.White.copy(alpha = 0.2f)
                                )
                                Text("Remote Video Stream", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
                            }
                        }

                        // Transparent Hand Gesture Overlay (Signing the incoming speech)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .size(140.dp)
                                .offset(y = avatarYOffset.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isAvatarSigning) Color.Cyan.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isAvatarSigning) 3.dp else 1.dp, 
                                color = if (isAvatarSigning) Color.Cyan else Color.White.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (currentGestureImage != null) {
                                    Image(
                                        painter = painterResource(id = currentGestureImage!!),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Face,
                                            contentDescription = null,
                                            tint = if (isAvatarSigning) Color.Cyan else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(if (isAvatarSigning) 70.dp else 60.dp)
                                        )
                                        Text(
                                            if (isAvatarSigning) "Signing..." else "AI Avatar", 
                                            color = Color.White, 
                                            fontSize = 10.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Incoming Speech Text Overlay (On top of remote video)
                        if (transcript != "Listening for speech...") {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = transcript,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Detection Status Overlay
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(translation, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color.Cyan, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting signs...", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // Local User Video (Small PIP)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(width = 100.dp, height = 150.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black
                ) {
                    CameraPreview(
                        onSignDetected = { sign ->
                            viewModel.onSignDetected(sign)
                        },
                        onImageCaptured = { base64 ->
                            viewModel.onImageCaptured(base64)
                        }
                    )
                }
            }

            // Bottom Controls & Translation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translate to:", color = TextSecondary, fontSize = 14.sp)
                    }
                    
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            onClick = { showLanguageMenu = true }
                        ) {
                            val flag = when(selectedLanguage) {
                                "Hindi" -> "🇮🇳 "
                                "Kannada" -> "🇮🇳 "
                                "Spanish" -> "🇪🇸 "
                                "French" -> "🇫🇷 "
                                "German" -> "🇩🇪 "
                                else -> "🇺🇸 "
                            }
                            Text(
                                "$flag $selectedLanguage", 
                                color = Color.White, 
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp
                            )
                        }
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Phrases
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPhrases) { phrase ->
                        QuickPhraseChip(phrase) { viewModel.onSignDetected(phrase) }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallActionButton(
                        icon = if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        containerColor = if (isVideoOff) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        onClick = { viewModel.toggleVideo() }
                    )
                    CallActionButton(
                        icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        containerColor = if (isMicMuted) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        onClick = { viewModel.toggleMic() }
                    )
                    CallActionButton(
                        icon = Icons.Default.CallEnd, 
                        containerColor = Color.Red, 
                        iconColor = Color.White,
                        onClick = onBackClick
                    )
                    CallActionButton(
                        icon = if (isSpeakerMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        containerColor = if (isSpeakerMuted) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                        onClick = { viewModel.toggleSpeaker() }
                    )
                    CallActionButton(icon = Icons.Default.FlipCameraAndroid, onClick = {})
                }
            }
        }
    }
}
