package com.example.qcmaster.utils

import android.content.Context
import android.os.Environment
import com.example.qcmaster.ClassModel
import com.example.qcmaster.models.Exam
import com.example.qcmaster.models.Student
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for generating PDF reports of student exam results.
 */
object PdfUtils {

    /**
     * Generates a PDF report with student exam results.
     *
     * @param context Android context
     * @param exam The exam for which to generate the report
     * @param className The name of the class
     * @param students List of students in the class
     * @return The generated PDF file or null if generation failed
     */
    fun generateExamResultsPdf(
        context: Context,
        exam: Exam,
        className: String,
        students: List<Student>
    ): File? {
        try {
            // Create a timestamp for the filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "exam_results_${exam.name.replace(" ", "_")}_${className.replace(" ", "_")}_$timestamp.pdf"

            // Create the file in the app's external files directory
            val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val outputStream = FileOutputStream(pdfFile)

            // Initialize PDF writer and document
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add title
            val title = Paragraph("Exam Results")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(title)

            // Add exam and class information
            document.add(Paragraph("Exam: ${exam.name}").setFontSize(16f))
            document.add(Paragraph("Class: $className").setFontSize(16f))
            document.add(Paragraph("Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}").setFontSize(12f))
            document.add(Paragraph("\n"))

            // Create table for student results
            val table = Table(UnitValue.createPercentArray(floatArrayOf(10f, 45f, 45f)))
                .setWidth(UnitValue.createPercentValue(100f))

            // Add table headers
            table.addHeaderCell(createHeaderCell("No."))
            table.addHeaderCell(createHeaderCell("Student Name"))
            table.addHeaderCell(createHeaderCell("Mark (/20)"))

            // Add student data
            students.forEachIndexed { index, student ->
                val studentId = student.cin
                val percentageMark = exam.studentMarks[studentId] ?: 0L
                // Convert percentage to a value out of 20
                val markOutOf20 = (percentageMark * 0.2).toInt()

                table.addCell(createCell((index + 1).toString()))
                table.addCell(createCell(student.name))
                table.addCell(createCell(markOutOf20.toString()))
            }

            document.add(table)

            // Add summary statistics
            val percentageMarks = students.mapNotNull { student -> 
                exam.studentMarks[student.cin] 
            }

            if (percentageMarks.isNotEmpty()) {
                // Convert percentage marks to marks out of 20
                val marksOutOf20 = percentageMarks.map { (it * 0.2).toInt() }

                document.add(Paragraph("\n"))
                document.add(Paragraph("Summary Statistics:").setFontSize(16f))
                document.add(Paragraph("Number of Students: ${students.size}"))
                document.add(Paragraph("Average Mark: ${marksOutOf20.average().toInt()}/20"))
                document.add(Paragraph("Highest Mark: ${marksOutOf20.maxOrNull()}/20"))
                document.add(Paragraph("Lowest Mark: ${marksOutOf20.minOrNull()}/20"))
            }

            // Close the document
            document.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Creates a header cell for the PDF table.
     */
    private fun createHeaderCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
    }

    /**
     * Creates a regular cell for the PDF table.
     */
    private fun createCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setTextAlignment(TextAlignment.CENTER)
    }
}
