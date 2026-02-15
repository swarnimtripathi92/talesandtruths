package com.kidverse.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object GKTracker {

    private const val PREF_NAME = "kidverse_prefs"
    private const val KEY_GK_READ_SET = "gkReadSet"

    fun recordGkRead(context: Context, contentId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_GK_READ_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (current.add(contentId)) {
            prefs.edit().putStringSet(KEY_GK_READ_SET, current).apply()
            syncGkStars(current.size)
        }
    }

    private fun syncGkStars(uniqueReadCount: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val gkStars = (uniqueReadCount * 5).coerceAtMost(100)

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(mapOf("gkStars" to gkStars), SetOptions.merge())
    }
}
