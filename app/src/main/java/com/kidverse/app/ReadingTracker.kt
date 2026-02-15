package com.kidverse.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar

object ReadingTracker {

    private const val PREF_NAME = "kidverse_prefs"
    private const val KEY_LAST_READ_STORY_ID = "lastReadStoryId"
    private const val KEY_WEEKLY_READ_PREFIX = "weeklyReadStoryIds_"

    fun recordStoryRead(context: Context, storyId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val weekKey = getCurrentWeekKey()
        val weeklyKey = "$KEY_WEEKLY_READ_PREFIX$weekKey"
        val existing = prefs.getStringSet(weeklyKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(storyId)

        prefs.edit()
            .putString(KEY_LAST_READ_STORY_ID, storyId)
            .putStringSet(weeklyKey, existing)
            .apply()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val payload = mapOf(
            "storyId" to storyId,
            "readAt" to System.currentTimeMillis(),
            "weekKey" to weekKey
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("readingHistory")
            .document("${weekKey}_$storyId")
            .set(payload, SetOptions.merge())
    }

    fun getLocalWeeklyReadCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val weeklyKey = "$KEY_WEEKLY_READ_PREFIX${getCurrentWeekKey()}"
        return prefs.getStringSet(weeklyKey, emptySet())?.size ?: 0
    }

    private fun getCurrentWeekKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}_W${cal.get(Calendar.WEEK_OF_YEAR)}"
    }
}
