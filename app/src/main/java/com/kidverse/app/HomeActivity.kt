package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardFeatured = findViewById<MaterialCardView>(R.id.cardFeatured)
        val cardKeepReading = findViewById<MaterialCardView>(R.id.cardKeepReading)

        // 🌟 Featured
        cardFeatured.setOnClickListener {
            openRandomStory()
        }

        // 📚 Continue Reading
        cardKeepReading.setOnClickListener {
            handleKeepReading()
        }

        // 🐯 Moral
        findViewById<MaterialCardView>(R.id.cardMoral)
            .setOnClickListener {
                startActivity(Intent(this, MoralStoriesActivity::class.java))
            }

        // 🌙 Bedtime
        findViewById<MaterialCardView>(R.id.cardBedtime)
            .setOnClickListener {
                startActivity(Intent(this, BedtimeStoriesActivity::class.java))
            }

        // 🧠 GK
        findViewById<MaterialCardView>(R.id.cardKidsInfotainment)
            .setOnClickListener {
                startActivity(Intent(this, KidsInfotainmentActivity::class.java))
            }

        // 👑 Premium
        findViewById<MaterialCardView>(R.id.cardPremiumBanner)
            .setOnClickListener {
                PremiumBottomSheet().show(
                    supportFragmentManager,
                    "PremiumSheet"
                )
            }

        // 🔒 Parent Zone
        findViewById<MaterialCardView>(R.id.btnParentZone)
            .setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

        addPressEffect(cardFeatured)
        addPressEffect(cardKeepReading)

        updateReadingStats()
    }

    // --------------------------------------------
    // 🌟 RANDOM STORY
    // --------------------------------------------

    private fun openRandomStory() {
        db.collection("stories")
            .whereEqualTo("status", "published")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                val randomDoc = snapshot.documents.random()
                openStory(randomDoc.id)
            }
            .addOnFailureListener {
                showMessage("Unable to load story.")
            }
    }

    // --------------------------------------------
    // 📚 CONTINUE READING
    // --------------------------------------------

    private fun handleKeepReading() {

        val user = auth.currentUser

        if (user == null) {
            suggestLogin()
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val lastStoryId = doc.getString("lastStoryId")
                val completed =
                    doc.get("completedStories") as? List<String> ?: emptyList()

                val isPremium = doc.getBoolean("isPremium") ?: false

                if (!lastStoryId.isNullOrEmpty() &&
                    !completed.contains(lastStoryId)
                ) {
                    openStory(lastStoryId)
                } else {
                    loadNextStory(isPremium, completed)
                }
            }
            .addOnFailureListener {
                openRandomStory()
            }
    }

    private fun loadNextStory(
        isPremium: Boolean,
        completedStories: List<String>
    ) {

        db.collection("stories")
            .whereEqualTo("status", "published")
            .orderBy("updatedAt")
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) return@addOnSuccessListener

                val all = snapshot.documents

                val allowed = if (isPremium) {
                    all
                } else {
                    val moral = all
                        .filter { it.getString("category") == "moral" }
                        .take(5)

                    val bedtime = all
                        .filter { it.getString("category") == "bedtime" }
                        .take(5)

                    moral + bedtime
                }

                val next = allowed.firstOrNull {
                    !completedStories.contains(it.id)
                }

                if (next != null) {
                    openStory(next.id)
                } else {
                    openStory(allowed.first().id)
                }
            }
            .addOnFailureListener {
                openRandomStory()
            }
    }

    private fun openStory(storyId: String) {
        val intent = Intent(this, StoryReaderActivity::class.java)
        intent.putExtra("storyId", storyId)
        startActivity(intent)
    }

    // --------------------------------------------
    // 🔥 WEEKLY READING STATS
    // --------------------------------------------

    private fun updateReadingStats() {

        val tvStats = findViewById<TextView>(R.id.tvReadingStats)
        val user = auth.currentUser ?: return

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        db.collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .whereGreaterThan("readAt", startOfWeek)
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                tvStats.text = "🔥 You read $count stories this week"
            }
    }

    // --------------------------------------------
    // 👤 LOGIN PROMPT
    // --------------------------------------------

    private fun suggestLogin() {
        AlertDialog.Builder(this)
            .setTitle("Login Required")
            .setMessage("Login to continue reading where you left off.")
            .setPositiveButton("Login") { _, _ ->
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            .setNegativeButton("Continue as Guest") { _, _ ->
                openRandomStory()
            }
            .show()
    }

    // --------------------------------------------
    // ✨ PRESS EFFECT
    // --------------------------------------------

    private fun addPressEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    v.animate().scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(80)
                        .start()

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()
            }
            false
        }
    }

    private fun showMessage(msg: String) {
        android.widget.Toast.makeText(
            this,
            msg,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
