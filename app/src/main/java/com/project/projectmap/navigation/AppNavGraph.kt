package com.project.projectmap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.projectmap.loginRegister.LoginScreen
import com.project.projectmap.loginRegister.RegisterScreen
import com.project.projectmap.screen.CalorieTrackerScreen

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val CALORIE_TRACKER_ROUTE = "calorie_tracker"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.CALORIE_TRACKER_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppDestinations.REGISTER_ROUTE)
                }
            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.CALORIE_TRACKER_ROUTE) {
            CalorieTrackerScreen()
        }
    }
}