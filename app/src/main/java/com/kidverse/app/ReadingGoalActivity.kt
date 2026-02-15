package com.kidverse.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class ReadingGoalActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var etGoal: EditText
    private lateinit var tvProgress: TextView
    private lateinit var tvPercent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvStreak: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_goal)

        etGoal = findViewById(R.id.etGoalMinutes)
        val btnSave = findViewById<Button>(R.id.btnSaveGoal)
        val btnQuick10 = findViewById<Button>(R.id.btnQuick10)
        val btnQuick20 = findViewById<Button>(R.id.btnQuick20)
        val btnQuick30 = findViewById<Button>(R.id.btnQuick30)
        tvProgress = findViewById(R.id.tvProgressText)
        tvPercent = findViewById(R.id.tvPercent)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvStatus = findViewById(R.id.tvStatus)
        tvStreak = findViewById(R.id.tvStreak)
        progressBar = findViewById(R.id.progressBar)

        prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)

        val savedGoal = prefs.getInt("daily_goal_min", 20)
        etGoal.setText(savedGoal.toString())

        btnQuick10.setOnClickListener { etGoal.setText("10") }
        btnQuick20.setOnClickListener { etGoal.setText("20") }
        btnQuick30.setOnClickListener { etGoal.setText("30") }

        btnSave.setOnClickListener {
            val parsedGoal = etGoal.text.toString().toIntOrNull()
            if (parsedGoal == null || parsedGoal < 5 || parsedGoal > 180) {
                Toast.makeText(this, "Please enter a goal between 5 and 180 minutes.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putInt("daily_goal_min", parsedGoal).apply()
            Toast.makeText(this, "Awesome! Daily goal saved.", Toast.LENGTH_SHORT).show()
            loadTodayProgress(parsedGoal)
        }

        loadTodayProgress(savedGoal)
    }

    private fun loadTodayProgress(goalMin: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            renderProgress(goalMin, 0)
            tvStatus.text = "Sign in to sync reading progress across devices."
            return
        }

        val todayStart = getStartOfToday()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .whereGreaterThanOrEqualTo("readAt", todayStart)
            .get()
            .addOnSuccessListener { snapshot ->
                var totalSec = 0
                for (doc in snapshot.documents) {
                    val sec = doc.getLong("readDurationSec") ?: 0
                    totalSec += sec.toInt()
                }

                val actualMin = totalSec / 60
                renderProgress(goalMin, actualMin)
            }
            .addOnFailureListener {
                renderProgress(goalMin, 0)
                tvStatus.text = "Unable to fetch today’s progress right now."
            }
    }

    private fun renderProgress(goalMin: Int, actualMin: Int) {
        val safeGoal = if (goalMin > 0) goalMin else 20
        val percent = min(100, (actualMin * 100) / safeGoal)
        val remaining = (safeGoal - actualMin).coerceAtLeast(0)

        progressBar.progress = percent
        tvPercent.text = "$percent%"
        tvProgress.text = "$actualMin / $safeGoal min"
        tvRemaining.text = if (remaining > 0) {
            "⏳ $remaining minutes remaining today"
        } else {
            "🎉 Goal achieved! Keep exploring more stories."
        }

        tvStatus.text = when {
            percent >= 100 -> "Superstar! Goal completed for today ✨"
            percent >= 75 -> "Almost there! A short reading session will finish it 💪"
            percent >= 40 -> "Great momentum! Keep going 📖"
            actualMin > 0 -> "Lovely start! Let’s continue the reading adventure 🌈"
            else -> "Settle in with a story and begin today’s streak 🌟"
        }

        val streak = updateStreak(safeGoal, actualMin)
        val bestStreak = prefs.getInt("best_streak", 0)
        tvStreak.text = "🔥 Streak: $streak day(s)   •   Best: $bestStreak"
    }

    private fun updateStreak(goalMin: Int, actualMin: Int): Int {
        val todayKey = getTodayKey()
        val lastUpdatedDay = prefs.getString("streak_last_updated_day", "") ?: ""

        var streak = prefs.getInt("streak_count", 0)
        if (lastUpdatedDay == todayKey) {
            return streak
        }

        val lastMetDay = prefs.getString("streak_last_met_day", "") ?: ""
        if (actualMin >= goalMin) {
            streak = if (isYesterday(lastMetDay, todayKey)) streak + 1 else 1
            prefs.edit()
                .putInt("streak_count", streak)
                .putString("streak_last_met_day", todayKey)
                .putString("streak_last_updated_day", todayKey)
                .putInt("best_streak", maxOf(streak, prefs.getInt("best_streak", 0)))
                .apply()
        } else {
            prefs.edit().putString("streak_last_updated_day", todayKey).apply()
        }

        return prefs.getInt("streak_count", streak)
    }

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().time)
    }

    private fun isYesterday(previousDay: String, currentDay: String): Boolean {
        if (previousDay.isBlank() || currentDay.isBlank()) return false

        val format = SimpleDateFormat("yyyyMMdd", Locale.US)
        val prev = format.parse(previousDay) ?: return false
        val curr = format.parse(currentDay) ?: return false

        val cal = Calendar.getInstance()
        cal.time = curr
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return format.format(cal.time) == format.format(prev)
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
