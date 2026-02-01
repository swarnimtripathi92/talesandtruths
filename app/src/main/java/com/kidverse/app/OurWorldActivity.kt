package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OurWorldActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_our_world)

        val recycler = findViewById<RecyclerView>(R.id.recyclerOurWorld)
        recycler.layoutManager = LinearLayoutManager(this)

        val items = listOf(
            OurWorldItem(
                id = "continents",
                title = "Continents",
                tagline = "Big lands of the Earth",
                imageRes = R.drawable.gk_continents
            ),
            OurWorldItem(
                id = "oceans",
                title = "Oceans",
                tagline = "Huge water worlds",
                imageRes = R.drawable.gk_oceans
            ),
            /*OurWorldItem(
                id = "mountains",
                title = "Mountains",
                tagline = "The tallest places on Earth",
                imageRes = R.drawable.gk_mountains
            ),*/
            OurWorldItem(
                id = "rivers",
                title = "Rivers",
                tagline = "Lifelines of people",
                imageRes = R.drawable.gk_rivers
            ),
            OurWorldItem(
                id = "landforms",
                title = "Landforms",
                tagline = "Different shapes of land",
                imageRes = R.drawable.gk_landforms
            ),
            OurWorldItem(
                id = "weather",
                title = "Weather & Seasons",
                tagline = "Changes in the sky",
                imageRes = R.drawable.gk_weather_seasons
            ),
            OurWorldItem(
                id = "capitals",
                title = "Countries & Capitals",
                tagline = "Homes of people",
                imageRes = R.drawable.gk_countries_capitals
            ),
            OurWorldItem(
                id = "symbols",
                title = "National Symbols",
                tagline = "Pride of a country",
                imageRes = R.drawable.gk_national_symbols
            )
        )

        recycler.adapter = OurWorldAdapter(items) { item ->
            when (item.id) {

                "continents" -> {
                    startActivity(
                        Intent(this, ContinentsActivity::class.java)
                    )
                }

                // 🔜 Future ready
                "oceans" -> startActivity(Intent(this, OceanActivity::class.java))
                "landforms" -> startActivity(Intent(this, LandFormsActivity::class.java))
                // "rivers" -> startActivity(Intent(this, RiversActivity::class.java))
                // "mountains" -> startActivity(Intent(this, MountainsActivity::class.java))
            }
        }
    }
}
