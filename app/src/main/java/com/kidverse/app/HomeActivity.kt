package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var featuredStoryId: String? = null

    private val prefs by lazy {
        getSharedPreferences("kidverse_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardFeatured = findViewById<MaterialCardView>(R.id.cardFeatured)
        val cardKeepReading = findViewById<MaterialCardView>(R.id.cardKeepReading)

        // 🌟 Featured
        cardFeatured.setOnClickListener {
            animateCard(cardFeatured)
            featuredStoryId?.let { openStory(it) }
        }

        // 📚 Continue Reading (INSTANT)
        cardKeepReading.setOnClickListener {
            animateCard(cardKeepReading)
            handleKeepReading()
        }
        findViewById<TextView>(R.id.tvHeaderCta)
            .setOnClickListener {
                startActivity(Intent(this, KidsStoriesActivity::class.java))
            }
        findViewById<MaterialCardView>(R.id.cardMoral)
            .setOnClickListener {
                startActivity(Intent(this, MoralStoriesActivity::class.java))
            }

        findViewById<MaterialCardView>(R.id.cardBedtime)
            .setOnClickListener {
                startActivity(Intent(this, BedtimeStoriesActivity::class.java))
            }

        findViewById<MaterialCardView>(R.id.cardKidsInfotainment)
            .setOnClickListener {
                startActivity(Intent(this, KidsInfotainmentActivity::class.java))
            }

        findViewById<MaterialCardView>(R.id.cardPremiumBanner)
            .setOnClickListener {
                PremiumBottomSheet().show(
                    supportFragmentManager,
                    "PremiumSheet"
                )
            }

        findViewById<MaterialCardView>(R.id.cardWelcomeKid)
            .setOnClickListener {
                startActivity(Intent(this, BadgesActivity::class.java))
            }

        findViewById<MaterialCardView>(R.id.btnParentZone)
            .setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
            }

        loadFeaturedStory()
        updateReadingStats()
        loadWelcomeCard()
    }

    // --------------------------------------------
    // ✨ Animation
    // --------------------------------------------

    private fun animateCard(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
            }
            .start()
    }

    // --------------------------------------------
    // 🌟 Daily Featured
    // --------------------------------------------

    private fun loadFeaturedStory() {

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            loadFeaturedPreview(false)
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val isUserPremium =
                    userSnapshot.getBoolean("isPremium") ?: false
                loadFeaturedPreview(isUserPremium)
            }
            .addOnFailureListener {
                loadFeaturedPreview(false)
            }
    }

    private fun loadFeaturedPreview(isUserPremium: Boolean) {

        var query = db.collection("stories")
            .whereEqualTo("status", "published")
            .whereEqualTo("featured", true)

        if (!isUserPremium) {
            query = query.whereEqualTo("isPremiumStory", false)
        }

        query.get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) return@addOnSuccessListener

                val docs = snapshot.documents
                val todayKey = getTodayKey()
                val index = abs(todayKey.hashCode()) % docs.size
                val dailyDoc = docs[index]

                featuredStoryId = dailyDoc.id

                val title = dailyDoc.getString("title") ?: ""
                val cover = dailyDoc.getString("coverImage") ?: ""

                findViewById<TextView>(R.id.tvFeaturedTitle).text = title

                Glide.with(this)
                    .load(cover)
                    .centerCrop()
                    .into(findViewById(R.id.ivFeaturedCover))
            }
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}_${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    // --------------------------------------------
    // 📖 Continue Reading (NEXT LEVEL FAST)
    // --------------------------------------------

    private fun handleKeepReading() {

        val lastStoryId = prefs.getString("lastReadStoryId", null)

        if (lastStoryId != null) {
            openStory(lastStoryId)
        } else {
            showStartReadingDialog()
        }
    }

    private fun showStartReadingDialog() {
        AlertDialog.Builder(this)
            .setTitle("No story yet 📖")
            .setMessage("Start reading a story first 😊")
            .setPositiveButton("Explore Stories") { _, _ ->
                startActivity(Intent(this, KidsStoriesActivity::class.java))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openStory(storyId: String) {
        val intent = Intent(this, StoryReaderActivity::class.java)
        intent.putExtra("storyId", storyId)
        startActivity(intent)
    }

    // --------------------------------------------
    // 🔥 Preview (Fast Local First)
    // --------------------------------------------

    private fun updateReadingStats() {

        val tvLast = findViewById<TextView>(R.id.tvLastStory)
        val tvTitle = findViewById<TextView>(R.id.tvContinueTitle)
        val imageView = findViewById<ImageView>(R.id.ivContinueIcon)

        val lastStoryId = prefs.getString("lastReadStoryId", null)

        if (lastStoryId != null) {
            loadStoryPreview(lastStoryId)
        } else {
            tvLast.visibility = View.GONE
            tvTitle.text = "Start Your First Story 📖"
        }
    }
    override fun onResume() {
        super.onResume()
        updateReadingStats()
        loadWelcomeCard()
    }

    private fun loadStoryPreview(storyId: String) {

        db.collection("stories")
            .document(storyId)
            .get()
            .addOnSuccessListener { storyDoc ->

                val title = storyDoc.getString("title") ?: return@addOnSuccessListener
                val cover = storyDoc.getString("coverImage") ?: ""

                prefs.edit()
                    .putString("lastReadStoryTitle", title)
                    .putString("lastReadStoryCover", cover)
                    .apply()

                findViewById<TextView>(R.id.tvLastStory).apply {
                    text = "Last: $title"
                    visibility = View.VISIBLE
                }

                Glide.with(this)
                    .load(cover)
                    .centerCrop()
                    .into(findViewById(R.id.ivContinueIcon))
            }
    }


    private fun loadWelcomeCard() {
        val tvWelcomeTitle = findViewById<TextView>(R.id.tvWelcomeTitle)
        val tvWelcomeMeta = findViewById<TextView>(R.id.tvWelcomeMeta)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            tvWelcomeTitle.text = "Welcome Kid"
            tvWelcomeMeta.text = "🏅 Story Spark • 0 stars"
            return
        }

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val childProfile = doc.get("childProfile") as? Map<*, *>
                val childName = (childProfile?.get("childName") as? String)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Kid"

                val storyStars = BadgeManager.storyWordsToStars((doc.getLong("storyWords") ?: 0L).toInt())
                val gkStars = (doc.getLong("gkStars") ?: 0L).toInt()
                val totalStars = (doc.getLong("totalStars") ?: (storyStars + gkStars).toLong()).toInt()
                val badgeTitle = doc.getString("currentBadge") ?: "Story Spark"

                tvWelcomeTitle.text = "Welcome $childName"
                tvWelcomeMeta.text = "🏅 $badgeTitle • $totalStars stars"
            }
            .addOnFailureListener {
                tvWelcomeTitle.text = "Welcome Kid"
                tvWelcomeMeta.text = "🏅 Story Spark • 0 stars"
            }
    }

}
