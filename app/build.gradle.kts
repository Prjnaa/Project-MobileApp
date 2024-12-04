import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

val secretPropertiesFile = rootProject.file("secrets.properties")
val secretProperties = Properties()
if (secretPropertiesFile.exists()) {
    secretProperties.load(FileInputStream(secretPropertiesFile))
} else {
    secretProperties["GOOGLE_API_TOKEN"] = "\"default-token\""
}

android {
    namespace = "com.project.projectmap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.projectmap"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "GOOGLE_API_TOKEN",
            secretProperties["GOOGLE_API_TOKEN"] as String
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}



dependencies {
    // Core AndroidX dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)

    // Jetpack Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.3.1")

    // Debugging and Testing
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore.ktx.v2481)

    // Google Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.kotlinx.coroutines.play.services)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.androidx.camera.extensions)
}
