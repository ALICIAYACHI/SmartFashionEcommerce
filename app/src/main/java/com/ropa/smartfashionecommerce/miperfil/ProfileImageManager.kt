package com.ropa.smartfashionecommerce.miperfil

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import java.io.File

object ProfileImageManager {

    private const val PREFS_NAME = "profile_prefs"
    private const val KEY_IMAGE_PATH = "profile_image_path"

    var profileImageUri = mutableStateOf<Uri?>(null)

    // ✅ Cargar la imagen de perfil guardada
    fun loadProfileImage(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val imagePath = prefs.getString(KEY_IMAGE_PATH, null)

        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                profileImageUri.value = Uri.fromFile(file)
            }
        }
    }

    // ✅ Guardar la imagen seleccionada en SharedPreferences
    fun saveProfileImage(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IMAGE_PATH, uri.path).apply()
        profileImageUri.value = uri
    }

    // ✅ Obtener un Bitmap desde un Uri (útil para mostrar en Image composable)
    fun getBitmapFromUri(context: Context, uri: Uri) =
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
}
