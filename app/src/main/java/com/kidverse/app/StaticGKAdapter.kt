package com.kidverse.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class StaticGKAdapter(
    private val list: List<StaticGKCategory>,
    private val onClick: (StaticGKCategory) -> Unit
) : RecyclerView.Adapter<StaticGKAdapter.GKViewHolder>() {

    class GKViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GKViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_static_gk_image, parent, false)
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
