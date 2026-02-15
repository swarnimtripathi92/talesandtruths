package com.kidverse.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BadgesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        val recycler = findViewById<RecyclerView>(R.id.recyclerBadges)
        recycler.layoutManager = GridLayoutManager(this, 2)

        val list = mutableListOf<Badge>()
        val adapter = BadgesAdapter(list)
        recycler.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user.uid)
            .collection("readingHistory")
            .get()
            .addOnSuccessListener { historySnap ->
                val totalStoryWords = historySnap.documents.sumOf {
                    (it.getLong("wordsRead") ?: 0L).toInt()
                }

                db.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val gkStars = (userDoc.getLong("gkStars") ?: 0L).toInt()
                        BadgeManager.checkAndUnlockBadges(totalStoryWords, gkStars)

                        db.collection("users")
                            .document(user.uid)
                            .collection("badges")
                            .get()
                            .addOnSuccessListener { snap ->
                                list.clear()
                                for (doc in snap.documents) {
                                    doc.toObject(Badge::class.java)?.let { list.add(it) }
                                }
                                list.sortWith(compareBy<Badge> { it.track }.thenBy { it.requiredStars })
                                adapter.notifyDataSetChanged()
                            }
                    }
            }
    }
}
