package com.kidverse.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object BadgeManager {

    fun checkAndUnlockBadges(
        totalStories: Int,
        totalMinutes: Int,
        totalWords: Int
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val badgeRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("badges")

        if (totalStories >= 1)
            unlock(badgeRef, "FIRST_STORY", "🌟 First Story")

        if (totalStories >= 10)
            unlock(badgeRef, "BOOKWORM", "📚 Bookworm")

        if (totalMinutes >= 100)
            unlock(badgeRef, "TIME_CHAMP", "⏱️ Time Champ")

        if (totalWords >= 5000)
            unlock(badgeRef, "WORD_MASTER", "🧠 Word Master")
    }

    private fun unlock(
        ref: CollectionReference,
        badgeId: String,
        title: String
    ) {
        ref.document(badgeId).get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                ref.document(badgeId).set(
                    mapOf(
                        "title" to title,
                        "unlockedAt" to System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
