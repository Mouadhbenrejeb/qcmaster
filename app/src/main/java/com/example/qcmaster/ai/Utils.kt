package com.example.qcmaster.ai

import android.R.attr.bitmap
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
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE
import org.opencv.imgproc.Imgproc.RETR_EXTERNAL
import java.io.ByteArrayOutputStream
import kotlin.math.max
import androidx.core.graphics.createBitmap


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

data class AnswersResult(
    val answers: Map<Int, Int?>,
    val circles: List<Circle>,
    val bitmap: Bitmap?,
)

suspend fun extractAnswers(
    apiKey: String,
    answerKeyBitmap: Bitmap,
): Map<String, String> = withContext(Dispatchers.IO) {
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

suspend fun extractAnswersOpenCv(
    answerKeyBitmap: Bitmap,
): AnswersResult {
    val gray = answerKeyBitmap.toGrayMat()
    val bw   = gray.toBinary()

    println("bw size: ${bw.width()}x${bw.height()}")

    val minCircleRadius = bw.width() / 20
    val maxCircleRadius = bw.width() / 6
    val minCircleArea = Math.PI * minCircleRadius * minCircleRadius
    val maxCircleArea = Math.PI * maxCircleRadius * maxCircleRadius

    println("minCircleArea: $minCircleArea")
    println("maxCircleArea: $maxCircleArea")

    // detect circles
    val circles = bw.findCircles(
        minArea = minCircleArea,  // tune for your scan DPI
        maxArea = maxCircleArea
    )
    val rows = circles.groupByRow(rowCount = 10)

    val bitmap = createBitmap(bw.cols(), bw.rows())

    Utils.matToBitmap(bw, bitmap)

    // for each question (row), detect filled
    return AnswersResult(
        answers = rows.mapIndexed { rowIdx, rowCircles ->
            val filled0 = bw.detectFilled(rowCircles)
            // convert to 1-based if not null
            rowIdx + 1 to (filled0?.plus(1))
        }.toMap(),
        circles = circles,
        bitmap = bitmap,
    )
}

fun Bitmap.toGrayMat(): Mat {
    // 1) ensure we have an ARGB_8888, mutable (or at least lockable) bitmap
    val lockable = copy(Bitmap.Config.ARGB_8888, false)

    // 2) convert to Mat
    val rgba = Mat()
    Utils.bitmapToMat(lockable, rgba)

    // 3) convert to gray
    val gray = Mat()
    Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
    rgba.release()

    return gray
}


fun Mat.toBinary(): Mat {
    val bw = Mat()
    // inverted: bubbles (ink) = white
    Imgproc.adaptiveThreshold(
        this, bw, 255.0,
        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
        Imgproc.THRESH_BINARY_INV,
        15, 10.0
    )
    this.release()
    return bw
}

data class Circle(val center: Point, val radius: Float)

fun Mat.findCircles(minArea: Double, maxArea: Double): List<Circle> {
    // 1) find all external contours
    val contours = ArrayList<MatOfPoint>()
    Imgproc.findContours(this, contours, Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)

    val circles = mutableListOf<Circle>()

    println("contours: ${contours.toList().map { Imgproc.contourArea(it) }}")
    println("topLeft: ${contours.toList().map { 
        val x = it.toList().toList().minOf { it.x }
        val y = it.toList().toList().minOf { it.y }
        "x: $x, y: $y"
    }}")
    for (c in contours) {
        val area = Imgproc.contourArea(c)
//        if (area !in minArea..maxArea) {
//            c.release()
//            continue
//        }

        // 2) wrap points for the circle fitter
        val pts = MatOfPoint2f(*c.toArray())

        // 3) prepare outputs
        val center = Point()
        val radiusArr = FloatArray(1)

        // 4) compute the minimum enclosing circle
        Imgproc.minEnclosingCircle(pts, center, radiusArr)
        val radius = radiusArr[0]

        // 5) filter by “roundness”
        if (area / (Math.PI * radius * radius) > 0.7) {
            circles += Circle(center, radius)
        }

        // 6) clean up
        pts.release()
        c.release()
    }

    return circles
}

fun List<Circle>.groupByRow(rowCount: Int = 10): List<List<Circle>> {
    // 1) sort all by y
    val sorted = this.sortedBy { it.center.y }
    // prepare empty rows
    val rows = MutableList(rowCount) { mutableListOf<Circle>() }
    if (sorted.isEmpty()) return rows

    // 2) make sure perRow >= 1
    val perRow = max(1, (sorted.size + rowCount - 1) / rowCount)

    // 3) chunk into roughly equal groups
    sorted.chunked(perRow).forEachIndexed { idx, chunk ->
        if (idx < rowCount) rows[idx].addAll(chunk)
    }

    // 4) within each row sort by x
    return rows.map { it.sortedBy { c -> c.center.x } }
}

fun Mat.detectFilled(circles: List<Circle>, threshold: Double = 0.5): Int? {
    // Create these once and reuse
    val mask     = Mat.zeros(this.size(), CvType.CV_8U)
    val masked   = Mat()

    var filledIndex: Int? = null
    for ((i, c) in circles.withIndex()) {
        // 1) draw a filled white circle into `mask`
        mask.setTo(Scalar(0.0))
        Imgproc.circle(mask, c.center, c.radius.toInt(), Scalar(255.0), -1)

        // 2) apply mask: masked = this & this under `mask`
        Core.bitwise_and(this, this, masked, mask)

        // 3) count white pixels (remember: we inverted so marks are “white”)
        val filledPixels = Core.countNonZero(masked)
        val totalPixels  = Math.PI * c.radius * c.radius

        if (filledPixels / totalPixels > threshold) {
            filledIndex = i
            break
        }
    }

    mask.release()
    masked.release()
    return filledIndex
}



