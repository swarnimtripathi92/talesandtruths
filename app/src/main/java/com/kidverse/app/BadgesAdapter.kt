package com.kidverse.app

import android.graphics.Color
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
        "#FFFDF5" to "#FFEFC9",
        "#F3F6FF" to "#E1E8FF"
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

        val (startColor, endColor) = badgePalettes[position % badgePalettes.size]
        (holder.badgeCard.background.mutate() as? GradientDrawable)?.apply {
            colors = intArrayOf(startColor.toColorInt(), endColor.toColorInt())
        }

        if (badge.unlocked) {
            holder.tvIcon.text = badge.icon
            holder.tvTitle.text = badge.title
            holder.tvSpark.text = "${badge.subtitle} • ${badge.requiredStars}⭐"
            holder.badgeCard.alpha = 1f
            holder.tvTitle.setTextColor("#2B2B47".toColorInt())
            holder.tvSpark.setTextColor("#6B68A0".toColorInt())
        } else {
            holder.tvIcon.text = "🔒"
            holder.tvTitle.text = badge.title
            holder.tvSpark.text = "Unlock at ${badge.requiredStars} stars"
            holder.badgeCard.alpha = 0.7f
            holder.tvTitle.setTextColor(Color.parseColor("#5D5D74"))
            holder.tvSpark.setTextColor(Color.parseColor("#8080A0"))
        }
    }

    override fun getItemCount(): Int = list.size
}
