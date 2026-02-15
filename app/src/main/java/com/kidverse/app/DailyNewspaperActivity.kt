package com.kidverse.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.kidverse.app.databinding.ActivityDailyNewspaperBinding
import com.kidverse.app.databinding.ItemNewspaperSectionBinding

class DailyNewspaperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyNewspaperBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyNewspaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val issueId = intent.getStringExtra("issue_id")
        if (issueId.isNullOrBlank()) {
            Toast.makeText(this, "Issue not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadIssue(issueId)
    }

    private fun loadIssue(issueId: String) {
        firestore.collection("daily_newspapers")
            .document(issueId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Issue not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val issue = DailyIssue.fromFirestore(doc.data.orEmpty())
                renderIssue(issue)

                GKTracker.recordGkRead(this, "current_daily_${issue.displayDate}")
                GKTracker.recordGkRead("current_daily_${issue.displayDate}")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load newspaper", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun renderIssue(issue: DailyIssue) {
        binding.txtIssueDate.text = issue.displayDate

        binding.txtEditorNote.text = if (issue.editorNote.isBlank()) "" else "Editor Note: ${issue.editorNote}"

        if (!issue.coverUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(issue.coverUrl)
                .into(binding.imgCover)
        }

        binding.containerSections.removeAllViews()

        issue.sectionsInOrder().forEach { section ->
            val sectionBinding = ItemNewspaperSectionBinding.inflate(
                LayoutInflater.from(this),
                binding.containerSections,
                false
            )

            sectionBinding.txtSectionTitle.text = section.title.ifBlank { "Section" }
            sectionBinding.txtSectionContent.text = section.content.ifBlank { "Coming soon..." }

            if (section.imageUrl.isBlank()) {
                sectionBinding.imgSection.visibility = View.GONE
            } else {
                sectionBinding.imgSection.visibility = View.VISIBLE
                Glide.with(this)
                    .load(section.imageUrl)
                    .into(sectionBinding.imgSection)
            }

            binding.containerSections.addView(sectionBinding.root)
        }

        binding.txtJoke.text = issue.jokeOfDay.ifBlank { "-" }
        binding.txtHabit.text = issue.goodHabit.ifBlank { "-" }
        binding.txtMoral.text = issue.moralStory.ifBlank { "-" }
    }
}
