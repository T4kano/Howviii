package com.example.howviii.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.howviii.data.ItemRepository
import com.example.howviii.model.Item
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemScreen(
    onAddClick: () -> Unit,
    onItemClick: (String) -> Unit
) {

    val repo = remember { ItemRepository() }
    val scope = rememberCoroutineScope()

    var search by remember { mutableStateOf("") }

    var campusList by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var selectedCampusId by remember { mutableStateOf<String?>(null) }

    var items by remember { mutableStateOf(listOf<Item>()) }
    var loading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        campusList = repo.loadCampus()
    }

    LaunchedEffect(search, selectedCampusId) {
        repo.resetPagination()
        loading = true
        endReached = false

        items = repo.loadItemsPaged(
            campusId = selectedCampusId,
            search = search
        )

        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achados e Perdidos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Novo Item")
            }
        }
    ) { padding ->

        Column(Modifier.padding(padding)) {

            // 🔍 BUSCA
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar itens") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(8.dp)
            ) {
                OutlinedTextField(
                    value = campusList[selectedCampusId] ?: "Todos os campus",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Campus") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Todos") },
                        onClick = {
                            selectedCampusId = null
                            expanded = false
                        }
                    )

                    campusList.forEach { (uuid, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedCampusId = uuid
                                expanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {

                items(items) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onItemClick(item.id) }
                    ) {

                        Column(Modifier.padding(12.dp)) {

                            Image(
                                painter = rememberAsyncImagePainter("https://picsum.photos/200"),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium, fontSize = 24.sp)

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (item.type) {
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
                                        text = item.type.replaceFirstChar { it.uppercase() },
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(item.description, maxLines = 2, fontSize = 16.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📍 ${item.local}")
                                Text("📅 ${formatDate(item.createdAt)}")
                            }

                            Text("☎ ${item.contact}")
                        }
                    }
                }

                if (!endReached && !loading) {
                    item {
                        Button(
                            onClick = {
                                scope.launch {
                                    val newItems = repo.loadItemsPaged(
                                        campusId = selectedCampusId,
                                        search = search
                                    )

                                    if (newItems.isEmpty()) {
                                        endReached = true
                                    } else {
                                        items = items + newItems
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Carregar mais")
                        }
                    }
                }

                if (loading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
