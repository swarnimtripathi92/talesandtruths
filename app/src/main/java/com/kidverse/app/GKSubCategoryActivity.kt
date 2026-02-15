package com.kidverse.app

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GKSubCategoryActivity : AppCompatActivity() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gk_subcategory)

        val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID).orEmpty()
        val categoryTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE).orEmpty()

        findViewById<TextView>(R.id.tvCategoryTitle).text = categoryTitle.ifBlank { "Static GK" }
        findViewById<TextView>(R.id.tvCategoryIntro).text = "Loading fun facts..."
        findViewById<TextView>(R.id.tvFactCount).text = "Loading..."

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadCategoryContent(categoryId, categoryTitle)
    }

    private fun loadCategoryContent(categoryId: String, fallbackTitle: String) {
        if (categoryId.isBlank()) {
            bindCategory(fallbackTitle, StaticGKContentRepository.getCategoryContent(""))
            return
        }

        firestore.collection(COLLECTION_STATIC_GK)
            .document(categoryId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val remoteTitle = snapshot.getString("title").orEmpty()
                    val intro = snapshot.getString("intro").orEmpty()
                    val factMaps = snapshot.get("facts") as? List<Map<String, Any>> ?: emptyList()

                    val facts = factMaps.map { map ->
                        FactItem(
                            icon = map["icon"]?.toString().orEmpty(),
                            title = map["title"]?.toString().orEmpty(),
                            text = map["text"]?.toString().orEmpty(),
                            imageUrl = map["imageUrl"]?.toString()?.takeIf { it.isNotBlank() }
                                ?: map["image_url"]?.toString().orEmpty()
                        )
                    }.filter { it.title.isNotBlank() || it.text.isNotBlank() }

                    val content = StaticGKContentRepository.CategoryContent(
                        intro = intro.ifBlank { "Explore and learn with fun facts." },
                        facts = if (facts.isNotEmpty()) facts else StaticGKContentRepository.getCategoryContent(categoryId).facts
                    )

                    bindCategory(remoteTitle.ifBlank { fallbackTitle }, content)
                } else {
                    bindCategory(fallbackTitle, StaticGKContentRepository.getCategoryContent(categoryId))
                }
            }
            .addOnFailureListener { err ->
                Log.e(TAG, "Failed to load static GK from Firestore", err)
                bindCategory(fallbackTitle, StaticGKContentRepository.getCategoryContent(categoryId))
            }
    }

    private fun bindCategory(displayTitle: String, content: StaticGKContentRepository.CategoryContent) {
        findViewById<TextView>(R.id.tvCategoryTitle).text = displayTitle.ifBlank { "Static GK" }
        findViewById<TextView>(R.id.tvCategoryIntro).text = content.intro
        findViewById<TextView>(R.id.tvFactCount).text = "${content.facts.size} fun facts"

        val recyclerFacts = findViewById<RecyclerView>(R.id.recyclerFacts)
        recyclerFacts.layoutManager = LinearLayoutManager(this)
        recyclerFacts.adapter = FactAdapter(content.facts)
    }

    companion object {
        private const val TAG = "GKSubCategoryActivity"
        private const val COLLECTION_STATIC_GK = "staticgk"
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_CATEGORY_TITLE = "extra_category_title"
    }
}
