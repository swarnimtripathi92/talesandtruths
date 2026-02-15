package com.kidverse.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class ReadingHistoryAdapter(
    private val list: List<ReadingHistoryItem>
) : RecyclerView.Adapter<ReadingHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvPower: TextView = itemView.findViewById(R.id.tvPower)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.storyTitle
        holder.tvCategory.text = item.category.ifBlank { "General" }

        val minutes = maxOf(1, item.readDurationSec / 60)
        holder.tvMeta.text = "⏱️ $minutes min • 📝 ${item.wordsRead} words"
        holder.tvDate.text = formatDate(item.readAt)
        holder.tvPower.text = readingPowerMessage(item)
    }

    override fun getItemCount(): Int = list.size

    private fun readingPowerMessage(item: ReadingHistoryItem): String {
        val score = min(100, (item.wordsRead / 15) + (item.readDurationSec / 30))
        return when {
            score >= 80 -> "⚡ Reading Power: Outstanding focus!"
            score >= 50 -> "✨ Reading Power: Great consistency today!"
            else -> "🌱 Reading Power: Nice start, keep going!"
        }
    }

    private fun formatDate(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }
}
