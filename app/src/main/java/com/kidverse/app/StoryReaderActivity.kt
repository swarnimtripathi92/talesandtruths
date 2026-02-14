package com.kidverse.app

import android.graphics.Typeface
import android.os.*
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.res.ResourcesCompat

import java.util.*

class StoryReaderActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: StoryContentAdapter
    private var ttsReady = false

    private lateinit var btnPlus: ImageButton
    private lateinit var btnMinus: ImageButton
    private lateinit var btnFont: ImageButton
    private lateinit var btnNight: ImageButton
    private lateinit var btnSpeak: ImageButton

    private lateinit var tts: TextToSpeech
    private lateinit var tvTitle: TextView

    private var textSize = 18f
    private var fontIndex = 0
    private var isDark = false
    private var speaking = false
    private var autoScroll = false

    private val handler = Handler(Looper.getMainLooper())
    private val fonts by lazy {
        arrayOf(
            ResourcesCompat.getFont(this, R.font.merriweather)!!,
            ResourcesCompat.getFont(this, R.font.lora)!!,
            ResourcesCompat.getFont(this, R.font.opensans)!!,
            ResourcesCompat.getFont(this, R.font.baloo)!!,
            ResourcesCompat.getFont(this, R.font.baloo_bold)!!
        )
    }



    private lateinit var storyId: String
    private val prefs by lazy {
        getSharedPreferences("reader_settings", MODE_PRIVATE)
    }

    // --------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_reader)

        rv = findViewById(R.id.rvContent)
        btnPlus = findViewById(R.id.btnAPlus)
        btnMinus = findViewById(R.id.btnAMinus)
        btnFont = findViewById(R.id.btnFont)
        btnNight = findViewById(R.id.btnNightMode)
        btnSpeak = findViewById(R.id.btnSpeak)

        rv.layoutManager = LinearLayoutManager(this)
        tvTitle = findViewById(R.id.tvStoryTitle)

        val receivedId = intent.getStringExtra("storyId")

        if (receivedId.isNullOrEmpty()) {
            Toast.makeText(this, "Story ID not received", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        storyId = receivedId

        restoreReaderSettings()
        loadStory()
        setupButtons()
        setupTTS()
        trackProgress()
    }


    // --------------------------------------------------
    // FIRESTORE LOAD + CACHE
    // --------------------------------------------------

    private fun loadStory() {

        FirebaseFirestore.getInstance()
            .collection("stories")
            .document(storyId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    Toast.makeText(this, "Story not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // ⭐ TITLE SET
                val title = doc.getString("title") ?: "Story"
                tvTitle.text = title

                // ⭐ CONTENT LOAD
                val rawList = doc.get("content") as? List<Map<String, Any>>

                if (rawList.isNullOrEmpty()) {
                    Toast.makeText(this, "Story empty", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val blocks = rawList.map {
                    ContentBlock(
                        it["type"].toString(),
                        it["value"].toString()
                    )
                }

                adapter = StoryContentAdapter(blocks, textSize, fonts[fontIndex])
                rv.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }


    private fun applyStory(list: List<ContentBlock>) {
        adapter = StoryContentAdapter(list, textSize, fonts[fontIndex])
        rv.adapter = adapter
        restoreScroll()
    }

    // --------------------------------------------------
    // CACHE
    // --------------------------------------------------

    private fun cacheStory(list: List<ContentBlock>) {
        val str = list.joinToString("|||") { it.type + ":::" + it.value }
        prefs.edit().putString("cached_$storyId", str).apply()
    }

    private fun parseCache(str: String): List<ContentBlock> {
        return str.split("|||").map {
            val p = it.split(":::")
            ContentBlock(p[0], p[1])
        }
    }

    // --------------------------------------------------
    // BUTTONS
    // --------------------------------------------------

    private fun setupButtons() {

        btnPlus.setOnClickListener {
            textSize += 2
            adapter.updateTextSize(textSize)
            saveSettings()
        }

        btnMinus.setOnClickListener {
            if (textSize > 12) {
                textSize -= 2
                adapter.updateTextSize(textSize)
                saveSettings()
            }
        }

        btnFont.setOnClickListener {

            fontIndex++
            if (fontIndex >= fonts.size) fontIndex = 0

            adapter.updateFont(fonts[fontIndex])
            saveSettings()
        }

        btnNight.setOnClickListener {

            isDark = !isDark

            AppCompatDelegate.setDefaultNightMode(
                if (isDark)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )

            saveSettings()
        }

        btnSpeak.setOnClickListener {

            if (!speaking) {
                speakAll()
            } else {
                stopSpeak()
            }
        }

        btnSpeak.setOnLongClickListener {
            autoScroll = !autoScroll
            startAutoScroll()
            true
        }
    }

    // --------------------------------------------------
    // TTS
    // --------------------------------------------------

    private fun setupTTS() {

        tts = TextToSpeech(this) { status ->

            if (status == TextToSpeech.SUCCESS) {

                // Try Hindi first (covers Hindi + mixed)
                var result = tts.setLanguage(Locale("hi", "IN"))

                // If Hindi not supported, fallback to English
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    result = tts.setLanguage(Locale.US)
                }

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Toast.makeText(this,"TTS language not supported",Toast.LENGTH_LONG).show()
                    ttsReady = false
                } else {
                    tts.setSpeechRate(1.0f)
                    tts.setPitch(1.0f)
                    ttsReady = true
                }

            } else {
                Toast.makeText(this,"TTS init failed",Toast.LENGTH_LONG).show()
                ttsReady = false
            }
        }
    }


    private fun speakAll() {

        if (!ttsReady) {
            Toast.makeText(this,"TTS not ready yet",Toast.LENGTH_SHORT).show()
            return
        }

        val ad = rv.adapter as? StoryContentAdapter ?: return

        val text = StringBuilder()

        for (i in 0 until ad.itemCount) {

            val block = ad.blocks[i]

            if (block.type == "text") {

                // Clean HTML before speaking
                val clean = HtmlCompat
                    .fromHtml(block.value, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    .toString()
                    .trim()

                if (clean.isNotEmpty())
                    text.append(clean).append(" ")
            }
        }

        if (text.isBlank()) {
            Toast.makeText(this,"No readable text found",Toast.LENGTH_SHORT).show()
            return
        }

        tts.stop()
        tts.speak(text.toString(), TextToSpeech.QUEUE_FLUSH, null, "story")

        speaking = true
        btnSpeak.alpha = 0.5f
    }

    private fun stopSpeak() {
        tts.stop()
        speaking = false
        btnSpeak.alpha = 1f
    }

    // --------------------------------------------------
    // AUTO SCROLL
    // --------------------------------------------------

    private fun startAutoScroll() {

        handler.post(object : Runnable {
            override fun run() {

                if (!autoScroll) return

                rv.scrollBy(0, 2)
                handler.postDelayed(this, 16)
            }
        })
    }

    // --------------------------------------------------
    // PROGRESS SAVE
    // --------------------------------------------------

    private fun trackProgress() {

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {

                val offset = r.computeVerticalScrollOffset()
                prefs.edit().putInt("scroll_$storyId", offset).apply()
            }
        })
    }

    private fun restoreScroll() {
        val pos = prefs.getInt("scroll_$storyId", 0)
        rv.post { rv.scrollBy(0, pos) }
    }

    // --------------------------------------------------
    // SETTINGS SAVE
    // --------------------------------------------------

    private fun saveSettings() {
        prefs.edit()
            .putFloat("size", textSize)
            .putInt("font", fontIndex)
            .putBoolean("dark", isDark)
            .apply()
    }

    private fun restoreReaderSettings() {
        textSize = prefs.getFloat("size", 18f)
        fontIndex = prefs.getInt("font", 0)
        isDark = prefs.getBoolean("dark", false)
    }

    // --------------------------------------------------

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
