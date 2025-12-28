package com.example.talesandtruths

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ChildProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Profile layout
    private lateinit var layoutProfile: LinearLayout
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileDetails: TextView
    private lateinit var btnEditProfile: Button

    // Form layout
    private lateinit var layoutForm: LinearLayout
    private lateinit var etName: EditText
    private lateinit var spAge: Spinner
    private lateinit var spClass: Spinner
    private lateinit var spLanguage: Spinner
    private lateinit var spReading: Spinner
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_profile)

        bindViews()
        setupSpinners()
        loadProfileUI()

        btnEditProfile.setOnClickListener {
            layoutProfile.visibility = View.GONE
            layoutForm.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun bindViews() {
        layoutProfile = findViewById(R.id.layoutProfile)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileDetails = findViewById(R.id.tvProfileDetails)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        layoutForm = findViewById(R.id.layoutForm)
        etName = findViewById(R.id.etChildName)
        spAge = findViewById(R.id.spAge)
        spClass = findViewById(R.id.spClass)
        spLanguage = findViewById(R.id.spLanguage)
        spReading = findViewById(R.id.spReadingLevel)
        btnSave = findViewById(R.id.btnSaveProfile)
    }

    private fun setupSpinners() {
        spAge.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            (3..15).map { it.toString() }
        )

        spClass.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("KG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5")
        )

        spLanguage.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("English", "Hindi")
        )

        spReading.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Beginner", "Intermediate", "Advanced")
        )
    }

    private fun loadProfileUI() {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val profile = doc.get("childProfile") as? Map<*, *>

                if (profile == null) {
                    // First time → show form
                    layoutForm.visibility = View.VISIBLE
                    layoutProfile.visibility = View.GONE
                } else {
                    // Profile exists → show profile card
                    layoutProfile.visibility = View.VISIBLE
                    layoutForm.visibility = View.GONE

                    val name = profile["childName"] as? String ?: ""
                    val age = profile["age"] as? String ?: ""
                    val cls = profile["classLevel"] as? String ?: ""
                    val lang = profile["language"] as? String ?: ""
                    val level = profile["readingLevel"] as? String ?: ""

                    tvProfileName.text = name
                    tvProfileDetails.text =
                        "🎂 Age: $age\n" +
                                "🏫 Class: $cls\n" +
                                "🌐 Language: $lang\n" +
                                "📘 Reading Level: $level"

                    // Prefill form for edit
                    etName.setText(name)
                    setSpinner(spAge, age)
                    setSpinner(spClass, cls)
                    setSpinner(spLanguage, lang)
                    setSpinner(spReading, level)
                }
            }
    }

    private fun saveProfile() {
        val user = auth.currentUser ?: return

        if (etName.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter child name", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "childProfile" to hashMapOf(
                "childName" to etName.text.toString().trim(),
                "age" to spAge.selectedItem.toString(),
                "classLevel" to spClass.selectedItem.toString(),
                "language" to spLanguage.selectedItem.toString(),
                "readingLevel" to spReading.selectedItem.toString(),
                "updatedAt" to System.currentTimeMillis()
            )
        )

        firestore.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Child profile saved ✅", Toast.LENGTH_SHORT).show()
                loadProfileUI()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSpinner(spinner: Spinner, value: String?) {
        value ?: return
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }
}
