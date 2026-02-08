package com.kidverse.app

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kidverse.app.databinding.ActivityPdfReaderBinding
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

class PdfReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfReaderBinding
    private var renderer: PdfRenderer? = null
    private var currentPage = 0

    companion object {
        private const val SCALE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.photoView.apply {
            adjustViewBounds = true
            minimumScale = 1f
            mediumScale = 2.5f
            maximumScale = 5f
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // 🔑 REQUIRED DATA
        val pdfUrl = intent.getStringExtra("pdf_url")
        val issueLabel = intent.getStringExtra("label") ?: "issue"

        if (pdfUrl.isNullOrEmpty()) {
            Toast.makeText(this, "PDF not available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // unique cache file (date / month based)
        val safeName = issueLabel.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val file = File(cacheDir, "pdf_$safeName.pdf")

        downloadPdf(pdfUrl, file)

        binding.btnNext.setOnClickListener { nextPage() }
        binding.btnPrev.setOnClickListener { prevPage() }
    }

    // ⬇️ Download PDF to cache
    private fun downloadPdf(url: String, file: File) {
        thread {
            try {
                URL(url).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                runOnUiThread { openPdf(file) }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // 📄 Open PDF
    private fun openPdf(file: File) {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fd)
        currentPage = 0
        renderPage(currentPage)
    }

    // 🖼 Render page
    private fun renderPage(index: Int) {
        val r = renderer ?: return

        val page = r.openPage(index)

        val bitmap = Bitmap.createBitmap(
            page.width * SCALE,
            page.height * SCALE,
            Bitmap.Config.ARGB_8888
        )

        bitmap.eraseColor(Color.WHITE)

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()

        binding.photoView.setImageBitmap(bitmap)
        binding.pageIndicator.text = "${index + 1} / ${r.pageCount}"
    }

    private fun nextPage() {
        renderer?.let {
            if (currentPage < it.pageCount - 1) {
                currentPage++
                renderPage(currentPage)
            }
        }
    }

    private fun prevPage() {
        if (currentPage > 0) {
            currentPage--
            renderPage(currentPage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        renderer?.close()
        renderer = null
    }
}
