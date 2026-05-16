package com.example.culator.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.culator.ui.calculator.CalculatorScreen
import com.example.culator.ui.calculator.CalculatorViewModel
import com.example.culator.ui.recordings.RecordingsListScreen
import com.example.culator.ui.recordings.RecordingsViewModel
import com.example.culator.ui.secret.SecretHomeScreen
import com.example.culator.ui.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route
    ) {
        composable(Screen.Calculator.route) {
            val viewModel: CalculatorViewModel = viewModel()
            CalculatorScreen(
                viewModel = viewModel,
                onNavigateToSecret = {
                    navController.navigate(Screen.SecretHome.route)
                }
            )
        }
        composable(Screen.SecretHome.route) {
            SecretHomeScreen(
                onNavigateToRecordings = {
                    navController.navigate(Screen.Recordings.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Recordings.route) {
            val viewModel: RecordingsViewModel = viewModel()
            RecordingsListScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object SecretHome : Screen("secret_home")
    object Recordings : Screen("recordings")
    object Settings : Screen("settings")
}
