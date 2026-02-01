package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kidverse.app.R
import com.google.android.material.card.MaterialCardView

class KidsInfotainmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_infotainment)

        findViewById<MaterialCardView>(R.id.cardStaticGK)
            .setOnClickListener {
                startActivity(Intent(this, StaticGKActivity::class.java))
            }

        findViewById<MaterialCardView>(R.id.cardCurrentGK)
            .setOnClickListener {
                // Future: Current GK screen
            }

        findViewById<MaterialCardView>(R.id.cardPuzzles)
            .setOnClickListener {
                // Future: Puzzles screen
            }
    }
}
