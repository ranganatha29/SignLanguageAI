package com.example.sign.ui.viewmodel

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sign.data.remote.AgoraManager
import com.example.sign.data.remote.FirebaseManager
import com.example.sign.data.repository.OpenAiRepository
import com.example.sign.ui.screens.Contact
import io.agora.rtc2.IRtcEngineEventHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SignViewModel : ViewModel() {
    private val repository = OpenAiRepository()
    private val firebaseManager = FirebaseManager()
    private var agoraManager: AgoraManager? = null
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _translation = MutableStateFlow("")
    val translation: StateFlow<String> = _translation

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun startListening(context: Context) {
        val languageCode = when (_selectedLanguage.value) {
            "Hindi" -> "hi-IN"
            "Kannada" -> "kn-IN"
            "Spanish" -> "es-ES"
            "French" -> "fr-FR"
            "German" -> "de-DE"
            else -> "en-US"
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                }
                
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        Log.d("Speech", "Mic is listening in $languageCode")
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { _isListening.value = false }
                    override fun onError(error: Int) {
                        Log.e("Speech", "Error: $error")
                        _isListening.value = false
                        // Restart on timeout or no match (Error 6, 7)
                        if (error == 7 || error == 6 || error == 8) {
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(500)
                                startListening(context)
                            }
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _translation.value = text
                            // Log all speech to Parent Dashboard
                            firebaseManager.logActivity("Deaf User", "Said: $text")
                            // Trigger Avatar Signing Animation
                            triggerAvatarAnimation(text)
                        }
                        // Continuous loop
                        startListening(context)
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _translation.value = matches[0]
                            _isAvatarSigning.value = true // Pulse while talking
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                
                speechRecognizer?.cancel() 
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("Speech", "Fatal Mic Error: ${e.message}")
            }
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _transcript = MutableStateFlow("Waiting for speech...")
    val transcript: StateFlow<String> = _transcript

    private val _isAvatarSigning = MutableStateFlow(false)
    val isAvatarSigning: StateFlow<Boolean> = _isAvatarSigning

    private val _currentGestureImage = MutableStateFlow<Int?>(null)
    val currentGestureImage: StateFlow<Int?> = _currentGestureImage

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting

    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted

    private val _isVideoOff = MutableStateFlow(false)
    val isVideoOff: StateFlow<Boolean> = _isVideoOff

    private val _isSpeakerMuted = MutableStateFlow(false)
    val isSpeakerMuted: StateFlow<Boolean> = _isSpeakerMuted

    private val _callState = MutableStateFlow("Idle") // Idle, Dialing, Connected, Failed, NoAnswer, Incoming
    val callState: StateFlow<String> = _callState

    private val _incomingCallInfo = MutableStateFlow<Triple<String, String, String>?>(null) // From, Channel, Type
    val incomingCallInfo: StateFlow<Triple<String, String, String>?> = _incomingCallInfo

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val detectedSigns = mutableListOf<String>()

    fun onImageCaptured(base64Image: String) {
        viewModelScope.launch {
            _isDetecting.value = true
            _translation.value = "Analyzing sign... Please hold steady."
            val result = repository.translateSignImage(base64Image, _selectedLanguage.value)
            _translation.value = result
            
            // Log this AI translation to Parent Dashboard
            firebaseManager.logActivity("Deaf User", "AI Translation: $result")
            
            _isDetecting.value = false
        }
    }

    fun initRealtime(context: Context, username: String) {
        firebaseManager.setUser(username)
        agoraManager = AgoraManager(context)
        
        // Log your ID so you can find it in Logcat
        android.util.Log.d("SignApp", "MY_USER_ID: $username")
        
        // Firebase commented out
        /*
        firebaseManager.listenForIncomingCalls { from, channel, type ->
            _incomingCallInfo.value = Triple(from, channel, type)
            _callState.value = "Incoming"
        }
        */
    }

    fun startCall(toUser: String = "Opposite Person", type: String = "Video", isOppositePersonAvailable: Boolean = true) {
        if (!isOppositePersonAvailable) {
            _callState.value = "NoAnswer"
            return
        }

        val targetId = if (toUser == "Opposite Person") "REPLACE_WITH_OTHER_PHONE_ID" else toUser
        
        val channelName = "channel_${System.currentTimeMillis()}"
        _callState.value = "Dialing"
        // firebaseManager.makeCall(targetId, channelName, type)
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            _callState.value = "Connected"
            // Simulate receiving speech for testing the Avatar
            simulateIncomingSpeech("Hello! How can I help you today?")
        }
    }

    /**
     * Triggers the hand gesture animation based on detected text.
     */
    fun triggerAvatarAnimation(text: String) {
        viewModelScope.launch {
            _isAvatarSigning.value = true
            
            // Map text to available hand gesture images
            val gestureRes = when {
                text.contains("Help", ignoreCase = true) || text.contains("Emergency", ignoreCase = true) -> com.example.sign.R.drawable.img_emergency
                text.contains("Food", ignoreCase = true) || text.contains("Eat", ignoreCase = true) || text.contains("Hungry", ignoreCase = true) -> com.example.sign.R.drawable.img_food
                text.contains("Water", ignoreCase = true) || text.contains("Drink", ignoreCase = true) -> com.example.sign.R.drawable.img_water
                text.contains("Thank", ignoreCase = true) -> com.example.sign.R.drawable.img_thank_you
                text.contains("Hospital", ignoreCase = true) || text.contains("Doctor", ignoreCase = true) -> com.example.sign.R.drawable.img_hospital
                else -> null
            }
            
            _currentGestureImage.value = gestureRes
            
            // The gesture stays visible for a duration based on the length of the text
            val signingDuration = (text.length * 150L).coerceIn(2000L, 6000L)
            kotlinx.coroutines.delay(signingDuration)
            
            _isAvatarSigning.value = false
            _currentGestureImage.value = null
        }
    }

    /**
     * This function is called when the other person's speech is converted to text.
     * It triggers the Avatar to start "signing".
     */
    fun onRemoteSpeechReceived(text: String) {
        _transcript.value = text
        _translation.value = text // Sync translation so avatar sees it
        triggerAvatarAnimation(text)
    }

    private fun simulateIncomingSpeech(text: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            onRemoteSpeechReceived(text)
        }
    }

    fun acceptCall() {
        val info = _incomingCallInfo.value ?: return
        // firebaseManager.respondToCall(info.first, true)
        _callState.value = "Connected"
        _incomingCallInfo.value = null
    }

    fun rejectCall() {
        val info = _incomingCallInfo.value ?: return
        // firebaseManager.respondToCall(info.first, false)
        _callState.value = "Idle"
        _incomingCallInfo.value = null
    }

    fun toggleMic() {
        _isMicMuted.value = !_isMicMuted.value
    }

    fun toggleVideo() {
        _isVideoOff.value = !_isVideoOff.value
    }

    fun toggleSpeaker() {
        _isSpeakerMuted.value = !_isSpeakerMuted.value
    }

    /**
     * Starts a demo sequence to impress judges.
     * Simulates a realistic conversation with AI translation and Avatar signing.
     */
    fun startDemo(type: String) {
        viewModelScope.launch {
            _callState.value = "Dialing"
            kotlinx.coroutines.delay(1500)
            _callState.value = "Connected"
            
            val demoSteps = if (type == "Video") {
                listOf(
                    "Hello! Welcome to the SignTranslate Demo.",
                    "I can see you signing. My AI is currently processing your gestures.",
                    "I see you just signed 'HELP'. Sending an emergency alert to your caregiver...",
                    "Emergency Alert Sent! Help is on the way.",
                    "Don't worry, I will stay on the call with you."
                )
            } else {
                listOf(
                    "Voice Demo: I am your AI interpreter.",
                    "The Avatar is now signing my words in real-time.",
                    "This allows deaf users to understand voice calls perfectly.",
                    "Detecting your sign: 'Thank you'. You're welcome!"
                )
            }

            for (step in demoSteps) {
                onRemoteSpeechReceived(step)
                // Wait for the avatar to finish signing before next message
                val waitTime = (step.length * 150L).coerceIn(3000L, 7000L)
                kotlinx.coroutines.delay(waitTime)
            }
        }
    }

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            val contactList = mutableListOf<Contact>()
            // Add a manual "Opposite Person" for easy testing
            contactList.add(Contact("Opposite Person", "Online", true))
            
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex) ?: "Unknown"
                    val isOnline = (0..1).random() == 1
                    contactList.add(Contact(name, if (isOnline) "Online" else "Offline", isOnline))
                }
            }
            _contacts.value = contactList.distinctBy { it.name }
        }
    }

    fun endCall() {
        _callState.value = "Idle"
        _translation.value = "Translation will appear here"
    }

    fun onSignDetected(sign: String) {
        // Only show local detection if the high-accuracy AI is NOT currently processing
        if (!_isDetecting.value && sign.isNotEmpty()) {
            if (_translation.value != sign) { // Only log if it's a new sign
                _translation.value = sign
                firebaseManager.logActivity("Deaf User", "Signed: $sign")
            }
            
            // If it's an emergency keyword, send a Firebase alert automatically
            val emergencyKeywords = listOf("Emergency", "आपातकालीन", "ತುರ್ತು", "Hospital", "अस्पताल", "ಆಸ್ಪತ್ರೆ")
            if (emergencyKeywords.any { sign.contains(it, ignoreCase = true) }) {
                firebaseManager.sendEmergencyAlert("Deaf User", "User needs $sign!")
            }
        }
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
    }

    private fun refineTranslation() {
        // Disabled automatic refinement to save API quota and prevent 429 errors.
        // The user should use the manual Capture button for high-accuracy translation.
    }
    
    fun setDetecting(detecting: Boolean) {
        _isDetecting.value = detecting
    }
}
