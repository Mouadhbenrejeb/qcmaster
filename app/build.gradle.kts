plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.qcmaster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.qcmaster"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.ui:ui:1.3.0")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation ("androidx.compose.ui:ui:1.4.0") // Make sure this is the latest version (or 1.3.x if you're using an older version)
    implementation ("androidx.compose.material3:material3:1.0.0") // or the appropriate version for material3
    implementation ("androidx.compose.ui:ui-tooling-preview:1.4.0") // For UI previews
    implementation ("androidx.compose.foundation:foundation:1.4.0") // For layout and other components

    // Ensure you're using Kotlin and Compose Compiler that is compatible
    implementation ("androidx.compose.compiler:compiler:1.4.0" ) // Compose Compiler for Kotli



}