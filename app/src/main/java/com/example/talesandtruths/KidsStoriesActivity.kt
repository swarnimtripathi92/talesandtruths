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
        recyclerView.adapter = adapter   // 🔥 adapter pehle attach

        fetchKidsStories()
    }

    private fun fetchKidsStories() {

        Log.d("KIDS_FLOW", "fetchKidsStories() ENTERED")

        val db = FirebaseFirestore.getInstance()
        Log.d("KIDS_FLOW", "Firestore instance created")

        db.collection("stories")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("KIDS_FLOW", "SUCCESS, docs = ${documents.size()}")

                storiesList.clear()

                for (doc in documents) {
                    val title = doc.getString("title") ?: continue
                    storiesList.add(KidsStory(doc.id, title))
                }

                Log.d("KIDS_FLOW", "Adapter list size = ${storiesList.size}")

                adapter.notifyDataSetChanged() // 🔥 THIS IS KEY
            }
            .addOnFailureListener { e ->
                Log.e("KIDS_FLOW", "FAILURE", e)
            }
    }
}
