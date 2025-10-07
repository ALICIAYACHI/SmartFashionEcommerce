package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class MiPerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                // Usamos la versión con el menú inferior
                MiPerfilScreen(onBack = { finish() })
            }
        }
    }
}
