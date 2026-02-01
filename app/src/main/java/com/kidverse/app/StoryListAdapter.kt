package com.kidverse.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StoryListAdapter(
    private val list: List<StoryItem>,
    private val onClick: (StoryItem) -> Unit,
    private val onLongClick: (StoryItem) -> Unit
) : RecyclerView.Adapter<StoryListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val story = list[position]

        holder.title.text = story.title

        Glide.with(holder.itemView.context)
            .load(story.coverImage)
            .centerCrop()
            .into(holder.cover)

        holder.itemView.setOnClickListener { onClick(story) }
        holder.itemView.setOnLongClickListener {
            onLongClick(story)
            true
        }
    }

    override fun getItemCount() = list.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cover: ImageView = v.findViewById(R.id.ivCover)
        val title: TextView = v.findViewById(R.id.tvTitle)
    }
}
