package com.ropa.smartfashionecommerce.detalles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BabySizeGuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                    BabySizeGuideScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabySizeGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de tallas bebé", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Medidas corporales (referencia)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            BabySizeTable()

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Las medidas se obtienen manualmente por lo que puede haber ligeras variaciones.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BabySizeTable() {
    val headerColor = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9))
    ) {
        // Encabezado
        BabySizeRow(
            values = listOf("Edad", "Estatura (cm)", "Largo (cm)", "Pecho (cm)"),
            bold = true,
            background = headerColor
        )

        val rows = listOf(
            listOf("0-3M", "56-62", "36", "46"),
            listOf("3-6M", "62-68", "38.5", "48"),
            listOf("6-9M", "68-74", "41", "50"),
            listOf("9-12M", "74-80", "43.5", "52"),
            listOf("12-18M", "80-86", "46", "54")
        )

        rows.forEach { row ->
            BabySizeRow(values = row)
        }
    }
}

@Composable
fun BabySizeRow(values: List<String>, bold: Boolean = false, background: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weights = listOf(0.9f, 1.1f, 1f, 1f)
        values.forEachIndexed { index, text ->
            Text(
                text = text,
                modifier = Modifier.weight(weights.getOrElse(index) { 1f }),
                fontSize = 13.sp,
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.Black
            )
        }
    }
}
