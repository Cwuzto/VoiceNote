plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.voicenote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.voicenote"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room: xuất schema để dễ viết Migration
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas"
                )
            }
        }
        vectorDrawables.useSupportLibrary = true
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

    // Bật ViewBinding để giữ nguyên UI bạn đã làm
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // UI/AndroidX sẵn có
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Lifecycle (Java-compatible)
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.6")

    // Room (Java) — dùng annotationProcessor, KHÔNG dùng kapt
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // (tuỳ chọn) Paging: implementation("androidx.room:room-paging:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
