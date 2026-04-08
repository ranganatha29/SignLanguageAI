package com.example.sign.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sign.ml.SignDetector
import java.util.concurrent.Executors

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream

import androidx.compose.runtime.*

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onSignDetected: (String) -> Unit,
    onImageCaptured: ((String) -> Unit)? = null,
    triggerCapture: Boolean = false,
    onCaptureHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Use a ref to access current trigger state in the analyzer
    val captureRequested = rememberUpdatedState(triggerCapture)

    val detector = remember {
        SignDetector(context, object : SignDetector.SignDetectionListener {
            override fun onSignDetected(sign: String) {
                onSignDetected(sign)
            }
            override fun onError(error: String) {
                Log.e("CameraPreview", "Detection error: $error")
            }
        })
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            // Local detection (fast)
                            val bitmap = imageProxy.toBitmap()
                            detector.detect(bitmap, imageProxy.imageInfo.timestamp)
                            
                            // Manual trigger for API-based detection
                            if (captureRequested.value && onImageCaptured != null) {
                                val base64 = bitmapToBase64(bitmap, imageProxy.imageInfo.rotationDegrees)
                                onImageCaptured(base64)
                                onCaptureHandled()
                            }

                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

fun bitmapToBase64(bitmap: Bitmap, rotation: Int): String {
    val matrix = Matrix().apply { 
        postRotate(rotation.toFloat())
        // Downscale to 480p to reduce payload size and stay under rate limits
        val scale = 480f / Math.max(bitmap.width, bitmap.height)
        if (scale < 1f) postScale(scale, scale)
    }
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val outputStream = ByteArrayOutputStream()
    // Lower quality to 40% to further reduce size
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
