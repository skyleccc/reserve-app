plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.reserve.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.reserve.app"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.common)
    implementation(libs.firebase.database)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // additional dependencies
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Add the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Firebase Database
    implementation("com.google.firebase:firebase-database")

    // Add Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Add Firestore (optional)
    implementation("com.google.firebase:firebase-firestore")

    // Google Sign-in
    implementation("com.google.android.gms:play-services-auth:20.7.0");

    // Credential Management
    implementation("androidx.credentials:credentials-play-services-auth:1.0.0");
    implementation("androidx.credentials:credentials:1.2.0-alpha03");
}