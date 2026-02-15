package com.kidverse.app

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView

class BadgesAdapter(
    private val list: List<Badge>
) : RecyclerView.Adapter<BadgesAdapter.BadgeViewHolder>() {

    private val badgePalettes = listOf(
        "#FFF7FB" to "#FFE5F3",
        "#F4FBFF" to "#DDF3FF",
        "#F8FFF5" to "#E3F9D8",
        "#FFFDF5" to "#FFEFC9"
    )

    inner class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeCard: LinearLayout = itemView.findViewById(R.id.badgeCard)
        val tvIcon: TextView = itemView.findViewById(R.id.tvBadgeIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
        val tvSpark: TextView = itemView.findViewById(R.id.tvBadgeSpark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = list[position]

        holder.tvIcon.text = pickBadgeIcon(badge.title)
        holder.tvTitle.text = badge.title
        holder.tvSpark.text = pickSparkLine(position)

        val (startColor, endColor) = badgePalettes[position % badgePalettes.size]
        (holder.badgeCard.background.mutate() as? GradientDrawable)?.apply {
            colors = intArrayOf(startColor.toColorInt(), endColor.toColorInt())
        }
    }

    override fun getItemCount(): Int = list.size

    private fun pickBadgeIcon(title: String): String {
        val normalized = title.lowercase()
        return when {
            "word" in normalized -> "🧠"
            "book" in normalized || "story" in normalized || "read" in normalized -> "📚"
            "streak" in normalized || "daily" in normalized -> "🔥"
            else -> "🏆"
        }
    }

    private fun pickSparkLine(position: Int): String = when (position % 4) {
        0 -> "✨ Brilliant work"
        1 -> "🌈 Keep growing"
        2 -> "🚀 Superstar reader"
        else -> "🎉 Awesome progress"
    }
}
