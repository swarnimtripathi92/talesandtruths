package com.example.talesandtruths

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
            OurWorldItem("asia", "Asia", "Largest continent", R.drawable.gk_asia),
            OurWorldItem("africa", "Africa", "Wildlife & deserts", R.drawable.gk_africa),
            OurWorldItem("europe", "Europe", "History & culture", R.drawable.gk_europe),
            OurWorldItem("north_america", "North America", "Modern cities", R.drawable.gk_north_america),
            OurWorldItem("south_america", "South America", "Rainforests", R.drawable.gk_south_america),
            OurWorldItem("australia", "Australia", "Smallest continent", R.drawable.gk_australia),
            OurWorldItem("antarctica", "Antarctica", "Frozen land", R.drawable.gk_antarctica)
        )

        recycler.adapter = OurWorldAdapter(continents) { continent ->

            // 🔥 YAHIN par click ka code aata hai
            val intent = Intent(this, ContinentDetailActivity::class.java)
            intent.putExtra("continentId", continent.id)
            startActivity(intent)

        }
    }
}
