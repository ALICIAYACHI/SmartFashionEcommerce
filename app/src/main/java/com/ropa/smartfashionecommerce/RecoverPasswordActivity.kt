package com.ropa.smartfashionecommerce

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class RecoverPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val oobCode = intent.getStringExtra("oobCode")

        setContent {
            SmartFashionEcommerceTheme {
                RecoverPasswordScreen(
                    oobCode = oobCode,
                    onBackToLogin = { finish() }
                )
            }
        }
    }
}

@Composable
fun RecoverPasswordScreen(
    oobCode: String? = null,
    onBackToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val isResetMode = oobCode != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Título
                Text(
                    text = "👔 SmartFashion",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0f3460)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtítulo
                Text(
                    text = if (isResetMode) "Restablecer Contraseña" else "Recuperar Contraseña",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1a1a2e)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción
                Text(
                    text = if (isResetMode)
                        "Ingresa tu nueva contraseña para completar el proceso."
                    else
                        "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 15.sp,
                    color = Color(0xFF5a5a5a),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isResetMode) {
                    // Campo de correo
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Email",
                                tint = Color(0xFF0f3460)
                            )
                        },
                        label = { Text("Correo electrónico", color = Color(0xFF5a5a5a)) },
                        placeholder = { Text("ejemplo@correo.com", color = Color(0xFFa0a0a0)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0f3460),
                            unfocusedBorderColor = Color(0xFFd0d0d0),
                            focusedLabelColor = Color(0xFF0f3460),
                            cursorColor = Color(0xFF0f3460)
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Botón enviar
                    Button(
                        onClick = {
                            if (email.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Por favor ingresa tu correo",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isLoading = true
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Correo enviado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = android.content.Intent(
                                            context,
                                            EmailSentActivity::class.java
                                        )
                                        context.startActivity(intent)
                                        (context as? Activity)?.finish()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0f3460),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFd0d0d0)
                        ),
                        enabled = email.isNotEmpty() && !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "Enviar Enlace de Recuperación",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Modo restablecer contraseña
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password",
                                tint = Color(0xFF0f3460)
                            )
                        },
                        label = { Text("Nueva contraseña", color = Color(0xFF5a5a5a)) },
                        placeholder = { Text("Mínimo 6 caracteres", color = Color(0xFFa0a0a0)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0f3460),
                            unfocusedBorderColor = Color(0xFFd0d0d0),
                            focusedLabelColor = Color(0xFF0f3460),
                            cursorColor = Color(0xFF0f3460)
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (newPassword.length < 6) {
                                Toast.makeText(
                                    context,
                                    "La contraseña debe tener al menos 6 caracteres",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            isLoading = true
                            auth.confirmPasswordReset(oobCode!!, newPassword)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Tu contraseña se ha restablecido correctamente",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        (context as? Activity)?.finish()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0f3460),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFd0d0d0)
                        ),
                        enabled = newPassword.isNotEmpty() && !isLoading,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "Cambiar Contraseña",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón volver
                if (!isResetMode) {
                    Row(
                        modifier = Modifier
                            .clickable { onBackToLogin() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF0f3460),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Volver al inicio de sesión",
                            color = Color(0xFF0f3460),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}