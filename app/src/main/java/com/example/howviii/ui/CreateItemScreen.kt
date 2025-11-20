package com.example.howviii.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.howviii.viewmodel.ItemViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    navController: NavController,
    viewModel: ItemViewModel,
    onSave: () -> Unit
) {
    // Estados dos campos
    var type by remember { mutableStateOf("perdido") }
    var title by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var campusId by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val campusMap by viewModel.campusMap.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(campusMap) {
        println("🔥 campusMap atualizado: $campusMap")
    }

    LaunchedEffect(campusMap) {
        if (campusId.isEmpty() && campusMap.isNotEmpty()) {
            campusId = campusMap.keys.first()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cadastrar Item") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Tipo (perdido / encontrado)
            Row {
                RadioButton(
                    selected = type == "perdido",
                    onClick = { type = "perdido" }
                )
                Text("Perdido")

                Spacer(Modifier.width(16.dp))

                RadioButton(
                    selected = type == "encontrado",
                    onClick = { type = "encontrado" }
                )
                Text("Encontrado")
            }

            // Campos obrigatórios
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contato *") },
                modifier = Modifier.fillMaxWidth()
            )

            // DROPDOWN DO CAMPUS
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = campusMap[campusId] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Campus *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    campusMap.forEach { (uuid, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                campusId = uuid
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = local,
                onValueChange = { local = it },
                label = { Text("Local *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Selecionar imagem")
            }

            imageUri?.let { uri ->
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigateUp() }
            ) {
                Text("Voltar")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (title.isBlank() || contact.isBlank() || campusId.isBlank() || local.isBlank()) {
                        return@Button
                    }

                    val date = Timestamp.now().toDate()

                    viewModel.saveItem(
                        type,
                        title,
                        description,
                        local,
                        contact,
                        campusId,
                        date,
                        onSuccess = { onSave() },
                        onError = { e -> e.printStackTrace() }
                    )
                }
            ) {
                Text("Cadastrar Item")
            }
        }
    }
}
