package com.example.talesandtruths

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ContinentDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var fullTextToSpeak = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(ThemePref.get(this))
        setContentView(R.layout.activity_continent_detail)

        tts = TextToSpeech(this, this)

        val continentId = intent.getStringExtra("continentId") ?: return

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val imgHeader = findViewById<ImageView>(R.id.imgHeader)
        val recycler = findViewById<RecyclerView>(R.id.recyclerFacts)

        val btnTextSize = findViewById<TextView>(R.id.btnTextSize)
        val btnTheme = findViewById<TextView>(R.id.btnTheme)
        val btnSpeak = findViewById<TextView>(R.id.btnSpeak)

        recycler.layoutManager = LinearLayoutManager(this)

        btnTextSize.setOnClickListener { showTextSizeDialog() }
        btnTheme.setOnClickListener { showThemeDialog() }
        btnSpeak.setOnClickListener { speakText() }

        FirebaseFirestore.getInstance()
            .collection("continents")
            .document(continentId)
            .get()
            .addOnSuccessListener { doc ->

                tvTitle.text = doc.getString("title") ?: ""

                val imageName = doc.getString("image") ?: "gk_asia"
                val resId = resources.getIdentifier(imageName, "drawable", packageName)
                if (resId != 0) imgHeader.setImageResource(resId)

                val factsRaw = doc.get("facts") as List<Map<String, String>>
                val factList = mutableListOf<FactItem>()

                fullTextToSpeak = ""

                for (item in factsRaw) {
                    val title = item["title"] ?: ""
                    val text = item["text"] ?: ""

                    factList.add(
                        FactItem(
                            icon = item["icon"] ?: "",
                            title = title,
                            text = text
                        )
                    )

                    fullTextToSpeak += "$title. $text. "
                }

                recycler.adapter = FactAdapter(factList)
            }
    }

    /* 🔊 Text To Speech */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    private fun speakText() {
        tts.speak(fullTextToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    /* 🔘 Dialogs */
    private fun showTextSizeDialog() {
        val options = arrayOf("Small", "Medium", "Large")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Text Size")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> TextPref.save(this, TextSizeConfig.SMALL)
                    1 -> TextPref.save(this, TextSizeConfig.MEDIUM)
                    2 -> TextPref.save(this, TextSizeConfig.LARGE)
                }
                recreate()
            }.show()
    }

    private fun showThemeDialog() {
        val options = arrayOf("Light", "Dark", "System")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Theme")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> ThemePref.save(this, AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> ThemePref.save(this, AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> ThemePref.save(this, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                recreate()
            }.show()
    }
}
