package com.kidverse.app

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.speech.tts.TextToSpeech
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

    private val blocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: StoryContentAdapter
    private lateinit var prefs: SharedPreferences

    private var storyId: String? = null
    private var startReadTime = 0L
    private var hasSaved = false
    private var lastSavedScrollPosition: Long = 0L

    private lateinit var rv: RecyclerView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_reader)

        startReadTime = System.currentTimeMillis()

        prefs = getSharedPreferences("reader_prefs", MODE_PRIVATE)
        storyId = intent.getStringExtra("storyId")

        val tvTitle = findViewById<TextView>(R.id.tvStoryTitle)
        rv = findViewById(R.id.rvContent)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = StoryContentAdapter(blocks, 18f, Typeface.SANS_SERIF)
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                lastSavedScrollPosition += dy
            }
        })

        onBackPressedDispatcher.addCallback(this) {
            saveReadingSession()
            finish()
        }

        loadStory(tvTitle)
    }

    private fun loadStory(tvTitle: TextView) {
        val id = storyId ?: return

        db.collection("stories")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                tvTitle.text = doc.getString("title") ?: ""
                saveLastReadStory(id)

                val content =
                    doc.get("content") as? List<Map<String, String>> ?: emptyList()

                blocks.clear()

                for (item in content) {
                    blocks.add(ContentBlock(item["type"] ?: "", item["value"] ?: ""))
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun saveLastReadStory(id: String) {

        val user = auth.currentUser

        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .update("lastReadStoryId", id)
        }

        val prefs = getSharedPreferences("kidverse_prefs", MODE_PRIVATE)
        prefs.edit().putString("lastReadStoryId", id).apply()
    }

    private fun saveReadingSession() {

        if (hasSaved) return
        hasSaved = true

        val sid = storyId ?: return
        val seconds = (System.currentTimeMillis() - startReadTime) / 1000
        if (seconds < 5) return

        val user = auth.currentUser

        if (user != null) {
            val userRef = db.collection("users").document(user.uid)

            userRef.update(
                mapOf(
                    "lastReadPosition" to lastSavedScrollPosition
                )
            )

            val layoutManager = rv.layoutManager as LinearLayoutManager
            val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

            if (lastVisible >= blocks.size - 1) {
                userRef.update(
                    "completedStories",
                    FieldValue.arrayUnion(sid)
                )
            }
        }

        // Save scroll locally also
        val prefs = getSharedPreferences("kidverse_prefs", MODE_PRIVATE)
        prefs.edit()
            .putLong("lastReadPosition", lastSavedScrollPosition)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onInit(status: Int) {}
}
