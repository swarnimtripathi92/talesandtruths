package com.example.talesandtruths

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
                "continents",
                "Continents",
                "Big lands of the Earth",
                R.drawable.gk_continents
            ),
            OurWorldItem(
                "oceans",
                "Oceans",
                "Huge water worlds",
                R.drawable.gk_oceans
            ),
            OurWorldItem(
                "mountains",
                "Mountains",
                "The tallest places on Earth",
                R.drawable.gk_mountains
            ),
            OurWorldItem(
                "rivers",
                "Rivers",
                "Lifelines of people",
                R.drawable.gk_rivers
            ),
            OurWorldItem(
                "landforms",
                "Landforms",
                "Different shapes of land",
                R.drawable.gk_landforms
            ),
            OurWorldItem(
                "weather",
                "Weather & Seasons",
                "Changes in the sky",
                R.drawable.gk_weather_seasons
            ),
            OurWorldItem(
                "capitals",
                "Countries & Capitals",
                "Homes of people",
                R.drawable.gk_countries_capitals
            ),
            OurWorldItem(
                "symbols",
                "National Symbols",
                "Pride of a country",
                R.drawable.gk_national_symbols
            )
        )

        recycler.adapter = OurWorldAdapter(items) { item ->

            when (item.id) {

                "continents" -> {
                    startActivity(
                        Intent(this, ContinentsActivity::class.java)
                    )
                }

                // future:
                // "oceans" -> startActivity(...)
                // "rivers" -> startActivity(...)
            }
        }

    }
}
