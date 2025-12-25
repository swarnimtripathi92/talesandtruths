package com.example.talesandtruths

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.appbar.MaterialToolbar

class StoryDetailActivity : AppCompatActivity() {

    private val blocks = mutableListOf<StoryBlock>()

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

        val adapter = StoryContentAdapter(blocks)
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
                    blocks.add(StoryBlock(type, value))
                }

                adapter.notifyDataSetChanged()
            }
    }

}
