package com.kidverse.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kidverse.app.R
import com.kidverse.app.loadFast
import com.kidverse.app.PdfDownloadUtil
import com.kidverse.app.model.IssueModel

import com.kidverse.app.databinding.ItemIssueBinding

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
            holder.binding.imgIssue.loadFast(item.imageUrl, placeholder = R.drawable.img_monthly_kids_magazine)
        } else {
            holder.binding.imgIssue.setImageDrawable(null)
        }

        holder.binding.btnRead.setOnClickListener {
            onRead(item)
        }

        if (item.pdfUrl.isNullOrBlank()) {
            holder.binding.btnDownload.isEnabled = false
            holder.binding.btnDownload.alpha = 0.5f
        } else {
            holder.binding.btnDownload.isEnabled = true
            holder.binding.btnDownload.alpha = 1f
        }

        holder.binding.btnDownload.setOnClickListener {
            val pdfUrl = item.pdfUrl
            if (pdfUrl.isNullOrBlank()) return@setOnClickListener

            PdfDownloadUtil.download(
                context = holder.itemView.context,
                pdfUrl = pdfUrl,
                fileName = "Kids_${item.title}.pdf"
            )
        }
    }

    override fun getItemCount() = list.size
}
