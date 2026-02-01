package com.kidverse.app

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StoryContentAdapter(
    private val blocks: List<ContentBlock>,
    private var textSize: Float,
    private var font: Typeface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }

    private var highlightedPos = -1

    override fun getItemViewType(position: Int): Int {
        return if (blocks[position].type == "image") TYPE_IMAGE else TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_text, parent, false)
            TextVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_image, parent, false)
            ImageVH(view)
        }
    }

    override fun getItemCount(): Int = blocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val block = blocks[position]

        if (holder is TextVH) {
            holder.bind(block.value, textSize, font, position == highlightedPos)
        } else if (holder is ImageVH) {
            holder.bind(block.value)
        }
    }

    // 🔴 PUBLIC METHOD — THIS WAS MISSING
    fun highlight(position: Int) {
        highlightedPos = position
        notifyDataSetChanged()
    }

    // 🔠 TEXT VIEW HOLDER
    inner class TextVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.tvText)

        fun bind(
            text: String,
            size: Float,
            typeface: Typeface,
            highlighted: Boolean
        ) {
            tv.text = text
            tv.textSize = size
            tv.typeface = typeface

            if (highlighted) {
                tv.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.ttsHighlight)
                )
            } else {
                tv.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    // 🖼 IMAGE VIEW HOLDER
    inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.imgStory)

        fun bind(url: String) {
            Glide.with(itemView.context)
                .load(url)
                .into(img)
        }
    }

    // 🔠 UPDATE METHODS
    fun updateTextSize(newSize: Float) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun updateFont(newFont: Typeface) {
        font = newFont
        notifyDataSetChanged()
    }
}
