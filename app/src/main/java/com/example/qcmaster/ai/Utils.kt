package com.example.qcmaster.ai

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

// 1️⃣ Data models (reuse from before)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<ContentPart>
)

@Serializable
sealed class ContentPart {
    @Serializable @SerialName("text")
    data class Text(val type: String = "text", val text: String) : ContentPart()

    @Serializable @SerialName("image_url")
    data class ImageUrl(val type: String = "image_url", val image_url: ImageRef) : ContentPart()
}

@Serializable
data class ImageRef(val url: String)

@Serializable
data class ChatResponse(val choices: List<Choice>)

@Serializable
data class Choice(val message: ResponseMessage)

@Serializable
data class ResponseMessage(val content: String)


// 2️⃣ Helper to make a Bitmap into a data URL

fun Bitmap.toDataUrl(quality: Int = 80): String {
    ByteArrayOutputStream().use { baos ->
        compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }
}


// 3️⃣ Extraction function

suspend fun extractAnswers(
    apiKey: String,
    answerKeyBitmap: Bitmap
): Map<String, String> = withContext(Dispatchers.IO) {
    // Build the chat payload
    val messages = listOf(
        ChatMessage(
            role = "system",
            content = listOf(
                ContentPart.Text(
                    text = """
            You are a grading assistant. 
            I’ll give you an image of the official answer key for an exam.
            Extract each question number and its correct answer choice.
            Return ONLY valid JSON in this form:
            {
              "1": "A",
              "2": "C",
              …
            }
          """.trimIndent()
                )
            )
        ),
        ChatMessage(
            role = "user",
            content = listOf(
                ContentPart.Text(text = "Here is the answer key image:"),
                ContentPart.ImageUrl(image_url = ImageRef(answerKeyBitmap.toDataUrl()))
            )
        )
    )

    val requestBody = ChatRequest(
        model = "gpt-4o",
        messages = messages
    )

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true

                    // ⚠️ change the classDiscriminator so it isn’t "type" anymore
                    classDiscriminator = "kind"
                }
            )
        }
    }

    try {
        val resp = client.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        println("Response: ${resp.bodyAsText()}")

        // parse the JSON from the assistant’s reply
        val jsonText = resp.body<ChatResponse>().choices.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?: throw IllegalStateException("No response from model")

        // Use kotlinx to decode into a Map<String,String>
        Json.decodeFromString(
            MapSerializer(String.serializer(), String.serializer()),
            jsonText
        )
    } finally {
        client.close()
    }
}
