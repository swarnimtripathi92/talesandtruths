package com.example.talesandtruths

import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class StoryContentAdapter(
    private val items: List<ContentBlock>,
    private var textSize: Float = 18f,
    private var typeface: Typeface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == "image") TYPE_IMAGE else TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_IMAGE) {
            ImageVH(inflater.inflate(R.layout.item_story_image, parent, false))
        } else {
            TextVH(inflater.inflate(R.layout.item_story_text, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val block = items[position]
        if (holder is TextVH) {
            holder.bind(block.value, textSize, typeface)
        } else if (holder is ImageVH) {
            holder.bind(block.value)
        }
    }

    override fun getItemCount(): Int = items.size

    // 🔠 A+ / A-
    fun updateTextSize(size: Float) {
        textSize = size
        notifyItemRangeChanged(0, itemCount)
    }

    // 🔤 Font switch
    fun updateFont(tf: Typeface) {
        typeface = tf
        notifyItemRangeChanged(0, itemCount)
    }

    // ---------------- VIEW HOLDERS ----------------

    class TextVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvStoryText)

        fun bind(text: String, size: Float, tf: Typeface) {
            tvText.text = text
            tvText.textSize = size
            tvText.typeface = tf

            // Line spacing
            tvText.setLineSpacing(0f, 1.4f)

            // Justification (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tvText.justificationMode =
                    android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
            }
        }
    }

    class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.imgStory)

        fun bind(url: String) {
            Glide.with(img.context)
                .load(url)
                .centerCrop()
                .into(img)
        }
    }
}
