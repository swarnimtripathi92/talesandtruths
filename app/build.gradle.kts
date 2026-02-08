plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    kotlin("kapt")
}

android {
    namespace = "com.kidverse.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kidverse.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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

    // 🔹 AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 🔥 Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // 🔐 Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // 📊 MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 📄 PDF Zoom support (for PdfRenderer pages)
    implementation("com.github.chrisbanes:PhotoView:2.3.0")


    // 🖼 Glide
    implementation(libs.glide)
    kapt(libs.glideCompiler)

    // 🧪 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
