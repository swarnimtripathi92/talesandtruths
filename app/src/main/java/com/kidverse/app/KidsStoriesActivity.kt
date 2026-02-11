package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class KidsStoriesActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val stories = mutableListOf<StoryItem>()
    private lateinit var adapter: StoryListAdapter

    private var isPremiumUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_stories)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerStories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkPremiumStatus {
            setupAdapter(recyclerView)
            fetchStories()
        }
    }

    private fun checkPremiumStatus(onReady: () -> Unit) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            isPremiumUser = false
            onReady()
            return
        }

        firestore.collection("users")
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
            freeLimit = if (isPremiumUser) Int.MAX_VALUE else 10,
            onPremiumRequired = {
                PremiumBottomSheet().show(
                    supportFragmentManager,
                    "PremiumSheet"
                )
            },
            onClick = { story ->
                val intent = Intent(this, StoryReaderActivity::class.java)
                intent.putExtra("storyId", story.id)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun fetchStories() {

        stories.clear()

        fetchCategory("moral") {
            fetchCategory("bedtime") {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun fetchCategory(category: String, onComplete: () -> Unit) {

        var query = firestore.collection("stories")
            .whereEqualTo("status", "published")
            .whereEqualTo("category", category)
            .orderBy("updatedAt", Query.Direction.ASCENDING) // oldest first

        if (!isPremiumUser) {
            query = query.limit(5) // 5 per category
        }

        query.get()
            .addOnSuccessListener { docs ->

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

                onComplete()
            }
            .addOnFailureListener {
                Log.e("KIDS_$category", "Failed to load $category stories", it)
                onComplete()
            }
    }
}
