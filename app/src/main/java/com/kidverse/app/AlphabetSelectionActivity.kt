package com.kidverse.app

import android.content.Intent   // ⭐ IMPORTANT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlphabetSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet_selection)

        val recycler = findViewById<RecyclerView>(R.id.recyclerAlphabets)
        recycler.layoutManager = GridLayoutManager(this, 4)

        val alphabets = ('A'..'Z').toList()

        recycler.adapter = AlphabetAdapter(alphabets) { letter: Char ->
            val intent = Intent(
                this@AlphabetSelectionActivity,
                AlphabetTraceActivity::class.java
            )
            intent.putExtra("LETTER", letter.toString())
            startActivity(intent)
        }
    }
}
