package com.kidverse.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReadingHistoryActivity : AppCompatActivity() {

    private val historyList = mutableListOf<ReadingHistoryItem>()
    private lateinit var adapter: ReadingHistoryAdapter

    private lateinit var tvSessionsCount: TextView
    private lateinit var tvMinutesCount: TextView
    private lateinit var tvWordsCount: TextView
    private lateinit var tvStreakSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_history)

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)

        tvSessionsCount = findViewById(R.id.tvSessionsCount)
        tvMinutesCount = findViewById(R.id.tvMinutesCount)
        tvWordsCount = findViewById(R.id.tvWordsCount)
        tvStreakSummary = findViewById(R.id.tvStreakSummary)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ReadingHistoryAdapter(historyList)
        recycler.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmpty(tvEmpty, recycler, "Please sign in to see reading history")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .orderBy("readAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                historyList.clear()

                if (snapshot.isEmpty) {
                    showEmpty(tvEmpty, recycler)
                    updateInsights(emptyList())
                    return@addOnSuccessListener
                }

                for (doc in snapshot.documents) {
                    val item = doc.toObject(ReadingHistoryItem::class.java) ?: continue
                    if (item.readDurationSec <= 0 && item.wordsRead <= 0) continue
                    historyList.add(item)
                }

                updateInsights(historyList)
                if (historyList.isEmpty()) {
                    showEmpty(tvEmpty, recycler)
                } else {
                    tvEmpty.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                showEmpty(tvEmpty, recycler, "Unable to load reading history")
                updateInsights(emptyList())
            }
    }

    private fun updateInsights(items: List<ReadingHistoryItem>) {
        val sessions = items.size
        val totalMinutes = items.sumOf { maxOf(1, it.readDurationSec / 60) }
        val totalWords = items.sumOf { it.wordsRead }
        val thisWeekSessions = items.count { isWithinLastDays(it.readAt, 7) }
        val streak = calculateStreak(items)

        tvSessionsCount.text = "$sessions Sessions"
        tvMinutesCount.text = "$totalMinutes Min"
        tvWordsCount.text = "$totalWords Words"
        tvStreakSummary.text = "🔥 Streak: $streak day | 📅 This Week: $thisWeekSessions sessions"
    }

    private fun calculateStreak(items: List<ReadingHistoryItem>): Int {
        if (items.isEmpty()) return 0

        val dayTimestamps = items.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.readAt
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        var expected = dayTimestamps.first()

        for (day in dayTimestamps) {
            if (day == expected) {
                streak++
                expected -= TimeUnit.DAYS.toMillis(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun isWithinLastDays(timestamp: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        return now - timestamp <= TimeUnit.DAYS.toMillis(days.toLong())
    }

    private fun showEmpty(
        tvEmpty: TextView,
        recycler: RecyclerView,
        msg: String = "No reading history yet 📖"
    ) {
        tvEmpty.visibility = View.VISIBLE
        tvEmpty.text = msg
        recycler.visibility = View.GONE
    }
}
