package com.kidverse.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.HashSet

class ReadingStatsActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_stats)

        rootLayout = findViewById(R.id.rootLayout)

        val user = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .orderBy("readAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val now = System.currentTimeMillis()
                val todayStart = getStartOfToday()
                val weekStart = now - 7 * 24 * 60 * 60 * 1000L

                var todayTime = 0
                var todayWords = 0
                val todayStorySet = HashSet<String>()

                var weekTime = 0
                var weekWords = 0
                val weekStorySet = HashSet<String>()

                var totalTime = 0
                var totalWords = 0
                val totalStorySet = HashSet<String>()

                for (doc in snapshot.documents) {
                    val item = doc.toObject(ReadingHistoryItem::class.java) ?: continue
                    val storyId = item.storyId

                    // 🏆 ALL TIME
                    totalTime += item.readDurationSec
                    totalWords += item.wordsRead
                    totalStorySet.add(storyId)

                    // 🗓️ LAST 7 DAYS
                    if (item.readAt >= weekStart) {
                        weekTime += item.readDurationSec
                        weekWords += item.wordsRead
                        weekStorySet.add(storyId)
                    }

                    // 📅 TODAY
                    if (item.readAt >= todayStart) {
                        todayTime += item.readDurationSec
                        todayWords += item.wordsRead
                        todayStorySet.add(storyId)
                    }
                }

                setupCard(
                    position = 1,
                    title = "📅 Today",
                    timeSec = todayTime,
                    words = todayWords,
                    stories = todayStorySet.size
                )

                setupCard(
                    position = 2,
                    title = "🗓️ Last 7 Days",
                    timeSec = weekTime,
                    words = weekWords,
                    stories = weekStorySet.size
                )

                setupCard(
                    position = 3,
                    title = "🏆 All Time",
                    timeSec = totalTime,
                    words = totalWords,
                    stories = totalStorySet.size
                )

                // 🏆 BADGES (based on unique stories & totals)
                BadgeManager.checkAndUnlockBadges(
                    totalStories = totalStorySet.size,
                    totalMinutes = totalTime / 60,
                    totalWords = totalWords
                )
            }
    }

    private fun setupCard(
        position: Int,
        title: String,
        timeSec: Int,
        words: Int,
        stories: Int
    ) {
        val card = rootLayout.getChildAt(position) as LinearLayout

        val tvTitle = card.findViewById<TextView>(R.id.tvTitle)
        val tvStats = card.findViewById<TextView>(R.id.tvStats)

        tvTitle.text = title
        tvStats.text =
            "⏱️ ${timeSec / 60} min\n" +
                    "📝 $words words\n" +
                    "📚 $stories stories"
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
