plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // 🔹 Plugin de Firebase
    id("com.google.gms.google-services")
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
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ✅ Core AndroidX y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // ✅ Material 3 (última versión estable para evitar warnings)
    implementation("androidx.compose.material3:material3:1.3.0")

    // ✅ MotionLayout (para animaciones y BottomSheet personalizados)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✅ UI adicionales
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.recyclerview)

    // ✅ Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.3")

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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
