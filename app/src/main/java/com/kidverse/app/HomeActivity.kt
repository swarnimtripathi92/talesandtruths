package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 📖 Start Reading
        val cardStartReading = findViewById<MaterialCardView>(R.id.cardStartReading)
        cardStartReading.setOnClickListener {
            startActivity(Intent(this, KidsStoriesActivity::class.java))
        }

        // 🐯 Moral Stories
        val cardMoral = findViewById<MaterialCardView>(R.id.cardMoral)
        cardMoral.setOnClickListener {
            startActivity(Intent(this, MoralStoriesActivity::class.java))
        }

        // 🌙 Bedtime Stories
        val cardBedtime = findViewById<MaterialCardView>(R.id.cardBedtime)
        cardBedtime.setOnClickListener {
            startActivity(Intent(this, BedtimeStoriesActivity::class.java))
        }

        // 🧠 Kids Infotainment
        val cardKidsInfotainment = findViewById<MaterialCardView>(R.id.cardKidsInfotainment)
        cardKidsInfotainment.setOnClickListener {
            startActivity(Intent(this, KidsInfotainmentActivity::class.java))
        }

        // 🔒 Parent Zone
        val btnParentZone = findViewById<MaterialCardView>(R.id.btnParentZone)
        btnParentZone.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // ✨ Press animations (premium feel)
        addPressEffect(cardStartReading)
        addPressEffect(cardMoral)
        addPressEffect(cardBedtime)
        addPressEffect(cardKidsInfotainment)
        addPressEffect(btnParentZone)
    }

    // ✨ Touch scale animation
    private fun addPressEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }
    }
}
