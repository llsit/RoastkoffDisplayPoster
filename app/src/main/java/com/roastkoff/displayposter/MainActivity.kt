package com.roastkoff.displayposter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.roastkoff.displayposter.ui.screen.home.HomeScreen
import com.roastkoff.displayposter.ui.screen.pairing.PairingScreen
import com.roastkoff.displayposter.ui.theme.DisplayPosterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisplayPosterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    DisplayPosterNavRoot()
                }
            }
        }
    }
}

@Composable
fun DisplayPosterNavRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Pairing.route
    ) {
        composable(Screen.Pairing.route) {
            PairingScreen(
                onPaired = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                },
                onUpdateAppClick = {
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}

sealed class Screen(val route: String) {
    data object Pairing : Screen("pairing")
    data object Home : Screen("home")
}