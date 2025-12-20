package com.example.talesandtruths

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class KidsStoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_stories)

        fetchKidsStories()
    }

    private fun fetchKidsStories() {
        val db = FirebaseFirestore.getInstance()

        db.collection("stories")
            .whereEqualTo("audience", "kids")
            .whereEqualTo("status", "published")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    Log.d("KIDS_STORY", "Title: $title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("KIDS_STORY", "Error fetching stories", e)
            }
    }
}
