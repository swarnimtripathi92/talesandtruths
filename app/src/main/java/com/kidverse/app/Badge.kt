package com.kidverse.app

data class Badge(
    val title: String = "",
    val subtitle: String = "",
    val icon: String = "🏆",
    val requiredStars: Int = 0,
    val track: String = "story",
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0L
)
