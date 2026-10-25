package com.manas.medbuddy.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrescriptionStorageManager(private val context: Context) {

    private val storageDir: File by lazy {
        File(context.filesDir, "prescriptions").apply {
            if (!exists()) mkdirs()
        }
    }

    fun saveUriToSecureStorage(sourceUri: Uri): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val mimeType = context.contentResolver.getType(sourceUri) ?: ""
            val extension = if (mimeType.contains("pdf", ignoreCase = true) || sourceUri.toString().endsWith(".pdf", ignoreCase = true)) {
                ".pdf"
            } else {
                ".jpg"
            }

            val targetFile = File(storageDir, "rx_$timeStamp$extension")
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(targetFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun renderPdfFirstPageToBitmap(pdfFile: File): Bitmap? {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            if (renderer.pageCount == 0) {
                renderer.close()
                fileDescriptor.close()
                return null
            }

            val page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            fileDescriptor.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromFile(file: File): Bitmap? {
        return try {
            if (file.name.endsWith(".pdf", ignoreCase = true)) {
                renderPdfFirstPageToBitmap(file)
            } else {
                BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
