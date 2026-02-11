package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KidsInfotainmentActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kids_infotainment)

        val cardStaticGK = findViewById<MaterialCardView>(R.id.cardStaticGK)
        val cardCurrentGK = findViewById<MaterialCardView>(R.id.cardCurrentGK)
        val cardPuzzles = findViewById<MaterialCardView>(R.id.cardPuzzles)

        // 🟢 Static GK – Open for all
        cardStaticGK.setOnClickListener {
            startActivity(Intent(this, StaticGKActivity::class.java))
        }

        // 🔒 Current GK – Premium only
        cardCurrentGK.setOnClickListener {
            checkPremiumAndOpenCurrentGK()
        }

        // 🧩 Puzzles – For now open (limit logic later)
        cardPuzzles.setOnClickListener {
            Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show()
        }

    }

    // 🔐 Premium gate BEFORE activity launch
    private fun checkPremiumAndOpenCurrentGK() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            PremiumBottomSheet().show(
                supportFragmentManager,
                "PremiumSheet"
            )
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                val isPremium = doc.getBoolean("isPremium") ?: false

                if (isPremium) {
                    startActivity(
                        Intent(this, CurrentGKActivity::class.java)
                    )
                } else {
                    PremiumBottomSheet().show(
                        supportFragmentManager,
                        "PremiumSheet"
                    )
                }
            }
            .addOnFailureListener {
                PremiumBottomSheet().show(
                    supportFragmentManager,
                    "PremiumSheet"
                )
            }
    }
}
