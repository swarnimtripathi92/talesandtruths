package com.example.talesandtruths
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AlphabetTraceActivity : AppCompatActivity() {

    private lateinit var tracingView: TracingView
    private lateinit var imgAlphabet: ImageView
    private lateinit var txtTitle: TextView

    private val alphabets = ('A'..'Z').toList()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet_trace)

        // Views
        tracingView = findViewById(R.id.tracingView)
        imgAlphabet = findViewById(R.id.imgAlphabet)
        txtTitle = findViewById(R.id.txtTitle)

        // Selected letter from previous screen
        val selectedLetter = intent.getStringExtra("LETTER") ?: "A"
        currentIndex = alphabets.indexOf(selectedLetter[0]).coerceAtLeast(0)

        loadAlphabet()

        // Clear button
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tracingView.clearCanvas()
        }

        // Next / Validate button
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (tracingView.isTraceGood()) {
                showSuccessDialog()
            } else {
                showTryAgainDialog()
            }
        }
    }

    // 🔤 Load alphabet image + title
    private fun loadAlphabet() {
        val letter = alphabets[currentIndex]
        txtTitle.text = "Trace the letter $letter"

        val resId = resources.getIdentifier(
            "${letter.lowercaseChar()}_trace",
            "drawable",
            packageName
        )
        imgAlphabet.setImageResource(resId)
    }

    // 🎉 Success dialog
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage(
                "You have won +10 points 🎁\n\n" +
                        "Want to draw another alphabet?"
            )
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                finish()   // back to AlphabetSelectionActivity
            }
            .setNegativeButton("No") { _, _ ->
                finish()
            }
            .show()
    }

    // ❌ Try again dialog
    private fun showTryAgainDialog() {
        AlertDialog.Builder(this)
            .setTitle("❌ Try Again")
            .setMessage("Please trace the letter carefully")
            .setPositiveButton("Retry") { _, _ ->
                tracingView.clearCanvas()
            }
            .show()
    }
}
