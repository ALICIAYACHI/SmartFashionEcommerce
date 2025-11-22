package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class MisResenasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = Color.White) {
                    MisResenasScreen(onBack = { finish() })
                }
            }
        }
    }
}
