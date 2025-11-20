package com.example.howviii

import android.os.Bundle
import android.util.Log
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
import com.example.howviii.firebase.FirebaseAuthService
import com.example.howviii.ui.theme.HowviiiTheme
import com.example.howviii.ui.ListItemScreen
import com.example.howviii.ui.ShowItemScreen
import com.example.howviii.ui.CreateItemScreen
import com.example.howviii.viewmodel.ItemViewModel

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
                                onItemClick = { id -> navController.navigate("detail/$id") }
                            )
                        }
                        composable("detail/{id}") {
                            ShowItemScreen(
                                navController,
                                id = it.arguments?.getString("id")?.toInt() ?: 0,
                                onEditClick = { navController.navigate("form?id=$it") }
                            )
                        }
                        composable("form?id={id}") {
                            CreateItemScreen(
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
