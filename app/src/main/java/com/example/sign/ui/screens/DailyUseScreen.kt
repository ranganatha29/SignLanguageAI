package com.example.sign.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sign.ui.theme.*
import com.example.sign.ui.viewmodel.SignViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sign.ui.components.CameraPreview
import com.example.sign.ui.components.QuickPhraseChip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.sign.R
import androidx.compose.foundation.Image

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.animation.core.*
import com.airbnb.lottie.compose.*

@Composable
fun DailyUseScreen(
    onBackClick: () -> Unit,
    viewModel: SignViewModel = viewModel()
) {
    val context = LocalContext.current
    val translation by viewModel.translation.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isAvatarSigning by viewModel.isAvatarSigning.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    var isVoiceMode by remember { mutableStateOf(false) }

    // 3D Floating Animation for the entire Avatar Container
    val infiniteTransition = rememberInfiniteTransition(label = "3d_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "y_float"
    )
    val animRotationZ by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "z_rotate"
    )

    // Automatically handle Mic when Voice Mode is toggled
    LaunchedEffect(isVoiceMode) {
        if (isVoiceMode) {
            viewModel.startListening(context)
        } else {
            viewModel.stopListening()
        }
    }
    
    val quickPhrases = when(selectedLanguage) {
        "Hindi" -> listOf("आपातकालीन", "अस्पताल", "भोजन", "पानी", "ಮದದ", "ಶೌಚಾಲಯ", "पुलिस", "धन्यवाद")
        "Kannada" -> listOf("ತುರ್ತು", "ಆಸ್ಪತ್ರೆ", "ಆಹಾರ", "ನೀರು", "ಸಹಾಯ", "ಶೌಚಾಲಯ", "ಪೊಲೀಸ್", "ಧನ್ಯವಾದಗಳು")
        else -> listOf("Emergency", "Hospital", "Food", "Water", "Help", "Toilet", "Police", "Thank you")
    }

    var showLanguageMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(GradientStart, GradientEnd)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
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
                    Text(if (isVoiceMode) "Voice to Sign" else "Sign to Text", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Daily Use Mode", color = TextSecondary, fontSize = 12.sp)
                }
                
                IconButton(
                    onClick = { showLanguageMenu = true },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size), contentDescription = "Translate", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Display Area (Camera or Avatar)
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(32.dp),
                color = Color.Black
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!isVoiceMode) {
                        // Camera Mode
                        var triggerCapture by remember { mutableStateOf(false) }
                        val isDetecting by viewModel.isDetecting.collectAsState()

                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onSignDetected = { sign -> viewModel.onSignDetected(sign) },
                            onImageCaptured = { base64 -> viewModel.onImageCaptured(base64) },
                            triggerCapture = triggerCapture,
                            onCaptureHandled = { triggerCapture = false }
                        )
                        
                        if (isDetecting) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            FloatingActionButton(
                                onClick = { triggerCapture = true },
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                                containerColor = Color.White.copy(alpha = 0.5f)
                            ) {
                                Icon(painter = painterResource(id = android.R.drawable.ic_menu_camera), contentDescription = "Capture")
                            }
                        }
                    } else {
                        // 3D Avatar Mode (Voice to Sign)
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 3D Animated Avatar Container
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(350.dp)
                                    .graphicsLayer {
                                        translationY = floatY
                                        rotationZ = animRotationZ
                                        cameraDistance = 12f * density
                                    }
                            ) {
                                // Background Glow Effect
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(Color.Cyan.copy(alpha = 0.2f), Color.Transparent)
                                            ),
                                            shape = CircleShape
                                        )
                                )

                                val avatarRes = when {
                                    translation.contains("Water", ignoreCase = true) || translation.contains("पानी", ignoreCase = true) -> R.drawable.img_water
                                    translation.contains("Food", ignoreCase = true) || translation.contains("भोजन", ignoreCase = true) -> R.drawable.img_food
                                    translation.contains("Help", ignoreCase = true) || translation.contains("मदद", ignoreCase = true) -> R.drawable.img_help
                                    translation.contains("Hospital", ignoreCase = true) || translation.contains("अस्पताल", ignoreCase = true) -> R.drawable.img_hospital
                                    translation.contains("Emergency", ignoreCase = true) || translation.contains("आपातकालीन", ignoreCase = true) -> R.drawable.img_emergency
                                    else -> R.drawable.img_thank_you
                                }

                                val signingScale by animateFloatAsState(
                                    targetValue = if (isAvatarSigning) 1.2f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "signing_scale"
                                )

                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(280.dp)
                                        .graphicsLayer {
                                            scaleX = signingScale
                                            scaleY = signingScale
                                            rotationY = if (isAvatarSigning) 15f else 0f // 3D tilt
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Visual Audio Waves (Simulated 3D look)
                            Row(
                                modifier = Modifier.height(40.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    val barHeight by infiniteTransition.animateFloat(
                                        initialValue = 10f,
                                        targetValue = if (isListening) 40f else 10f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(300 + (index * 100), easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ), label = "wave_$index"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(barHeight.dp)
                                            .background(Color.Cyan, RoundedCornerShape(10.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Translation Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardBackground
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (isVoiceMode) "Speech to Sign Output" else "Sign Translation",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        translation.ifEmpty { "Waiting for input..." },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickPhrases) { phrase ->
                            QuickPhraseChip(phrase) { viewModel.onSignDetected(phrase) }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isVoiceMode = !isVoiceMode },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVoiceMode) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(if (isVoiceMode) Icons.Default.Mic else Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isVoiceMode) "Voice Mode ON" else "Switch to Voice")
                }
                
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    IconButton(onClick = { viewModel.onSignDetected("") }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                }
            }
        }
    }
}
