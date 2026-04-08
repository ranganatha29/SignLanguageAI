package com.example.sign.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

data class ChatCompletionRequest(
    val model: String = "gpt-4o",
    val messages: List<VisionMessage>,
    val max_tokens: Int = 300
)

data class VisionMessage(
    val role: String,
    val content: Any // Can be String or List<ContentPart>
)

data class ContentPart(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String // This will be "data:image/jpeg;base64,{base64_image}"
)

data class ChatCompletionResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val role: String,
    val content: String
)
