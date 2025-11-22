package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class DetalleTerminoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titulo = intent.getStringExtra("titulo").orEmpty()
        val contenido = intent.getStringExtra("contenido").orEmpty()

        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DetalleTerminoScreen(
                        titulo = titulo,
                        contenido = contenido,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
