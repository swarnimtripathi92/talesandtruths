package com.example.talesandtruths

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)

        // 🔐 Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            recreate()
        }

        val user = auth.currentUser
        if (user == null) {
            tvName.text = "Welcome"
            tvEmail.text = "Sign in to personalize your experience"
            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            btnAdmin.visibility = View.GONE
        } else {
            tvName.text = user.displayName ?: "User"
            tvEmail.text = user.email ?: ""
            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            btnAdmin.visibility = View.GONE // admin next step
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    recreate()
                }
        }
    }
    private fun updateUI() {
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)

        val user = auth.currentUser

        if (user == null) {
            tvName.text = "Welcome"
            tvEmail.text = "Sign in to personalize your experience"
            btnLogin.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            btnAdmin.visibility = View.GONE
        } else {
            tvName.text = user.displayName ?: "User"
            tvEmail.text = user.email ?: ""
            btnLogin.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            btnAdmin.visibility = View.GONE // admin next step
        }
    }

}
