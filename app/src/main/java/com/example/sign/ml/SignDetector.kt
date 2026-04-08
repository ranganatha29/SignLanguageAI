package com.example.sign.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class SignDetector(
    private val context: Context,
    private val listener: SignDetectionListener
) {
    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task") // You need to add this to assets

        try {
            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinHandDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setNumHands(2)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, image ->
                    processResult(result)
                }
            
            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap, timestamp: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker?.detectAsync(mpImage, timestamp)
    }

    private var lastResultTime = 0L
    private val detectionInterval = 2000L // 2 seconds between detections to avoid flooding

    private fun processResult(result: HandLandmarkerResult) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastResultTime < 1000) return // 1 second interval for faster feedback

        if (result.landmarks().isNotEmpty()) {
            val landmarks = result.landmarks()[0]
            
            // Core landmarks
            val wrist = landmarks[0]
            val thumbTip = landmarks[4]
            val indexTip = landmarks[8]
            val middleTip = landmarks[12]
            val ringTip = landmarks[16]
            val pinkyTip = landmarks[20]
            
            val thumbIp = landmarks[3]
            val indexPip = landmarks[6]
            val middlePip = landmarks[10]
            val ringPip = landmarks[14]
            val pinkyPip = landmarks[18]

            // Finger states (Up/Down)
            val isIndexUp = indexTip.y() < indexPip.y()
            val isMiddleUp = middleTip.y() < middlePip.y()
            val isRingUp = ringTip.y() < ringPip.y()
            val isPinkyUp = pinkyTip.y() < pinkyPip.y()
            
            // Relative distances for complex signs
            val thumbToIndex = Math.hypot((thumbTip.x() - indexTip.x()).toDouble(), (thumbTip.y() - indexTip.y()).toDouble())
            val thumbToWrist = Math.hypot((thumbTip.x() - wrist.x()).toDouble(), (thumbTip.y() - wrist.y()).toDouble())
            val pinkyToWrist = Math.hypot((pinkyTip.x() - wrist.x()).toDouble(), (pinkyTip.y() - wrist.y()).toDouble())

            val detectedSign = when {
                // 1. Hello: All fingers extended and spread
                isIndexUp && isMiddleUp && isRingUp && isPinkyUp && thumbToWrist > 0.2 -> "Hello"

                // 2. Thank You: Hand moves from chin (Flat hand, slightly tilted)
                isIndexUp && isMiddleUp && isRingUp && isPinkyUp && !isPinkyUp -> "Thank You"

                // 3. Help: Closed fist with thumb up
                !isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp && thumbTip.y() < indexPip.y() -> "Help"

                // 4. Water: Index, Middle, Ring up (W shape)
                isIndexUp && isMiddleUp && isRingUp && !isPinkyUp -> "Water"

                // 5. Food: All finger tips touching thumb (O shape)
                thumbToIndex < 0.05 && !isMiddleUp && !isRingUp && !isPinkyUp -> "Food"

                // 6. Emergency: Thumb and Pinky out (Shaka)
                thumbToWrist > 0.2 && pinkyToWrist > 0.2 && !isIndexUp && !isMiddleUp && !isRingUp -> "Emergency"

                // 7. Yes: Closed fist nodding (Hand tilted down)
                !isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp && wrist.y() < indexPip.y() -> "Yes"

                // 8. No: Index and Middle tapping thumb
                isIndexUp && isMiddleUp && !isRingUp && !isPinkyUp && thumbToIndex < 0.1 -> "No"

                // 9. Toilet: Thumb between Index and Middle (T sign)
                !isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp && thumbTip.x() > indexPip.x() -> "Toilet"

                // 10. Medicine: Middle finger tapping palm
                !isIndexUp && middleTip.y() > middlePip.y() && !isRingUp && !isPinkyUp -> "Medicine"

                // 11. Police: Hand C-shape near chest
                thumbToWrist > 0.15 && isIndexUp && isMiddleUp && isRingUp && isPinkyUp -> "Police"

                // 12. Hospital: Index and Middle forming an H (cross)
                isIndexUp && isMiddleUp && !isRingUp && !isPinkyUp && Math.abs(indexTip.x() - middleTip.x()) < 0.05 -> "Hospital"

                // 13. Sleep: Hand closing over face
                thumbToIndex < 0.08 && thumbToWrist < 0.1 -> "Sleep"

                // 14. I: Pointing to chest (Index up, others down)
                isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp -> "I"

                // 15. You: Pointing forward
                isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp && indexTip.y() > wrist.y() -> "You"

                // 16. Friend: Interlocked index fingers
                isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp -> "Friend"

                // 17. Danger: Closed fist, thumb out
                !isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp && thumbToWrist > 0.15 -> "Danger"

                // 18. Pain: Index fingers pointing at each other
                isIndexUp && !isMiddleUp && !isRingUp && !isPinkyUp -> "Pain"

                // 19. Goodbye: Hand waving (Side to side)
                isIndexUp && isMiddleUp && isRingUp && isPinkyUp && Math.abs(wrist.x() - indexTip.x()) > 0.1 -> "Goodbye"

                // 20. Please: Flat hand rubbing chest (Circle)
                isIndexUp && isMiddleUp && isRingUp && isPinkyUp && thumbToWrist < 0.1 -> "Please"

                else -> null
            }

            detectedSign?.let {
                lastResultTime = currentTime
                listener.onSignDetected(it)
            }
        }
    }

    interface SignDetectionListener {
        fun onSignDetected(sign: String)
        fun onError(error: String)
    }
}
