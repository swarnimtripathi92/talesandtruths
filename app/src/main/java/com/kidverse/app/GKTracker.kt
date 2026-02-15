package com.kidverse.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object GKTracker {

    // Backward-compatible signature (old call sites passed context)
    fun recordGkRead(context: Context, contentId: String) {
        recordGkRead(contentId)
    }

    fun recordGkRead(contentId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userDoc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)

        userDoc.collection("gkHistory")
            .document(contentId)
            .set(
                mapOf(
                    "contentId" to contentId,
                    "readAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                BadgeManager.refreshFromCloud()
            }
    }
}
