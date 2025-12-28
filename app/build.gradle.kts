plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 🔥 VERY IMPORTANT
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.talesandtruths"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.talesandtruths"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 🔥 Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // 🔥 Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // (baad me kaam aayega)
    // implementation("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-storage-ktx")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.glide)
    kapt(libs.glideCompiler)
// 🔐 Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    // 🔐 Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation ("com.google.firebase:firebase-storage-ktx")


    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
