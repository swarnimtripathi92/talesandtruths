package com.example.talesandtruths

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FactAdapter(
    private val list: List<FactItem>
) : RecyclerView.Adapter<FactAdapter.FactVH>() {

    class FactVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.tvIcon)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val text: TextView = itemView.findViewById(R.id.tvText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FactVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fact_card, parent, false)
        return FactVH(view)
    }

    override fun onBindViewHolder(holder: FactVH, position: Int) {
        val item = list[position]

        val textSize = TextPref.get(holder.itemView.context)

        holder.icon.text = item.icon
        holder.title.text = item.title
        holder.text.text = item.text

        holder.title.textSize = textSize + 2
        holder.text.textSize = textSize
    }

    override fun getItemCount(): Int = list.size
}
