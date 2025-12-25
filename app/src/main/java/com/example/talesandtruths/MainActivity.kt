package com.example.talesandtruths

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Kids Stories card
        val kidsCard = findViewById<MaterialCardView>(R.id.cardKids)

        kidsCard.setOnClickListener {
            val intent = Intent(this, KidsStoriesActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

}
