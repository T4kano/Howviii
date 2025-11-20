package com.example.howviii.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.howviii.viewmodel.ItemViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowItemScreen(
    navController: NavController,
    uuid: String,
    viewModel: ItemViewModel
) {
    val item by viewModel.currentItem.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwner = item?.createdBy == currentUserId
    val isReturned = item?.type == "recuperado"

    // 🔥 Carrega o item quando a tela abre
    LaunchedEffect(uuid) {
        viewModel.loadItem(uuid)
    }

    LaunchedEffect(item) {
        if (item == null) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->

        // ⏳ Loading enquanto item for null
        if (item == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // 🔥 Conteúdo do item
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Imagem
            Image(
                painter = rememberAsyncImagePainter("https://picsum.photos/200"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item!!.title, style = MaterialTheme.typography.titleMedium, fontSize = 24.sp)

                Box(
                    modifier = Modifier
                        .background(
                            color = when (item!!.type) {
                                "perdido" -> {
                                    Color(0xFFe63946)
                                }
                                "recuperado" -> {
                                    Color(0xFF2a9d8f)
                                }
                                else -> {
                                    Color(0xFF0077b6)
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item!!.type.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (item!!.description.isNotBlank()) {
                Text(item!!.description)
                Spacer(Modifier.height(8.dp))
            }

            Text("📍 ${item!!.local}")
            Text("📅 ${formatDate(item!!.createdAt)}") // Mon Nov 03 03:00:00 GMT 2025
            Text("\uD83D\uDCDE ${item!!.contact}")

            // 🔒 Botões só se o item pertence ao usuário
            if (isOwner) {
                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isReturned) {
                        Button(
                            onClick = { viewModel.markAsReturned(item!!.id) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Devolver Item") }
                    }

                    Button(
                        onClick = { viewModel.deleteItem(item!!.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) { Text("Excluir") }
                }
            }
        }
    }
}

