plugins {




        alias(libs.plugins.android.application) // plugin de Android
        alias(libs.plugins.kotlin.android)     // plugin de Kotlin
        alias(libs.plugins.kotlin.compose)     // plugin Compose Compiler obligatorio
        id("org.jetbrains.kotlin.plugin.serialization") // opcional
        id("org.jetbrains.kotlin.kapt") // opcional
        id("com.google.gms.google-services") // Firebase



}

android {
    namespace = "com.ropa.smartfashionecommerce"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ropa.smartfashionecommerce"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // o la última

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.ui:ui-graphics:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation(libs.androidx.recyclerview)



    // ✅ Firebase BOM (maneja versiones automáticamente)

    // Coil para imágenes
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Firebase BOM

    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))


    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-common-ktx")

    // Accompanist (barra de estado, etc.)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.7")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")
}
