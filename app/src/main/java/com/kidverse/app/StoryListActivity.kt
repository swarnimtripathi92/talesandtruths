package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class StoryListActivity : AppCompatActivity() {

    private val stories = mutableListOf<StoryItem>()
    private lateinit var adapter: StoryListAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_list)

        val spinner = findViewById<Spinner>(R.id.spFilter)
        val rv = findViewById<RecyclerView>(R.id.rvStories)

        // 👑 Admin screen → Always premium access
        adapter = StoryListAdapter(
            list = stories,
            isPremium = true,
            onPremiumRequired = {},
            onClick = { story ->
                val i = Intent(this, AddStoryActivity::class.java)
                i.putExtra("storyId", story.id)
                startActivity(i)
            }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        ArrayAdapter.createFromResource(
            this,
            R.array.story_filters,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (parent.getItemAtPosition(position).toString()) {
                        "Published" -> loadStories("published")
                        "Draft" -> loadStories("draft")
                        else -> loadStories(null)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun loadStories(status: String?) {

        val baseQuery = firestore.collection("stories")

        val query = if (status != null) {
            baseQuery.whereEqualTo("status", status)
        } else {
            baseQuery
        }

        query.orderBy("updatedAt")
            .get()
            .addOnSuccessListener { snapshot ->

                stories.clear()

                for (doc in snapshot.documents) {

                    stories.add(
                        StoryItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            coverImage = doc.getString("coverImage") ?: "",
                            status = doc.getString("status") ?: "draft",
                            isPremiumStory = doc.getBoolean("isPremiumStory") ?: false
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
    }

    // 🗑 Delete (Admin Only)
    private fun confirmDelete(story: StoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Story")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                firestore.collection("stories")
                    .document(story.id)
                    .delete()
                    .addOnSuccessListener { loadStories(null) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
