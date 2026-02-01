package com.kidverse.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.math.min

class ReadingGoalActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_goal)

        val etGoal = findViewById<EditText>(R.id.etGoalMinutes)
        val btnSave = findViewById<Button>(R.id.btnSaveGoal)
        val tvProgress = findViewById<TextView>(R.id.tvProgressText)
        val tvPercent = findViewById<TextView>(R.id.tvPercent)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)

        // Load saved goal
        val savedGoal = prefs.getInt("daily_goal_min", 0)
        if (savedGoal > 0) {
            etGoal.setText(savedGoal.toString())
        }

        btnSave.setOnClickListener {
            val goal = etGoal.text.toString().toIntOrNull() ?: 0
            prefs.edit().putInt("daily_goal_min", goal).apply()
            Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show()
            loadTodayProgress(goal, tvProgress, tvPercent, progressBar)
        }

        loadTodayProgress(savedGoal, tvProgress, tvPercent, progressBar)
    }

    private fun loadTodayProgress(
        goalMin: Int,
        tvProgress: TextView,
        tvPercent: TextView,
        progressBar: ProgressBar
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
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
                tvProgress.text = "$actualMin / $goalMin min"

                if (goalMin > 0) {
                    val percent = min(100, (actualMin * 100) / goalMin)
                    progressBar.progress = percent
                    tvPercent.text = "$percent%"
                } else {
                    progressBar.progress = 0
                    tvPercent.text = "0%"
                }
            }
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
