package com.example.sign.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onDailyUseClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onParentDashboardClick: () -> Unit
) {
    // 3D floating animation for the background elements
    val infiniteTransition = rememberInfiniteTransition(label = "home_float")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "background_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
    ) {
        // Decorative background 3D circles
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (50).dp)
                .size(250.dp)
                .graphicsLayer { translationY = floatAnim }
                .background(Color.Cyan.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = (-100).dp)
                .size(300.dp)
                .graphicsLayer { translationY = -floatAnim }
                .background(Color.Magenta.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Logo / Icon with 3D Pop
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        rotationY = floatAnim * 0.5f
                        cameraDistance = 12f * density
                    },
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 20.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.background(
                    Brush.linearGradient(listOf(Color(0xFF00c6ff), Color(0xFF0072ff)))
                )) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SignTranslate",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = "Empowering Silent Voices",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FeatureCard(
                    title = "Daily Use Mode",
                    description = "Real-time sign language translation",
                    iconColor = Color(0xFF43cea2),
                    endColor = Color(0xFF185a9d),
                    onClick = onDailyUseClick
                )
                
                FeatureCard(
                    title = "Video Call AI",
                    description = "Connect with live AI translation",
                    iconColor = Color(0xFF8e2de2),
                    endColor = Color(0xFF4a00e0),
                    onClick = onVideoCallClick
                )
                
                FeatureCard(
                    title = "Caregiver Mode",
                    description = "Dashboard for family members",
                    iconColor = Color(0xFFff9966),
                    endColor = Color(0xFFff5e62),
                    onClick = onParentDashboardClick
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            // Quick Start Voice Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable { onVoiceCallClick() },
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(painter = painterResource(id = android.R.drawable.ic_btn_speak_now), contentDescription = null, tint = Color.Cyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Start Voice Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    iconColor: Color,
    endColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "card_scale")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(listOf(iconColor.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(2.dp) // Border effect
                .background(Color(0xFF1A1A1A).copy(alpha = 0.8f), RoundedCornerShape(23.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(
                            brush = Brush.linearGradient(listOf(iconColor, endColor)),
                            shape = RoundedCornerShape(16.dp)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        lineHeight = 16.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}
