package com.project.projectmap.components.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.projectmap.ui.screens.auth.login.LoginScreen
import com.project.projectmap.ui.screens.auth.register.RegisterScreen
import com.project.projectmap.ui.screens.badges.BadgesPage
import com.project.projectmap.ui.screens.calendar.CalendarPage
import com.project.projectmap.ui.screens.main.MainTrackerScreen
import com.project.projectmap.ui.screens.main.SetTargetScreen
import com.project.projectmap.ui.screens.profile.ProfileScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//import com.project.projectmap.utilities.getCachedUserId
//import com.project.projectmap.utilities.isSessionExpired
//import com.project.projectmap.utilities.listenToUserDeletion


object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val MAIN_ROUTE = "main_tracker"
    const val CALENDAR_ROUTE = "calendar"
    const val BADGES_ROUTE = "badges"
    const val PROFILE_ROUTE = "profile"
    const val NEW_TARGET_ROUTE = "new_target"
}

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    context: Context
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Log.d("NAVSPACE", "Current User: ${currentUser}")

    var startDestination by remember { mutableStateOf(AppDestinations.MAIN_ROUTE) }

    if (currentUser == null) {
        startDestination = AppDestinations.LOGIN_ROUTE
    }


    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

//        ROUTE FOR LOGIN PAGE
        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(AppDestinations.NEW_TARGET_ROUTE) {
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppDestinations.MAIN_ROUTE) {
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onTargetNotFound = {
                    Log.d("LoginScreen", "Navigating to New Target Screen")
                    navController.navigate(AppDestinations.NEW_TARGET_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

//        ROUTE FOR REGISTER PAGE
        composable(AppDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppDestinations.NEW_TARGET_ROUTE) {
                        popUpTo(AppDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                }
            )
        }

//        ROUTE FOR NEW TARGET PAGE
        composable(AppDestinations.NEW_TARGET_ROUTE) {
            SetTargetScreen(
                onClose = {
                    // On close, check if user has target set
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        navController.navigate(AppDestinations.MAIN_ROUTE) {
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
                    navController.navigate(AppDestinations.MAIN_ROUTE) {
                        popUpTo(AppDestinations.NEW_TARGET_ROUTE) { inclusive = true }
                    }
                }
            )
        }

//        ROUTE FOR MAIN PAGE
        composable(AppDestinations.MAIN_ROUTE) {
            MainTrackerScreen(
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

//        ROUTE FOR BADGES PAGE
        composable(AppDestinations.BADGES_ROUTE) {
            BadgesPage(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

//        ROUTE FOR CALENDAR PAGE
        composable(AppDestinations.CALENDAR_ROUTE) {
            CalendarPage(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

//        ROUTE FOR PROFILE PAGE
        composable(AppDestinations.PROFILE_ROUTE) {
            ProfileScreen(
                onClose = {
                    navController.popBackStack()
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    clearCachedUserData(context)
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun clearCachedUserData(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().clear().apply()
}


fun FirebaseAuth.getUser(userId: String): FirebaseUser? {
    return this.getUser(userId)
}