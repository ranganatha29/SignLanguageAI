package com.example.sign.data.repository

import com.example.sign.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenAiRepository {
    private val api: OpenAiApi = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAiApi::class.java)

    // OpenAI API Key with Bearer prefix
    private val apiKey = "Bearer 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"

    private var lastRequestTime = 0L
    private var lastResult = "Ready to translate"
    private var isApiBusy = false

    suspend fun translateSignImage(base64Image: String, targetLanguage: String = "English"): String = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        
        // 1. Check if already processing
        if (isApiBusy) return@withContext "Processing previous request... Please wait."

        // 2. Hard 10-second global throttle to prevent 429 errors
        val timeSinceLast = currentTime - lastRequestTime
        if (timeSinceLast < 10000) {
            val secondsLeft = (10000 - timeSinceLast) / 1000
            return@withContext "Rate limited. Try again in $secondsLeft seconds."
        }
        
        try {
            isApiBusy = true
            lastRequestTime = currentTime
            android.util.Log.d("OpenAiRepository", "Sending image to OpenAI...")
            
            val response = api.getChatCompletion(
                apiKey = apiKey,
                request = ChatCompletionRequest(
                    model = "gpt-4o-mini", // Mini is more stable and has higher rate limits
                    messages = listOf(
                        VisionMessage(
                            role = "system",
                            content = "You are an expert Sign Language interpreter. Translate this sign into a short $targetLanguage sentence. Be very accurate."
                        ),
                        VisionMessage(
                            role = "user",
                            content = listOf(
                                ContentPart(type = "text", text = "Translate this sign:"),
                                ContentPart(type = "image_url", image_url = ImageUrl(url = "data:image/jpeg;base64,$base64Image"))
                            )
                        )
                    )
                )
            )
            
            val result = response.choices.firstOrNull()?.message?.content ?: "Could not interpret"
            lastResult = result
            result
        } catch (e: Exception) {
            android.util.Log.e("OpenAiRepository", "API Error", e)
            if (e.message?.contains("429") == true) {
                "OpenAI is busy. Please wait 10 seconds."
            } else {
                "Error: ${e.localizedMessage}"
            }
        } finally {
            isApiBusy = false
        }
    }

    suspend fun refineTranslation(rawSignText: String, targetLanguage: String = "English"): String = withContext(Dispatchers.IO) {
        try {
            val response = api.getChatCompletion(
                apiKey = apiKey,
                request = ChatCompletionRequest(
                    messages = listOf(
                        VisionMessage(
                            role = "system",
                            content = "You are an expert sign language interpreter. Convert the following raw sign language labels into a grammatically correct and natural $targetLanguage sentence."
                        ),
                        VisionMessage(
                            role = "user",
                            content = rawSignText
                        )
                    )
                )
            )
            response.choices.firstOrNull()?.message?.content ?: rawSignText
        } catch (e: Exception) {
            e.printStackTrace()
            "Error refining translation: ${e.message}"
        }
    }
}
