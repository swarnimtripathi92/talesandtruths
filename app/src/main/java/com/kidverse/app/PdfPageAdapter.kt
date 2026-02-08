package com.kidverse.app

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView

class PdfPageAdapter(
    private val renderer: PdfRenderer
) : RecyclerView.Adapter<PdfPageAdapter.PageVH>() {

    companion object {
        private const val TAG = "PdfPageAdapter"
        private const val SCALE_FACTOR = 2   // 🔥 IMPORTANT: memory control
        private const val MAX_PAGES_DEBUG = Int.MAX_VALUE
        // testing ke liye chaho to yahan 3 likh sakte ho
    }

    inner class PageVH(val photoView: PhotoView)
        : RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {

        Log.d(TAG, "onCreateViewHolder")

        val photoView = PhotoView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
            maximumScale = 5f
            mediumScale = 2.5f
            minimumScale = 1f
            setBackgroundColor(Color.WHITE)
        }

        return PageVH(photoView)
    }

    override fun getItemCount(): Int {
        val count = minOf(renderer.pageCount, MAX_PAGES_DEBUG)
        Log.d(TAG, "getItemCount = $count")
        return count
    }

    override fun onBindViewHolder(holder: PageVH, position: Int) {

        Log.d(TAG, "Rendering page: $position")

        try {
            val page = renderer.openPage(position)

            val width = page.width / SCALE_FACTOR
            val height = page.height / SCALE_FACTOR

            val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.RGB_565   // 🔥 half memory
            )

            bitmap.eraseColor(Color.WHITE)

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            holder.photoView.setImageBitmap(bitmap)

            page.close()

            Log.d(TAG, "Page rendered successfully: $position")

        } catch (e: Exception) {
            Log.e(TAG, "Error rendering page $position", e)
        }
    }

    override fun onViewRecycled(holder: PageVH) {
        super.onViewRecycled(holder)
        Log.d(TAG, "onViewRecycled")
        holder.photoView.setImageDrawable(null)
    }
}
