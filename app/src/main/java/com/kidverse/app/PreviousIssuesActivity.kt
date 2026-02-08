package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kidverse.app.adapter.IssueAdapter
import com.kidverse.app.databinding.ActivityPreviousIssuesBinding
import com.kidverse.app.model.IssueModel
import java.text.SimpleDateFormat
import java.util.*

class PreviousIssuesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviousIssuesBinding
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "PreviousIssuesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviousIssuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        when (intent.getStringExtra("type")) {
            "DAILY" -> loadDailyIssues()
            "MONTHLY" -> loadMonthlyIssues()
            else -> finish()
        }
    }

    // 📰 DAILY PREVIOUS ISSUES (date + image)
    private fun loadDailyIssues() {

        firestore.collection("daily_newspapers")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No newspapers found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                val list = snapshot.documents.mapNotNull { doc ->
                    val ts = doc.getTimestamp("date")
                    val pdf = doc.getString("pdf_url")
                    val img = doc.getString("image_url")

                    if (ts != null && pdf != null) {
                        IssueModel(
                            title = sdf.format(ts.toDate()),
                            pdfUrl = pdf,
                            imageUrl = img
                        )
                    } else null
                }

                binding.recyclerView.adapter =
                    IssueAdapter(list) { issue ->
                        openPdf(issue.pdfUrl, issue.title)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load daily newspapers", e)
                Toast.makeText(this, "Failed to load newspapers", Toast.LENGTH_SHORT).show()
            }
    }

    // 📚 MONTHLY PREVIOUS ISSUES (month + image)
    private fun loadMonthlyIssues() {

        firestore.collection("monthly_magazine")
            .orderBy("order", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No magazines found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val month = doc.getString("month")
                    val pdf = doc.getString("pdf_url")
                    val img = doc.getString("image_url")

                    if (month != null && pdf != null) {
                        IssueModel(
                            title = month,
                            pdfUrl = pdf,
                            imageUrl = img
                        )
                    } else null
                }

                binding.recyclerView.adapter =
                    IssueAdapter(list) { issue ->
                        openPdf(issue.pdfUrl, issue.title)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load monthly magazines", e)
                Toast.makeText(this, "Failed to load magazines", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openPdf(pdfUrl: String, label: String) {
        val intent = Intent(this, PdfReaderActivity::class.java)
        intent.putExtra("pdf_url", pdfUrl)
        intent.putExtra("label", label)
        startActivity(intent)
    }
}
