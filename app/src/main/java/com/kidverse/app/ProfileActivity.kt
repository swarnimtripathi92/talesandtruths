package com.kidverse.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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
                    updateUI()
                }
        }
    }

    private fun updateUI() {
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)

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

            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE

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

            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE

            // 👶 Child Profile
            btnChildProfile.visibility = View.VISIBLE
            btnChildProfile.setOnClickListener {
                startActivity(Intent(this, ChildProfileActivity::class.java))
            }

            // 📖 Reading History
            btnReadingHistory.visibility = View.VISIBLE
            btnReadingHistory.setOnClickListener {
                startActivity(Intent(this, ReadingHistoryActivity::class.java))
            }

            // 📊 Reading Stats
            btnReadingStats.visibility = View.VISIBLE
            btnReadingStats.setOnClickListener {
                startActivity(Intent(this, ReadingStatsActivity::class.java))
            }

            // 🎯 Daily Reading Goal
            btnReadingGoal.visibility = View.VISIBLE
            btnReadingGoal.setOnClickListener {
                startActivity(Intent(this, ReadingGoalActivity::class.java))
            }

            // 🏆 Badges
            btnBadges.visibility = View.VISIBLE
            btnBadges.setOnClickListener {
                startActivity(Intent(this, BadgesActivity::class.java))
            }

            // 📈 Weekly Graph
            btnWeeklyGraph.visibility = View.VISIBLE
            btnWeeklyGraph.setOnClickListener {
                startActivity(Intent(this, WeeklyGraphActivity::class.java))
            }

            // 👑 Admin Panel
            btnAdmin.visibility = View.GONE
            checkAdminAndUpdateUI()
        }
    }

    private fun checkAdminAndUpdateUI() {
        val user = auth.currentUser ?: return
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)

        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    btnAdmin.visibility = View.VISIBLE
                    btnAdmin.setOnClickListener {
                        startActivity(
                            Intent(
                                this@ProfileActivity,
                                AdminPanelActivity::class.java
                            )
                        )
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
