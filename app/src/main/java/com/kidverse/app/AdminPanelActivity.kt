package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminPanelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    finish() // ❌ non-admin blocked
                } else {
                    setContentView(R.layout.activity_admin_panel)
                    setupUI()
                }
            }
            .addOnFailureListener {
                finish()
            }
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnAddStory).setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnAllStories).setOnClickListener {
            startActivity(Intent(this, StoryListActivity::class.java))
        }
    }
}
