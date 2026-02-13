package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BedtimeStoriesActivity : AppCompatActivity() {

    private val stories = mutableListOf<StoryItem>()
    private lateinit var adapter: StoryListAdapter

    private var isPremiumUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bedtime_stories)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerStories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkPremiumStatus {
            setupAdapter(recyclerView)
            fetchBedtimeStories()
        }
    }

    // 🔐 Check premium status (Unlogged = non-premium)
    private fun checkPremiumStatus(onReady: () -> Unit) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            isPremiumUser = false
            onReady()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                isPremiumUser = doc.getBoolean("isPremium") ?: false
                onReady()
            }
            .addOnFailureListener {
                isPremiumUser = false
                onReady()
            }
    }

    private fun setupAdapter(recyclerView: RecyclerView) {

        adapter = StoryListAdapter(
            list = stories,
            isPremium = isPremiumUser,
            onPremiumRequired = {
                PremiumBottomSheet().show(
                    supportFragmentManager,
                    "PremiumSheet"
                )
            },
            onClick = { story ->

                if (!isPremiumUser && story.isPremiumStory) {

                    PremiumBottomSheet().show(
                        supportFragmentManager,
                        "PremiumSheet"
                    )

                } else {

                    val intent = Intent(this, StoryReaderActivity::class.java)
                    intent.putExtra("storyId", story.id)
                    startActivity(intent)
                }
            }
        )

        recyclerView.adapter = adapter
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
                            status = doc.getString("status") ?: "",
                            isPremiumStory = doc.getBoolean("isPremiumStory") ?: false
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
