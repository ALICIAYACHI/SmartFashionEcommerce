package com.ropa.smartfashionecommerce.miperfil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.ropa.smartfashionecommerce.utils.UserSessionManager
import java.io.File
import java.io.FileOutputStream

object ProfileImageManager {

    var profileImageUri = mutableStateOf<Uri?>(null)

    /**
     * ✅ Guarda la imagen de perfil en el almacenamiento interno
     * Copia el archivo desde la URI al almacenamiento interno de la app
     */
    fun saveProfileImage(context: Context, uri: Uri) {
        try {
            // Leer el bitmap desde la URI
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return

            // Crear archivo en el almacenamiento interno con ID único por usuario
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
            val file = File(context.filesDir, "profile_image_$userId.jpg")

            // Guardar el bitmap como JPEG
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Guardar la ruta en SharedPreferences del usuario actual
            val fileUri = Uri.fromFile(file)
            val sharedPrefs = UserSessionManager.getUserPreferences(context)
            sharedPrefs.edit().putString("fotoPerfilUri", fileUri.toString()).apply()

            // Actualizar el estado en memoria
            profileImageUri.value = fileUri

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ✅ Carga la imagen de perfil desde SharedPreferences
     */
    fun loadProfileImage(context: Context) {
        try {
            val sharedPrefs = UserSessionManager.getUserPreferences(context)
            val uriString = sharedPrefs.getString("fotoPerfilUri", null)

            if (uriString != null) {
                val uri = Uri.parse(uriString)
                val file = File(uri.path ?: "")

                // Verificar que el archivo existe
                if (file.exists()) {
                    profileImageUri.value = uri
                } else {
                    profileImageUri.value = null
                }
            } else {
                profileImageUri.value = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            profileImageUri.value = null
        }
    }

    /**
     * ✅ Obtiene un Bitmap desde una URI para mostrar en Image composable
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                // Cargar desde archivo local
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                // Intentar cargar desde ContentResolver (por si es URI externa)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ✅ Limpia la imagen de perfil del usuario actual
     * Elimina el archivo físico y la referencia en SharedPreferences
     */
    fun clearProfileImage(context: Context) {
        try {
            // Limpiar SharedPreferences
            val sharedPrefs = UserSessionManager.getUserPreferences(context)
            sharedPrefs.edit().remove("fotoPerfilUri").apply()

            // Limpiar estado en memoria
            profileImageUri.value = null

            // Eliminar archivo físico
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
            val file = File(context.filesDir, "profile_image_$userId.jpg")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}