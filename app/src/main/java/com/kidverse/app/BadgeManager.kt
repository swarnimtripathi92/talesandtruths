package com.kidverse.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object BadgeManager {

    private data class BadgeDefinition(
        val id: String,
        val title: String,
        val subtitle: String,
        val icon: String,
        val requiredStars: Int,
        val track: String
    )

    private val storyBadges = listOf(
        BadgeDefinition("STORY_SPARK", "Story Spark", "Read your first story", "📘", 5, "story"),
        BadgeDefinition("PAGE_EXPLORER", "Page Explorer", "Keep turning pages", "📖", 20, "story"),
        BadgeDefinition("STORY_TRAILBLAZER", "Story Trailblazer", "Building a strong reading habit", "🧭", 45, "story"),
        BadgeDefinition("WORD_WIZARD", "Word Wizard", "Big vocabulary power", "🧠", 70, "story"),
        BadgeDefinition("LEGEND_READER", "Legend Reader", "You are a story superstar", "👑", 100, "story")
    )

    private val gkBadges = listOf(
        BadgeDefinition("GK_STARTER", "GK Starter", "First steps into knowledge", "🌍", 5, "gk"),
        BadgeDefinition("FACT_FINDER", "Fact Finder", "Discovering amazing facts", "🔎", 20, "gk"),
        BadgeDefinition("NEWS_SCOUT", "News Scout", "Staying curious every day", "📰", 45, "gk"),
        BadgeDefinition("QUIZ_CHAMP", "Quiz Champ", "Smart answers, sharp mind", "🎯", 70, "gk"),
        BadgeDefinition("GK_GALAXY", "GK Galaxy", "Master of current + static GK", "🚀", 100, "gk")
    )

    fun checkAndUnlockBadges(totalStoryWords: Int, totalGkStars: Int) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(user.uid)
        val badgeRef = userDoc.collection("badges")

        val storyStars = storyWordsToStars(totalStoryWords)
        val bestBadge = getBestUnlockedBadge(storyStars, totalGkStars)

        userDoc.set(
            mapOf(
                "storyWords" to totalStoryWords,
                "storyStars" to storyStars,
                "gkStars" to totalGkStars,
                "totalStars" to storyStars + totalGkStars,
                "currentBadge" to bestBadge.title,
                "currentBadgeIcon" to bestBadge.icon
            ),
            SetOptions.merge()
        )

        for (definition in storyBadges + gkBadges) {
            val stars = if (definition.track == "story") storyStars else totalGkStars
            val unlocked = stars >= definition.requiredStars
            badgeRef.document(definition.id).set(
                mapOf(
                    "title" to definition.title,
                    "subtitle" to definition.subtitle,
                    "icon" to definition.icon,
                    "requiredStars" to definition.requiredStars,
                    "track" to definition.track,
                    "unlocked" to unlocked,
                    "unlockedAt" to if (unlocked) System.currentTimeMillis() else 0L
                ),
                SetOptions.merge()
            )
        }
    }

    fun storyWordsToStars(totalStoryWords: Int): Int {
        val stars = totalStoryWords / 250
        return stars.coerceIn(0, 100)
    }

    private fun getBestUnlockedBadge(storyStars: Int, gkStars: Int): BadgeDefinition {
        val unlocked = (storyBadges.filter { storyStars >= it.requiredStars } +
            gkBadges.filter { gkStars >= it.requiredStars })
            .ifEmpty { listOf(storyBadges.first()) }
        return unlocked.maxBy { it.requiredStars }
    }
}
