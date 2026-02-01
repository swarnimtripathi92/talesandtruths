package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class GamesForKidsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_for_kids)

        val cardTraceDraw = findViewById<MaterialCardView>(R.id.cardTraceDraw)

        cardTraceDraw.setOnClickListener {
            val intent = Intent(
                this@GamesForKidsActivity,
                AlphabetSelectionActivity::class.java
            )
            startActivity(intent)
        }
    }

}
