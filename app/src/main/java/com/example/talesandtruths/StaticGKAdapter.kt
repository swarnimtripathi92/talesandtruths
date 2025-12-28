package com.example.talesandtruths

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StaticGKAdapter(
    private val list: List<StaticGKCategory>,
    private val onClick: (StaticGKCategory) -> Unit
) : RecyclerView.Adapter<StaticGKAdapter.GKViewHolder>() {

    class GKViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgCategory)
        val title: TextView = view.findViewById(R.id.txtTitle)
        val tagline: TextView = view.findViewById(R.id.txtTagline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GKViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_static_gk, parent, false)
        return GKViewHolder(view)
    }

    override fun onBindViewHolder(holder: GKViewHolder, position: Int) {
        val item = list[position]

        holder.image.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.tagline.text = item.tagline

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = list.size
}
