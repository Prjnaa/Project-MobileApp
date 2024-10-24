package com.project.projectmap.components.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.projectmap.ui.screens.auth.login.LoginScreen
import com.project.projectmap.ui.screens.auth.register.RegisterScreen
import com.project.projectmap.ui.screens.badges.BadgesPage
import com.project.projectmap.ui.screens.calendarPage.CalendarPage
import com.project.projectmap.ui.screens.main.CalorieTrackerScreen

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val CALORIE_TRACKER_ROUTE = "calorie_tracker"
    const val CALENDAR_ROUTE = "calendar"
    const val BADGES_ROUTE = "badges"
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
                    navController.popBackStack() // Kembali ke login screen setelah registrasi sukses
                },
                onNavigateToLogin = {
                    navController.popBackStack(AppDestinations.LOGIN_ROUTE, false) // Kembali ke login jika di klik tombol login
                }
            )
        }

        composable(AppDestinations.CALORIE_TRACKER_ROUTE) {
            CalorieTrackerScreen()
        }

        composable(AppDestinations.CALORIE_TRACKER_ROUTE) {
            CalorieTrackerScreen(
                onNavigateToCalendar = {
                    navController.navigate(AppDestinations.CALENDAR_ROUTE)
                }
            )
        }
        composable(AppDestinations.CALORIE_TRACKER_ROUTE){
            CalorieTrackerScreen(
                onNavigateToBadges = {
                    navController.navigate(AppDestinations.BADGES_ROUTE)
                }
            )
        }

        composable(AppDestinations.BADGES_ROUTE) {
            BadgesPage(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.CALENDAR_ROUTE) {
            CalendarPage(
                onClose = {
                    navController.popBackStack()
                }
            )
        }



    }
}
