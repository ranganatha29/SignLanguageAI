package com.example.sign

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sign.ui.screens.*
import com.example.sign.ui.theme.SignTheme
import com.example.sign.ui.viewmodel.SignViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignTheme {
                RequestPermissions()
                SignApp()
            }
        }
    }
}

@Composable
fun RequestPermissions() {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS
    )
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val needsPermission = permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needsPermission) {
            launcher.launch(permissions)
        }
    }
}

@Composable
fun SignApp(viewModel: SignViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val callState by viewModel.callState.collectAsState()
    val incomingInfo by viewModel.incomingCallInfo.collectAsState()
    
    LaunchedEffect(Unit) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver, 
            Settings.Secure.ANDROID_ID
        ).take(5)
        viewModel.initRealtime(context, "User_$deviceId")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onDailyUseClick = { navController.navigate("daily_use") },
                    onVideoCallClick = { navController.navigate("contacts/Video") },
                    onVoiceCallClick = { navController.navigate("contacts/Voice") },
                    onParentDashboardClick = { navController.navigate("parent_dashboard") }
                )
            }
            composable("parent_dashboard") {
                ParentDashboardScreen(onBackClick = { navController.popBackStack() })
            }
            composable("contacts/{callType}") { backStackEntry ->
                val callType = backStackEntry.arguments?.getString("callType") ?: "Video"
                ContactsScreen(
                    callType = callType,
                    onBackClick = { navController.popBackStack() },
                    onContactClick = { contact ->
                        viewModel.startCall(contact.name, callType)
                        if (callType == "Video") {
                            navController.navigate("video_call/${contact.isOnline}")
                        } else {
                            navController.navigate("voice_call/${contact.isOnline}")
                        }
                    }
                )
            }
            composable("daily_use") {
                DailyUseScreen(onBackClick = { navController.popBackStack() })
            }
            composable("video_call/{isAvailable}") { backStackEntry ->
                val isAvailable = backStackEntry.arguments?.getString("isAvailable")?.toBoolean() ?: true
                VideoCallScreen(
                    isAvailable = isAvailable,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("voice_call/{isAvailable}") { backStackEntry ->
                val isAvailable = backStackEntry.arguments?.getString("isAvailable")?.toBoolean() ?: true
                VoiceCallScreen(
                    isAvailable = isAvailable,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        if (callState == "Incoming" && incomingInfo != null) {
            IncomingCallScreen(
                callerName = incomingInfo!!.first,
                callType = incomingInfo!!.third,
                onAccept = {
                    viewModel.acceptCall()
                    if (incomingInfo!!.third == "Video") {
                        navController.navigate("video_call/true")
                    } else {
                        navController.navigate("voice_call/true")
                    }
                },
                onReject = { viewModel.rejectCall() }
            )
        }
    }
}

@Composable
fun IncomingCallScreen(
    callerName: String,
    callType: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Icon(
                    Icons.Default.Call, 
                    contentDescription = null, 
                    modifier = Modifier.padding(24.dp).size(60.dp), 
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Incoming $callType Call", color = Color.White, fontSize = 18.sp)
            Text(callerName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(72.dp).background(Color.Red, CircleShape)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "Reject", tint = Color.White)
                }
                
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier.size(72.dp).background(Color.Green, CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.White)
                }
            }
        }
    }
}
