package com.waw.messenger.workspace

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object WorkspaceDocumentTools {
    fun isEditableText(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".log") || lower.endsWith(".csv") || lower.endsWith(".json") || lower.endsWith(".xml") || lower.endsWith(".html") || lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".kt")
    }

    fun isPdf(name: String): Boolean = name.lowercase().endsWith(".pdf")

    fun exportTextToPdf(context: Context, output: Uri, title: String, text: String): Boolean = runCatching {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f; isFakeBoldText = true }
        val lineHeight = 16f
        val maxChars = 92
        val lines = text.replace("\r\n", "\n").split('\n').flatMap { line ->
            if (line.isEmpty()) listOf("") else line.chunked(maxChars)
        }

        var pageNumber = 1
        var index = 0
        while (index < lines.size || (lines.isEmpty() && pageNumber == 1)) {
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            val canvas = page.canvas
            canvas.drawText(title.take(70), margin, margin, titlePaint)
            var y = margin + 30f
            while (index < lines.size && y < pageHeight - margin) {
                canvas.drawText(lines[index], margin, y, paint)
                y += lineHeight
                index++
            }
            pdf.finishPage(page)
            pageNumber++
            if (lines.isEmpty()) break
        }

        context.contentResolver.openFileDescriptor(output, "w")?.use { pfd ->
            FileOutputStream(pfd.fileDescriptor).use { pdf.writeTo(it) }
        } ?: error("Unable to open output document")
        pdf.close()
        true
    }.getOrDefault(false)

    fun cacheCopy(context: Context, source: Uri): File? = runCatching {
        val dir = File(context.cacheDir, "workspace-preview").apply { mkdirs() }
        val out = File(dir, "preview-${System.currentTimeMillis()}.pdf")
        context.contentResolver.openInputStream(source)?.use { input -> out.outputStream().use { input.copyTo(it) } }
            ?: error("Unable to read PDF")
        out
    }.getOrNull()
}
