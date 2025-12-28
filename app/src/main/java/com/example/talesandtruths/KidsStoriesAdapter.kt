package com.example.talesandtruths

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class KidsStoriesAdapter(
    private val stories: List<KidsStory>
) : RecyclerView.Adapter<KidsStoriesAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.imgCover)
        val titleText: TextView = itemView.findViewById(R.id.txtTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kids_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]

        holder.titleText.text = story.title

        val imageUrl = story.coverImageUrl

        if (imageUrl.isNotBlank()) {
            // ✅ REAL COVER IMAGE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_story)
                .error(R.drawable.placeholder_story)
                .into(holder.coverImage)
        } else {
            // ✅ NO COVER IMAGE → SHOW PLACEHOLDER
            holder.coverImage.setImageResource(R.drawable.placeholder_story)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, StoryDetailActivity::class.java)
            intent.putExtra("storyId", story.id)
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = stories.size
}
