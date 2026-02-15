package com.kidverse.app

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.kidverse.app.databinding.ActivityCurrentGkBinding
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

class CurrentGKActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrentGkBinding
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "CurrentGKActivity"
    }

    private var todayIssueId: String? = null
    private var todayIssue: DailyIssue? = null

    private var monthlyPdfUrl: String? = null
    private var monthlyLabel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyPremiumAndLoad()
    }

    private fun verifyPremiumAndLoad() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            showPremiumAndClose()
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val isPremium = doc.getBoolean("isPremium") ?: false
                if (isPremium) {
                    loadContent()
                } else {
                    showPremiumAndClose()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Subscription verification failed", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun showPremiumAndClose() {
        PremiumBottomSheet().show(supportFragmentManager, "PremiumSheet")
        finish()
    }

    private fun loadContent() {
        binding = ActivityCurrentGkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchLatestDaily()
        fetchLatestMonthly()
        setupClicks()
    }

    private fun fetchLatestDaily() {
        firestore.collection("daily_newspapers")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.e(TAG, "No daily newspaper found")
                    Toast.makeText(this, "No daily newspaper found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents[0]
                todayIssueId = doc.id
                todayIssue = DailyIssue.fromFirestore(doc.data.orEmpty())

                val imageUrl = doc.getString("image_url")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.imgTodayNewspaper)
                }

                val timestamp = doc.getTimestamp("date")
                val label = timestamp?.let {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it.toDate())
                } ?: "Daily Newspaper"
                binding.txtIssueDate.text = label
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Daily query failed", e)
                Toast.makeText(this, "Failed to load newspaper", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchLatestMonthly() {
        firestore.collection("monthly_magazine")
            .orderBy("order", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.e(TAG, "No monthly magazine found")
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents[0]
                val imageUrl = doc.getString("image_url")
                monthlyPdfUrl = doc.getString("pdf_url")
                monthlyLabel = doc.getString("month") ?: "Monthly Magazine"

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.imgMonthlyMagazine)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch monthly magazine", it)
            }
    }

    private fun setupClicks() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnReadToday.setOnClickListener {
            val issueId = todayIssueId
            if (issueId.isNullOrBlank()) {
                Toast.makeText(this, "Newspaper loading...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DailyNewspaperActivity::class.java)
            intent.putExtra("issue_id", issueId)
            startActivity(intent)
        }

        binding.btnDownloadTodayPdf.setOnClickListener {
            val issue = todayIssue
            if (issue == null) {
                Toast.makeText(this, "Newspaper loading...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateAndSaveIssuePdf(issue)
        }

        binding.btnPreviousNewspapers.setOnClickListener {
            startActivity(Intent(this, PreviousIssuesActivity::class.java).putExtra("type", "DAILY"))
        }

        binding.btnReadMagazine.setOnClickListener {
            if (monthlyPdfUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Magazine not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GKTracker.recordGkRead(this, "current_monthly_${monthlyLabel ?: "latest"}")
            GKTracker.recordGkRead("current_monthly_${monthlyLabel ?: "latest"}")

            val intent = Intent(this, PdfReaderActivity::class.java)
            intent.putExtra("pdf_url", monthlyPdfUrl)
            intent.putExtra("label", monthlyLabel)
            startActivity(intent)
        }

        binding.btnDownloadMagazinePdf.setOnClickListener {
            if (monthlyPdfUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Magazine not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PdfDownloadUtil.download(
                context = this,
                pdfUrl = monthlyPdfUrl!!,
                fileName = "Kids_Monthly_${monthlyLabel}.pdf"
            )
        }

        binding.btnPreviousMagazines.setOnClickListener {
            startActivity(Intent(this, PreviousIssuesActivity::class.java).putExtra("type", "MONTHLY"))
        }
    }

    private fun generateAndSaveIssuePdf(issue: DailyIssue) {
        Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val pdfBytes = createIssuePdf(issue)
                val fileName = "Kids_Daily_${issue.fileSafeDate}.pdf"
                savePdfToDownloads(fileName, pdfBytes)
                runOnUiThread {
                    Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "PDF generation failed", e)
                runOnUiThread {
                    Toast.makeText(this, "PDF generation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createIssuePdf(issue: DailyIssue): ByteArray {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 32f
        val lineSpacing = 20f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 13f
        }

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            pdfDocument.finishPage(page)
            pageNumber += 1
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = margin
        }

        fun ensureSpace(required: Float) {
            if (y + required > pageHeight - margin) newPage()
        }

        canvas.drawText("KIDS DAILY NEWSPAPER", margin, y, titlePaint)
        y += 28f
        canvas.drawText("Issue Date: ${issue.displayDate}", margin, y, bodyPaint)
        y += lineSpacing

        if (issue.editorNote.isNotBlank()) {
            wrapText("Editor Note: ${issue.editorNote}", bodyPaint, pageWidth - margin * 2).forEach {
                ensureSpace(lineSpacing)
                canvas.drawText(it, margin, y, bodyPaint)
                y += lineSpacing
            }
        }

        issue.sectionsInOrder().forEach { section ->
            ensureSpace(30f)
            y += 8f
            canvas.drawText(section.title, margin, y, headingPaint)
            y += lineSpacing

            if (section.imageUrl.isNotBlank()) {
                val bitmap = downloadBitmap(section.imageUrl)
                if (bitmap != null) {
                    val availableWidth = pageWidth - (margin * 2)
                    val targetHeight = (bitmap.height * (availableWidth / bitmap.width)).coerceAtMost(220f)
                    ensureSpace(targetHeight + 10f)
                    val scaled = Bitmap.createScaledBitmap(bitmap, availableWidth.toInt(), targetHeight.toInt(), true)
                    canvas.drawBitmap(scaled, margin, y, null)
                    y += targetHeight + 10f
                    scaled.recycle()
                    bitmap.recycle()
                }
            }

            wrapText(section.content.ifBlank { "-" }, bodyPaint, pageWidth - margin * 2).forEach {
                ensureSpace(lineSpacing)
                canvas.drawText(it, margin, y, bodyPaint)
                y += lineSpacing
            }
        }

        ensureSpace(40f)
        y += 8f
        canvas.drawText("Quick Cards", margin, y, headingPaint)
        y += lineSpacing

        listOf(
            "Joke of the Day: ${issue.jokeOfDay.ifBlank { "-" }}",
            "Good Habit of the Day: ${issue.goodHabit.ifBlank { "-" }}",
            "Moral Mini Story: ${issue.moralStory.ifBlank { "-" }}"
        ).forEach { text ->
            wrapText(text, bodyPaint, pageWidth - margin * 2).forEach {
                ensureSpace(lineSpacing)
                canvas.drawText(it, margin, y, bodyPaint)
                y += lineSpacing
            }
        }

        pdfDocument.finishPage(page)

        return ByteArrayOutputStream().use { out ->
            pdfDocument.writeTo(out)
            pdfDocument.close()
            out.toByteArray()
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Image download failed for PDF", e)
            null
        }
    }

    private fun savePdfToDownloads(fileName: String, data: ByteArray) {
        val resolver = contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Unable to create download entry")

            resolver.openOutputStream(uri)?.use { it.write(data) }
                ?: throw IllegalStateException("Unable to open download output stream")

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val legacyDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            java.io.File(legacyDir, fileName).outputStream().use { it.write(data) }
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split("\\s+").filter { it.isNotBlank() }
        if (words.isEmpty()) return listOf("")

        val lines = mutableListOf<String>()
        var current = StringBuilder()

        words.forEach { word ->
            val candidate = if (current.isEmpty()) word else "${current} $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }

        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}

data class DailyIssue(
    val displayDate: String,
    val fileSafeDate: String,
    val coverUrl: String?,
    val editorNote: String,
    val sections: Map<String, DailySection>,
    val jokeOfDay: String,
    val goodHabit: String,
    val moralStory: String
) {
    fun sectionsInOrder(): List<DailySection> {
        val order = listOf("topStory", "gkBooster", "wonderWorld", "puzzle")
        return order.map { key ->
            sections[key] ?: DailySection(defaultTitle(key), "")
        }
    }

    private fun defaultTitle(key: String): String {
        return when (key) {
            "topStory" -> "Top Story"
            "gkBooster" -> "GK Booster"
            "wonderWorld" -> "Wonder World"
            "puzzle" -> "Puzzle"
            else -> key
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(data: Map<String, Any>): DailyIssue {
            val ts = data["date"] as? Timestamp
            val date = ts?.toDate()
            val display = if (date != null) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            } else {
                "Daily Newspaper"
            }

            val safeDate = if (date != null) {
                DateFormat.format("dd-MM-yyyy", date).toString()
            } else {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(java.util.Date())
            }

            val rawSections = data["sections"] as? Map<String, Any?> ?: emptyMap()
            val sectionMap = rawSections.mapValues { (_, value) ->
                val map = value as? Map<String, Any?> ?: emptyMap()
                DailySection(
                    title = map["title"]?.toString().orEmpty(),
                    content = map["content"]?.toString().orEmpty(),
                    imageUrl = map["imageUrl"]?.toString().orEmpty()
                )
            }

            val miniCards = data["mini_cards"] as? Map<String, Any?> ?: emptyMap()

            return DailyIssue(
                displayDate = display,
                fileSafeDate = safeDate,
                coverUrl = data["image_url"]?.toString(),
                editorNote = data["editor_note"]?.toString().orEmpty(),
                sections = sectionMap,
                jokeOfDay = miniCards["jokeOfTheDay"]?.toString().orEmpty(),
                goodHabit = miniCards["goodHabitOfTheDay"]?.toString().orEmpty(),
                moralStory = miniCards["moralMiniStory"]?.toString().orEmpty()
            )
        }
    }
}

data class DailySection(
    val title: String,
    val content: String,
    val imageUrl: String = ""
)
