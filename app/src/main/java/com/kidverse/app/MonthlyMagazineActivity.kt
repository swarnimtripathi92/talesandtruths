package com.kidverse.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.kidverse.app.databinding.ActivityMonthlyMagazineBinding
import com.kidverse.app.databinding.ItemNewspaperSectionBinding

class MonthlyMagazineActivity : AppCompatActivity() {

    data class SectionMeta(val key: String, val page: String, val label: String)

    private lateinit var binding: ActivityMonthlyMagazineBinding
    private val firestore = FirebaseFirestore.getInstance()

    private val sectionPlan = listOf(
        SectionMeta("welcomeNote", "2", "From the Editor's Desk"),
        SectionMeta("contents", "3", "What’s Inside This Month?"),
        SectionMeta("topStoryOfMonth", "4", "Big News for Young Minds"),
        SectionMeta("amazingFacts", "5", "Did You Know?"),
        SectionMeta("laughZone", "6", "Jokes & Giggles"),
        SectionMeta("moralStory", "7", "Story Time"),
        SectionMeta("animalWorld", "8", "Creature of the Month"),
        SectionMeta("scienceCorner", "9", "Mini Scientist Lab"),
        SectionMeta("brainGym", "10", "Puzzle Planet"),
        SectionMeta("creativeCorner", "11", "Kids Talent Gallery"),
        SectionMeta("heroOfMonth", "12", "Young Achiever Spotlight"),
        SectionMeta("goodHabitOfMonth", "13", "Grow Better Every Day"),
        SectionMeta("exploreWorld", "14", "Country Explorer"),
        SectionMeta("challengePage", "15", "Mission for You!"),
        SectionMeta("funActivity", "16", "Make & Do"),
        SectionMeta("wordPower", "17", "Word of the Month"),
        SectionMeta("comicStrip", "18", "Adventures of [Mascot Name]"),
        SectionMeta("quizTime", "19", "How Much Did You Learn?"),
        SectionMeta("winnersPage", "20", "Star Kids"),
        SectionMeta("lettersFromReaders", "21", "Your Voice Matters"),
        SectionMeta("nextMonthSneakPeek", "22", "Coming Soon…"),
        SectionMeta("backCover", "Back", "Back Cover Activity")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyMagazineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val issueId = intent.getStringExtra("issue_id")
        if (issueId.isNullOrBlank()) {
            Toast.makeText(this, "Magazine not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadIssue(issueId)
    }

    private fun loadIssue(issueId: String) {
        firestore.collection("monthly_magazine")
            .document(issueId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Magazine not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val data = doc.data.orEmpty()
                val issue = DailyIssue.fromFirestore(data, "Monthly Magazine")
                val coverHook = data["cover_hook"]?.toString().orEmpty()
                renderIssue(issue, coverHook)

                GKTracker.recordGkRead(this, "current_monthly_${issue.displayDate}")
                GKTracker.recordGkRead("current_monthly_${issue.displayDate}")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load magazine", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun renderIssue(issue: DailyIssue, coverHook: String) {
        binding.txtIssueDate.text = issue.displayDate

        val note = buildString {
            if (coverHook.isNotBlank()) append("This Month: $coverHook")
            if (issue.editorNote.isNotBlank()) {
                if (isNotBlank()) append("\n\n")
                append("Editor Note: ${issue.editorNote}")
            }
        }
        binding.txtEditorNote.text = note

        if (!issue.coverUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(issue.coverUrl)
                .into(binding.imgCover)
        }

        binding.containerSections.removeAllViews()

        sectionPlan.forEach { sectionMeta ->
            val section = issue.sections[sectionMeta.key] ?: DailySection(sectionMeta.label, "")
            val sectionBinding = ItemNewspaperSectionBinding.inflate(
                LayoutInflater.from(this),
                binding.containerSections,
                false
            )

            val titleText = section.title.ifBlank { sectionMeta.label }
            sectionBinding.txtSectionTitle.text = "Page ${sectionMeta.page} • $titleText"
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
