plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // 🔹 Plugin de Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ropa.smartfashionecommerce"
    compileSdk = 36 // ✅ Mantengo tu valor

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

    // ✅ Versión actualizada del compilador de Compose
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ✅ Compose BOM (controla versiones compatibles entre sí)
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))

    // ✅ Core AndroidX y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-graphics")

    // ✅ Material 3 (última estable compatible)
    implementation("androidx.compose.material3:material3:1.3.0")

    // ✅ Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // ✅ MotionLayout (para animaciones y BottomSheet personalizados)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✅ UI adicionales
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.recyclerview)

    // ✅ COIL para imágenes
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ✅ Firebase (usa BOM para manejar versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ✅ Google Sign-In (para login con Google)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // ✅ Navegación Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ✅ Gson para guardar y cargar el carrito
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ✅ Debug — corregido para que encuentre correctamente el test manifest
    debugImplementation(platform("androidx.compose:compose-bom:2025.01.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest") // 👈 corregido nombre correcto
}
