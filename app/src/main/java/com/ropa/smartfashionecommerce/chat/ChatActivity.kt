package com.ropa.smartfashionecommerce.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.ChatbotRequest
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.launch
import android.util.Log

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9F9F9)
                ) {
                    ChatScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Mensaje inicial
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage("¡Hola! Soy tu asistente IA de SmartFashion. ¿En qué puedo ayudarte?", false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartFashion IA", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D6EFD)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatMessageBubble(message)
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Escribe tu pregunta...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D6EFD),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMessage = inputText.trim()
                            messages = messages + ChatMessage(userMessage, isUser = true)
                            inputText = ""
                            isLoading = true

                            // Llamar a la API del chatbot
                            scope.launch {
                                try {
                                    Log.d("ChatActivity", "Enviando solicitud con query: $userMessage")
                                    val response = ApiClient.apiService.chatbotQuery(userMessage)
                                    Log.d("ChatActivity", "Response code: ${response.code()}")
                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        Log.d("ChatActivity", "Response body: $body")
                                        val aiReply = body?.response ?: body?.message ?: "Lo siento, no pude procesar tu pregunta."
                                        messages = messages + ChatMessage(aiReply, isUser = false)
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        Log.e("ChatActivity", "Error response: ${response.code()} - $errorBody")
                                        messages = messages + ChatMessage(
                                            "Error: No pude conectar con el servidor (${response.code()}).",
                                            isUser = false
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("ChatActivity", "Exception: ${e.message}", e)
                                    messages = messages + ChatMessage(
                                        "Error: ${e.message}",
                                        isUser = false
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D6EFD)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF0D6EFD) else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

