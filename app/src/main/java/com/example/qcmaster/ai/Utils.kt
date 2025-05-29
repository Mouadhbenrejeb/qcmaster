package com.example.qcmaster.ai

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import androidx.compose.ui.geometry.Offset
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
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE
import org.opencv.imgproc.Imgproc.RETR_EXTERNAL
import java.io.ByteArrayOutputStream
import kotlin.math.max
import androidx.core.graphics.createBitmap
import kotlin.math.absoluteValue
import kotlin.math.min
import androidx.core.graphics.get


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

data class AnswerRow(
    val shapes: List<Shape>,
    val answer: Int,
)

data class AnswersResult(
    val answers: List<AnswerRow>,
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

//    val blurred = Mat()
//    Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
//
//    val bw = Mat()
//    Imgproc.adaptiveThreshold(
//        blurred, bw,
//        255.0,
//        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//        Imgproc.THRESH_BINARY_INV, // NOTE: Inverted threshold
//        21,  // block size (odd)
//        10.0  // tweakable constant
//    )
//
//    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
//    Imgproc.morphologyEx(bw, bw, Imgproc.MORPH_CLOSE, kernel)

    println("answerKeyBitmap size: ${answerKeyBitmap.width}x${answerKeyBitmap.height}")
    println("bw size: ${bw.width()}x${bw.height()}")

    val minCircleRadius = bw.width() / 20
    val maxCircleRadius = bw.width() / 6
    val minCircleArea = Math.PI * minCircleRadius * minCircleRadius
    val maxCircleArea = Math.PI * maxCircleRadius * maxCircleRadius

    println("minCircleArea: $minCircleArea")
    println("maxCircleArea: $maxCircleArea")

    // detect circles
    val shapes = bw.findCircles(
        minArea = minCircleArea,  // tune for your scan DPI
        maxArea = maxCircleArea
    )
//    val rows = circles.groupByRow(rowCount = 10)
    val rows = shapes

    val bitmap = createBitmap(bw.cols(), bw.rows())

    Utils.matToBitmap(bw, bitmap)

    // for each question (row), detect filled
    return AnswersResult(
        answers = rows.mapIndexed { index, rowCircles ->
            val sortedShapes = rowCircles.sortedBy { it.center.x }

            println("Detect filled for row: $index")
            val index = gray.detectFilled(sortedShapes, bitmap = answerKeyBitmap.copy(Bitmap.Config.ARGB_8888, false))

            AnswerRow(
                shapes = sortedShapes,
                answer = index ?: -1,
//                answer = -1,
            )
        },
        bitmap = bitmap,
    )
}

fun Bitmap.toGrayMat(): Mat {
    // 1) ensure we have an ARGB_8888, immutable (or at least lockable) bitmap
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

sealed interface Shape {

    val center: Offset

    val size: Float

    val contour: MatOfPoint

}

data class Circle(
    override val center: Offset,
    val radius: Float,
    override val contour: MatOfPoint,
): Shape {
    override val size: Float
        get() = radius * 2
}

data class Square(
    val topLeft: Offset,
    override val size: Float,
    override val contour: MatOfPoint,
): Shape {
    override val center: Offset
        get() = Offset(topLeft.x + size / 2, topLeft.y + size / 2)
}

fun Mat.findCircles(minArea: Double, maxArea: Double): List<List<Shape>> {
    // 1) find all external contours
    val contours = ArrayList<MatOfPoint>()
    Imgproc.findContours(this, contours, Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)

    val shapeRows = mutableListOf<List<Shape>>()

    println("contours: ${contours.toList().map { Imgproc.contourArea(it) }}")
    println("topLeft: ${contours.toList().map { 
        val x = it.toList().toList().minOf { it.x }
        val y = it.toList().toList().minOf { it.y }
        "x: $x, y: $y"
    }}")

    // first: shape + size filter from before
    val qcmCandidates = filterQcmContours(contours, width())

// then: pattern filter
    val qcmRows = filterByRowPattern(
        contours = qcmCandidates,
        imageHeight = height(),
        minInRowCount = 3,
        yTolerancePct = 0.02,
        spacingTolerancePct = 0.1
    )

    qcmRows.forEach { row ->
        val shapes = mutableListOf<Shape>()

        row.forEach { c ->
//    for (c in contours) {
            val area = Imgproc.contourArea(c)
//        if (area !in minArea..maxArea) {
//            c.release()
//            continue
//        }

            // 2) wrap points for the circle fitter
            val pts = MatOfPoint2f(*c.toArray())

            // 3) prepare outputs
            if (isCircle(c)) {
                val center = Point()
                val radiusArr = FloatArray(1)

                // 4) compute the minimum enclosing circle
                Imgproc.minEnclosingCircle(pts, center, radiusArr)
                val radius = radiusArr[0]

                // 5) filter by “roundness”
                if (area / (Math.PI * radius * radius) > 0.7) {
                    shapes += Circle(center.toOffset(), radius, c)
                }
            } else if (isSquare(c)) {
                // 5) filter by “roundness”
                val minX = c.toList().minOf { it.x }.toFloat()
                val maxX = c.toList().maxOf { it.x }.toFloat()
                val minY = c.toList().minOf { it.y }.toFloat()

                val topLeft = Offset(minX, minY)
                val width = (maxX - minX).absoluteValue

                shapes += Square(topLeft, width, c)
            }

            // 6) clean up
            pts.release()
            c.release()
        }

        shapeRows += shapes.toList()
    }

    return shapeRows
}

fun List<Shape>.groupByRow(rowCount: Int = 10): List<List<Shape>> {
    // 1) sort all by y
    val sorted = this.sortedBy { it.center.y }
    // prepare empty rows
    val rows = MutableList(rowCount) { mutableListOf<Shape>() }
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

fun Mat.detectFilled(circles: List<Shape>, threshold: Double = 0.5, bitmap: Bitmap): Int? {
    // Create these once and reuse
    val mask     = Mat.zeros(this.size(), CvType.CV_8U)
    val masked   = Mat()

    var filledIndex: Int? = null
    for ((i, c) in circles.withIndex()) {
        val centerX = c.center.x
        val centerY = c.center.y
        val radius = c.size / 2f
        var darkPixels = 0
        var totalPixels = 0
        var allPixels = 0

        val startX = max(0f, centerX - radius)
        val endX = min(bitmap.width - 1f, centerX + radius)
        val startY = max(0f, centerY - radius)
        val endY = min(bitmap.height - 1f, centerY + radius)

        val s = 2

        println("startX: $startX, endX: $endX, startY: $startY, endY: $endY, s: $s")

        for (x in startX.toInt()..endX.toInt() step s) {
            for (y in startY.toInt()..endY.toInt() step s) {
                val dx = x - centerX
                val dy = y - centerY
                if (dx * dx + dy * dy <= radius * radius) {
                    val color = bitmap[x, y]
                    val gray = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
                    totalPixels++
                    if (gray / 255f < threshold) darkPixels++
                }
                allPixels++
            }
        }

        val fillRatio =
            if (totalPixels == 0)
                0.0
            else
                darkPixels.toDouble() / totalPixels

        println("all pixels for $i = $allPixels")
        println("total pixels for $i = $totalPixels")
        println("dark pixels for $i = $darkPixels")
        println("center for $i = ${c.center}, radius; $radius")

        if (fillRatio >= threshold) {
            filledIndex = i
            break
        }
    }

    mask.release()
    masked.release()
    return filledIndex
}

/**
 * Checks whether a contour is a square.
 *
 * @param contour the contour to test
 * @param epsilonFactor fraction of arc length for polygon approximation (default 0.02)
 * @param aspectRatioTolerance allowable deviation from 1.0 (default 0.1 for ±10%)
 * @return true if contour is a square
 */
fun isSquare(
    contour: MatOfPoint,
    epsilonFactor: Double = 0.02,
    aspectRatioTolerance: Double = 0.1,
): Boolean {
    // convert to MatOfPoint2f
    val contour2f = MatOfPoint2f(*contour.toArray())
    // perimeter
    val peri = Imgproc.arcLength(contour2f, true)
    // approximate polygon
    val approx2f = MatOfPoint2f()
    Imgproc.approxPolyDP(contour2f, approx2f, epsilonFactor * peri, true)
    val pts = approx2f.toArray()
    // must have 4 vertices
    if (pts.size != 4) return false

    // check aspect ratio of bounding rect
    val rect = Imgproc.boundingRect(MatOfPoint(*pts))
    val ar = rect.width.toDouble() / rect.height
    if (ar < 1.0 - aspectRatioTolerance || ar > 1.0 + aspectRatioTolerance) {
        return false
    }

    // optional: ensure contour fills most of its bounding rect
    val area = Imgproc.contourArea(contour)
    val rectArea = rect.width.toDouble() * rect.height
    if (area / rectArea < 0.8) {
        return false
    }

    return true
}

/**
 * Checks whether a contour is a circle.
 *
 * @param contour the contour to test
 * @param epsilonFactor fraction of arc length for polygon approximation (default 0.02)
 * @param circularityThreshold minimum area ratio vs. enclosing circle (default 0.8)
 * @return true if contour is a circle
 */
fun isCircle(
    contour: MatOfPoint,
    epsilonFactor: Double = 0.02,
    circularityThreshold: Double = 0.8,
): Boolean {
    // convert to MatOfPoint2f
    val contour2f = MatOfPoint2f(*contour.toArray())
    // perimeter
    val peri = Imgproc.arcLength(contour2f, true)
    // approximate to smooth irregularities
    val approx2f = MatOfPoint2f()
    Imgproc.approxPolyDP(contour2f, approx2f, epsilonFactor * peri, true)
    // too few vertices → not a circle
    if (approx2f.toArray().size < 5) return false

    // contour area
    val area = Imgproc.contourArea(contour)
    // minimum enclosing circle
    val center = Point()
    val radius = FloatArray(1)
    Imgproc.minEnclosingCircle(contour2f, center, radius)
    val circleArea = Math.PI * radius[0] * radius[0]

    // compare area ratio
    return area / circleArea >= circularityThreshold
}

/**
 * Returns true if contour is roughly the right size (diameter or side) relative to image width.
 *
 * @param contour    the contour to test
 * @param imageWidth width of your source image in pixels
 * @param minPct     minimum allowed size as fraction of imageWidth (e.g. 0.03 for 3%)
 * @param maxPct     maximum allowed size as fraction of imageWidth (e.g. 0.08 for 8%)
 */
fun isWithinSize(
    contour: MatOfPoint,
    imageWidth: Int,
    minPct: Double,
    maxPct: Double,
): Boolean {
    // For circles: use diameter of min enclosing circle
    val contour2f = MatOfPoint2f(*contour.toArray())
    val center = Point()
    val radius = FloatArray(1)
    Imgproc.minEnclosingCircle(contour2f, center, radius)
    val diameter = 2 * radius[0]

    // For squares: use width of bounding rect
    val rect = Imgproc.boundingRect(contour)
    val side = maxOf(rect.width, rect.height).toFloat()

    // whichever is larger — ensures we cover both shapes
    val sizePx = maxOf(diameter, side).toDouble()
    val frac = sizePx / imageWidth.toDouble()

    return frac in minPct..maxPct
}

/**
 * Compute the centroid of a contour.
 */
private fun contourCenter(contour: MatOfPoint): Point {
    val m = Imgproc.moments(contour)
    return if (m.m00 != 0.0) Point(m.m10 / m.m00, m.m01 / m.m00)
    else {
        // fallback to bounding-rect center
        val r = Imgproc.boundingRect(contour)
        Point(r.x + r.width / 2.0, r.y + r.height / 2.0)
    }
}

/**
 * Filters contours to those that form at least one horizontal row of minInRowCount
 * with roughly equal X-spacing and Y-alignment.
 *
 * @param contours            list of candidate contours
 * @param imageHeight         height of source image in pixels
 * @param minInRowCount       minimum items in a valid row (default 3)
 * @param yTolerancePct       max Y-deviation (fraction of imageHeight) to group in same row (default 0.02 i.e. ±2%)
 * @param spacingTolerancePct allowed deviation from average X-spacing (default 0.1 i.e. ±10%)
 */
fun filterByRowPattern(
    contours: List<MatOfPoint>,
    imageHeight: Int,
    minInRowCount: Int = 3,
    yTolerancePct: Double = 0.02,         // ±2% of image height for Y alignment
    spacingTolerancePct: Double = 0.1,     // ±10% around median spacing
): List<List<MatOfPoint>> {
    if (contours.size < minInRowCount) return emptyList()

    // 1) cluster by Y
    data class C(val contour: MatOfPoint, val center: Point)
    val yTolPx = yTolerancePct * imageHeight
    val centers = contours.map { C(it, contourCenter(it)) }

    val yClusters = mutableListOf<MutableList<C>>()
    for (c in centers) {
        val row = yClusters.find { cluster ->
            cluster.any { Math.abs(it.center.y - c.center.y) <= yTolPx }
        }
        if (row != null) row += c else yClusters += mutableListOf(c)
    }

    // 2) within each Y-cluster, split by X-spacing consistency
    val subrows = mutableListOf<MutableList<C>>()
//    val accepted = mutableSetOf<MatOfPoint>()
    for (cluster in yClusters) {
        if (cluster.size < minInRowCount) continue

        val sorted = cluster.sortedBy { it.center.x }
        val spacings = sorted
            .zipWithNext()
            .map { (a, b) -> b.center.x - a.center.x }
        if (spacings.isEmpty()) continue

        // median spacing
        val sortedSp = spacings.sorted()
        val median = sortedSp[sortedSp.size / 2]
        val lowThr = median * (1 - spacingTolerancePct)
        val highThr = median * (1 + spacingTolerancePct)

        // build subrows
        var current = mutableListOf(sorted[0])
        for (i in spacings.indices) {
            val dist = spacings[i]
            if (dist in lowThr..highThr) {
                current += sorted[i + 1]
            } else {
                if (current.size >= minInRowCount) subrows += current
                current = mutableListOf(sorted[i + 1])
            }
        }
        if (current.size >= minInRowCount) subrows += current

        // collect all contours from valid subrows
//        subrows.forEach { row -> row.forEach { accepted += it.contour } }
    }

    return subrows.map { it.map { it.contour } }
//    return contours.filter { it in accepted }
}

/**
 * Filters a list of contours, keeping only those that are square or circle
 * and that lie within the given size bounds.
 *
 * @param contours       all detected contours
 * @param imageWidth     width of source image
 * @param minPct         minimum size as fraction of imageWidth
 * @param maxPct         maximum size as fraction of imageWidth
 */
fun filterQcmContours(
    contours: List<MatOfPoint>,
    imageWidth: Int,
    minPct: Double = 0.01,
    maxPct: Double = 0.1,
): List<MatOfPoint> {
    return contours.filter { cnt ->
        (isSquare(cnt) || isCircle(cnt)) &&
                isWithinSize(cnt, imageWidth, minPct, maxPct)
    }
}

fun Point.toOffset() = Offset(x.toFloat(), y.toFloat())

fun Offset.toPoint() = Point(x.toDouble(), y.toDouble())