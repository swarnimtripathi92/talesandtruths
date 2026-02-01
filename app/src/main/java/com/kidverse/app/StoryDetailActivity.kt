package com.kidverse.app

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore

class StoryDetailActivity : AppCompatActivity() {

    // ✅ Correct type
    private val blocks = mutableListOf<ContentBlock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        val storyId = intent.getStringExtra("storyId") ?: return

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerContent)
        recycler.layoutManager = LinearLayoutManager(this)

        // ✅ Adapter expects List<ContentBlock>
        val adapter =  StoryContentAdapter(
            blocks,
            18f,
            Typeface.SANS_SERIF
        )
        recycler.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("stories")
            .document(storyId)
            .get()
            .addOnSuccessListener { doc ->

                toolbar.title = doc.getString("title") ?: "Story"

                val content = doc.get("content") as? List<*> ?: return@addOnSuccessListener
                blocks.clear()

                for (item in content) {
                    val map = item as? Map<*, *> ?: continue
                    val type = map["type"] as? String ?: continue
                    val value = map["value"] as? String ?: continue

                    // ✅ FIX HERE
                    blocks.add(
                        ContentBlock(
                            type = type,
                            value = value
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("STORY_DETAIL", "Failed to load story", it)
            }
    }
}
