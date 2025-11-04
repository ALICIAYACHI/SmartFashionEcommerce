package com.ropa.smartfashionecommerce.miperfil

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser

    // 🔑 Usar el email del usuario como clave única para SharedPreferences
    val userEmail = user?.email ?: ""
    val sharedPrefs = context.getSharedPreferences("user_profile_$userEmail", android.content.Context.MODE_PRIVATE)

    // 🟢 Cargar datos guardados
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    // 🔄 Cargar datos al iniciar
    LaunchedEffect(userEmail) {
        nombre = sharedPrefs.getString("nombre", user?.displayName ?: "") ?: user?.displayName ?: ""
        email = userEmail
        telefono = sharedPrefs.getString("telefono", "") ?: ""
        direccion = sharedPrefs.getString("direccion", "") ?: ""

        // Cargar foto guardada específica del usuario
        val savedImageUri = sharedPrefs.getString("foto_perfil_uri", null)
        fotoUri = savedImageUri?.toUri()
    }

    // 🟢 Estados de edición
    var editandoNombre by remember { mutableStateOf(false) }
    var editandoTelefono by remember { mutableStateOf(false) }
    var editandoDireccion by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // 📸 Launcher para seleccionar imagen
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoUri = it
            // Guardar URI de la foto en SharedPreferences del usuario
            sharedPrefs.edit().putString("foto_perfil_uri", it.toString()).apply()
            Toast.makeText(context, "Foto actualizada ✅", Toast.LENGTH_SHORT).show()
        }
    }

    // 📸 Launcher para tomar foto
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            try {
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver, it, "profile_pic_${System.currentTimeMillis()}", null
                )
                val uri = Uri.parse(path)
                fotoUri = uri
                // Guardar URI de la foto en SharedPreferences del usuario
                sharedPrefs.edit().putString("foto_perfil_uri", uri.toString()).apply()
                Toast.makeText(context, "Foto actualizada ✅", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            // 🟣 Sección de foto de perfil
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { showDialog = true }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Foto",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoUri != null) {
                            val bitmap = ProfileImageManager.getBitmapFromUri(context, fotoUri!!)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(50.dp).clip(CircleShape)
                                )
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(R.drawable.ic_person),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(user?.photoUrl ?: R.drawable.ic_person),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(50.dp).clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Editar",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp).background(Color(0xFFE0E0E0)))

            // 🟣 Campo Nombre
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nombre", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)

                    if (!editandoNombre) {
                        Button(
                            onClick = { editandoNombre = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(if (nombre.isEmpty()) "Añade" else "Editar", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                if (editandoNombre) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        placeholder = { Text("Ingresa tu nombre completo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF8C00),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editandoNombre = false
                            nombre = sharedPrefs.getString("nombre", user?.displayName ?: "") ?: ""
                        }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                sharedPrefs.edit().putString("nombre", nombre).apply()
                                editandoNombre = false
                                Toast.makeText(context, "Nombre actualizado ✅", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                        ) {
                            Text("Guardar", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        nombre.ifEmpty { "Agregar nombre" },
                        fontSize = 16.sp,
                        color = if (nombre.isEmpty()) Color.Gray else Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🟣 Campo Email (no editable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("Email", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                Text(
                    email.ifEmpty { "Sin email registrado" },
                    fontSize = 16.sp,
                    color = if (email.isEmpty()) Color.Gray else Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(1.dp).background(Color(0xFFE0E0E0)))

            // 🟣 Campo Teléfono
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Número de teléfono celular", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)

                    if (!editandoTelefono) {
                        Button(
                            onClick = { editandoTelefono = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(if (telefono.isEmpty()) "Añade" else "Editar", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                if (editandoTelefono) {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("Ej: +51 987654321") },
                        textStyle = TextStyle(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF8C00),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editandoTelefono = false
                            telefono = sharedPrefs.getString("telefono", "") ?: ""
                        }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                sharedPrefs.edit().putString("telefono", telefono).apply()
                                editandoTelefono = false
                                Toast.makeText(context, "Teléfono actualizado ✅", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                        ) {
                            Text("Guardar", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        telefono.ifEmpty { "Agregar teléfono" },
                        fontSize = 16.sp,
                        color = if (telefono.isEmpty()) Color.Gray else Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🟣 Campo Dirección
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dirección", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)

                    if (!editandoDireccion) {
                        Button(
                            onClick = { editandoDireccion = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(if (direccion.isEmpty()) "Añade" else "Editar", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                if (editandoDireccion) {
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("Ej: Av. Principal 123, Lima") },
                        textStyle = TextStyle(fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF8C00),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editandoDireccion = false
                            direccion = sharedPrefs.getString("direccion", "") ?: ""
                        }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                sharedPrefs.edit().putString("direccion", direccion).apply()
                                editandoDireccion = false
                                Toast.makeText(context, "Dirección actualizada ✅", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                        ) {
                            Text("Guardar", color = Color.White)
                        }
                    }
                } else {
                    Text(
                        direccion.ifEmpty { "Agregar dirección" },
                        fontSize = 16.sp,
                        color = if (direccion.isEmpty()) Color.Gray else Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

        }
    }

    // 🟣 Diálogo para cambiar foto
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            text = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Elegir de la galería",
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                pickImageLauncher.launch("image/*")
                            }
                            .padding(vertical = 16.dp),
                        fontSize = 16.sp
                    )
                    Divider()
                    Text(
                        "Tomar una foto",
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                takePhotoLauncher.launch(null)
                            }
                            .padding(vertical = 16.dp),
                        fontSize = 16.sp
                    )
                    Divider()
                    Text(
                        "Cancelar",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDialog = false }
                            .padding(vertical = 16.dp),
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}