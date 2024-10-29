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
import com.project.projectmap.ui.screens.main.NewTargetScreen
import com.project.projectmap.ui.screens.profilePage.ProfileScreen

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val CALORIE_TRACKER_ROUTE = "calorie_tracker"
    const val CALENDAR_ROUTE = "calendar"
    const val BADGES_ROUTE = "badges"
    const val PROFILE_ROUTE = "profile"
    const val NEW_TARGET_ROUTE = "new_target"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(

            )
        }

        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack(AppDestinations.LOGIN_ROUTE, false)
                }
            )
        }

        composable(AppDestinations.CALORIE_TRACKER_ROUTE) {
            CalorieTrackerScreen(
                onNavigateToCalendar = {
                    navController.navigate(AppDestinations.CALENDAR_ROUTE)
                },
                onNavigateToBadges = {
                    navController.navigate(AppDestinations.BADGES_ROUTE)
                },
                onNavigateToProfile = {
                    navController.navigate(AppDestinations.PROFILE_ROUTE)
                },
                onNavigateToNewTarget = {
                    navController.navigate(AppDestinations.NEW_TARGET_ROUTE)
                }
            )
        }

        composable(AppDestinations.NEW_TARGET_ROUTE) {
            NewTargetScreen(
                onClose = {
                    navController.popBackStack()
                },
                onContinue = {
                    navController.popBackStack()
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

        composable(AppDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onClose = {
                    navController.popBackStack()
                },
                onLogout = {
                    // Navigate to login and clear the back stack
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}