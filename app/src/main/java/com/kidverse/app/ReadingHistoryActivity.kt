package com.kidverse.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReadingHistoryActivity : AppCompatActivity() {

    private val historyList = mutableListOf<ReadingHistoryItem>()
    private lateinit var adapter: ReadingHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reading_history)

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ReadingHistoryAdapter(historyList)
        recycler.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showEmpty(tvEmpty, recycler, "Please sign in to see reading history")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .orderBy("readAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                historyList.clear()

                if (snapshot.isEmpty) {
                    showEmpty(tvEmpty, recycler)
                    return@addOnSuccessListener
                }

                for (doc in snapshot.documents) {
                    val item = doc.toObject(ReadingHistoryItem::class.java) ?: continue

                    // ✅ STRICT BUT SAFE FILTER
                    // show only meaningful sessions
                    if (item.readDurationSec <= 0 && item.wordsRead <= 0) continue

                    historyList.add(item)
                }

                if (historyList.isEmpty()) {
                    showEmpty(tvEmpty, recycler)
                } else {
                    tvEmpty.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                showEmpty(tvEmpty, recycler, "Unable to load reading history")
            }
    }

    private fun showEmpty(
        tvEmpty: TextView,
        recycler: RecyclerView,
        msg: String = "No reading history yet 📖"
    ) {
        tvEmpty.visibility = View.VISIBLE
        tvEmpty.text = msg
        recycler.visibility = View.GONE
    }
}
