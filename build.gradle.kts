// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugins de Android y Kotlin (no se aplican aquí, solo se exponen para los módulos)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ✅ Plugin de Google Services (Firebase, Google Sign-In, etc.)
    id("com.google.gms.google-services") version "4.4.3" apply false
}
