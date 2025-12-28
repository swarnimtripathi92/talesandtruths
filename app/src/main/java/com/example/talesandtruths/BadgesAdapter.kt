package com.example.talesandtruths

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BadgesAdapter(
    private val list: List<Badge>
) : RecyclerView.Adapter<BadgesAdapter.BadgeViewHolder>() {

    inner class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcon: TextView = itemView.findViewById(R.id.tvBadgeIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = list[position]

        // Emoji already included in title if you want
        holder.tvIcon.text = "🏆"
        holder.tvTitle.text = badge.title
    }

    override fun getItemCount(): Int = list.size
}
