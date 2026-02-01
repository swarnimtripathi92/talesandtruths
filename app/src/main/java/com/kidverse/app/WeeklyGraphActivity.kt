package com.kidverse.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class WeeklyGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_graph)

        val chart = findViewById<BarChart>(R.id.barChart)
        val user = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .get()
            .addOnSuccessListener { snap ->

                val map = mutableMapOf<Int, Int>() // day → minutes

                for (doc in snap.documents) {
                    val item = doc.toObject(ReadingHistoryItem::class.java) ?: continue
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = item.readAt
                    val day = cal.get(Calendar.DAY_OF_WEEK)
                    map[day] = (map[day] ?: 0) + item.readDurationSec / 60
                }

                val entries = map.map {
                    BarEntry(it.key.toFloat(), it.value.toFloat())
                }

                val dataSet = BarDataSet(entries, "Minutes Read")
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
    }
}
