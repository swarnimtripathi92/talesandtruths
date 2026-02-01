package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LandFormsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landforms)

        val recycler = findViewById<RecyclerView>(R.id.recyclerLandForms)
        recycler.layoutManager = LinearLayoutManager(this)

        val landforms = listOf(
            OurWorldItem(
                id = "mountains",
                title = "Mountains",
                tagline = "Very high land",
                imageRes = R.drawable.gk_mountains1
            ),
            OurWorldItem(
                id = "hills",
                title = "Hills",
                tagline = "Small raised land",
                imageRes = R.drawable.gk_hills
            ),
            OurWorldItem(
                id = "plains",
                title = "Plains",
                tagline = "Flat fertile land",
                imageRes = R.drawable.gk_plains
            ),
            OurWorldItem(
                id = "deserts",
                title = "Deserts",
                tagline = "Dry sandy land",
                imageRes = R.drawable.gk_deserts
            ),
            OurWorldItem(
                id = "valleys",
                title = "Valleys",
                tagline = "Land between mountains",
                imageRes = R.drawable.gk_valleys
            ),
            OurWorldItem(
                id = "islands",
                title = "Islands",
                tagline = "Land surrounded by water",
                imageRes = R.drawable.gk_islands
            ),
            OurWorldItem(
                id = "peninsulas",
                title = "Peninsulas",
                tagline = "Land surrounded by water on 3 sides",
                imageRes = R.drawable.gk_peninsulas
            )
        )

        recycler.adapter = OurWorldAdapter(landforms) { item ->
            val intent = Intent(this, LandFormsDetailActivity::class.java)
            intent.putExtra("landformId", item.id)
            startActivity(intent)
        }
    }
}
