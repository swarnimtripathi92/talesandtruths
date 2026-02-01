package com.kidverse.app

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ContentAdapter(
    private val blocks: MutableList<ContentBlock>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onDeleteClick: ((ContentBlock, Int) -> Unit)? = null

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }
    // 🔁 Reorder support
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val item = blocks.removeAt(fromPosition)
        blocks.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun getItemViewType(position: Int): Int {
        return if (blocks[position].type == "text") TYPE_TEXT else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_block_text, parent, false)
            TextVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_block_image, parent, false)
            ImageVH(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val block = blocks[position]

        if (holder is TextVH) {

            // 🛑 remove old watcher
            holder.editText.tag?.let {
                holder.editText.removeTextChangedListener(it as TextWatcher)
            }

            holder.editText.setText(block.value)

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        blocks[pos].value = s.toString()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            holder.editText.addTextChangedListener(watcher)
            holder.editText.tag = watcher

            holder.delete.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteClick?.invoke(blocks[pos], pos)
                }
            }
        }

        if (holder is ImageVH) {
            if (block.value.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(block.value)
                    .into(holder.image)
            }

            holder.delete.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDeleteClick?.invoke(blocks[pos], pos)
                }
            }
        }
    }

    override fun getItemCount(): Int = blocks.size

    class TextVH(view: View) : RecyclerView.ViewHolder(view) {
        val editText: EditText = view.findViewById(R.id.etBlockText)
        val delete: ImageView = view.findViewById(R.id.ivDelete)
    }

    class ImageVH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivBlockImage)
        val delete: ImageView = view.findViewById(R.id.ivDelete)
    }
}
