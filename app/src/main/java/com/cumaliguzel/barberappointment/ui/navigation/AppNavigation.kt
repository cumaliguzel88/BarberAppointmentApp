package com.cumaliguzel.barberappointment.ui.navigation

import DailyEarningsScreen
import OperationPricesScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cumaliguzel.barberappointment.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Appointments.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Appointments.route) {
            AppointmentListScreen(
                onAddAppointment = { navController.navigate("appointment/add") },
                onEditAppointment = { id -> navController.navigate("appointment/edit/$id") }
            )
        }
        
        composable(BottomNavItem.DailyEarnings.route) {
            DailyEarningsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(BottomNavItem.Pricing.route) {
            OperationPricesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("appointment/add") {
            AddEditAppointmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("appointment/edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: -1
            AddEditAppointmentScreen(
                appointmentId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 