import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Demo-only: the key is compiled into the APK (TECH-009 requires a backend for production).
val nvidiaProperties = Properties().apply {
    val file = rootProject.file("nvidia.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun nvidiaValue(key: String, default: String = ""): String =
    "\"" + (nvidiaProperties.getProperty(key) ?: default).trim()
        .replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.sportprog3"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.sportprog3"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NVIDIA_API_KEY", nvidiaValue("NVIDIA_API_KEY"))
        buildConfigField("String", "NVIDIA_MODEL", nvidiaValue("NVIDIA_MODEL", "nvidia/nemotron-3.5-lightning-30b-a3b"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")
}