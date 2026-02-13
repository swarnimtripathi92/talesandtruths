package com.kidverse.app

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.max

class StoryReaderActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "STORY_READER"
    }

    private val blocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: StoryContentAdapter
    private lateinit var prefs: SharedPreferences

    private var currentTextSize = 18f
    private var currentFont: Typeface = Typeface.SANS_SERIF

    private var storyId: String? = null
    private var startReadTime = 0L
    private var hasSaved = false

    private var lastSavedScrollPosition: Long = 0L

    private lateinit var rv: RecyclerView
    private lateinit var btnSpeak: ImageButton

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_reader)

        startReadTime = System.currentTimeMillis()

        prefs = getSharedPreferences("reader_prefs", MODE_PRIVATE)
        currentTextSize = prefs.getFloat("text_size", 18f)

        currentFont = when (prefs.getInt("font_style", 0)) {
            1 -> Typeface.SERIF
            2 -> Typeface.MONOSPACE
            else -> Typeface.SANS_SERIF
        }

        storyId = intent.getStringExtra("storyId")

        val tvTitle = findViewById<TextView>(R.id.tvStoryTitle)
        rv = findViewById(R.id.rvContent)
        btnSpeak = findViewById(R.id.btnSpeak)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = StoryContentAdapter(blocks, currentTextSize, currentFont)
        rv.adapter = adapter

        // Scroll tracking
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                lastSavedScrollPosition += dy
            }
        })

        tts = TextToSpeech(this, this)

        findViewById<ImageButton>(R.id.btnAPlus).setOnClickListener {
            currentTextSize += 2f
            prefs.edit().putFloat("text_size", currentTextSize).apply()
            adapter.updateTextSize(currentTextSize)
        }

        findViewById<ImageButton>(R.id.btnAMinus).setOnClickListener {
            if (currentTextSize > 14f) {
                currentTextSize -= 2f
                prefs.edit().putFloat("text_size", currentTextSize).apply()
                adapter.updateTextSize(currentTextSize)
            }
        }

        findViewById<ImageButton>(R.id.btnNightMode).setOnClickListener {
            val mode = AppCompatDelegate.getDefaultNightMode()
            AppCompatDelegate.setDefaultNightMode(
                if (mode == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
            )
            recreate()
        }

        findViewById<ImageButton>(R.id.btnFont).setOnClickListener {
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

        onBackPressedDispatcher.addCallback(this) {
            saveReadingSession()
            finish()
        }

        loadStory(tvTitle)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun loadStory(tvTitle: TextView) {
        val id = storyId ?: return

        db.collection("stories")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->

                tvTitle.text = doc.getString("title") ?: ""

                val content =
                    doc.get("content") as? List<Map<String, String>> ?: emptyList()

                blocks.clear()

                for (item in content) {
                    val type = item["type"] ?: ""
                    val value = item["value"] ?: ""
                    blocks.add(ContentBlock(type, value))
                }

                adapter.notifyDataSetChanged()
                restoreScrollPosition()
            }
    }

    private fun restoreScrollPosition() {

        val user = auth.currentUser ?: return
        val sid = storyId ?: return

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val lastStory = doc.getString("lastStoryId")
                val lastPosition = doc.getLong("lastReadPosition") ?: 0L

                if (lastStory == sid && lastPosition > 0) {
                    rv.post {
                        rv.scrollBy(0, lastPosition.toInt())
                    }
                }
            }
    }

    private fun saveReadingSession() {

        if (hasSaved) return
        hasSaved = true

        val user = auth.currentUser ?: return
        val sid = storyId ?: return

        val seconds = (System.currentTimeMillis() - startReadTime) / 1000
        if (seconds < 5) return

        val minutes = max(1, (seconds / 60).toInt())
        val words = countWordsTillScroll()

        val title =
            findViewById<TextView>(R.id.tvStoryTitle).text.toString()

        val docId = "${sid}_${todayKey()}"

        db.collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .document(docId)
            .set(
                mapOf(
                    "storyId" to sid,
                    "storyTitle" to title,
                    "readAt" to System.currentTimeMillis(),
                    "readDurationSec" to (minutes * 60),
                    "wordsRead" to words
                )
            )

        val userRef = db.collection("users").document(user.uid)

        userRef.update(
            mapOf(
                "lastStoryId" to sid,
                "lastReadPosition" to lastSavedScrollPosition
            )
        )

        val layoutManager =
            rv.layoutManager as LinearLayoutManager
        val lastVisible =
            layoutManager.findLastCompletelyVisibleItemPosition()

        if (lastVisible >= blocks.size - 1) {
            userRef.update(
                "completedStories",
                FieldValue.arrayUnion(sid)
            )
        }
    }

    private fun countWordsTillScroll(): Int {
        var count = 0
        for (block in blocks) {
            if (block.type == "text") {
                count += block.value.trim()
                    .split("\\s+".toRegex()).size
            }
        }
        return count
    }

    private fun todayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}_${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
