package com.kidverse.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StoryListAdapter(
    private val list: List<StoryItem>,
    private val isPremium: Boolean,
    private val onPremiumRequired: () -> Unit,
    private val onClick: (StoryItem) -> Unit
) : RecyclerView.Adapter<StoryListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val story = list[position]

        holder.title.text = story.title

        holder.cover.loadFast(story.coverImage)

        // 🔒 New Premium Lock Logic
        val isLocked = !isPremium && story.isPremiumStory

        if (isLocked) {
            holder.lockOverlay.visibility = View.VISIBLE
        } else {
            holder.lockOverlay.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (isLocked) {
                onPremiumRequired()
            } else {
                onClick(story)
            }
        }
    }

    override fun getItemCount() = list.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cover: ImageView = v.findViewById(R.id.ivCover)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val lockOverlay: LinearLayout = v.findViewById(R.id.lockOverlay)
    }
}
