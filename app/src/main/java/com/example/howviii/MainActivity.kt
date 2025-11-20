package com.example.howviii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.howviii.ui.theme.HowviiiTheme
import com.example.howviii.ui.ListItemScreen
import com.example.howviii.ui.ShowItemScreen
import com.example.howviii.ui.CreateItemScreen
import com.example.howviii.viewmodel.ItemViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}

class MainActivity : ComponentActivity() {
    private val itemViewModel: ItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemViewModel.ensureAnonymousAuth()
        itemViewModel.loadCampus()

        enableEdgeToEdge()
        setContent {
            HowviiiTheme {
                val navController = rememberNavController()

                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "list",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("list") {
                            ListItemScreen(
                                onAddClick = { navController.navigate("form") },
                                onItemClick = { uuid -> navController.navigate("detail/$uuid") }
                            )
                        }
                        composable("detail/{uuid}") {
                            ShowItemScreen(
                                navController,
                                uuid = it.arguments?.getString("uuid") ?: "",
                                viewModel = itemViewModel,
                            )
                        }
                        composable("form") {
                            CreateItemScreen(
                                navController,
                                viewModel = itemViewModel,
                                onSave = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
