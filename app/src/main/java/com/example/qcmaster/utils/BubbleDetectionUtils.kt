package com.example.qcmaster.utils



import android.graphics.Bitmap
import android.graphics.Rect

object BubbleDetectionUtils {

    private val answerOptions = listOf("A", "B", "C", "D")

    fun detectFilledBubbles(
        bitmap: Bitmap,
        bubbleRegions: List<List<Rect>>
    ): List<String> {
        val selectedAnswers = mutableListOf<String>()

        for (questionBubbles in bubbleRegions) {
            var darkestIndex = -1
            var minBrightness = Float.MAX_VALUE

            questionBubbles.forEachIndexed { index, rect ->
                val cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                val avgBrightness = calculateAverageBrightness(cropped)

                if (avgBrightness < minBrightness) {
                    minBrightness = avgBrightness
                    darkestIndex = index
                }
            }

            selectedAnswers.add(answerOptions[darkestIndex])
        }

        return selectedAnswers
    }

    private fun calculateAverageBrightness(bitmap: Bitmap): Float {
        var sum = 0L
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                sum += (r + g + b) / 3
            }
        }
        return sum.toFloat() / (bitmap.width * bitmap.height)
    }

    // Example fixed bubble regions (you must update these according to your real template)
    fun getExampleBubbleRegions(): List<List<Rect>> {
        val result = mutableListOf<List<Rect>>()
        val startX = 100
        val startY = 200
        val bubbleWidth = 40
        val bubbleHeight = 40
        val bubbleSpacing = 60
        val rowSpacing = 60

        for (i in 0 until 10) {
            val rowTop = startY + i * rowSpacing
            val row = List(4) { j ->
                Rect(
                    startX + j * bubbleSpacing,
                    rowTop,
                    startX + j * bubbleSpacing + bubbleWidth,
                    rowTop + bubbleHeight
                )
            }
            result.add(row)
        }
        return result
    }
}
