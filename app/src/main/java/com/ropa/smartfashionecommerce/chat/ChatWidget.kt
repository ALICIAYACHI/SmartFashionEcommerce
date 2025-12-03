package com.ropa.smartfashionecommerce.chat

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.ChatbotRequest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log

@Composable
fun ChatFAB(
    modifier: Modifier = Modifier,
    activity: Activity? = null
) {
    var showChatDialog by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { showChatDialog = true },
        containerColor = Color(0xFF0D6EFD),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = "Chat",
            tint = Color.White
        )
    }

    if (showChatDialog) {
        ChatDialog(onDismiss = { showChatDialog = false })
    }
}

@Composable
fun ChatDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            ChatContent(onDismiss = onDismiss)
        }
    }
}

@Composable
fun ChatContent(onDismiss: () -> Unit) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(max = 600.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D6EFD))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SmartFashion IA",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

            // Mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatWidgetBubble(message)
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Escribe tu pregunta...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D6EFD),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMessage = inputText.trim()
                            messages = messages + ChatMessage(userMessage, isUser = true)
                            inputText = ""
                            isLoading = true

                            scope.launch {
                                try {
                                    Log.d("ChatWidget", "Enviando solicitud con query: $userMessage")
                                    val response = ApiClient.apiService.chatbotQuery(
                                        ChatbotRequest(
                                            query = userMessage,
                                            question = userMessage,
                                            text = userMessage,
                                            message = userMessage,
                                            input = userMessage,
                                            prompt = userMessage
                                        )
                                    )
                                    Log.d("ChatWidget", "Response code: ${response.code()}")
                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        Log.d("ChatWidget", "Response body: $body")
                                        val aiReply = body?.response ?: body?.message ?: "Lo siento, no pude procesar tu pregunta."
                                        messages = messages + ChatMessage(aiReply, isUser = false)
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        Log.e("ChatWidget", "Error response: ${response.code()} - $errorBody")
                                        messages = messages + ChatMessage(
                                            "Error: No pude conectar (${response.code()})",
                                            isUser = false
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("ChatWidget", "Exception", e)
                                    messages = messages + ChatMessage("Error: ${e.message}", isUser = false)
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    containerColor = Color(0xFF0D6EFD),
                    modifier = Modifier.size(44.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatWidgetBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 12.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF0D6EFD) else Color.White
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(10.dp),
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}
