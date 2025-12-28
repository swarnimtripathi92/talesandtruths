package com.example.talesandtruths

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class KidsInfotainmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_infotainment)

        findViewById<CardView>(R.id.cardStaticGK).setOnClickListener {
            startActivity(Intent(this, StaticGKActivity::class.java))
        }

        findViewById<CardView>(R.id.cardCurrentGK).setOnClickListener {
            startActivity(Intent(this, CurrentGKActivity::class.java))
        }

        findViewById<CardView>(R.id.cardGames).setOnClickListener {
            startActivity(Intent(this, GamesForKidsActivity::class.java))
        }
    }
}
