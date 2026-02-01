package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BedtimeStoriesActivity : AppCompatActivity() {

    private val stories = mutableListOf<StoryItem>()
    private lateinit var adapter: StoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bedtime_stories)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerStories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StoryListAdapter(
            list = stories,
            onClick = { story ->
                // 👉 Story Reader open
                val intent = Intent(this, StoryReaderActivity::class.java)
                intent.putExtra("storyId", story.id)
                startActivity(intent)
            },
            onLongClick = {
                // user side me kuch nahi
            }
        )

        recyclerView.adapter = adapter

        fetchBedtimeStories()
    }

    private fun fetchBedtimeStories() {
        FirebaseFirestore.getInstance()
            .collection("stories")
            .whereEqualTo("status", "published")
            .whereEqualTo("category", "bedtime")
            .orderBy("updatedAt")
            .get()
            .addOnSuccessListener { docs ->
                stories.clear()

                for (doc in docs) {
                    stories.add(
                        StoryItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            coverImage = doc.getString("coverImage") ?: "",
                            status = doc.getString("status") ?: ""
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("BEDTIME", "Failed to load bedtime stories", it)
            }
    }
}
