package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OceanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocean)

        val recycler = findViewById<RecyclerView>(R.id.recyclerOceans)
        recycler.layoutManager = LinearLayoutManager(this)

        val oceans = listOf(
            OurWorldItem(
                id = "pacific",
                title = "Pacific Ocean",
                tagline = "Largest & deepest ocean",
                imageRes = R.drawable.gk_pacific
            ),
            OurWorldItem(
                id = "atlantic",
                title = "Atlantic Ocean",
                tagline = "Separates continents",
                imageRes = R.drawable.gk_atlantic
            ),
            OurWorldItem(
                id = "indian",
                title = "Indian Ocean",
                tagline = "Warm & rich in marine life",
                imageRes = R.drawable.gk_indian
            ),
            OurWorldItem(
                id = "arctic",
                title = "Arctic Ocean",
                tagline = "Smallest & coldest ocean",
                imageRes = R.drawable.gk_arctic
            ),
            OurWorldItem(
                id = "antarctic",
                title = "Antarctic Ocean",
                tagline = "Surrounds Antarctica",
                imageRes = R.drawable.gk_antarctic
            )
        )

        recycler.adapter = OurWorldAdapter(oceans) { ocean ->
            val intent = Intent(this, OceanDetailActivity::class.java)
            intent.putExtra("oceanId", ocean.id)
            startActivity(intent)
        }
    }
}
