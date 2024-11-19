package com.project.projectmap.components.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.project.projectmap.ui.screens.auth.login.LoginScreen
import com.project.projectmap.ui.screens.auth.register.RegisterScreen
import com.project.projectmap.ui.screens.badges.BadgesPage
import com.project.projectmap.ui.screens.calendarPage.CalendarPage
import com.project.projectmap.ui.screens.camera.PhotoBottomSheetContent
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
    const val CAMERA_ROUTE = "camera"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        // If new user, navigate to set target first
                        navController.navigate(AppDestinations.NEW_TARGET_ROUTE) {
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    } else {
                        // If existing user, go directly to calorie tracker
                        navController.navigate(AppDestinations.CALORIE_TRACKER_ROUTE) {
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
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
                    // After registration, navigate to set target
                    navController.navigate(AppDestinations.NEW_TARGET_ROUTE) {
                        popUpTo(AppDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.NEW_TARGET_ROUTE) {
            NewTargetScreen(
                onClose = {
                    // On close, check if user has target set
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        navController.navigate(AppDestinations.CALORIE_TRACKER_ROUTE) {
                            popUpTo(AppDestinations.NEW_TARGET_ROUTE) { inclusive = true }
                        }
                    } else {
                        // If somehow there's no user, go back to login
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onContinue = {
                    navController.navigate(AppDestinations.CALORIE_TRACKER_ROUTE) {
                        popUpTo(AppDestinations.NEW_TARGET_ROUTE) { inclusive = true }
                    }
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

// Optional: Helper function to check if user should start with target screen
suspend fun shouldStartWithTargetScreen(userId: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        val document = db.collection("userTargets")
            .document(userId)
            .get()
            .await()
        !document.exists()
    } catch (e: Exception) {
        // If there's an error checking, default to false
        false
    }
}