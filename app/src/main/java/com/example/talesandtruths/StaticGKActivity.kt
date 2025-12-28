package com.example.talesandtruths

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StaticGKActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static_gk)

        val recycler = findViewById<RecyclerView>(R.id.recyclerStaticGK)
        recycler.layoutManager = LinearLayoutManager(this)

        val categories = listOf(
            StaticGKCategory(
                "our_world",
                "Our World",
                "Know about our world",
                R.drawable.gk_our_world
            ),
            StaticGKCategory(
                "animals",
                "Animals & Birds",
                "Learn about amazing animals",
                R.drawable.gk_animals_birds
            ),
            StaticGKCategory(
                "plants",
                "Plants & Nature",
                "Discover plants and trees",
                R.drawable.gk_plants_nature
            ),
            StaticGKCategory(
                "space",
                "Space & Science",
                "Explore space and science",
                R.drawable.gk_space_science
            ),
            StaticGKCategory(
                "india",
                "India GK",
                "Know about our country",
                R.drawable.gk_india_gk
            ),
            StaticGKCategory(
                "body",
                "Human Body",
                "Learn how our body works",
                R.drawable.gk_human_body
            ),
            StaticGKCategory(
                "safety",
                "Safety & Good Habits",
                "Stay safe and healthy",
                R.drawable.gk_safety_habits
            ),
            StaticGKCategory(
                "colours",
                "Colours, Shapes & Numbers",
                "Learn colours and numbers",
                R.drawable.gk_colours_shapes_numbers
            ),
            StaticGKCategory(
                "people",
                "People & Professions",
                "Know different professions",
                R.drawable.gk_people_professions
            ),
            StaticGKCategory(
                "fun",
                "Fun Facts",
                "Amazing facts to enjoy",
                R.drawable.gk_fun_facts
            )
        )

        recycler.adapter = StaticGKAdapter(categories) { category ->
            val intent = Intent(this, GKSubCategoryActivity::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("title", category.title)
            startActivity(intent)
        }
    }
}
