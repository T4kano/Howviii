package com.example.howviii.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.howviii.model.Item
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ShowItemScreen(navController: NavController, id: Int, onEditClick: () -> Unit) {
    /*
    val item = remember {
        Item(UUID.randomUUID().toString(), "Mochila Preta Nike", "Mochila preta com notebook e cadernos dentro.", "Perdido", "Itajaí", "Bloco B7", "20/06/2025")
    }
     */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            /*
            item.imageUrl?.let {
                Image(painter = rememberAsyncImagePainter(it), contentDescription = null)
            }
            Text(item.title, style = MaterialTheme.typography.titleLarge)
            Text(item.description)
            Spacer(Modifier.height(8.dp))
            Text("📍 ${item.local}")
            Text("📅 ${item.createdAt}")
             */
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEditClick) { Text("Editar") }
                OutlinedButton(onClick = { /* Excluir */ }) { Text("Excluir") }
            }
        }
    }
}
