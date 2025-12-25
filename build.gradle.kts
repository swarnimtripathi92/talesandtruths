plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // 🔥 Firebase ke liye REQUIRED
    alias(libs.plugins.google.services) apply false
}
