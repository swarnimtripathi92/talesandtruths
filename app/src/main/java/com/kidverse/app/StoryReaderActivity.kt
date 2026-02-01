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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

class StoryReaderActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "STORY_READER"
    }

    // 📖 Story
    private val blocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: StoryContentAdapter
    private lateinit var prefs: SharedPreferences

    private var currentTextSize = 18f
    private var currentFont: Typeface = Typeface.SANS_SERIF

    // 📊 Reading session
    private var storyId: String? = null
    private var startReadTime = 0L
    private var hasSaved = false
    private var maxReadPosition = 0

    // 🔊 TTS
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var isPaused = false

    // 🔊 Paragraph-wise speaking
    private val speakableBlocks = mutableListOf<Int>() // adapter positions
    private var currentSpeakIndex = 0

    private lateinit var btnSpeak: ImageButton
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_reader)

        Log.d(TAG, "onCreate")

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

        val btnAPlus = findViewById<ImageButton>(R.id.btnAPlus)
        val btnAMinus = findViewById<ImageButton>(R.id.btnAMinus)
        val btnNightMode = findViewById<ImageButton>(R.id.btnNightMode)
        val btnFont = findViewById<ImageButton>(R.id.btnFont)
        btnSpeak = findViewById(R.id.btnSpeak)

        btnSpeak.isEnabled = false

        rv.layoutManager = LinearLayoutManager(this)
        adapter = StoryContentAdapter(blocks, currentTextSize, currentFont)
        rv.adapter = adapter

        // 🔊 Init TTS
        tts = TextToSpeech(this, this)

        // ▶️ / ⏸ Play Pause
        btnSpeak.setOnClickListener {
            if (!ttsReady || speakableBlocks.isEmpty()) return@setOnClickListener

            if (tts.isSpeaking) {
                tts.stop()
                isPaused = true
                btnSpeak.setImageResource(R.drawable.ic_speaker)
            } else {
                if (!isPaused) {
                    speakFrom(0)
                } else {
                    speakFrom(currentSpeakIndex)
                }
                isPaused = false
                btnSpeak.setImageResource(R.drawable.ic_pause)
            }
        }

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

        // 🔙 Back
        onBackPressedDispatcher.addCallback(this) {
            tts.stop()
            saveReadingSession()
            finish()
        }

        loadStory(tvTitle)
    }

    // 🔊 TTS ready
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

            Log.d(TAG, "TTS ready=$ttsReady")

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                override fun onStart(utteranceId: String?) {
                    val index =
                        utteranceId?.removePrefix("para_")?.toIntOrNull() ?: return

                    runOnUiThread {
                        currentSpeakIndex = index
                        val pos = speakableBlocks[index]
                        adapter.highlight(pos)
                        rv.smoothScrollToPosition(pos)
                        maxReadPosition = max(maxReadPosition, pos)
                    }
                }

                override fun onDone(utteranceId: String?) {
                    val index =
                        utteranceId?.removePrefix("para_")?.toIntOrNull() ?: return

                    if (index + 1 < speakableBlocks.size) {
                        speakFrom(index + 1)
                    } else {
                        runOnUiThread {
                            btnSpeak.setImageResource(R.drawable.ic_speaker)
                        }
                    }
                }

                override fun onError(utteranceId: String?) {}
            })
        }
    }

    // 🔊 Speak paragraph-wise
    private fun speakFrom(index: Int) {
        if (!ttsReady || index !in speakableBlocks.indices) return

        currentSpeakIndex = index
        val blockPos = speakableBlocks[index]
        val text = blocks[blockPos].value

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "para_$index"
        )
    }

    // 📥 Load story
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
                speakableBlocks.clear()

                for (item in content) {
                    val type = item["type"] ?: ""
                    val value = item["value"] ?: ""

                    blocks.add(ContentBlock(type, value))

                    if (type == "text" && value.isNotBlank()) {
                        speakableBlocks.add(blocks.size - 1)
                    }
                }

                adapter.notifyDataSetChanged()

                btnSpeak.isEnabled = speakableBlocks.isNotEmpty()
            }
    }

    // 📊 Save reading session
    private fun saveReadingSession() {
        if (hasSaved) return
        hasSaved = true

        val seconds = (System.currentTimeMillis() - startReadTime) / 1000
        if (seconds < 10) return

        val minutes = max(1, (seconds / 60).toInt())
        val words = countWordsTillScroll()

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val sid = storyId ?: return
        val title = findViewById<TextView>(R.id.tvStoryTitle).text.toString()

        val docId = "${sid}_${todayKey()}"

        FirebaseFirestore.getInstance()
            .collection("users")
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
    }

    private fun countWordsTillScroll(): Int {
        var count = 0
        for (i in 0..maxReadPosition) {
            val block = blocks.getOrNull(i) ?: continue
            if (block.type == "text") {
                count += block.value.trim().split("\\s+".toRegex()).size
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
