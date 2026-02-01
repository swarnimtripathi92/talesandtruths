package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContinentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_continents)

        val recycler = findViewById<RecyclerView>(R.id.recyclerContinents)
        recycler.layoutManager = LinearLayoutManager(this)

        val continents = listOf(
            OurWorldItem(
                id = "asia",
                title = "Asia",
                tagline = "Largest continent",
                imageRes = R.drawable.gk_asia
            ),
            OurWorldItem(
                id = "africa",
                title = "Africa",
                tagline = "Wildlife & deserts",
                imageRes = R.drawable.gk_africa
            ),
            OurWorldItem(
                id = "europe",
                title = "Europe",
                tagline = "History & culture",
                imageRes = R.drawable.gk_europe
            ),
            OurWorldItem(
                id = "north_america",
                title = "North America",
                tagline = "Modern cities",
                imageRes = R.drawable.gk_north_america
            ),
            OurWorldItem(
                id = "south_america",
                title = "South America",
                tagline = "Rainforests",
                imageRes = R.drawable.gk_south_america
            ),
            OurWorldItem(
                id = "australia",
                title = "Australia",
                tagline = "Smallest continent",
                imageRes = R.drawable.gk_australia
            ),
            OurWorldItem(
                id = "antarctica",
                title = "Antarctica",
                tagline = "Frozen land",
                imageRes = R.drawable.gk_antarctica
            )
        )

        recycler.adapter = OurWorldAdapter(continents) { continent ->
            val intent = Intent(this, ContinentDetailActivity::class.java)
            intent.putExtra("continentId", continent.id)
            startActivity(intent)
        }
    }
}
