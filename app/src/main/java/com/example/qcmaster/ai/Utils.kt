package com.example.qcmaster.ai

import android.graphics.Bitmap
import android.util.Base64
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream


// 1️⃣ Data models (reuse from before)

@Serializable
data class ContentPart(
    val type: String,                   // “text” or “image_url”
    val text: String? = null,           // only for text parts
    @SerialName("image_url")
    val imageUrl: ImageRef? = null,      // only for image parts
)

@Serializable
data class ImageRef(val url: String)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<ContentPart>,
)

@Serializable
data class ChatRequest(
    val model: String,
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    val messages: List<ChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 500,
)

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
    answerKeyBitmap: Bitmap,
): Map<String, String> = withContext(Dispatchers.IO) {
    println("bimap size: ${answerKeyBitmap.width}x${answerKeyBitmap.height}")
    // 1) turn bitmap into data URL
    val answerUrl = answerKeyBitmap.toDataUrl()

    // 2) build messages
    val sys = ChatMessage(
        role = "system",
        content = listOf(
            ContentPart(
                type = "text",
                text = """You are an assistant that reads a scanned student QCM answer sheet and returns only which boxes the student filled""".trimIndent()
            )
        )
    )

    val usr = ChatMessage(
        role = "user",
        content = listOf(
            ContentPart(
                type = "text", text = """
                I’m giving you an image of a student’s completed QCM.  
                For each question, return **only** the **1-based index** of the box the student marked.  
                Do **not** compare to an answer key or judge correctness—just report which box is filled.  
                Boxes may be unlabeled and arranged vertically or horizontally.  
                Return **ONLY** valid JSON, e.g.:

                {
                  "1": "3",
                  "2": "1",
                  "3": "5",
                  …
                }
            """.trimIndent()
            ),
            ContentPart(type = "image_url", imageUrl = ImageRef(answerUrl))
        )
    )

    val requestBody = ChatRequest(
        model = "gpt-4o",
        messages = listOf(sys, usr)
    )

    // 3) fire off with Ktor
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                }
            )  // default Json is fine now
        }
    }

    try {
        val resp = client.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        println("resposne: ${resp.bodyAsText()}")
        val chatResponse = resp.body<ChatResponse>()
        println("chatResponse: $chatResponse")
        // 4) parse JSON map out of the assistant’s reply
        val jsonText = chatResponse.choices.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.removePrefix("```json")
            ?.removeSuffix("```")
            ?.trim()
            ?: throw IllegalStateException("No response from model")

        Json.decodeFromString<Map<String, String>>(jsonText)
    } catch (e: ClientRequestException) {
        // This exception is thrown for 4xx responses.
        val errorJson = e.response.bodyAsText()
        throw RuntimeException("OpenAI API error: $errorJson")
    } finally {
        client.close()
    }
}
