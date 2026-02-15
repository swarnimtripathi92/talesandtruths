package com.kidverse.app

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class StoryContentAdapter(
    val blocks: List<ContentBlock>,
    private var textSize: Float,
    private var font: Typeface?   // ✅ nullable for safety
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_IMAGE = 1
    }

    private var highlightedPos = -1
    private val client = OkHttpClient()

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

    fun highlight(position: Int) {
        highlightedPos = position
        notifyItemChanged(position)
    }

    // ================= TEXT VIEW HOLDER =================

    inner class TextVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tv: TextView = itemView.findViewById(R.id.tvText)

        fun bind(
            text: String,
            size: Float,
            typeface: Typeface?,
            highlighted: Boolean
        ) {

            val cleaned = text
                .replace("&nbsp;", " ")
                .replace("&nbsb;", " ")
                .replace("&nbps;", " ")

            val spanned = HtmlCompat.fromHtml(cleaned, HtmlCompat.FROM_HTML_MODE_LEGACY)

            tv.text = spanned

            tv.setTextIsSelectable(true)

           // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             //   tv.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
            //}

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)

            // ✅ Apply custom font safely
            typeface?.let {
                tv.typeface = it
            }

            tv.movementMethod = LinkMovementMethod.getInstance()

            val isDark =
                (itemView.context.resources.configuration.uiMode
                        and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

            tv.setTextColor(
                if (isDark)
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                else
                    ContextCompat.getColor(itemView.context, android.R.color.black)
            )

            if (highlighted) {
                tv.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.ttsHighlight)
                )
            } else {
                tv.setBackgroundColor(Color.TRANSPARENT)
            }

            tv.customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    menu?.add(0, 1001, 0, "Meaning")
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    if (item?.itemId == 1001) {

                        val rawSelected = tv.text
                            .subSequence(tv.selectionStart, tv.selectionEnd)
                            .toString()

                        val selected = rawSelected
                            .trim()
                            .replace(Regex("^[^a-zA-Z]+"), "")   // remove starting punctuation
                            .replace(Regex("[^a-zA-Z]+$"), "")   // remove ending punctuation


                        if (selected.isNotEmpty()) {
                            fetchMeaning(selected, itemView)
                        }

                        mode?.finish()
                        return true
                    }
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }

        private fun fetchMeaning(word: String, view: View) {

            val dialog = AlertDialog.Builder(view.context)
                .setTitle(word)
                .setMessage("Fetching meaning...")
                .setCancelable(true)
                .create()

            dialog.setCanceledOnTouchOutside(true)
            dialog.show()

            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        dialog.setMessage("Failed to fetch meaning.")
                    }
                }

                override fun onResponse(call: Call, response: Response) {

                    val body = response.body?.string()

                    Handler(Looper.getMainLooper()).post {

                        try {
                            val jsonArray = JSONArray(body)
                            val meanings = jsonArray
                                .getJSONObject(0)
                                .getJSONArray("meanings")
                                .getJSONObject(0)
                                .getJSONArray("definitions")
                                .getJSONObject(0)
                                .getString("definition")

                            dialog.setMessage(meanings)

                        } catch (e: Exception) {
                            dialog.setMessage("No definition found.")
                        }
                    }
                }
            })
        }
    }

    // ================= IMAGE VIEW HOLDER =================

    inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val img: ImageView = itemView.findViewById(R.id.imgStory)

        fun bind(url: String) {
            img.loadFast(url, centerCrop = false)
        }
    }

    fun updateTextSize(newSize: Float) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun updateFont(newFont: Typeface?) {
        font = newFont
        notifyDataSetChanged()
    }
}
