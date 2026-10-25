plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.manas.medbuddy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.manas.medbuddy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val placesKey = (project.findProperty("PLACES_API_KEY") as String?) ?: ""
        buildConfigField("String", "PLACES_API_KEY", "\"$placesKey\"")
        manifestPlaceholders["PLACES_API_KEY"] = placesKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.01"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // ML Kit Text Recognition for AI Prescription Scanning
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Location, Maps & JSON dependencies for SOS & Hospital Finder
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("com.google.android.gms:play-services-maps:20.1.0")
    implementation("com.google.maps.android:maps-compose:6.5.0")
    implementation("com.google.code.gson:gson:2.14.0")

    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
