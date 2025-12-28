package com.example.talesandtruths

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class KidsStoriesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KidsStoriesAdapter
    private val storiesList = mutableListOf<KidsStory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_stories)

        recyclerView = findViewById(R.id.recyclerStories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = KidsStoriesAdapter(storiesList)
        recyclerView.adapter = adapter

        fetchKidsStories()
    }

    private fun fetchKidsStories() {

        FirebaseFirestore.getInstance()
            .collection("stories")
            //.whereEqualTo("status", "published")
           // .whereEqualTo("audience", "kids")
            .orderBy("updatedAt")
            .get()
            .addOnSuccessListener { documents ->

                storiesList.clear()

                for (doc in documents) {

                    val title = doc.getString("title") ?: continue
                    val coverImage = doc.getString("coverImage") ?: ""   // ✅ FIX HERE

                    storiesList.add(
                        KidsStory(
                            id = doc.id,
                            title = title,
                            coverImageUrl = coverImage
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("KIDS_FLOW", "FAILURE", e)
            }
    }
}
