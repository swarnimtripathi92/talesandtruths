package com.example.talesandtruths

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 📚 Start Reading (Kids Stories)
        findViewById<MaterialCardView>(R.id.cardStartReading)
            .setOnClickListener {
                startActivity(
                    Intent(this, KidsStoriesActivity::class.java)
                )
            }

        // 🐯 Moral Stories
        findViewById<MaterialCardView>(R.id.cardMoral)
            .setOnClickListener {
                startActivity(
                    Intent(this, MoralStoriesActivity::class.java)
                )
            }

        // 🌙 Bedtime Stories (currently same as kids stories)
        findViewById<MaterialCardView>(R.id.cardBedtime)
            .setOnClickListener {
                startActivity(
                    Intent(this, KidsStoriesActivity::class.java)
                )
            }

        // 🧠 Kids Infotainment  ⭐⭐ NEW ⭐⭐
        findViewById<MaterialCardView>(R.id.cardKidsInfotainment)
            .setOnClickListener {
                startActivity(
                    Intent(this, KidsInfotainmentActivity::class.java)
                )
            }

        // 🔒 Parent Zone / Profile
        findViewById<Button>(R.id.btnParentZone)
            .setOnClickListener {
                startActivity(
                    Intent(this, ProfileActivity::class.java)
                )
            }
    }
}
