package com.kidverse.app

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddStoryActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val blocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: ContentAdapter

    private var storyId: String? = null
    private var coverImageUrl: String = ""

    // 🔹 Block image picker
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadBlockImage(it) }
        }

    // 🔹 Cover image picker
    private val coverPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadCoverImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val ivCover = findViewById<ImageView>(R.id.ivCoverImage)
        val btnCover = findViewById<Button>(R.id.btnSelectCover)
        val etTitle = findViewById<EditText>(R.id.etStoryTitle)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val rbDraft = findViewById<RadioButton>(R.id.rbDraft)
        val rbPublished = findViewById<RadioButton>(R.id.rbPublished)
        val btnAddText = findViewById<Button>(R.id.btnAddText)
        val btnAddImage = findViewById<Button>(R.id.btnAddImage)
        val btnSave = findViewById<Button>(R.id.btnSaveStory)
        val rvBlocks = findViewById<RecyclerView>(R.id.rvBlocks)

        // 📖 Blocks RecyclerView
        adapter = ContentAdapter(blocks)
        rvBlocks.layoutManager = LinearLayoutManager(this)
        rvBlocks.adapter = adapter

        // 🔥 Drag & drop reorder
        ItemTouchHelper(BlockReorderCallback(adapter))
            .attachToRecyclerView(rvBlocks)

        // 🔥 Delete block
        adapter.onDeleteClick = { block, pos ->
            if (block.type == "image" && block.value.isNotEmpty()) {
                try {
                    FirebaseStorage.getInstance()
                        .getReferenceFromUrl(block.value)
                        .delete()
                } catch (_: Exception) {}
            }
            blocks.removeAt(pos)
            adapter.notifyItemRemoved(pos)
        }

        // 📝 Edit mode
        storyId = intent.getStringExtra("storyId")
        if (storyId != null) {
            btnSave.text = "Update Story"
            loadStoryForEdit(etTitle, ivCover, spinnerCategory, rbDraft, rbPublished)
        }

        // 📸 Cover image
        btnCover.setOnClickListener { coverPicker.launch("image/*") }

        // ➕ Add text block
        btnAddText.setOnClickListener {
            blocks.add(ContentBlock("text", ""))
            adapter.notifyItemInserted(blocks.size - 1)
        }

        // 🖼️ Add image block
        btnAddImage.setOnClickListener { imagePicker.launch("image/*") }

        // 💾 Save / Update story
        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "Title required"
                return@setOnClickListener
            }

            val status = if (rbPublished.isChecked) "published" else "draft"
            val category = spinnerCategory.selectedItem.toString()

            val contentList = blocks.map {
                mapOf("type" to it.type, "value" to it.value)
            }

            val data = hashMapOf(
                "title" to title,
                "category" to category,          // 👈 CATEGORY
                "status" to status,
                "audience" to "kids",
                "language" to "en",
                "coverImage" to coverImageUrl,
                "content" to contentList,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (storyId == null) {
                data["createdAt"] = FieldValue.serverTimestamp()
                data["createdBy"] = auth.currentUser?.uid ?: "admin"

                firestore.collection("stories")
                    .add(data)
                    .addOnSuccessListener { finish() }

            } else {
                firestore.collection("stories")
                    .document(storyId!!)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener { finish() }
            }
        }
    }

    // 🔄 Load story for edit
    private fun loadStoryForEdit(
        etTitle: EditText,
        ivCover: ImageView,
        spinnerCategory: Spinner,
        rbDraft: RadioButton,
        rbPublished: RadioButton
    ) {
        firestore.collection("stories").document(storyId!!)
            .get()
            .addOnSuccessListener { doc ->

                etTitle.setText(doc.getString("title") ?: "")

                coverImageUrl = doc.getString("coverImage") ?: ""
                if (coverImageUrl.isNotEmpty()) {
                    Glide.with(this).load(coverImageUrl).into(ivCover)
                }

                val savedCategory = doc.getString("category") ?: "General"
                val categories = resources.getStringArray(R.array.story_categories)
                val index = categories.indexOf(savedCategory)
                if (index >= 0) spinnerCategory.setSelection(index)

                if (doc.getString("status") == "published")
                    rbPublished.isChecked = true
                else rbDraft.isChecked = true

                blocks.clear()
                val list = doc.get("content") as? List<Map<String, String>> ?: emptyList()
                for (item in list) {
                    blocks.add(ContentBlock(item["type"]!!, item["value"]!!))
                }
                adapter.notifyDataSetChanged()
            }
    }

    // 📤 Upload block image
    private fun uploadBlockImage(uri: Uri) {
        val ref = storage.reference.child("stories/blocks/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                blocks.add(ContentBlock("image", it.toString()))
                adapter.notifyItemInserted(blocks.size - 1)
            }
        }
    }

    // 📤 Upload cover image
    private fun uploadCoverImage(uri: Uri) {
        val ref = storage.reference.child("stories/covers/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                coverImageUrl = it.toString()
                Glide.with(this)
                    .load(coverImageUrl)
                    .into(findViewById(R.id.ivCoverImage))
            }
        }
    }
}
