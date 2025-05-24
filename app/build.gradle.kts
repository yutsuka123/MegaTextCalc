plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.megatextcalc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.megatextcalc"
        minSdk = 25
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"  // Updated from 1.5.0 to work with Kotlin 1.9.20
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.compose.ui:ui:1.5.4")  // Updated from 1.5.0
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")  // Updated from 1.5.0
    implementation("androidx.wear.compose:compose-foundation:1.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")  // Updated from 1.5.0
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 既存の ui/ui-tooling-preview/foundation/foundation-layout はそのまま
    implementation ("androidx.compose.material:material:1.6.0")
    // デバッグ用ツール
    debugImplementation ("androidx.compose.ui:ui-tooling:1.6.0")
}

