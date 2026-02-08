package com.kidverse.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kidverse.app.PdfDownloadUtil
import com.kidverse.app.databinding.ItemIssueBinding
import com.kidverse.app.model.IssueModel

class IssueAdapter(
    private val list: List<IssueModel>,
    private val onRead: (IssueModel) -> Unit
) : RecyclerView.Adapter<IssueAdapter.VH>() {

    inner class VH(val binding: ItemIssueBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemIssueBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.binding.tvTitle.text = item.title

        if (!item.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .into(holder.binding.imgIssue)
        } else {
            holder.binding.imgIssue.setImageDrawable(null)
        }

        holder.binding.btnRead.setOnClickListener {
            onRead(item)
        }

        holder.binding.btnDownload.setOnClickListener {
            PdfDownloadUtil.download(
                context = holder.itemView.context,
                pdfUrl = item.pdfUrl,
                fileName = "Kids_${item.title}.pdf"
            )
        }
    }

    override fun getItemCount() = list.size
}
