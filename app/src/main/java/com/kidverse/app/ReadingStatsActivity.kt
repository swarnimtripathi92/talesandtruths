package com.kidverse.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.HashSet

class ReadingStatsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_stats)


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
                    cardId = R.id.cardToday,
                    title = "📅 Today",
                    timeSec = todayTime,
                    words = todayWords,
                    stories = todayStorySet.size
                )

                setupCard(
                    cardId = R.id.cardWeek,
                    title = "🗓️ Last 7 Days",
                    timeSec = weekTime,
                    words = weekWords,
                    stories = weekStorySet.size
                )

                setupCard(
                    cardId = R.id.cardAllTime,
                    title = "🏆 All Time",
                    timeSec = totalTime,
                    words = totalWords,
                    stories = totalStorySet.size
                )

                BadgeManager.refreshFromCloud()
            }
    }

    private fun setupCard(
        cardId: Int,
        title: String,
        timeSec: Int,
        words: Int,
        stories: Int
    ) {
        val card = findViewById<View>(cardId)

        val tvTitle = card.findViewById<TextView>(R.id.tvTitle)
        val tvStats = card.findViewById<TextView>(R.id.tvStats)

        tvTitle.text = title
        val minutes = timeSec / 60
        tvStats.text =
            "⏱️  Reading time   •   ${minutes} min\n" +
                    "📝  Total words   •   $words\n" +
                    "📚  Stories done  •   $stories"
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
