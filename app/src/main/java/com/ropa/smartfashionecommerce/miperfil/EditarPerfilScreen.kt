package com.ropa.smartfashionecommerce.miperfil

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ropa.smartfashionecommerce.R
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("SmartFashionPrefs", Context.MODE_PRIVATE)

    // 🟣 Cargar datos guardados
    var nombre by remember { mutableStateOf(sharedPrefs.getString("nombre", "") ?: "") }
    var telefono by remember { mutableStateOf(sharedPrefs.getString("telefono", "") ?: "") }
    var fechaNacimiento by remember { mutableStateOf(sharedPrefs.getString("fechaNacimiento", "") ?: "") }

    // 🟢 Cargar imagen desde ProfileImageManager
    LaunchedEffect(Unit) {
        ProfileImageManager.loadProfileImage(context)
    }
    var fotoUri by remember { mutableStateOf(ProfileImageManager.profileImageUri.value) }

    var showDialog by remember { mutableStateOf(false) }

    // 📸 Lanzadores de selección de imagen
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fotoUri = it
                ProfileImageManager.saveProfileImage(context, it)
            }
        }

    val takePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver, it, "profile_pic", null
                )
                val uri = Uri.parse(path)
                fotoUri = uri
                ProfileImageManager.saveProfileImage(context, uri)
            }
        }

    // Guardar imagen localmente (opcional refuerzo)
    fun saveProfileImage(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🟣 Imagen de perfil
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .shadow(6.dp, CircleShape)
                    .background(Color.White)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri != null) {
                    val bitmap = ProfileImageManager.getBitmapFromUri(context, fotoUri!!)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.ic_person),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campos
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Número de teléfono") },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento") },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val savedPath = saveProfileImage(context, fotoUri)
                    sharedPrefs.edit().apply {
                        putString("nombre", nombre)
                        putString("telefono", telefono)
                        putString("fechaNacimiento", fechaNacimiento)
                        if (savedPath != null) putString("fotoPerfilUri", savedPath)
                        apply()
                    }
                    Toast.makeText(context, "Cambios guardados correctamente ✅", Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // 🟣 Diálogo elegante
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.background(Color.White)
                ) {
                    Text(
                        "Seleccionar foto de perfil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Divider()

                    Text(
                        "📁 Elegir de la galería",
                        color = Color(0xFF007AFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                pickImageLauncher.launch("image/*")
                            }
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp
                    )

                    Text(
                        "📸 Tomar una foto",
                        color = Color(0xFF007AFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                takePhotoLauncher.launch(null)
                            }
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp
                    )

                    Divider()

                    Text(
                        "Cancelar",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = false }
                            .padding(vertical = 12.dp),
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
