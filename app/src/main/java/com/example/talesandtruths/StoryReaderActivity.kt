package com.example.talesandtruths

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlin.math.max

class StoryReaderActivity : AppCompatActivity() {

    private val blocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: StoryContentAdapter
    private lateinit var prefs: SharedPreferences

    private var currentTextSize = 18f
    private var currentFont: Typeface = Typeface.SANS_SERIF

    private var storyId: String? = null
    private var startReadTime: Long = 0L
    private var hasSaved = false

    // 🔥 Scroll based tracking
    private var maxReadPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_reader)

        Log.d("READING_FLOW", "StoryReader opened")

        startReadTime = System.currentTimeMillis()

        prefs = getSharedPreferences("reader_prefs", MODE_PRIVATE)
        currentTextSize = prefs.getFloat("text_size", 18f)

        currentFont = when (prefs.getInt("font_style", 0)) {
            1 -> Typeface.SERIF
            2 -> Typeface.MONOSPACE
            else -> Typeface.SANS_SERIF
        }

        storyId = intent.getStringExtra("storyId")
        Log.d("READING_FLOW", "storyId=$storyId")

        val tvTitle = findViewById<TextView>(R.id.tvStoryTitle)
        val rv = findViewById<RecyclerView>(R.id.rvContent)

        val btnAPlus = findViewById<ImageButton>(R.id.btnAPlus)
        val btnAMinus = findViewById<ImageButton>(R.id.btnAMinus)
        val btnNightMode = findViewById<ImageButton>(R.id.btnNightMode)
        val btnFont = findViewById<ImageButton>(R.id.btnFont)

        val layoutManager = LinearLayoutManager(this)
        rv.layoutManager = layoutManager

        adapter = StoryContentAdapter(blocks, currentTextSize, currentFont)
        rv.adapter = adapter

        // 🔥 Scroll listener → actual read tracking
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisible =
                    layoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisible > maxReadPosition) {
                    maxReadPosition = lastVisible
                    Log.d("READ_SCROLL", "Max read position=$maxReadPosition")
                }
            }
        })

        // 🔠 Text size +
        btnAPlus.setOnClickListener {
            currentTextSize += 2f
            prefs.edit().putFloat("text_size", currentTextSize).apply()
            adapter.updateTextSize(currentTextSize)
        }

        // 🔠 Text size –
        btnAMinus.setOnClickListener {
            if (currentTextSize > 14f) {
                currentTextSize -= 2f
                prefs.edit().putFloat("text_size", currentTextSize).apply()
                adapter.updateTextSize(currentTextSize)
            }
        }

        // 🌙 Night mode
        btnNightMode.setOnClickListener {
            val mode = AppCompatDelegate.getDefaultNightMode()
            AppCompatDelegate.setDefaultNightMode(
                if (mode == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
            )
            recreate()
        }

        // 🔤 Font switch
        btnFont.setOnClickListener {
            currentFont = when (currentFont) {
                Typeface.SANS_SERIF -> Typeface.SERIF
                Typeface.SERIF -> Typeface.MONOSPACE
                else -> Typeface.SANS_SERIF
            }
            prefs.edit().putInt(
                "font_style",
                when (currentFont) {
                    Typeface.SERIF -> 1
                    Typeface.MONOSPACE -> 2
                    else -> 0
                }
            ).apply()
            adapter.updateFont(currentFont)
        }

        // 🔙 Back handling (gesture safe)
        onBackPressedDispatcher.addCallback(this) {
            Log.d("READING_FLOW", "Back gesture detected")
            saveReadingSession()
            finish()
        }

        loadStory(tvTitle)
    }

    private fun loadStory(tvTitle: TextView) {
        val id = storyId ?: return

        FirebaseFirestore.getInstance()
            .collection("stories")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                tvTitle.text = doc.getString("title") ?: ""

                val content =
                    doc.get("content") as? List<Map<String, String>> ?: emptyList()

                blocks.clear()
                for (item in content) {
                    blocks.add(
                        ContentBlock(
                            type = item["type"] ?: "text",
                            value = item["value"] ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()

                Log.d("READING_FLOW", "Story loaded, blocks=${blocks.size}")
            }
    }

    // 🔥 SAVE READING SESSION (FINAL)
    private fun saveReadingSession() {
        if (hasSaved) return
        hasSaved = true

        val seconds = (System.currentTimeMillis() - startReadTime) / 1000
        Log.d("READING_SAVE", "secondsSpent=$seconds")

        if (seconds < 10) {
            Log.d("READING_SAVE", "Session too short, ignored")
            return
        }

        val minutes = max(1, (seconds / 60).toInt())
        val words = countWordsTillScroll()

        Log.d("READING_SAVE", "minutes=$minutes words=$words")

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val sid = storyId ?: return
        val title = findViewById<TextView>(R.id.tvStoryTitle).text.toString()

        val docId = "${sid}_${todayKey()}"

        val ref = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .document(docId)

        ref.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val oldTime = doc.getLong("readDurationSec") ?: 0
                val oldWords = doc.getLong("wordsRead") ?: 0

                ref.update(
                    mapOf(
                        "readDurationSec" to (oldTime + minutes * 60),
                        "wordsRead" to (oldWords + words),
                        "readAt" to System.currentTimeMillis()
                    )
                )
                Log.d("READING_SAVE", "Updated history")
            } else {
                ref.set(
                    mapOf(
                        "storyId" to sid,
                        "storyTitle" to title,
                        "category" to "moral",
                        "readAt" to System.currentTimeMillis(),
                        "readDurationSec" to (minutes * 60),
                        "wordsRead" to words
                    )
                )
                Log.d("READING_SAVE", "Created history")
            }
        }
    }

    // 🔥 Scroll-based word count
    private fun countWordsTillScroll(): Int {
        var count = 0
        for (i in 0..maxReadPosition) {
            val block = blocks.getOrNull(i) ?: continue
            if (block.type == "text") {
                count += block.value
                    .trim()
                    .split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                    .size
            }
        }
        Log.d("READ_SCROLL", "Words till scroll=$count")
        return count
    }

    private fun todayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}_${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}
