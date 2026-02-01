package com.kidverse.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GKImageAdapter(
    private val list: List<GKItem>,
    private val onClick: (GKItem) -> Unit
) : RecyclerView.Adapter<GKImageAdapter.GKViewHolder>() {

    class GKViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgGK)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GKViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gk_image_card, parent, false)
        return GKViewHolder(view)
    }

    override fun onBindViewHolder(holder: GKViewHolder, position: Int) {
        val item = list[position]

        holder.image.setImageResource(item.imageRes)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}
