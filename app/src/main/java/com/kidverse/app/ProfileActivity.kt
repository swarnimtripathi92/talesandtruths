package com.kidverse.app
import com.kidverse.app.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 🔐 Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, 1001)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            updateUI()
        }

        updateUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result ?: return

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    ensureUserDocument()
                    updateUI()
                }
        }
    }

    // 🔐 Ensure Firestore user document exists
    private fun ensureUserDocument() {
        val user = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val data = hashMapOf(
                    "isPremium" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userRef.set(data)
            }
        }
    }

    private fun updateUI() {
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPremium = findViewById<TextView>(R.id.tvPremiumStatus)
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)
        val btnTogglePremium = findViewById<Button>(R.id.btnTogglePremium)

        val btnChildProfile = findViewById<Button>(R.id.btnChildProfile)
        val btnReadingHistory = findViewById<Button>(R.id.btnReadingHistory)
        val btnReadingStats = findViewById<Button>(R.id.btnReadingStats)
        val btnReadingGoal = findViewById<Button>(R.id.btnReadingGoal)
        val btnBadges = findViewById<Button>(R.id.btnBadges)
        val btnWeeklyGraph = findViewById<Button>(R.id.btnWeeklyGraph)

        val user = auth.currentUser

        if (user == null) {
            tvName.text = "Welcome"
            tvEmail.text = "Sign in to personalize your experience"
            tvPremium.text = ""
            tvPremium.visibility = View.GONE

            Glide.with(this)
                .load(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(imgAvatar)

            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            btnTogglePremium.visibility = View.GONE

            btnChildProfile.visibility = View.GONE
            btnReadingHistory.visibility = View.GONE
            btnReadingStats.visibility = View.GONE
            btnReadingGoal.visibility = View.GONE
            btnBadges.visibility = View.GONE
            btnWeeklyGraph.visibility = View.GONE
            btnAdmin.visibility = View.GONE

        } else {
            tvName.text = user.displayName ?: "User"
            tvEmail.text = user.email ?: ""
            tvPremium.visibility = View.VISIBLE

            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(imgAvatar)

            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE

            btnChildProfile.visibility = View.VISIBLE
            btnReadingHistory.visibility = View.VISIBLE
            btnReadingStats.visibility = View.VISIBLE
            btnReadingGoal.visibility = View.VISIBLE
            btnBadges.visibility = View.VISIBLE
            btnWeeklyGraph.visibility = View.VISIBLE

            btnChildProfile.setOnClickListener {
                startActivity(Intent(this, ChildProfileActivity::class.java))
            }
            btnReadingHistory.setOnClickListener {
                startActivity(Intent(this, ReadingHistoryActivity::class.java))
            }
            btnReadingStats.setOnClickListener {
                startActivity(Intent(this, ReadingStatsActivity::class.java))
            }
            btnReadingGoal.setOnClickListener {
                startActivity(Intent(this, ReadingGoalActivity::class.java))
            }
            btnBadges.setOnClickListener {
                startActivity(Intent(this, BadgesActivity::class.java))
            }
            btnWeeklyGraph.setOnClickListener {
                startActivity(Intent(this, WeeklyGraphActivity::class.java))
            }

            loadPremiumStatus()

            if (BuildConfig.DEBUG) {
                btnTogglePremium.visibility = View.VISIBLE
                btnTogglePremium.setOnClickListener {
                    togglePremium()
                }
            } else {
                btnTogglePremium.visibility = View.GONE
            }

            checkAdminAndUpdateUI()
        }
    }

    // 💎 Load premium status
    private fun loadPremiumStatus() {
        val user = auth.currentUser ?: return
        val tvPremium = findViewById<TextView>(R.id.tvPremiumStatus)

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val isPremium = doc.getBoolean("isPremium") ?: false
                tvPremium.text =
                    if (isPremium) "💎 Premium User" else "🆓 Free User"
            }
    }

    // 🔁 DEV ONLY – Toggle premium
    private fun togglePremium() {
        val user = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener { doc ->
            val current = doc.getBoolean("isPremium") ?: false
            userRef.update("isPremium", !current)
                .addOnSuccessListener {
                    Toast.makeText(this, "Premium toggled (DEV)", Toast.LENGTH_SHORT).show()
                    loadPremiumStatus()
                }
        }
    }

    // 👑 Admin check
    private fun checkAdminAndUpdateUI() {
        val user = auth.currentUser ?: return
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)

        firestore.collection("admins")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    btnAdmin.visibility = View.VISIBLE
                    btnAdmin.setOnClickListener {
                        startActivity(Intent(this, AdminPanelActivity::class.java))
                    }
                    Log.d("ADMIN_CHECK", "Admin user: ${user.uid}")
                } else {
                    btnAdmin.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                btnAdmin.visibility = View.GONE
            }
    }
}
