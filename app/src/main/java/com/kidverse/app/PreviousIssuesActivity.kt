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
import java.text.SimpleDateFormat
import com.kidverse.app.model.IssueModel
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

                    if (ts != null) {
                        IssueModel(
                            issueId = doc.id,
                            title = sdf.format(ts.toDate()),
                            pdfUrl = pdf,
                            type = "DAILY",
                            imageUrl = img
                        )
                    } else null
                }

                binding.recyclerView.adapter =
                    IssueAdapter(list) { issue ->
                        openIssue(issue)
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

                    if (month != null) {
                        IssueModel(
                            issueId = doc.id,
                            title = month,
                            pdfUrl = pdf,
                            type = "MONTHLY",
                            imageUrl = img
                        )
                    } else null
                }

                binding.recyclerView.adapter =
                    IssueAdapter(list) { issue ->
                        openIssue(issue)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load monthly magazines", e)
                Toast.makeText(this, "Failed to load magazines", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openIssue(issue: IssueModel) {
        if (issue.type == "DAILY") {
            startActivity(
                Intent(this, DailyNewspaperActivity::class.java)
                    .putExtra("issue_id", issue.issueId)
            )
            return
        }

        if (issue.type == "MONTHLY") {
            startActivity(
                Intent(this, MonthlyMagazineActivity::class.java)
                    .putExtra("issue_id", issue.issueId)
            )
            return
        }

        val pdfUrl = issue.pdfUrl
        if (!pdfUrl.isNullOrBlank()) {
            startActivity(
                Intent(this, PdfReaderActivity::class.java)
                    .putExtra("pdf_url", pdfUrl)
                    .putExtra("label", issue.title)
            )
        }
    }
}
