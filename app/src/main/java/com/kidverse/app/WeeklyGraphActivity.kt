package com.kidverse.app

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class WeeklyGraphActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_graph)

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val barChart = findViewById<BarChart>(R.id.barChart)
        val tvStoriesBreakdown = findViewById<TextView>(R.id.tvStoriesBreakdown)
        val tvGkBreakdown = findViewById<TextView>(R.id.tvGkBreakdown)
        val user = FirebaseAuth.getInstance().currentUser ?: return

        setupPieChartStyle(pieChart)
        setupBarChartStyle(barChart)

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .get()
            .addOnSuccessListener { snap ->
                val weeklyMinutes = IntArray(7)
                val categoryMinutes = mutableMapOf(
                    "Moral Stories" to 0f,
                    "Bedtime Stories" to 0f,
                    "Static GK" to 0f,
                    "Current GK" to 0f
                )

                for (doc in snap.documents) {
                    val item = doc.toObject(ReadingHistoryItem::class.java) ?: continue
                    if (item.readDurationSec <= 0) continue

                    val minutes = item.readDurationSec / 60f
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = item.readAt
                    val dayIndex = mapCalendarDayToMonStart(cal.get(Calendar.DAY_OF_WEEK))
                    weeklyMinutes[dayIndex] += minutes.toInt()

                    val bucket = resolveCategoryBucket(item.category)
                    if (bucket != null) {
                        categoryMinutes[bucket] = (categoryMinutes[bucket] ?: 0f) + minutes
                    }
                }

                bindPieChartData(pieChart, categoryMinutes)
                bindBarChartData(barChart, weeklyMinutes)
                bindBreakdownSummary(tvStoriesBreakdown, tvGkBreakdown, categoryMinutes)
            }
    }

    private fun setupPieChartStyle(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 63f
        pieChart.setCenterText("Reading Mix")
        pieChart.setCenterTextSize(14f)
        pieChart.setCenterTextColor(Color.parseColor("#273043"))
        pieChart.legend.textColor = Color.parseColor("#4A5568")
        pieChart.legend.textSize = 12f
        pieChart.legend.isWordWrapEnabled = true
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(11f)
        pieChart.setNoDataText("Abhi category-wise reading data available nahi hai")
    }

    private fun setupBarChartStyle(barChart: BarChart) {
        barChart.description.isEnabled = false
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setFitBars(true)
        barChart.animateY(900)
        barChart.setExtraOffsets(8f, 8f, 8f, 12f)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.parseColor("#4A5568")
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        )

        barChart.axisLeft.apply {
            axisMinimum = 0f
            textColor = Color.parseColor("#4A5568")
            gridColor = Color.parseColor("#E2E8F0")
        }

        barChart.setNoDataText("Abhi weekly reading data available nahi hai")
    }

    private fun bindPieChartData(pieChart: PieChart, categoryMinutes: Map<String, Float>) {
        val entries = categoryMinutes
            .filterValues { it > 0f }
            .map { PieEntry(it.value, it.key) }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "No data"
            pieChart.invalidate()
            return
        }

        val storiesTotal = (categoryMinutes["Moral Stories"] ?: 0f) +
                (categoryMinutes["Bedtime Stories"] ?: 0f)
        val gkTotal = (categoryMinutes["Static GK"] ?: 0f) +
                (categoryMinutes["Current GK"] ?: 0f)
        pieChart.centerText = "Stories ${storiesTotal.toInt()}m\nGK ${gkTotal.toInt()}m"

        val dataSet = PieDataSet(entries, "Category split")
        dataSet.colors = listOf(
            Color.parseColor("#7C3AED"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#0EA5E9"),
            Color.parseColor("#22C55E")
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 8f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun bindBreakdownSummary(
        tvStoriesBreakdown: TextView,
        tvGkBreakdown: TextView,
        categoryMinutes: Map<String, Float>
    ) {
        val moral = (categoryMinutes["Moral Stories"] ?: 0f).toInt()
        val bedtime = (categoryMinutes["Bedtime Stories"] ?: 0f).toInt()
        val staticGk = (categoryMinutes["Static GK"] ?: 0f).toInt()
        val currentGk = (categoryMinutes["Current GK"] ?: 0f).toInt()

        tvStoriesBreakdown.text = "Moral ${moral}m • Bedtime ${bedtime}m"
        tvGkBreakdown.text = "Static ${staticGk}m • Current ${currentGk}m"
    }

    private fun bindBarChartData(barChart: BarChart, weeklyMinutes: IntArray) {
        val entries = weeklyMinutes.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Minutes Read")
        dataSet.color = Color.parseColor("#38BDF8")
        dataSet.valueTextColor = Color.parseColor("#334155")
        dataSet.valueTextSize = 11f

        val data = BarData(dataSet)
        data.barWidth = 0.56f

        barChart.data = data
        barChart.invalidate()
    }

    private fun mapCalendarDayToMonStart(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }

    private fun resolveCategoryBucket(category: String): String? {
        val normalized = category.lowercase(Locale.ROOT)
        return when {
            normalized.contains("moral") -> "Moral Stories"
            normalized.contains("bedtime") || normalized.contains("night") -> "Bedtime Stories"
            normalized.contains("current") && normalized.contains("gk") -> "Current GK"
            normalized.contains("static") && normalized.contains("gk") -> "Static GK"
            normalized.contains("gk") && normalized.contains("current") -> "Current GK"
            normalized.contains("gk") -> "Static GK"
            else -> null
        }
    }
}
