package com.kidverse.app

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

object PdfDownloadUtil {

    fun download(context: Context, pdfUrl: String, fileName: String) {

        thread {
            try {
                // ✅ SAFE location (no permission needed)
                val dir = File(context.getExternalFilesDir(null), "KidVerse")

                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, fileName)

                Log.d("PDF_DOWNLOAD", "Saving at: ${file.absolutePath}")

                URL(pdfUrl).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                (context as Activity).runOnUiThread {
                    Toast.makeText(
                        context,
                        "Downloaded successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("PDF_DOWNLOAD", "Failed", e)
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
