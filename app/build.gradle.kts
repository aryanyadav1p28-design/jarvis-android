plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jarvis.assistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jarvis.assistant"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }

    signingConfigs {
        create("jarvisStable") {
            val stableKeystore = rootProject.file("signing/jarvis-signing.jks")
            if (stableKeystore.exists()) {
                storeFile = stableKeystore
                storePassword = System.getenv("JARVIS_KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("JARVIS_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("JARVIS_KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        getByName("debug") {
            val stableKeystore = rootProject.file("signing/jarvis-signing.jks")
            if (stableKeystore.exists()) {
                signingConfig = signingConfigs.getByName("jarvisStable")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}
