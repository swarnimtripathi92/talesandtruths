package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kidverse.app.databinding.ActivityCurrentGkBinding
import java.text.SimpleDateFormat
import java.util.*

class CurrentGKActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrentGkBinding
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "CurrentGKActivity"
    }

    private var todayPdfUrl: String? = null
    private var todayLabel: String? = null

    private var monthlyPdfUrl: String? = null
    private var monthlyLabel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Premium check BEFORE loading layout
        verifyPremiumAndLoad()
    }

    // ✅ Premium verification (no white flash)
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
                    // ✅ Premium user → Load full UI
                    loadContent()
                } else {
                    // ❌ Free user → Show sheet
                    showPremiumAndClose()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Subscription verification failed",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
    }

    // 👑 Show premium bottom sheet and close activity
    private fun showPremiumAndClose() {
        PremiumBottomSheet().show(
            supportFragmentManager,
            "PremiumSheet"
        )
        finish()
    }

    // ✅ Inflate layout only for premium users
    private fun loadContent() {

        binding = ActivityCurrentGkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchLatestDaily()
        fetchLatestMonthly()
        setupClicks()
    }

    // 📰 Fetch latest daily newspaper
    private fun fetchLatestDaily() {

        firestore.collection("daily_newspapers")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    Log.e(TAG, "No daily newspaper found")
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents[0]

                val imageUrl = doc.getString("image_url")
                todayPdfUrl = doc.getString("pdf_url")

                val timestamp = doc.getTimestamp("date")
                todayLabel = timestamp?.let {
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(it.toDate())
                } ?: "Daily Newspaper"

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.imgTodayNewspaper)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Daily query failed", e)
            }
    }

    // 📚 Fetch latest monthly magazine
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

        // 📰 Read Daily
        binding.btnReadToday.setOnClickListener {

            if (todayPdfUrl.isNullOrEmpty()) {
                Toast.makeText(this, "PDF not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GKTracker.recordGkRead(this, "current_daily_${todayLabel ?: "latest"}")

            val intent = Intent(this, PdfReaderActivity::class.java)
            intent.putExtra("pdf_url", todayPdfUrl)
            intent.putExtra("label", todayLabel)
            startActivity(intent)
        }

        // 📥 Download Daily
        binding.btnDownloadTodayPdf.setOnClickListener {

            if (todayPdfUrl.isNullOrEmpty()) {
                Toast.makeText(this, "PDF not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PdfDownloadUtil.download(
                context = this,
                pdfUrl = todayPdfUrl!!,
                fileName = "Kids_Daily_${todayLabel}.pdf"
            )
        }

        // 🗂 Previous Daily
        binding.btnPreviousNewspapers.setOnClickListener {
            startActivity(
                Intent(this, PreviousIssuesActivity::class.java)
                    .putExtra("type", "DAILY")
            )
        }

        // 📚 Read Monthly
        binding.btnReadMagazine.setOnClickListener {

            if (monthlyPdfUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Magazine not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GKTracker.recordGkRead(this, "current_monthly_${monthlyLabel ?: "latest"}")

            val intent = Intent(this, PdfReaderActivity::class.java)
            intent.putExtra("pdf_url", monthlyPdfUrl)
            intent.putExtra("label", monthlyLabel)
            startActivity(intent)
        }

        // 📥 Download Monthly
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

        // 🗂 Previous Monthly
        binding.btnPreviousMagazines.setOnClickListener {
            startActivity(
                Intent(this, PreviousIssuesActivity::class.java)
                    .putExtra("type", "MONTHLY")
            )
        }
    }
}
